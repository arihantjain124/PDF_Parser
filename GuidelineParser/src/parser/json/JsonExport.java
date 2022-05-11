package parser.json;


import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.File;
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
			HashMap<String, PageInfo> pageHashMap, List<GraphJsonObject> allGraphObject,HashMap<String, List<RegionWithBound>> labelsHashMap)
			throws JsonIOException, IOException {

		GraphJsonObject currJsonObject = new GraphJsonObject();
		Pattern pattern = Pattern.compile(ConfigProperty.getProperty("page.key.regex"));

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
								
				double currentRegionY = region.getBound().getY();
				
				List<RegionWithBound> currentPageLabels = labelsHashMap.get(region.getPageKey());
				for (RegionWithBound labelbox : currentPageLabels) {
					
					Rectangle2D currRect = labelbox.getBound();
					Rectangle2D transformedRect = new Rectangle();
					transformedRect.setRect(currRect.getX(),currentRegionY,currRect.getWidth(),currRect.getHeight());
					
					if (region.getBound().intersects(transformedRect)) {
						String currentLabel = "";
						for (WordWithBounds word : labelbox.getContentLines()) {
							currentLabel = currentLabel + word.getText();
						}
						currJsonObject.addLabel(currentLabel);
					}
				}
				
				currJsonObject.setType("object");
				currJsonObject.setPageKey(region.getPageKey());
				currJsonObject.setPageNo(region.getPageNo());
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
	
		
	
	public static void writeJson(List<?> allJsonObject, int startPage, int endPage, String prefix)
			throws JsonIOException, IOException {

		String filePath = "jsonexport/";

		if (!Paths.get(filePath).toFile().isDirectory()) {
			//System.out.println("No Folder for jsonexport");
			File f = new File(filePath);
			f.mkdir();
			//System.out.println("Folder created");
		}

		String JsonFilePath = filePath + "/NCCN_NSCL" + prefix + "_" + startPage + "_" + endPage + ".json";
		Writer writer = Files.newBufferedWriter(Paths.get(JsonFilePath));
		try {
			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			Gson gson = builder.create();
			gson.toJson(allJsonObject, writer);
		} finally {
			writer.close();
		}

	}

	

}
