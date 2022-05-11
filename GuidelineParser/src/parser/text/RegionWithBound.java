package parser.text;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class RegionWithBound {

	private Rectangle2D bound;
	
	private List<WordWithBounds> contentLines = new ArrayList<WordWithBounds>();
	private List<Integer> nextRegions = new ArrayList<Integer>();
	private String pageKey = "";
	private int pageNo;
	private List<Integer> prevRegions = new ArrayList<Integer>();
	
	public RegionWithBound(Rectangle2D bound, WordWithBounds line) {
		this.bound = bound;
		this.contentLines.add(line);
	}
	
	public Rectangle2D getBound() {
		return bound;
	}
	
	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}
	
	public int getPageNo() {
		return this.pageNo;
	}
	
	public void setPageKey(String newPageKey) {
		this.pageKey = newPageKey;
	}
	
	public String getPageKey() {
		return pageKey;
	}
	
	public void setBound(Rectangle2D newBound) {
		bound = newBound;
	}
	
	public void addContentLine(WordWithBounds line) {
		contentLines.add(line);
	}
	
	public List<WordWithBounds> getContentLines() {
		return contentLines;
	}
	
	public void addNextRegion(int nextRegionIndex) {
		nextRegions.add(nextRegionIndex);
	}
	
	public List<Integer> getNextRegions() {
		return nextRegions;
	}
	
	public void addPrevRegion(int prevRegionIndex) {
		prevRegions.add(prevRegionIndex);
	}
	
	public List<Integer> getPrevRegions() {
		return prevRegions;
	}
}
