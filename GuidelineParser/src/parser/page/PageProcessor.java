package parser.page;

import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import parser.config.ConfigProperty;
import parser.graphics.GraphObject;
import parser.graphics.GraphProcessing;
import parser.json.FootNotesJsonObject;
import parser.json.GraphJsonObject;
import parser.json.GuidelineContent;
import parser.json.JsonExport;
import parser.renderer.GuidelinePageRenderer;
import parser.table.GuidelineTableExtractor;
import parser.table.TableDetails;
import parser.text.FootnoteAnalyser;
import parser.text.FootnoteDetails;
import parser.text.GuidelineTextStripper;
import parser.text.RegionWithBound;
import parser.text.TextRegionAnalyser;
import parser.text.WordWithBounds;
import technology.tabula.Table;

import org.javatuples.Pair;
import parser.json.LabelJsonObject;
import java.awt.geom.Rectangle2D;

public class PageProcessor {
	
    private static final Log LOG = LogFactory.getLog(PageProcessor.class);

    private String extractKey(PDDocument doc, int pageIndex) throws IOException
	{
    	
    	PDFTextStripperByArea pageKeyStripper = new PDFTextStripperByArea();
    	pageKeyStripper.setSortByPosition( true );
    	
    	String[] regionofPageKey = ConfigProperty.getProperty("page.key.region").split("[,]");
        Rectangle pageKeyRect = new Rectangle(Integer.valueOf(regionofPageKey[0]),Integer.valueOf(regionofPageKey[1]),Integer.valueOf(regionofPageKey[2]),Integer.valueOf(regionofPageKey[3]));
    	
    	pageKeyStripper.addRegion( "keyArea",pageKeyRect);

		String regexForPageId = ConfigProperty.getProperty("page.key.regex");
        Pattern pattern = Pattern.compile(regexForPageId);
        
		PDPage firstPage = doc.getPage(pageIndex - 1);
		pageKeyStripper.extractRegions( firstPage );
		
		Matcher matcher = pattern.matcher(pageKeyStripper.getTextForRegion( "keyArea" ));
		if (matcher.find())
		{
			return matcher.group();
		}
        return null;
	}

    public void processPages(int startPage, int endPage, GuidelineTextStripper mainContentStripper, PDDocument document, Writer output) 
    		throws IOException {
    	
    	HashMap<String, PageInfo> pageHashMap = new HashMap<String, PageInfo>();
    	
    	int indexOffset = 0;
    	int labelOffset = 0;

    	List<RegionWithBound> allRegionList = new ArrayList<RegionWithBound>();
    	HashMap<String, List<RegionWithBound>> labelsHashMap = new HashMap<String, List<RegionWithBound>>();
    	
    	HashMap<String, FootnoteDetails> docFootnotes = new HashMap<String, FootnoteDetails>();
    	HashMap<String, List<Pair<Rectangle2D, LabelJsonObject>>> labelsJsonHashMap = new HashMap<String, List<Pair<Rectangle2D, LabelJsonObject>>>();
    	List<LabelJsonObject> alllabelsObject = new ArrayList<LabelJsonObject>();
    	
    	List<TableDetails> allTablesList = new ArrayList<TableDetails>();
    	
    	for (int p = startPage; p <= endPage; ++p)
        {        	
            try
            {
            	String pageKey = extractKey(document, p);
            	PageInfo curPageInfo = new PageInfo(p, pageKey);
            	pageHashMap.put(pageKey, curPageInfo);
            	
            	mainContentStripper.setStartPage(p);
                mainContentStripper.setEndPage(p);
                
                mainContentStripper.writeText(document, output);
                
                List<WordWithBounds> wordRects = mainContentStripper.getWordBounds();
                
                HashMap<String, FootnoteDetails> pageFootnotes = FootnoteAnalyser.analyseFootnotes(wordRects);
                pageFootnotes.forEach(docFootnotes::putIfAbsent);
                
                GuidelinePageRenderer renderer = new GuidelinePageRenderer(document, p - 1 ,72);
                renderer.intializeImage();
                renderer.getGeometry();
                
            	ArrayList<GeneralPath> lines = renderer.getLines();
            	ArrayList<GeneralPath> triangles = renderer.getTriangles();
            	
            	if(!lines.isEmpty() && !triangles.isEmpty()) {
	            	
            		GraphProcessing graphProc = new GraphProcessing();
            		ArrayList<GraphObject> graphLine = graphProc.checkIntersectionToTriangles(lines, triangles);
	            	
//	            	ArrayList<VerticalGraphObject> verticalLine = graphProc.getVerticalLines(lines);
//	            	ArrayList<GraphObject> graphVerticalLine = graphProc.getVerticalLinesGraphObject(graphLine);
//	            	ArrayList<GraphObject> pairVerticalLine = graphProc.pairVerticalLine(verticalLine);
	            	
	                List<RegionWithBound> regionBounds = TextRegionAnalyser.getRegions(wordRects, lines, pageKey, p);
	                
	            	if(!graphLine.isEmpty()) {
	            		
	            		TextRegionAnalyser.generateTextRegionAssociation(graphLine, regionBounds);//add prev & next pointers in regions
	            		
	            		//renderer.drawRegionBoundsWithRelations(regionBounds, java.awt.Color.RED);

	            		List<RegionWithBound> newRegionList = collectFlowRegions(regionBounds, curPageInfo,indexOffset,pageKey,p);		            	
	            		if (newRegionList.size()>0) 
	            		{
	            			List<RegionWithBound> labels = regionBounds.stream().distinct().filter(x -> (!(newRegionList.contains(x)) && (x.getBound().getY() < 512))).collect(Collectors.toList());
	            			labelProc(labels, labelsJsonHashMap, labelOffset, pageKey, alllabelsObject);
							labelOffset = labelOffset + labels.size();
	            			labelsHashMap.put(pageKey, labels);
		            		
		            		TextRegionAnalyser.generateChildRegions(newRegionList, allRegionList.size());
		            		indexOffset = indexOffset + newRegionList.size();
		            		
		            		allRegionList.addAll(newRegionList);
	            		}

	            	}            	
            	}else {
            		
            		if(GuidelineTableExtractor.isTablePage(p)) {
            			
            			extractTables(document, p, pageKey, allTablesList);
            		}
            	}
            	
            	//renderer.OutputImage(true);
            }
            catch (IOException ex)
            {
                LOG.error("Failed to process page " + p, ex);
            }
        }

    	FootnoteAnalyser.analyzeAllFootNoteReferences(allRegionList);
    	FootnoteAnalyser.analyzeAllFootNoteReferences(labelsHashMap);
    	
    	List<GraphJsonObject> allGraphObject = new ArrayList<GraphJsonObject>();
    	JsonExport.generateJsonGraphObject(allRegionList, pageHashMap, allGraphObject, labelsJsonHashMap);
    	
    	List<FootNotesJsonObject> allFootNoteObject = new ArrayList<FootNotesJsonObject>();
    	JsonExport.generateJsonFootNote(docFootnotes, allFootNoteObject);
    	
    	GuidelineContent guidelineContentObjs = new GuidelineContent();
    	guidelineContentObjs.setGraphObjects(allGraphObject);
    	guidelineContentObjs.setFootNotesJsonObject(allFootNoteObject);
    	guidelineContentObjs.setLabelObjects(alllabelsObject);
    	guidelineContentObjs.setTablesList(allTablesList);
    	
        JsonExport.writeJsonLD(guidelineContentObjs, startPage, endPage, "Graph");
        //JsonExport.writeJson(allFootNoteObject, startPage, endPage, "FootNote");


    }
    
    private List<RegionWithBound> collectFlowRegions(List<RegionWithBound> allRegions, PageInfo pageInfo, int indexOffset,String pageKey,int pageNo){
    	
    	ArrayList<RegionWithBound> flowRegions = new ArrayList<RegionWithBound>();
    	HashMap<Integer, Integer> oldVsNewIndex = new HashMap<Integer, Integer>();
    	
    	for(int i = 0; i < allRegions.size(); i++) {
    		
    		RegionWithBound region = allRegions.get(i);
    		if(!region.getNextRegions().isEmpty() || !region.getPrevRegions().isEmpty()) {
    			
    			int newIndex = flowRegions.size() + indexOffset;
    			oldVsNewIndex.put(i, newIndex);
    			
    			flowRegions.add(region);
    			
    			if(!region.getNextRegions().isEmpty() & region.getPrevRegions().isEmpty()) {
    				//This is a first region in the flow.
    				pageInfo.addStartRegionIndex(newIndex);
    			}
    		}
    	}
    	
    	for(RegionWithBound region : flowRegions) {
    		
    		List<Integer> nextRegions = region.getNextRegions();
    		List<Integer> prevRegions = region.getPrevRegions();
    		
    		for(int i = 0; i < prevRegions.size(); i++) {
    			prevRegions.set(i, oldVsNewIndex.get(prevRegions.get(i)));
    		}
    		for(int i = 0; i < nextRegions.size(); i++) {
    			nextRegions.set(i, oldVsNewIndex.get(nextRegions.get(i)));
    		}
    		
    		region.setPageKey(pageKey);
    		region.setPageNo(pageNo);
    	}
    	
    	return flowRegions;
    }
    
    private void labelProc(List<RegionWithBound> currentPageLabels,
			HashMap<String, List<Pair<Rectangle2D, LabelJsonObject>>> labelsJsonHashMap, Integer labelOffset,
			String pageKey, List<LabelJsonObject> alllabelsObject) {

		int currIndex = labelOffset;
		List<Pair<Rectangle2D, LabelJsonObject>> currPage = new ArrayList<Pair<Rectangle2D, LabelJsonObject>>();
		LabelJsonObject currlabelObject = new LabelJsonObject();
		for (RegionWithBound labelbox : currentPageLabels) {

			currlabelObject = new LabelJsonObject();
			Rectangle2D currRect = labelbox.getBound();

			String currentLabel = "";
			List<WordWithBounds> labelBounds = labelbox.getContentLines();
			currlabelObject.setIndex(currIndex);
			currlabelObject
					.setFootnoteRefs(FootnoteAnalyser.analyzeFootNoteReferences(labelbox.getContentLines(), false));
			for (WordWithBounds word : labelBounds) {
				currentLabel = currentLabel + word.getText();
			}
			currlabelObject.setContent(currentLabel);
			alllabelsObject.add(currlabelObject);
			currPage.add(Pair.with(currRect, currlabelObject));
			currIndex += 1;
		}
		labelsJsonHashMap.put(pageKey, currPage);
	}
    
    public void extractTables(String pdfFile, String password) {
    	GuidelineTableExtractor tableStripper = null;
        File inputFile = new File(pdfFile);
        File tableOutputFolder = new File("jsonexport/");
        try {
			tableStripper = new GuidelineTableExtractor(password);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
        
        try {
			tableStripper.extractTablesFromFile(inputFile, tableOutputFolder);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private List<Table> extractTables(PDDocument pdDocument, int pageNum, String pageKey, List<TableDetails> tablesList) {
    	
    	GuidelineTableExtractor tableStripper = null;
    	
    	List<Table> tables = null;
        
        try {
			tableStripper = new GuidelineTableExtractor("");
        	tables = tableStripper.extractTablesFromPDDoc(pdDocument, pageNum);
        	
        	for(Table table : tables) {
				TableDetails tableDetails = new TableDetails();
				tableDetails.setPageNumber(pageNum);
				tableDetails.setTable(table);
				tableDetails.setPageKey(pageKey);
				tableDetails.setIndex(tablesList.size());
				
				tablesList.add(tableDetails);
        	}
        	
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        return tables;
    }
	
}
