package parser.json;

import java.lang.reflect.Type;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import parser.config.ConfigProperty;
import parser.table.TableDetails;

public final class GuidelineContentSerializer implements JsonSerializer<GuidelineContent> {

    public static final GuidelineContentSerializer INSTANCE = new GuidelineContentSerializer();
    
    private static final String ID_URL = ConfigProperty.getProperty("json-ld.id.url") + "#";

    private GuidelineContentSerializer() {}

    @Override
    public JsonElement serialize(GuidelineContent content, Type type, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        JsonArray graph = new JsonArray();
        JsonArray pageInfo = new JsonArray();
        JsonArray updates = new JsonArray();
        
        JsonObject contextObj = new JsonObject();
        contextObj.addProperty("nccn", ID_URL);
        contextObj.addProperty("xsd", "http://www.w3.org/2001/XMLSchema#");
        
        JsonObject genericRefObj = new JsonObject();
        genericRefObj.addProperty("@type", "@id");
        
        contextObj.add("nccn:next", genericRefObj);
        contextObj.add("nccn:prev", genericRefObj);
        contextObj.add("nccn:contains", genericRefObj);
        contextObj.add("nccn:parent", genericRefObj);
        contextObj.add("nccn:reference", genericRefObj);
        contextObj.add("nccn:labels", genericRefObj);
        
        json.add("@context", contextObj);
        json.add("@nccn:page-info", pageInfo);
        json.add("@graph", graph);
        json.add("@updates", updates);

        for (GraphJsonObject graphJsonObject : content.getGraphObjects()) {
            graph.add(context.serialize(graphJsonObject));
        }
        
        for(TableDetails tableDetails : content.getTablesList()) {
        	graph.add(context.serialize(tableDetails));
        }
        
        for (FootNotesJsonObject footNoteJsonObject : content.getFootNotesJsonObject()) {
            graph.add(context.serialize(footNoteJsonObject));
        }
        
        for (LabelJsonObject labelJsonObject : content.getLabelObjects()) {
			graph.add(context.serialize(labelJsonObject));
		}
        
        for (TextJsonObject currTextObject : content.getTextObject()) {
			graph.add(context.serialize(currTextObject));
		}
        for(BookmarkJsonObject bookmarkobj : content.getBookmarkObjects()) {
        	pageInfo.add(context.serialize(bookmarkobj));
        }
        for(UpdatesJsonObject updateObj : content.getUpdateJsonObject()) {
        	updates.add(context.serialize(updateObj));
        }
        
        return json;
    }

}