package parser.json;

import java.lang.reflect.Type;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import parser.config.ConfigProperty;

public final class GuidelineContentSerializer implements JsonSerializer<GuidelineContent> {

    public static final GuidelineContentSerializer INSTANCE = new GuidelineContentSerializer();
    
    private static final String ID_URL = ConfigProperty.getProperty("json-ld.id.url") + "#";

    private GuidelineContentSerializer() {}

    @Override
    public JsonElement serialize(GuidelineContent content, Type type, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        JsonArray graph = new JsonArray();
        
        JsonObject contextObj = new JsonObject();
        contextObj.addProperty("nccn", ID_URL);
        contextObj.addProperty("xsd", "http://www.w3.org/2001/XMLSchema#");
        
        JsonObject genericRefObj = new JsonObject();
        genericRefObj.addProperty("@type", "@id");
        
        contextObj.add("nccn:next", genericRefObj);
        contextObj.add("nccn:prev", genericRefObj);
        contextObj.add("nccn:contains", genericRefObj);
        contextObj.add("nccn:reference", genericRefObj);
        contextObj.add("nccn:TScore", genericRefObj);
        contextObj.add("nccn:MScore", genericRefObj);
        contextObj.add("nccn:NScore", genericRefObj);
        contextObj.add("nccn:Stage", genericRefObj);
        
        json.add("@context", contextObj);
        json.add("@graph", graph);

        for (GraphJsonObject graphJsonObject : content.getGraphObjects()) {
            graph.add(context.serialize(graphJsonObject));
        }
        
        for (FootNotesJsonObject footNoteJsonObject : content.getFootNotesJsonObject()) {
            graph.add(context.serialize(footNoteJsonObject));
        }

        return json;
    }

}