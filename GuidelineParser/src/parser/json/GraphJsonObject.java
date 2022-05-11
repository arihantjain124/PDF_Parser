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

	public void setIndex(int index) {
		nConnections = new ArrayList<>();
		pConnections = new ArrayList<>();
		labels = new ArrayList<>();
		this.index = index;
	}
	
	public void setPageKey(String key) {
		this.pageKey = key;
	}
	
	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}
	
	public void addLabel(String label) {
		labels.add(label);
	}

	public void setConent(String content) {
		this.content = content;
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

	public void setType(String type) {
		this.type = type;
	}
}
