package parser.json;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import parser.config.ConfigProperty;

public class GraphJsonObjectSerializer implements JsonSerializer<GraphJsonObject> {
	
    public static final GraphJsonObjectSerializer INSTANCE = new GraphJsonObjectSerializer();
    
    private static final String ID_URL = ConfigProperty.getProperty("json-ld.id.url") + "/";

    private GraphJsonObjectSerializer() {}

	@Override
	public JsonElement serialize(GraphJsonObject graphJsonObject, Type type, JsonSerializationContext arg2) {
		JsonObject json = new JsonObject();
		
		json.addProperty("@id", ID_URL + graphJsonObject.getIndex());
		json.addProperty("nccn:page-key", graphJsonObject.getPageKey());
		json.addProperty("nccn:page-no", graphJsonObject.getPageNo());
		
		HashMap<String, Double> bounds = new HashMap<>();

		bounds.put("X",
				new BigDecimal(graphJsonObject.getBound().getX()).setScale(2, RoundingMode.HALF_UP).doubleValue());
		bounds.put("Y",
				new BigDecimal(graphJsonObject.getBound().getY()).setScale(2, RoundingMode.HALF_UP).doubleValue());
		bounds.put("W",
				new BigDecimal(graphJsonObject.getBound().getWidth()).setScale(2, RoundingMode.HALF_UP).doubleValue());
		bounds.put("H",
				new BigDecimal(graphJsonObject.getBound().getHeight()).setScale(2, RoundingMode.HALF_UP).doubleValue());
		
		String link="http://localhost:8080/nscl.pdf?page=";
		
		json.addProperty("nccn:nodelink",link+ graphJsonObject.getPageNo()+"&x="+bounds.get("X").toString()+"&y="+bounds.get("Y").toString()+"&width="+bounds.get("W").toString()+"&height="+bounds.get("H").toString());
		json.addProperty("nccn:pagelink", "http://localhost:8080/nscl.pdf#page="+graphJsonObject.getPageNo());
		
//		Gson gson = new Gson();
//		json.add("nccn:bounds", gson.toJsonTree(bounds).getAsJsonObject());
		json.addProperty("nccn:bounds", "X=" + bounds.get("X") + ",Y=" + bounds.get("Y") + ",W=" + bounds.get("W") + ",H=" + bounds.get("H") );
		JsonArray labels = new JsonArray();
		
		for (Integer label : graphJsonObject.getLabels()) {
			labels.add(ID_URL + "labels/" + label);
		}	
		json.add("nccn:labels", labels);
		
		if(!graphJsonObject.getStageScore().isEmpty()) {
			JsonArray stage = new JsonArray();
			for(String label : graphJsonObject.getStageScore()) {
				stage.add(label);
			}		
			json.add("nccn:stage", stage);
		}
		
		if(!graphJsonObject.getTScore().isEmpty()) {
			JsonArray tScore = new JsonArray();
			for(String label : graphJsonObject.getTScore()) {
				tScore.add(label);
			}		
			json.add("nccn:tscore", tScore);
		}

		if(!graphJsonObject.getMScore().isEmpty()) {
			JsonArray mScore = new JsonArray();
			for(String label : graphJsonObject.getMScore()) {
				mScore.add(label);
			}		
			json.add("nccn:mscore", mScore);
		}
		
		if(!graphJsonObject.getNScore().isEmpty()) {
			JsonArray nScore = new JsonArray();
			for(String label : graphJsonObject.getNScore()) {
				nScore.add(label);
			}		
			json.add("nccn:nscore", nScore);
		}
		
		String content = graphJsonObject.getConent();
		if(content.startsWith("\u2022")) {
			content = content.substring(1).trim();//Remove starting bullets
		}
		
		content = content.replace("\u2013", "-");//Change unicode "-" to ASCII "-" 
		
		json.addProperty("nccn:content", content);

		if(!graphJsonObject.getPConnections().isEmpty()) {
			JsonArray prev = new JsonArray();
			for(Integer index : graphJsonObject.getPConnections()) {
				prev.add(ID_URL + index);
			}		
			json.add("nccn:prev", prev);
		}
		
		if(!graphJsonObject.getNConnections().isEmpty()) {
			JsonArray next = new JsonArray();
			for(Integer index : graphJsonObject.getNConnections()) {
				next.add(ID_URL + index);
			}		
			json.add("nccn:next", next);
		}
		
		if(!graphJsonObject.getChildren().isEmpty()) {
			JsonArray children = new JsonArray();
			for(Integer index : graphJsonObject.getChildren()) {
				children.add(ID_URL + index);
			}		
			json.add("nccn:contains", children);
		}
		
		if(graphJsonObject.getParent() >= 0) {
			int parentIndex = graphJsonObject.getParent();
			json.addProperty("nccn:parent", ID_URL + parentIndex);
		}
		
		if(!graphJsonObject.getFootnoteRefs().isEmpty()) {
			JsonArray references = new JsonArray();
			for(String footnoteKey : graphJsonObject.getFootnoteRefs()) {
				references.add(ID_URL + "footnote/" + footnoteKey);
			}		
			json.add("nccn:reference", references);
		}
		
		return json;
	}

}
