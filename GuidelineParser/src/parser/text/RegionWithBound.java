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
	private List<Integer> childRegions = new ArrayList<Integer>();
	private int parentRegionIndex = -1;
	private boolean isImaginary = false;
	
	private ArrayList<String> footnoteRefs = new ArrayList<String>();
	
	public RegionWithBound(Rectangle2D bound) {
		this.bound = bound;
	}
	
	public RegionWithBound(Rectangle2D bound, WordWithBounds line) {
		this.bound = bound;
		this.contentLines.add(line);
	}
	
	public RegionWithBound(List<WordWithBounds> lines, int parentIndex) {
		
		Rectangle2D regionBound = lines.get(0).getbound();
		for(int i = 1; i < lines.size(); i++) {
			Rectangle2D curBound = lines.get(i).getbound();
			regionBound = (Rectangle2D.Float) regionBound.createUnion(curBound);
		}
		
		this.bound = regionBound;
		this.contentLines.addAll(lines);
		this.parentRegionIndex = parentIndex;
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
	
	public void resetContentLine() {
		contentLines.clear();
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
	
	public List<Integer> getChildRegions() {
		return childRegions;
	}
	
	public void addChildRegion(int childRegionIndex) {
		childRegions.add(childRegionIndex);
	}
	
	public int getParentRegionIndex() {
		return parentRegionIndex;
	}
	
	public void addFootnoteRefs(List<String> footnoteRefs) {
		this.footnoteRefs.addAll(footnoteRefs);
	}
	
	public List<String> getFootnoteRefs() {
		return footnoteRefs;
	}
	
	public void setIsImaginary(boolean imaginary) {
		isImaginary = imaginary;
	}
	
	public boolean isImaginary() {
		return isImaginary;
	}
}
