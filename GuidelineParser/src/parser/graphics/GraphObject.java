package parser.graphics;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

public class GraphObject
{
	private static final float EPSILON = 0.000001f;
	
	private GeneralPath path = new GeneralPath();
	private Point2D source;
	private Point2D target;

	public GraphObject(Point2D source, Point2D target) {
		this.source = source;
		this.target = target;
		intializePath();
	}

	public GraphObject(GeneralPath currLine) {
		this.path=currLine;
	}

	private void intializePath() {
		path = new GeneralPath();
		path.moveTo(source.getX(), source.getY());
		path.lineTo(target.getX(), target.getY());
	}
	
	private double extrapolateY(double x) {
		
		double tX = target.getX();
		double tY = target.getY();
		double sX = source.getX();
		double sY = source.getY();
		
		double slope = (tY - sY)/(tX - sX);
		double y = sY + (slope * (x - sX));
	    return y;
	}
	
	private double extrapolateX(double y) {

		double tX = target.getX();
		double tY = target.getY();
		double sX = source.getX();
		double sY = source.getY();

		double slope_inverse = (tX - sX)/(tY - sY);
		double x = sX + (slope_inverse * (y - sY));
	    return x;
	}

	public void extrapolateTarget(double extraLength) {
		
		boolean isHorizontal = Math.abs(source.getY() - target.getY()) <= EPSILON;
		boolean isVertical = Math.abs(source.getX() - target.getX()) <= EPSILON;
		
		boolean isLeftToRight = (source.getX() < target.getX());
		boolean isTopToBottom = (source.getY() < target.getY());
		
		boolean isLengthGreater = Math.abs(source.getX() - target.getX()) > Math.abs(source.getY() - target.getY());
		
		if(isHorizontal) {
			
			double newTargetX = target.getX();
			if(isLeftToRight) {
				newTargetX += extraLength;
			}else {
				newTargetX -= extraLength;
			}
			
			target.setLocation(newTargetX, target.getY());
			
		}else if(isVertical) {
			
			double newTargetY = target.getY();
			if(isTopToBottom) {
				newTargetY += extraLength;
			}else {
				newTargetY -= extraLength;
			}
			
			target.setLocation(target.getX(), newTargetY);
			
		}else {
			
			double newTargetX = target.getX();
			double newTargetY = target.getY();
			
			if(isLengthGreater) {
				
				if(isLeftToRight) {
					newTargetX += extraLength;
				}else {
					newTargetX -= extraLength;
				}
				newTargetY = extrapolateY(newTargetX);
				
			}else {
				
				if(isTopToBottom) {
					newTargetY += extraLength;
				}else {
					newTargetY -= extraLength;
				}				
				newTargetX = extrapolateX(newTargetY);			
			}
			
			target.setLocation(newTargetX, newTargetY);	
		}
		
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
