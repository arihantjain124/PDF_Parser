package parser.json;

import java.util.ArrayList;
import java.util.List;

public class GraphJsonObject {
	
	private int index;
	
	private String pageKey;

	private int pageNo;
	
	private String content;

	private List<Integer> pConnections = null;

	private List<Integer> nConnections = null;

	private List<String> labels = null;

	private String type;
	
	private List<Integer> children = null;
	
	private int parent;
	
	private List<String> footnoteRefs = null;

	public void setIndex(int index) {
		nConnections = new ArrayList<>();
		pConnections = new ArrayList<>();
		labels = new ArrayList<>();
		children = new ArrayList<>();
		footnoteRefs = new ArrayList<String>();
		parent = -1;
		this.index = index;
	}
	
	public int getIndex() {
		return index;
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
	
	public void addLabel(String label) {
		labels.add(label);
	}
	
	public List<String> getLabels(){
		return labels;
	}

	public void setConent(String content) {
		this.content = content;
	}
	
	public String getConent() {
		return this.content;
	}

	public void addPrevIndex(List<Integer> pIndex) {
		this.pConnections = pIndex;
	}

	public void addNextIndex(List<Integer> nIndex) {
		this.nConnections.addAll(nIndex);
	}
	
	public void addPrevIndex(int pIndex) {
		this.pConnections.add(pIndex);
	}

	public void addNextIndex(int nIndex) {
		this.nConnections.add(nIndex);
	}
	
	public void addChild(int cIndex) {
		this.children.add(cIndex);
	}

	public void setParent(int parentIndex) {
		this.parent = parentIndex;
	}
	
	public List<Integer> getPConnections(){
		return pConnections;
	}
	
	public List<Integer> getNConnections(){
		return nConnections;
	}
	
	public List<Integer> getChildren(){
		return children;
	}
	
	public int getParent() {
		return this.parent;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public String getType() {
		return this.type;
	}
	
	public void addFootnoteRefs(List<String> footnoteRefs) {
		this.footnoteRefs.addAll(footnoteRefs);
	}
	
	public List<String> getFootnoteRefs(){
		return footnoteRefs;
	}
}
