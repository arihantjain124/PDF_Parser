package parser.page;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
import parser.json.StagingJsonObject;
import parser.renderer.GuidelinePageRenderer;
import parser.text.FootnoteAnalyser;
import parser.text.GuidelineTextStripper;
import parser.text.RegionWithBound;
import parser.text.TextRegionAnalyser;
import parser.text.WordWithBounds;

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
    	HashMap<String, HashMap<String, String>> documentFootnotes = new HashMap<String, HashMap<String, String>>();
    	
    	int indexOffset = 0;

    	List<RegionWithBound> allRegionList = new ArrayList<RegionWithBound>();
    	List<GraphJsonObject> allGraphObject = new ArrayList<GraphJsonObject>();
    	HashMap<String, List<RegionWithBound>> labelsHashMap = new HashMap<String, List<RegionWithBound>>();
    	List<FootNotesJsonObject> allFootNoteObject = new ArrayList<FootNotesJsonObject>();
    	List<StagingJsonObject> allStagingObject = new ArrayList<StagingJsonObject>();
    	
    	HashMap<String, String> docFootnotes = new HashMap<String, String>();
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
                
                List<WordWithBounds> capitalWordRects = mainContentStripper.getCapitalWordBounds();
                
                HashMap<String, String> pageFootnotes = FootnoteAnalyser.analyseFootnotes(wordRects);
                pageFootnotes.forEach(docFootnotes::putIfAbsent);
                
                documentFootnotes.put(pageKey, pageFootnotes);
                                
                List<RegionWithBound> regionBounds = TextRegionAnalyser.getRegions(wordRects);
                
                GuidelinePageRenderer renderer = new GuidelinePageRenderer(document, p - 1 ,72);
                renderer.intializeImage();
                renderer.getGeometry();
                
            	ArrayList<GeneralPath> lines = renderer.getLines();
            	ArrayList<GeneralPath> triangles = renderer.getTriangles();
            	if(!lines.isEmpty() && !triangles.isEmpty()) {
	            	
            		GraphProcessing graphProc = new GraphProcessing();
	            	graphProc.checkIntersectionToTriangles(lines, triangles);
	            	ArrayList<GraphObject> graphLine = graphProc.getGraphObject();
	            	ArrayList<GraphObject> verticalLine = graphProc.getVerticalLines(lines);
	            	ArrayList<GraphObject> graphVerticalLine = graphProc.getVerticalLinesGraphObject(graphLine);
	            	ArrayList<GraphObject> pairVerticalLine = graphProc.pairVerticalLine(verticalLine);
	            	if(!graphLine.isEmpty()) {
	            		
	            		TextRegionAnalyser.generateTextRegionAssociation(graphLine, regionBounds);
	            		List<RegionWithBound> newRegionList = collectFlowRegions(regionBounds, curPageInfo,indexOffset,pageKey,p);
	            		if (newRegionList.size()>0) 
	            		{
	            			List<RegionWithBound> labels = regionBounds.stream().distinct().filter(x -> (!(newRegionList.contains(x)) && (x.getBound().getY() < 512))).collect(Collectors.toList());
		            		indexOffset = indexOffset + newRegionList.size();
		            		labelsHashMap.put(pageKey, labels);
		            		allRegionList.addAll(newRegionList);
	            		}

	            	}
            	}
            }
            catch (IOException ex)
            {
                LOG.error("Failed to process page " + p, ex);
            }
        }

    	JsonExport.generateJsonGraphObject(allRegionList, pageHashMap, allGraphObject, labelsHashMap);
//    	JsonExport.generateStagingJsonObject(allRegionList, allStagingObject);
    	JsonExport.generateJsonFootNote(docFootnotes, allFootNoteObject);
    	
    	GuidelineContent guidelineContentObjs = new GuidelineContent();
    	guidelineContentObjs.setGraphObjects(allGraphObject);
    	guidelineContentObjs.setFootNotesJsonObject(allFootNoteObject);
    	
        JsonExport.writeJsonLD(guidelineContentObjs, startPage, endPage, "Graph");
        JsonExport.writeJson(allFootNoteObject, startPage, endPage, "FootNote");
//        JsonExport.writeJson(allStagingObject, startPage, endPage, "Staging");


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
	
}
