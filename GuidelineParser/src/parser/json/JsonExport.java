package parser.json;


import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import org.javatuples.Pair;

import parser.config.ConfigProperty;
import parser.page.PageInfo;
import parser.table.TableDetails;
import parser.text.FootnoteDetails;
import parser.text.RegionWithBound;
import parser.text.WordWithBounds;

public class JsonExport {
	
	public static void generateStagingJsonObject(List<RegionWithBound> regionBounds,List<StagingJsonObject> allStagingObject)
			throws JsonIOException, IOException {
		
		List<Pattern> stagingRegex= new ArrayList<>();
		
		boolean noStagingInfo = true;
		
		stagingRegex.add(Pattern.compile(ConfigProperty.getProperty("Tscore.regex")));
		stagingRegex.add(Pattern.compile(ConfigProperty.getProperty("Mscore.regex")));
		stagingRegex.add(Pattern.compile(ConfigProperty.getProperty("Nscore.regex")));
		stagingRegex.add(Pattern.compile(ConfigProperty.getProperty("StageScore.regex")));


        Matcher matcher = null;
        
		for (RegionWithBound region : regionBounds) {
			noStagingInfo = true;
			StagingJsonObject currJsonObject = new StagingJsonObject();

			int index = regionBounds.indexOf(region);
			
			currJsonObject.setIndex(index);

			if (!region.getNextRegions().isEmpty() || !region.getPrevRegions().isEmpty()) {

				String currentContent = "";
				for (WordWithBounds word : region.getContentLines()) {
					currentContent = currentContent + word.getText();
				}
				currJsonObject.setConent(currentContent);
				for(int i=0 ; i<4 ; i++) 
				{
					int temp = 0;
					Pattern currPattern = stagingRegex.get(i);
					matcher = currPattern.matcher(currentContent);
					while (matcher.find()) {
					    temp++;
					}
					if (temp>0) {
						noStagingInfo = false;
						matcher = currPattern.matcher(currentContent);
						while (matcher.find()) {
							switch(i)
							{
							case 0:
								currJsonObject.setTScore(matcher.group());
								break;
							case 1:
								currJsonObject.setMScore(matcher.group());
								break;
							case 2:
								currJsonObject.setNScore(matcher.group());
								break;
							case 3:
								currJsonObject.setStageScore(matcher.group());
								break;
							}
							
							}
					}
				}
				if(!noStagingInfo)
				allStagingObject.add(currJsonObject);
			}
		}
		
	}
	
	
	public static void generateJsonGraphObject(List<RegionWithBound> regionBounds,HashMap<String, PageInfo> pageHashMap, List<GraphJsonObject> allGraphObject,
			HashMap<String, List<Pair<Rectangle2D, LabelJsonObject>>> labelsJsonHashMap)
			throws JsonIOException, IOException {

		GraphJsonObject currJsonObject = new GraphJsonObject();

		Pattern pageKeyRegex = Pattern.compile(ConfigProperty.getProperty("page.key.regex"));
		
		List<Pattern> stagingRegex= new ArrayList<>();
		
		
		stagingRegex.add(Pattern.compile(ConfigProperty.getProperty("Tscore.regex")));
		stagingRegex.add(Pattern.compile(ConfigProperty.getProperty("Mscore.regex")));
		stagingRegex.add(Pattern.compile(ConfigProperty.getProperty("Nscore.regex")));
		stagingRegex.add(Pattern.compile(ConfigProperty.getProperty("StageScore.regex")));


        Matcher matcher = null;
		Matcher pageKeymatcher = null;

		for (RegionWithBound region : regionBounds) {

			currJsonObject = new GraphJsonObject();
			int index = regionBounds.indexOf(region);
			
			currJsonObject.setIndex(index);

			if (!region.getNextRegions().isEmpty() || !region.getPrevRegions().isEmpty() || 
					(region.getParentRegionIndex() >= 0)) {

				String currentContent = "";
				if(region.isImaginary()) {
					currentContent = "IMAGINARY NODE";
				}else {
					for (WordWithBounds word : region.getContentLines()) {
						if(!currentContent.isEmpty()) {
							currentContent = currentContent + " " + word.getText().trim();
						}else {
							currentContent = word.getText().trim();
						}
					}
				}
			
				currJsonObject.setConent(currentContent);
				
				for(int i=0 ; i<4 ; i++) 
				{
					int temp = 0;
					Pattern currPattern = stagingRegex.get(i);
					matcher = currPattern.matcher(currentContent);
					while (matcher.find()) {
					    temp++;
					}
					if (temp>0) {
						matcher = currPattern.matcher(currentContent);
						while (matcher.find()) {
							switch(i)
							{
							case 0:
								currJsonObject.setTScore(matcher.group());
								break;
							case 1:
								currJsonObject.setMScore(matcher.group());
								break;
							case 2:
								currJsonObject.setNScore(matcher.group());
								break;
							case 3:
								currJsonObject.setStageScore(matcher.group());
								break;
							}	
						}
					}
				}
				for (int prevRegionIndex : region.getPrevRegions()) {
					currJsonObject.addPrevIndex(prevRegionIndex);
				}

				for (int nextRegionIndex : region.getNextRegions()) {
					currJsonObject.addNextIndex(nextRegionIndex);
				}
				
				for (int childRegionIndex : region.getChildRegions()) {
					currJsonObject.addChild(childRegionIndex);
				}

				String contentWithoutFootnoteRef = currentContent.replaceAll("\\{[^\\{]+\\}", "");//Footnote reference may have page key
				pageKeymatcher = pageKeyRegex.matcher(contentWithoutFootnoteRef);
				if (pageKeymatcher.groupCount() >= 0) {
					
					while (pageKeymatcher.find()) {
						PageInfo currPageInfo = pageHashMap.get(pageKeymatcher.group());
						
						if (currPageInfo != null) {
							currJsonObject.addNextIndex(currPageInfo.getStartRegionIndices());
							for (int linkPrevPage : currPageInfo.getStartRegionIndices()) {
								regionBounds.get(linkPrevPage).addPrevRegion(index);
							}
						}
					}
				}
								
				double currentRegionY = region.getBound().getY();
				
				List<Pair<Rectangle2D, LabelJsonObject>> currentPageLabels = labelsJsonHashMap
						.get(region.getPageKey());
				for (Pair<Rectangle2D, LabelJsonObject> labelbox : currentPageLabels) {

					Rectangle2D currRect = labelbox.getValue0();
					Rectangle2D transformedRect = new Rectangle();
					transformedRect.setRect(currRect.getX(), currentRegionY, currRect.getWidth(), currRect.getHeight());

					if (region.getBound().intersects(transformedRect)) {
						currJsonObject.addLabel(labelbox.getValue1().getIndex());
					}
				}
				currJsonObject.setType("object");
				currJsonObject.setPageKey(region.getPageKey());
				currJsonObject.setPageNo(region.getPageNo());
				currJsonObject.setParent(region.getParentRegionIndex());
				currJsonObject.addFootnoteRefs(region.getFootnoteRefs());
				currJsonObject.setBound(region.getBound());
				allGraphObject.add(currJsonObject);
			}
		}

	}
	
	public static void generateJsonFootNote(HashMap<String, FootnoteDetails> pageFootnotes ,List<FootNotesJsonObject> allFootNoteObject) {
		
		for (String key : pageFootnotes.keySet()) {
			FootNotesJsonObject currFootNotesJsonObject = new FootNotesJsonObject();
			currFootNotesJsonObject.setFootNoteKey(key);
			currFootNotesJsonObject.setFootNoteContent(pageFootnotes.get(key).getFootNoteText());
			currFootNotesJsonObject.setFootNoteLink(pageFootnotes.get(key).getLinkKeys());
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
	
	public static void writeJsonLD(GuidelineContent guidelineContent, int startPage, int endPage, String prefix)
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
			Gson gson = gson();
			writer.append(gson.toJson(guidelineContent, GuidelineContent.class));
		} finally {
			writer.close();
		}

	}	
	
    private static Gson gson() {
        return new GsonBuilder()
                .registerTypeAdapter(GuidelineContent.class, GuidelineContentSerializer.INSTANCE)
                .registerTypeAdapter(GraphJsonObject.class, GraphJsonObjectSerializer.INSTANCE)
                .registerTypeAdapter(FootNotesJsonObject.class, FootNotesJsonObjectSerializer.INSTANCE)
                .registerTypeAdapter(LabelJsonObject.class, LabelJsonObjectSerializer.INSTANCE)
                .registerTypeAdapter(TextJsonObject.class, TextJsonObjectSerializer.INSTANCE)
                .registerTypeAdapter(TableDetails.class, GuidelineTableSerializer.INSTANCE)
                .registerTypeAdapter(BookmarkJsonObject.class, BookmarkJsonSerializer.INSTANCE)
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();
    }
}
