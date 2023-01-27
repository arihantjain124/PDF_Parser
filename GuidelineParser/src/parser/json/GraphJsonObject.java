package parser.json;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GraphJsonObject {
	
	private int index;
	
	private String pageKey;

	private int pageNo;
	
	private String content;

	private List<Integer> pConnections = null;
	private List<String> pConnectionsPageKey = null; //Must be in sync with pConnections 

	private List<Integer> nConnections = null;
	private List<String> nConnectionsPageKey = null; //Must be in sync with nConnections

	private List<Integer> labels = null;
	
	private Set<String> stageScore = null;
	
	private Set<String> tScore = null;
	
	private Set<String> mScore = null;
	
	private Set<String> nScore = null;

	private String type;
	
	private List<Integer> children = null;
	
	private int parent;
	
	private Rectangle2D bound = null;
	
	private List<String> footnoteRefs = null;

	public void setIndex(int index) {
		nConnections = new ArrayList<>();
		pConnections = new ArrayList<>();
		pConnectionsPageKey = new ArrayList<>();
		nConnectionsPageKey = new ArrayList<>();
		labels = new ArrayList<>();
		stageScore = new HashSet<> ();
		tScore = new HashSet<> ();
		mScore = new HashSet<> ();
		nScore = new HashSet<> ();
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
	
	public void addLabel(Integer label) {
		labels.add(label);
	}
	
	public List<Integer> getLabels(){
		return labels;
	}

	public void setConent(String content) {
		this.content = content;
	}
	
	public String getConent() {
		return this.content;
	}

	public void addPrevIndex(List<Integer> pIndex, String pageKey) {
		this.pConnections = pIndex;
		this.pConnectionsPageKey.addAll(Collections.nCopies(pIndex.size(), pageKey));
	}

	public void addNextIndex(List<Integer> nIndex, String pageKey) {
		this.nConnections.addAll(nIndex);
		this.nConnectionsPageKey.addAll(Collections.nCopies(nIndex.size(), pageKey));
	}
	
	public void addPrevIndex(int pIndex, String pageKey) {
		this.pConnections.add(pIndex);
		this.pConnectionsPageKey.add(pageKey);
	}

	public void addNextIndex(int nIndex, String pageKey) {
		this.nConnections.add(nIndex);
		this.nConnectionsPageKey.add(pageKey);
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

	public List<String> getPConnectionsPageKey(){
		return pConnectionsPageKey;
	}
	
	public List<String> getNConnectionsPageKey(){
		return nConnectionsPageKey;
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
	
	public void setTScore(String tScore) {
		this.tScore.add(tScore);
	}
	
	public void setMScore(String mScore) {
		this.mScore.add(mScore);
	}
	
	public void setNScore(String nScore) {
		this.nScore.add(nScore);
	}
	
	public void setStageScore(String stageScore) {
		this.stageScore.add(stageScore);
	}
	
	public Set<String> getStageScore() {
		return stageScore;
	}
	
	public Set<String> getTScore() {
		return tScore;
	}

	public Set<String> getMScore() {
		return mScore;
	}
	
	public Set<String> getNScore() {
		return nScore;
	}
	public void addFootnoteRefs(List<String> footnoteRefs) {
		this.footnoteRefs.addAll(footnoteRefs);
	}
	
	public List<String> getFootnoteRefs(){
		return footnoteRefs;
	}

	public Rectangle2D getBound() {
		return bound;
	}

	public void setBound(Rectangle2D bound) {
		this.bound = bound;
	}
}
