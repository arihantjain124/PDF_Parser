package parser.json;

import java.lang.reflect.Type;

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
		
		JsonArray labels = new JsonArray();
		for(String label : graphJsonObject.getLabels()) {
			labels.add("nccn:" + label);
		}		
		json.add("@type", labels);
		
		if(!graphJsonObject.getStageScore().isEmpty()) {
			JsonArray stage = new JsonArray();
			for(String label : graphJsonObject.getStageScore()) {
				stage.add(label);
			}		
			json.add("Stage", stage);
		}
		
		if(!graphJsonObject.getTScore().isEmpty()) {
			JsonArray tScore = new JsonArray();
			for(String label : graphJsonObject.getTScore()) {
				tScore.add(label);
			}		
			json.add("T-Score", tScore);
		}

		if(!graphJsonObject.getMScore().isEmpty()) {
			JsonArray mScore = new JsonArray();
			for(String label : graphJsonObject.getMScore()) {
				mScore.add(label);
			}		
			json.add("M-Score", mScore);
		}
		
		if(!graphJsonObject.getNScore().isEmpty()) {
			JsonArray nScore = new JsonArray();
			for(String label : graphJsonObject.getNScore()) {
				nScore.add(label);
			}		
			json.add("N-Score", nScore);
		}
		
		
		json.addProperty("nccn:content", graphJsonObject.getConent());

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
				references.add(ID_URL + footnoteKey);
			}		
			json.add("nccn:reference", references);
		}
		
		return json;
	}

}
