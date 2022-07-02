package parser.json;

import java.util.ArrayList;
import java.util.List;

public class LabelJsonObject {

	private int index;

	private String content;

	private List<String> footnoteRefs = null;

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
		setFootnoteRefs(new ArrayList<String>());
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public List<String> getFootnoteRefs() {
		return footnoteRefs;
	}

	public void setFootnoteRefs(List<String> footnoteRefs) {
		this.footnoteRefs = footnoteRefs;
	}

}
