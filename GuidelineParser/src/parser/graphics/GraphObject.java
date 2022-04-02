package parser.graphics;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class GraphObject
{
	private GeneralPath path=new GeneralPath();
	private ArrayList<Point2D> Targets = new ArrayList<Point2D>();
	
	GraphObject(GeneralPath Path){
		this.path=Path;
	}
	
	public void addTarget(float x,float y) {
		Point2D currPoint = new Point2D.Float(x,y); 
		Targets.add(currPoint);
	}
	
	public void addTarget(Point2D currPoint) {
		Targets.add(currPoint);
	}
	
	public ArrayList<Point2D> getTarget() {
		
		return Targets;
		
	}
	
	public GeneralPath getpath()
    {
        return path;
    }
}
