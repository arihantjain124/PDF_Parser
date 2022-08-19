package parser.json;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import parser.config.ConfigProperty;

public class FootNotesJsonObjectSerializer implements JsonSerializer<FootNotesJsonObject> {
	
    public static final FootNotesJsonObjectSerializer INSTANCE = new FootNotesJsonObjectSerializer();
    
    private static final String ID_URL = ConfigProperty.getProperty("json-ld.id.url") + "/footnote/";

    private FootNotesJsonObjectSerializer() {}

	@Override
	public JsonElement serialize(FootNotesJsonObject footNoteJsonObject, Type type, JsonSerializationContext arg2) {
		JsonObject json = new JsonObject();
		
		json.addProperty("@id", ID_URL + footNoteJsonObject.getFootNoteKey());
		json.addProperty("@type", "nccn:Footnote");
		json.addProperty("nccn:content", footNoteJsonObject.getFootNoteContent());
		if (!footNoteJsonObject.getFootNoteLink().isEmpty()) {
			JsonArray link = new JsonArray();
			
			for (String linkkey : footNoteJsonObject.getFootNoteLink()) {
				link.add( linkkey);
			}
			json.add("nccn:links", link);
		}
		return json;
	}

}
