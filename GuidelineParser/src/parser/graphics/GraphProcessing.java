package parser.graphics;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;

import parser.text.WordWithBounds;

public class GraphProcessing {
	private ArrayList<GraphObject> graphLine = new ArrayList<GraphObject>();
	
	public void checkIntersection(ArrayList<GeneralPath> lines,ArrayList<GeneralPath> triangles) {

		Iterator<GeneralPath> lineIterator = lines.iterator();
		int numObject = 0;
		boolean flag = false;
        while (lineIterator.hasNext()) {
        	
			GeneralPath currLine = lineIterator.next();
			Iterator<GeneralPath> triangleIterator = triangles.iterator();
    		
			while (triangleIterator.hasNext()) {
				GeneralPath currTriangle = triangleIterator.next();
				
				if (currLine.intersects(currTriangle.getBounds2D())){
					if (flag == false) {
					graphLine.add(new GraphObject(currLine));
					numObject+=1;
					flag = true;
					}
					Point2D targetPoints=currTriangle.getCurrentPoint();
//					graphLine.get(numObject-1).addTarget(targetPoints);
				}
				
			}
			flag = false;
		}
		
		
	}
	

	
	public void checkIntersectionToTriangles(ArrayList<GeneralPath> lines,ArrayList<GeneralPath> triangles) {

		Iterator<GeneralPath> lineIterator = lines.iterator();
		//Iterating over all lines in the page
        while (lineIterator.hasNext()) {
        	
			boolean flag;
			
			//Current line from the iteration
			GeneralPath currLine = lineIterator.next();
			ArrayList<Point2D> lineCoor = new ArrayList<Point2D>();
			//List to hold current line co-ordinates and list to hold target indices
			ArrayList<Integer> targets = new ArrayList<Integer>();
			Point2D source = null;
			
			int currIndex;
			int currTarget = 0;
			
			float[] coords = new float[6];
			PathIterator i = currLine.getPathIterator(null);
			while (i.isDone() == false) {
				i.currentSegment(coords);
				lineCoor.add(new Point2D.Float(coords[0],coords[1]));
				i.next();
			}
			
			Iterator<GeneralPath> triangleIterator = triangles.iterator();
			//Iterating over all triangles in the page
			
			while (triangleIterator.hasNext()) {
				
				flag = false;
				GeneralPath currTriangle = triangleIterator.next();

				//if current line in iteration intersects with current triangle in iteration 
				//we will be looking for closest point of line to that triangle.
				if (currLine.intersects(currTriangle.getBounds2D())){
					
					flag = true;
					double distance = 0;
					double smallestDistance = Double.MAX_VALUE;
					
					//iterating over each co-ordinate of the current line
					currIndex = 0;
					for(Point2D currCoor : lineCoor) {
						
						distance = (currTriangle.getCurrentPoint().distance(currCoor));
						if (distance < smallestDistance) {
							currTarget = currIndex;
							smallestDistance = distance;
						}
						currIndex+=1;
					}
				}
				if (flag) {
					targets.add(currTarget);
				}
			}
			
			if (!(targets.size() > 2)) {
				currIndex = 0;
				for(Point2D currCoor : lineCoor) {
					if (!targets.contains(currIndex)) {
						source = lineCoor.get(currIndex);
					}
					currIndex+=1;
				}
				for(Integer To: targets) {
					graphLine.add(new GraphObject(source,lineCoor.get(To)));
				}
			}
        }
	}
	

	public ArrayList<GraphObject> getGraphObject(){
		return graphLine;
	}
}
