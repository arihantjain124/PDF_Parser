package parser.json;

import java.util.ArrayList;
import java.util.List;

public class TextJsonObject {
	
	private int index;
	
	private String content;
	
	private String pageKey;
	
	private int pageNo;

	private List<String> footnoteRefs = null;
	
	private int label;

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		footnoteRefs = new ArrayList<String>();
		this.index = index;
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
		this.footnoteRefs.addAll(footnoteRefs);
	}

	public String getPageKey() {
		return pageKey;
	}

	public void setPageKey(String pageKey) {
		this.pageKey = pageKey;
	}

	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}

	public int getLabel() {
		return label;
	}

	public void setLabel(int currlabel) {
		this.label = currlabel;
	}

}
