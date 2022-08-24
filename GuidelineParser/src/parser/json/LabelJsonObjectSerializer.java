package parser.json;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import parser.config.ConfigProperty;

public class LabelJsonObjectSerializer implements JsonSerializer<LabelJsonObject> {

	public static final LabelJsonObjectSerializer INSTANCE = new LabelJsonObjectSerializer();

	private static final String ID_URL = ConfigProperty.getProperty("json-ld.id.url") + "/";

	private LabelJsonObjectSerializer() {
	}

	@Override
	public JsonElement serialize(LabelJsonObject labelJsonObject, Type type, JsonSerializationContext arg2) {
		JsonObject json = new JsonObject();

		json.addProperty("@id", ID_URL + "labels/" + labelJsonObject.getIndex());
		json.addProperty("@type", "nccn:Labels");
		json.addProperty("nccn:content", labelJsonObject.getContent());

		if (!labelJsonObject.getFootnoteRefs().isEmpty()) {
			JsonArray references = new JsonArray();
			for (String footnoteKey : labelJsonObject.getFootnoteRefs()) {
				references.add(ID_URL + "footnote/" + footnoteKey);
			}
			json.add("nccn:reference", references);
		}

		return json;
	}

}
