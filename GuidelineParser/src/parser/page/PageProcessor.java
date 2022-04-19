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
    	
        for (int p = startPage; p <= endPage; ++p)
        {        	
            try
            {
            	String pageKey = extractKey(document, p);
            	pageHashMap.put(pageKey, new PageInfo(p, pageKey));
            	
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
	            		JsonExport.generateTextRegionJson(graphLine, regionBounds, p);
	            	}
            	}
            }
            catch (IOException ex)
            {
                LOG.error("Failed to process page " + p, ex);
            }
        }
    }
	
}
