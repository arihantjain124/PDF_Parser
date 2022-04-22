package parser.json;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;

import parser.config.ConfigProperty;
import parser.page.PageInfo;
import parser.text.RegionWithBound;
import parser.text.WordWithBounds;

public class JsonExport {
	
	public static void generateJsonGraphObject(List<RegionWithBound> regionBounds,
			HashMap<String, PageInfo> pageHashMap, List<GraphJsonObject> allGraphObject)
			throws JsonIOException, IOException {

		GraphJsonObject currJsonObject = new GraphJsonObject();
		Pattern pattern = Pattern.compile(ConfigProperty.getProperty("regexForPageKey"));

		Matcher matcher = null;

		for (RegionWithBound region : regionBounds) {

			currJsonObject = new GraphJsonObject();
			int index = regionBounds.indexOf(region);
			currJsonObject.setIndex(index);

			if (!region.getNextRegions().isEmpty() || !region.getPrevRegions().isEmpty()) {

				String currentContent = "";
				for (WordWithBounds word : region.getContentLines()) {
					currentContent = currentContent + word.getText();
				}

				currJsonObject.setConent(currentContent);

				for (int prevRegionIndex : region.getPrevRegions()) {
					currJsonObject.addPrevIndex(prevRegionIndex);
				}

				for (int nextRegionIndex : region.getNextRegions()) {
					currJsonObject.addNextIndex(nextRegionIndex);
				}

				matcher = pattern.matcher(currentContent);
				if (matcher.groupCount() >= 0) {
					
					while (matcher.find()) {
						PageInfo currPageInfo = pageHashMap.get(matcher.group());
						
						if (currPageInfo != null) {
							currJsonObject.addNextIndex(currPageInfo.getStartRegionIndices());
							for (int linkPrevPage : currPageInfo.getStartRegionIndices()) {
								regionBounds.get(linkPrevPage).addPrevRegion(index);
							}
						}
					}
				}

				currJsonObject.setType("object");

				allGraphObject.add(currJsonObject);
			}
		}

	}
	
	public static void generateJsonFootNote(HashMap<String, String> pageFootnotes ,List<FootNotesJsonObject> allFootNoteObject) {
		
		for (String key : pageFootnotes.keySet()) {
			FootNotesJsonObject currFootNotesJsonObject = new FootNotesJsonObject();
			currFootNotesJsonObject.SetFootNoteKey(key);
			currFootNotesJsonObject.setFootNoteContent(pageFootnotes.get(key));
			allFootNoteObject.add(currFootNotesJsonObject);
		}
	}
	
		
	
	public static void generateJson(List allJsonObject,Writer writer) throws JsonIOException, IOException {
       
        try {
			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			Gson gson = builder.create();
			gson.toJson(allJsonObject, writer);
        }finally {
			writer.close();
		}
	   
	   }

	

}
