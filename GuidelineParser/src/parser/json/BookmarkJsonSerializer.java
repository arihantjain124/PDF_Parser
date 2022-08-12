package parser.json;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import parser.config.ConfigProperty;



public class BookmarkJsonSerializer implements JsonSerializer<BookmarkJsonObject> {

	public static final BookmarkJsonSerializer INSTANCE = new BookmarkJsonSerializer();
	
	private static final String ID_URL = ConfigProperty.getProperty("json-ld.id.url") + "/";
	
	private BookmarkJsonSerializer() {}

	@Override
	public JsonElement serialize(BookmarkJsonObject bookmarkobj, Type arg1, JsonSerializationContext arg2) {
		JsonObject json = new JsonObject();
		
		json.addProperty("@id", ID_URL +"page-label/"+ bookmarkobj.getId());
		json.addProperty("nccn:page-key", bookmarkobj.getPageKey());
		json.addProperty("nccn:page-no", bookmarkobj.getPageNo());
		json.addProperty("nccn:page-labels", bookmarkobj.getLabels());
		return json;
	}
	
	

}
