package parser.page;

import java.util.ArrayList;
import java.util.List;

public class PageInfo {

	private int pageNumber;
	private String pageKey;
	
	private List<Integer> startRegionIndices = new ArrayList<Integer>();
	
	private int startRegionIndexOffset = 0; 
	
	public int getStartRegionIndexOffset() {
		return startRegionIndexOffset;
	}

	public void setStartRegionIndexOffset(int startRegionIndexOffset) {
		this.startRegionIndexOffset = startRegionIndexOffset;
	}

	public PageInfo(int pageNum, String key) {
		this.pageNumber = pageNum;
		this.pageKey = key;
	}
	
	public void setPageNumber(int number) {
		this.pageNumber = number;
	}
	
	public int getPageNumber() {
		return pageNumber;
	}
	
	public void setPageKey(String key) {
		this.pageKey = key;
	}
	
	public String getPageKey() {
		return pageKey;
	}
	
	public void addStartRegionIndex(int index){
		startRegionIndices.add(index);
	}
	
	public List<Integer> getStartRegionIndices(){
		return startRegionIndices;
	}
}
