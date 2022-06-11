package parser.json;

import java.util.HashSet;
import java.util.Set;

public class StagingJsonObject {
	
	private Set<String> stageScore = null;
	
	private Set<String> tScore = null;
	
	private Set<String> mScore = null;
	
	private Set<String> nScore = null;
	
	private String content;
	
	public void setIndex(int index) {
		stageScore = new HashSet<> ();
		tScore = new HashSet<> ();
		mScore = new HashSet<> ();
		nScore = new HashSet<> ();
	}
	
	public void setConent(String content) {
		this.content = content;
	}
	
	public String getConent() {
		return content;
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
	
	
}
