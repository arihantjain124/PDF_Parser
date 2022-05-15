package parser.json;

public class FootNotesJsonObject {
	
	private String footNoteKey;

	private String content;
	
	public void setFootNoteKey(String key) {
		this.footNoteKey = key;
	}
	
	public void setFootNoteContent(String content) {
		this.content = content;
	}

	public String getFootNoteKey() {
		return this.footNoteKey;
	}
	
	public String getFootNoteContent() {
		return this.content;
	}
}
