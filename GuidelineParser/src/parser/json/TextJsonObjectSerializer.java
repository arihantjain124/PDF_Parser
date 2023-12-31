package parser.json;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import parser.config.ConfigProperty;

public class TextJsonObjectSerializer implements JsonSerializer<TextJsonObject> {
	
	public static final TextJsonObjectSerializer INSTANCE = new TextJsonObjectSerializer();

	private static final String ID_URL = ConfigProperty.getProperty("json-ld.id.url") + "/textbox/";
	
	private static final String Base_ID_URL = ConfigProperty.getProperty("json-ld.id.url") + "/"; 

	private TextJsonObjectSerializer() {
	}
	@Override
	public JsonElement serialize(TextJsonObject textJsonObject, Type type, JsonSerializationContext arg2) {
		JsonObject json = new JsonObject();

		json.addProperty("@id", ID_URL + textJsonObject.getIndex());
//		json.addProperty("@type", "nccn:Textbox");
		json.addProperty("nccn:content", textJsonObject.getContent());
		json.addProperty("nccn:pageNo", textJsonObject.getPageNo());
		json.addProperty("nccn:pageKey", textJsonObject.getPageKey());
		JsonArray labels = new JsonArray();
		labels.add(ID_URL + "labels/" + textJsonObject.getLabel());
		json.add("nccn:labels", labels);
		
		if (textJsonObject.getFootnoteRefs() != null) {
			JsonArray references = new JsonArray();
			for (String footnoteKey : textJsonObject.getFootnoteRefs()) {
				references.add(Base_ID_URL + "footnote/" + footnoteKey);
			}
			json.add("nccn:reference", references);
		}
		return json;
	}

}
