package parser;

import java.io.FileWriter;
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
	
	static void generateTextRegionJson(ArrayList<GraphObject> graphLine, List<RegionWithBound>  regionBounds, int pageIndex) throws JsonIOException, IOException {
		
		GsonBuilder builder = new GsonBuilder(); 
		builder.setPrettyPrinting(); 
		Gson gson = builder.create();
		GraphJsonObject currJsonObject = new GraphJsonObject();
		
		
		String outputPrefix = "NCCN_NSCL_pdf_";
        String filePath = outputPrefix + (pageIndex + 1) + ".json";
        Writer writer = Files.newBufferedWriter(Paths.get(filePath));
        
	    for(RegionWithBound region : regionBounds) {

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
            	
            	gson.toJson(currJsonObject, writer);

	    	}
        	
	    } 

        writer.close();
        }
	}
