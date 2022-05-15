package parser.json;

import java.util.List;

public class GuidelineContent {

	private List<GraphJsonObject> graphObjects;
	
	private List<FootNotesJsonObject> footNoteObjects;
	
	public void setGraphObjects(List<GraphJsonObject> contentObjects) {
		this.graphObjects = contentObjects;
	}
	
	public List<GraphJsonObject> getGraphObjects() {
		return this.graphObjects;
	}
	
	public void setFootNotesJsonObject(List<FootNotesJsonObject> footNoteObjects) {
		this.footNoteObjects = footNoteObjects;
	}
	
	public List<FootNotesJsonObject> getFootNotesJsonObject() {
		return this.footNoteObjects;
	}
}
