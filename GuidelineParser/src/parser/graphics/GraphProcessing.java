package parser.graphics;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Iterator;

import parser.config.ConfigProperty;


public class GraphProcessing {
	
	public ArrayList<GraphObject> checkIntersection(ArrayList<GeneralPath> lines,ArrayList<GeneralPath> triangles) {

		ArrayList<GraphObject> graphLine = new ArrayList<GraphObject>();
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
		
        return graphLine;
	}
	
	public ArrayList<GraphObject> pairVerticalLine(ArrayList<GraphObject> verticalLines) {
		ArrayList<GraphObject> pairedVerticalLine = new ArrayList<GraphObject>();
		for (GraphObject currLine : verticalLines) {

			ArrayList<Point2D> lineCoor = new ArrayList<Point2D>();
			// List to hold current line co-ordinates

			float[] coords = new float[6];
			PathIterator pathIterator = currLine.getpath().getPathIterator(null);
			while (pathIterator.isDone() == false) {
				pathIterator.currentSegment(coords);
				lineCoor.add(new Point2D.Float(coords[0], coords[1]));
				pathIterator.next();
			}

			for (GraphObject nextLine : verticalLines) {
				ArrayList<Point2D> nextlineCoor = new ArrayList<Point2D>();

				float[] nextlinecoords = new float[6];
				PathIterator nextlinepathIterator = nextLine.getpath().getPathIterator(null);
				while (nextlinepathIterator.isDone() == false) {

					nextlinepathIterator.currentSegment(nextlinecoords);
					nextlineCoor.add(new Point2D.Float(nextlinecoords[0], nextlinecoords[1]));
					nextlinepathIterator.next();

				}
				if ((lineCoor.get(0).getX() == nextlineCoor.get(0).getX())
						&& (lineCoor.get(1).getX() == nextlineCoor.get(1).getX())) {
					continue;
				} else {
					if ((lineCoor.get(0).getY() == nextlineCoor.get(0).getY())
							&& (lineCoor.get(1).getY() == nextlineCoor.get(1).getY())) {
						pairedVerticalLine.add(currLine);
						pairedVerticalLine.add(nextLine);
					}

				}
			}
		}
		return pairedVerticalLine;
	}
	
	
	public ArrayList<GraphObject> getVerticalLinesGraphObject(ArrayList<GraphObject> lines) {
		ArrayList<GraphObject> verticalLine = new ArrayList<GraphObject>();
		for (GraphObject currLine : lines) {
			ArrayList<Point2D> lineCoor = new ArrayList<Point2D>();
			// List to hold current line co-ordinates

			float[] coords = new float[6];
			PathIterator pathIterator = currLine.getpath().getPathIterator(null);
			while (pathIterator.isDone() == false) {
				pathIterator.currentSegment(coords);
				lineCoor.add(new Point2D.Float(coords[0], coords[1]));
				pathIterator.next();
			}
			if (lineCoor.size() == 2 && (lineCoor.get(0).getX() == lineCoor.get(1).getX())) {
				verticalLine.add(currLine);
			}
		}
		return verticalLine;
	}
	
	public ArrayList<VerticalGraphObject> getVerticalLines(ArrayList<GeneralPath> lines) {
		
		ArrayList<VerticalGraphObject> verticalLine = new ArrayList<VerticalGraphObject>();
		Iterator<GeneralPath> lineIterator = lines.iterator();
		while (lineIterator.hasNext()) {

			// Current line from the iteration
			GeneralPath currLine = lineIterator.next();
			ArrayList<Point2D> lineCoor = new ArrayList<Point2D>();
			// List to hold current line co-ordinates

			float[] coords = new float[6];
			PathIterator pathIterator = currLine.getPathIterator(null);
			while (pathIterator.isDone() == false) {
				pathIterator.currentSegment(coords);
				lineCoor.add(new Point2D.Float(coords[0], coords[1]));
				pathIterator.next();
			}

			if (lineCoor.size() == 2 && Math.abs(lineCoor.get(0).getX() - lineCoor.get(1).getX()) <= GraphObject.EPSILON) {
				verticalLine.add(new VerticalGraphObject(lineCoor.get(0), lineCoor.get(1)));
			}
		}
		return verticalLine;
	}
	
	public ArrayList<GraphObject> checkIntersectionToTriangles(ArrayList<GeneralPath> lines,ArrayList<GeneralPath> triangles) 
			throws NumberFormatException, IOException {

		ArrayList<GraphObject> graphLine = new ArrayList<GraphObject>();
		ArrayList<GeneralPath> linesToBeRemoved = new ArrayList<GeneralPath>();
		
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
					
						if(!targets.contains(index)) {
							targets.add(index);
						}
					}
				}
			}
			
			if (targets.size() > 0) {
				
				boolean handledTriangleIntersection = false;
				if(lineCoor.size() == 2 && targets.size() == 1) {
					graphLine.add(new GraphObject(lineCoor.get(1 - targets.get(0)), lineCoor.get(targets.get(0))));
					handledTriangleIntersection = true;
				} 
				
				if(lineCoor.size() == 3 && targets.size() == 2) {
					int indexSum = targets.get(0) + targets.get(1);
					graphLine.add(new GraphObject(lineCoor.get(3 - indexSum), lineCoor.get(targets.get(0))));
					graphLine.add(new GraphObject(lineCoor.get(3 - indexSum), lineCoor.get(targets.get(1))));
					handledTriangleIntersection = true;
				} 
				
				if(!handledTriangleIntersection)
				{
					System.out.println("Unhandled triangle intersection");
				}else {
					linesToBeRemoved.add(currLine);
				}
			}
        }
        
        lines.removeAll(linesToBeRemoved);
        
        double extrapolateLength = Double.parseDouble(ConfigProperty.getProperty("graph.line.traingle.height"));
        for (GraphObject currGraphLine : graphLine) {
			currGraphLine.extrapolateTarget(extrapolateLength);
		}
        
        extendMultisegmentArrows(lines, graphLine);
        return graphLine;
	}
	
	private void extendMultisegmentArrows(ArrayList<GeneralPath> lines, ArrayList<GraphObject> arrowLines) {
		
		ArrayList<ArrayList<GraphObject>> graphObjectForLines = new ArrayList<ArrayList<GraphObject>>();
		for (GeneralPath currLine : lines) {

			float[] coords = new float[6];
			PathIterator pathIterator = currLine.getPathIterator(null);
			Point2D startPoint = null;

			ArrayList<GraphObject> curLineSegments = new ArrayList<GraphObject>();
			while (pathIterator.isDone() == false) 
			{
				if(pathIterator.currentSegment(coords) == PathIterator.SEG_MOVETO)
				{
					startPoint = new Point2D.Float(coords[0], coords[1]);
				}
				else if(pathIterator.currentSegment(coords) == PathIterator.SEG_LINETO)
				{
					Point2D endPoint = new Point2D.Float(coords[0], coords[1]);
					
					curLineSegments.add(new GraphObject(startPoint, endPoint));
					
					startPoint = endPoint;
				}
				pathIterator.next();
			}
			
			graphObjectForLines.add(curLineSegments);
		}
		
		for (GraphObject currArrowLine : arrowLines) {
			
			Point2D arrowSource = currArrowLine.getSource();

			Point2D newArrowSource = null;
			do {
				int i = 0;
				newArrowSource = null;
				
				for (; i < lines.size(); i++) {
					
					ArrayList<GraphObject> lineSegments = graphObjectForLines.get(i);
					
					if(lineSegments.size() == 1) 
					{
						if(closeEnough(arrowSource, lineSegments.get(0).getSource())) {
							newArrowSource = lineSegments.get(0).getTarget();
						}else if(closeEnough(arrowSource, lineSegments.get(0).getTarget())) {
							newArrowSource = lineSegments.get(0).getSource();
						}
					}
					else 
					{
						System.out.println("More than one segment. Need to be handled.");
					}
					
					if(newArrowSource != null) {
						break;
					}
				}
				
				if(newArrowSource != null) {
					arrowSource = newArrowSource;
					lines.remove(i);
					graphObjectForLines.remove(i);
				}
			}while(newArrowSource != null);
			
			currArrowLine.setSource(arrowSource);
		}
	}
	
	private boolean closeEnough(Point2D p1, Point2D p2) {
		
		if(Math.abs(p1.getX() - p2.getX()) <= 1 &&  Math.abs(p1.getY() - p2.getY()) <= 1) {
			return true;
		}
		
		return false;
	}
}
