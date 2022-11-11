package parser.json;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import parser.config.ConfigProperty;

public class UpdatesJsonObjectSerializer implements JsonSerializer<UpdatesJsonObject>  {
	
	public static final UpdatesJsonObjectSerializer INSTANCE = new UpdatesJsonObjectSerializer();
	
	private static final String ID_URL = ConfigProperty.getProperty("json-ld.id.url") + "/updates/";
	
	private UpdatesJsonObjectSerializer() {
		
	}
	@Override
	public JsonElement serialize(UpdatesJsonObject updateJsonObject, Type type, JsonSerializationContext arg2) {
		JsonObject json = new JsonObject();
		
		json.addProperty("@id", ID_URL + updateJsonObject.getIndex());
		json.addProperty("nccn:pageKey" , updateJsonObject.getPageKey());
		json.addProperty("nccn:content" , updateJsonObject.getContent());
		
		return json;
	}
}
