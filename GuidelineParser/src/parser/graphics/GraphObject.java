package parser.graphics;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

public class GraphObject
{
	private GeneralPath path=new GeneralPath();
	private Point2D source;
	private Point2D target;
	
	public GraphObject(Point2D source, Point2D target) {
		this.source = source;
		this.target = target;
		this.path.moveTo(source.getX(), source.getY());
		this.path.lineTo(target.getX(), target.getY());
	}

	public GraphObject(GeneralPath currLine) {
		this.path=currLine;
	}

	public Point2D getTarget() {
		return target;
	}

	public Point2D getSource() {
		return source;
	}
	
	public GeneralPath getpath()
    {
        return path;
    }
}
