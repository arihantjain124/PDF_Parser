package parser.json;

public class UpdatesJsonObject {
	
	private int index;

	private String content;
	
	private String PageKey;

	public String getPageKey() {
		return PageKey;
	}

	public void setPageKey(String pageKey) {
		PageKey = pageKey;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
}
