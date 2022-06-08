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
			labels.add(label);
		}		
		json.add("@type", labels);
		
		JsonArray stage = new JsonArray();
		for(String label : graphJsonObject.getStageScore()) {
			stage.add(label);
		}		
		json.add("Stage", stage);
		
		JsonArray tScore = new JsonArray();
		for(String label : graphJsonObject.getTScore()) {
			tScore.add(label);
		}		
		json.add("T-Score", tScore);

		JsonArray mScore = new JsonArray();
		for(String label : graphJsonObject.getMScore()) {
			mScore.add(label);
		}		
		json.add("M-Score", mScore);
		
		JsonArray nScore = new JsonArray();
		for(String label : graphJsonObject.getNScore()) {
			nScore.add(label);
		}		
		json.add("N-Score", nScore);
		
		
		json.addProperty("nccn:content", graphJsonObject.getConent());

		JsonArray prev = new JsonArray();
		for(Integer index : graphJsonObject.getPConnections()) {
			prev.add(ID_URL + index);
		}		
		json.add("nccn:prev", prev);
		
		JsonArray next = new JsonArray();
		for(Integer index : graphJsonObject.getNConnections()) {
			next.add(ID_URL + index);
		}		
		json.add("nccn:next", next);
		
		return json;
	}

}
