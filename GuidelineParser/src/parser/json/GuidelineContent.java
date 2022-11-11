package parser.json;

import java.util.List;

import parser.table.TableDetails;

public class GuidelineContent {

	private List<GraphJsonObject> graphObjects;
	
	private List<FootNotesJsonObject> footNoteObjects;

	private List<LabelJsonObject> labelObjects;
	
	private List<TableDetails> tablesList;

	private List<TextJsonObject> alltextObject;
	
	private List<BookmarkJsonObject> bookmarkobject;
	
	private List<UpdatesJsonObject> updateJsonObject;
	
	public List<TableDetails> getTablesList() {
		return tablesList;
	}

	public void setTablesList(List<TableDetails> tables) {
		this.tablesList = tables;
	}

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

	public List<TextJsonObject> getTextObject() {
		return alltextObject;
	}

	public void setTextObject(List<TextJsonObject> alltextObject) {
		this.alltextObject = alltextObject;
	}
	public void setBookmarkObjects(List<BookmarkJsonObject> bookmarkobj) {
		 this.bookmarkobject=bookmarkobj;
	}
	
	public List<BookmarkJsonObject> getBookmarkObjects() {
		return bookmarkobject;
	}

	public List<UpdatesJsonObject> getUpdateJsonObject() {
		return updateJsonObject;
	}

	public void setUpdateJsonObject(List<UpdatesJsonObject> updateJsonObject) {
		this.updateJsonObject = updateJsonObject;
	}
}
