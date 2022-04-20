package parser.json;

import java.util.ArrayList;
import java.util.List;

public class GraphJsonObject {
	
	private int index;
	
	private String content;
	
	private List<Integer> pConnections = null;
	
	private List<Integer> nConnections = null;
	
	private String type;

	
	public void setIndex(int index) {
		nConnections = new ArrayList<>();
		pConnections = new ArrayList<>();
	      this.index = index;
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
