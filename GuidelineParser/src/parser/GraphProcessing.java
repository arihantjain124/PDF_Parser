package parser;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;

import parser.NewDataTypes.GraphObject;

public class GraphProcessing {
	ArrayList<GraphObject> graphLine = new ArrayList<GraphObject>();
	
	public void checkIntersection(ArrayList<GeneralPath> lines,ArrayList<GeneralPath> triangles) {

		Iterator<GeneralPath> lineIterator = lines.iterator();
		int numObject = 0;
		
        while (lineIterator.hasNext()) {
        	
			GeneralPath currLine = lineIterator.next();
			Iterator<GeneralPath> triangleIterator = triangles.iterator();
    		
			while (triangleIterator.hasNext()) {
				GeneralPath currTriangle = triangleIterator.next();
				
				if (currLine.intersects(currTriangle.getBounds2D())){
					
					graphLine.add(new GraphObject(currLine));
					Point2D targetPoints=currTriangle.getCurrentPoint();
					graphLine.get(numObject).addTarget(targetPoints);
				}
				
			}
			numObject+=1;
		}
		
		
	}
	
	public ArrayList<GraphObject> getGraphObject(){
		return graphLine;
	}

}
