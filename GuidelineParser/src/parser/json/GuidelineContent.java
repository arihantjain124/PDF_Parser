package parser.json;

import java.util.List;

public class GuidelineContent {

	private List<GraphJsonObject> graphObjects;
	
	private List<FootNotesJsonObject> footNoteObjects;

	private List<LabelJsonObject> labelObjects;
	
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
	
	public List<LabelJsonObject> getLabelObjects() {
		return labelObjects;
	}

	public void setLabelObjects(List<LabelJsonObject> labelObjects) {
		this.labelObjects = labelObjects;
	}
}
