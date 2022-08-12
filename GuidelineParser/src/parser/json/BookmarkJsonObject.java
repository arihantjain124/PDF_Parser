package parser.json;

public class BookmarkJsonObject {
	
	private int pageid;
	
	private String pageKey;
	
	private String pageLabels;

	private int pageNo;
	
	public void setId(int  id) {
		this.pageid=id;
	}
	
	public int getId() {
		return pageid;
	}
	
	
	public void setPageKey(String key) {
		this.pageKey = key;
	}
	
	public String getPageKey() {
		return pageKey;
	}
	
	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}
	
	public int getPageNo() {
		return pageNo;
	}
	public void setLabels(String labels) {
		this.pageLabels=labels;
	}
	public String getLabels() {
		return pageLabels;
	}
}
