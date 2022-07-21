package parser.json;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import parser.config.ConfigProperty;
import parser.table.TableDetails;
import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;

public class GuidelineTableSerializer implements JsonSerializer<TableDetails>{

	public static final GuidelineTableSerializer INSTANCE = new GuidelineTableSerializer();
	
	 private static final String ID_URL = ConfigProperty.getProperty("json-ld.id.url") + "/table/";

	@SuppressWarnings("rawtypes")
	@Override
	public JsonElement serialize(TableDetails tableDetails, Type arg1, JsonSerializationContext context) {

		JsonObject json = new JsonObject();
		JsonObject tableData = new JsonObject();
        
        Table table = tableDetails.getTable();
        
		json.addProperty("@id", ID_URL + tableDetails.getPageKey() + "/" + tableDetails.getIndex());
		json.addProperty("nccn:page-key", tableDetails.getPageKey());
		json.addProperty("nccn:page-no", tableDetails.getPageNumber());
        json.add("nccn:table", tableData);

        JsonArray jsonRows = new JsonArray();
        tableData.add("nccn:row", jsonRows);
        
        for (int i = 0; i < table.getRows().size(); i++) {
        	
        	List<RectangularTextContainer> tableRow = table.getRows().get(i);
        	
        	JsonObject jsonRow = new JsonObject();
        	jsonRow.addProperty("nccn:row-num", i+1);
        	
        	JsonArray jsonCells = new JsonArray();
        	jsonRow.add("nccn:cell", jsonCells);
            
            for (int j = 0; j < tableRow.size(); j++) {
            	
            	JsonObject jsonCell = new JsonObject();
            	RectangularTextContainer textChunk = tableRow.get(j);
            	jsonCell.addProperty("nccn:cell-num", j+1);
            	jsonCell.addProperty("nccn:content", textChunk.getText());
            	
            	jsonCells.add(jsonCell);
            }
            
            jsonRows.add(jsonRow);
        }

        return json;
	}
}
