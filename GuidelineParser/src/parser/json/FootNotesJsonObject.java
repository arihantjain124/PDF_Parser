package parser.json;

import java.util.Set;

public class FootNotesJsonObject {
	
	private String footNoteKey;

	private String content;
	
	private Set<String> footnotelink;
	
	public void setFootNoteKey(String key) {
		this.footNoteKey = key;
	}
	
	public void setFootNoteContent(String content) {
		this.content = content;
	}
	public void setFootNoteLink(Set<String> linkinContent) { 
		this.footnotelink = linkinContent;
	}

	public String getFootNoteKey() {
		return this.footNoteKey;
	}
	
	public String getFootNoteContent() {
		return this.content;
	}
	public Set<String> getFootNoteLink() { 
		return this.footnotelink;
	}
}
