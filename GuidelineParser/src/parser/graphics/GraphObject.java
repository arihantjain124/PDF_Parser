package parser.graphics;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

public class GraphObject
{
	private GeneralPath path=new GeneralPath();
	private Point2D source;
	private Point2D target;
	double tX,tY,sX,sY;
	public GraphObject(Point2D source, Point2D target) {
		this.source = source;
		this.target = target;
		intializePath();
	}

	public GraphObject(GeneralPath currLine) {
		this.path=currLine;
	}

	private void intializePath() {
		path=new GeneralPath();
		path.moveTo(source.getX(), source.getY());
		path.lineTo(target.getX(), target.getY());
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
