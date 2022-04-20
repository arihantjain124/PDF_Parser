package parser.page;

import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import parser.graphics.GraphObject;
import parser.graphics.GraphProcessing;
import parser.json.JsonExport;
import parser.renderer.GuidelinePageRenderer;
import parser.text.FootnoteAnalyser;
import parser.text.GuidelineTextStripper;
import parser.text.RegionWithBound;
import parser.text.TextRegionAnalyser;
import parser.text.WordWithBounds;

public class PageProcessor {
	
    private static final Log LOG = LogFactory.getLog(PageProcessor.class);
    
	Rectangle pageKeyAreaRect = new Rectangle( 700, 560, 92, 52 );//TODO: Hard coding the content area for key now.
    
    private String extractKey(PDDocument doc, int pageIndex) throws IOException
	{
    	PDFTextStripperByArea pageKeyStripper = new PDFTextStripperByArea();
    	pageKeyStripper.setSortByPosition( true );
    	pageKeyStripper.addRegion( "keyArea", pageKeyAreaRect);
    	
		PDPage firstPage = doc.getPage(pageIndex - 1);
		pageKeyStripper.extractRegions( firstPage );
		
        return pageKeyStripper.getTextForRegion( "keyArea" );
	}

    public void processPages(int startPage, int endPage, GuidelineTextStripper mainContentStripper, PDDocument document, Writer output) {
    	
    	HashMap<String, PageInfo> pageHashMap = new HashMap<String, PageInfo>();
    	HashMap<String, HashMap<String, String>> documentFootnotes = new HashMap<String, HashMap<String, String>>();
    	
    	HashMap<String, List<RegionWithBound>> documentRegions = new HashMap<String, List<RegionWithBound>>();
    	
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
                
                HashMap<String, String> pageFootnotes = FootnoteAnalyser.analyseFootnotes(wordRects);
                documentFootnotes.put(pageKey, pageFootnotes);
        		for (String key : pageFootnotes.keySet()) {
        			System.out.println(key + " " + pageFootnotes.get(key));
        		}
                
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
	            	
	            	if(!graphLine.isEmpty()) {
	            		TextRegionAnalyser.generateTextRegionAssociation(graphLine, regionBounds);
	            		
	            		List<RegionWithBound> newRegionList = collectFlowRegions(regionBounds, curPageInfo);
	            		documentRegions.put(pageKey, newRegionList);
	            		JsonExport.generateTextRegionJson(graphLine, newRegionList, p);
	            	}
            	}
            }
            catch (IOException ex)
            {
                LOG.error("Failed to process page " + p, ex);
            }
        }
    }
    
    private List<RegionWithBound> collectFlowRegions(List<RegionWithBound> allRegions, PageInfo pageInfo){
    	
    	ArrayList<RegionWithBound> flowRegions = new ArrayList<RegionWithBound>();
    	HashMap<Integer, Integer> oldVsNewIndex = new HashMap<Integer, Integer>();
    	
    	for(int i = 0; i < allRegions.size(); i++) {
    		
    		RegionWithBound region = allRegions.get(i);
    		if(!region.getNextRegions().isEmpty() || !region.getPrevRegions().isEmpty()) {
    			
    			int newIndex = flowRegions.size();
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
    	}
    	
    	return flowRegions;
    }
	
}
