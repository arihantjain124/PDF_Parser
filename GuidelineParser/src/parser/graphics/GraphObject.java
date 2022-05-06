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
		
		tX = target.getX();
		tY = target.getY();
		sX = source.getX();
		sY = source.getY();
		
		this.path.moveTo(source.getX(), source.getY());
		this.path.lineTo(target.getX(), target.getY());
		
	}
	private double extrapolate(double x)
	{
		double slope = (tY - sY)/(tX - sX);
		double y = sY + (slope * (x - sX));
	    return y;
	}
	
	
	public void extrapolatePath(double factor) {
		
		double currentX = source.getX() * (1-(factor/100));
		source.setLocation(currentX,extrapolate(currentX));
		currentX = target.getX() * (1+(factor/100));
		target.setLocation(currentX,extrapolate(currentX));
		intializePath();
		
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
