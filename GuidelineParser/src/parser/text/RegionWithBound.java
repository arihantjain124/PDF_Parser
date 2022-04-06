package parser.text;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class RegionWithBound {

	private Rectangle2D bound;
	
	private List<WordWithBounds> contentLines = new ArrayList<WordWithBounds>();
	
	public RegionWithBound(Rectangle2D bound, WordWithBounds line) {
		this.bound = bound;
		this.contentLines.add(line);
	}
	
	public Rectangle2D getBound() {
		return bound;
	}
	
	public void setBound(Rectangle2D newBound) {
		bound = newBound;
	}
	
	public void addContentLine(WordWithBounds line) {
		contentLines.add(line);
	}
}
