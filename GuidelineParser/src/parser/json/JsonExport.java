package parser.json;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;

import parser.graphics.GraphJsonObject;
import parser.graphics.GraphObject;
import parser.text.RegionWithBound;
import parser.text.WordWithBounds;

public class JsonExport {
	
	public static void generateTextRegionJson(ArrayList<GraphObject> graphLine, List<RegionWithBound>  regionBounds, int pageIndex) 
			throws JsonIOException, IOException {
		
		GsonBuilder builder = new GsonBuilder(); 
		builder.setPrettyPrinting(); 
		Gson gson = builder.create();
		
        List<GraphJsonObject> graphJsonObjectList = new ArrayList<GraphJsonObject>();
        
	    for(RegionWithBound region : regionBounds) {

	    	GraphJsonObject currJsonObject = new GraphJsonObject();
	    	currJsonObject.setIndex(regionBounds.indexOf(region));
        	
        	if(!region.getNextRegions().isEmpty() || !region.getPrevRegions().isEmpty()) {

        		String currentContent = "" ;
        		for(WordWithBounds word : region.getContentLines()) {
        			currentContent = currentContent + word.getText();
        		}
        		
        		currJsonObject.setConent(currentContent);
    			currJsonObject.addPrevIndex(region.getPrevRegions());
    			currJsonObject.addNextIndex(region.getNextRegions());
    			
            	currJsonObject.setType("object");
            	
            	graphJsonObjectList.add(currJsonObject);

	    	}
        	
	    }
	    
		String outputPrefix = "NCCN_NSCL_pdf_";
        String filePath = outputPrefix + pageIndex + ".json";
        Writer writer = Files.newBufferedWriter(Paths.get(filePath));
        
	    gson.toJson(graphJsonObjectList, writer);

        writer.close();
    }
}
