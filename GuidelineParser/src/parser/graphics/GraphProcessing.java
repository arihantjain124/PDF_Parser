package parser.graphics;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Iterator;

import parser.config.ConfigProperty;

public class GraphProcessing {
	private ArrayList<GraphObject> graphLine = new ArrayList<GraphObject>();
	
	public void checkIntersection(ArrayList<GeneralPath> lines,ArrayList<GeneralPath> triangles) {

		Iterator<GeneralPath> lineIterator = lines.iterator();

		boolean flag = false;
        while (lineIterator.hasNext()) {
        	
			GeneralPath currLine = lineIterator.next();
			Iterator<GeneralPath> triangleIterator = triangles.iterator();
    		
			while (triangleIterator.hasNext()) {
				GeneralPath currTriangle = triangleIterator.next();
				
				if (currLine.intersects(currTriangle.getBounds2D())){
					if (flag == false) {
						graphLine.add(new GraphObject(currLine));
						flag = true;
					}
				}
				
			}
			flag = false;
		}
		
	}
	

	
	public void checkIntersectionToTriangles(ArrayList<GeneralPath> lines,ArrayList<GeneralPath> triangles) throws NumberFormatException, IOException {

		Iterator<GeneralPath> lineIterator = lines.iterator();
		//Iterating over all lines in the page
        while (lineIterator.hasNext()) {
			
			//Current line from the iteration
			GeneralPath currLine = lineIterator.next();
			ArrayList<Point2D> lineCoor = new ArrayList<Point2D>();
			//List to hold current line co-ordinates and list to hold target indices
			ArrayList<Integer> targets = new ArrayList<Integer>();
			
			float[] coords = new float[6];
			PathIterator pathIterator = currLine.getPathIterator(null);
			while (pathIterator.isDone() == false) {
				pathIterator.currentSegment(coords);
				lineCoor.add(new Point2D.Float(coords[0],coords[1]));
				pathIterator.next();
			}
			
			Iterator<GeneralPath> triangleIterator = triangles.iterator();
			//Iterating over all triangles in the page
			
			while (triangleIterator.hasNext()) {
				
				GeneralPath currTriangle = triangleIterator.next();
				
				//iterating over each co-ordinate of the current line
				for(int index = 0; index < lineCoor.size(); index++) {
					
					//if the current triangle in iteration contains any point of the current line in iteration
					//that point is a target for that line.
					if (currTriangle.getBounds2D().contains(lineCoor.get(index))){
					
						targets.add(index);
					}
				}
			}
			
			if (targets.size() > 0) {
				
				if(lineCoor.size() == 2 && targets.size() == 1) {
					graphLine.add(new GraphObject(lineCoor.get(1 - targets.get(0)), lineCoor.get(targets.get(0))));
				}
				
				if(lineCoor.size() == 3 && targets.size() == 2) {
					int indexSum = targets.get(0) + targets.get(1);
					graphLine.add(new GraphObject(lineCoor.get(3 - indexSum), lineCoor.get(targets.get(0))));
					graphLine.add(new GraphObject(lineCoor.get(3 - indexSum), lineCoor.get(targets.get(1))));
				}
			}
			double scale = Double.parseDouble(ConfigProperty.getProperty("graph.line.scale"));
			for (GraphObject currGraphLine : graphLine) {
				currGraphLine.extrapolatePath(scale);
			}
        }
	}
	

	public ArrayList<GraphObject> getGraphObject(){
		return graphLine;
	}
}
