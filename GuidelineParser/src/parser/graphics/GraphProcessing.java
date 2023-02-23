package parser.graphics;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.HashMap;
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
	
	public ArrayList<GraphObject> checkIntersectionToTriangles(ArrayList<GeneralPath> lines,ArrayList<GeneralPath> triangles, ArrayList<VerticalGraphObject> fanoutLines) 
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
			
			HashMap<Integer, GeneralPath> matchedTriangles = new HashMap<Integer, GeneralPath>(); 
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
							matchedTriangles.put(index, currTriangle);
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
				
				if(lineCoor.size() == 2 && targets.size() == 2) 
				{	
					//Match the orientation of triangles and with that of line, pick the triangle having the same orientation as line.
					int lineOrientation = -1; //0: Line is Horizontal, 1: Line is Vertical
					if(Math.abs(lineCoor.get(0).getY() - lineCoor.get(1).getY()) <= GraphObject.EPSILON) 
					{
						lineOrientation = 0;
					}
					else if(Math.abs(lineCoor.get(0).getX() - lineCoor.get(1).getX()) <= GraphObject.EPSILON) 
					{
						lineOrientation = 1;
					}
					
					for(int target : targets)
					{
						GeneralPath triangle = matchedTriangles.get(target);
						if(getTriangleOrientation(triangle) == lineOrientation)
						{
							graphLine.add(new GraphObject(lineCoor.get(1 - target), lineCoor.get(target)));
							handledTriangleIntersection = true;
							break;
						}
					}
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
        
        detectFanoutLines(lines, graphLine, fanoutLines);
        extendMultisegmentArrows(lines, graphLine);
        mergeArrows(graphLine);
        return graphLine;
	}
	
	private int getTriangleOrientation(GeneralPath triangle)
	{
		int orientation = -1; //0: horizontal, 1: vertical
		ArrayList<Point2D> lineCoor = new ArrayList<Point2D>();

		float[] coords = new float[6];
		PathIterator pathIterator = triangle.getPathIterator(null);
		while (pathIterator.isDone() == false) {
			pathIterator.currentSegment(coords);
			lineCoor.add(new Point2D.Float(coords[0], coords[1]));
			pathIterator.next();
		}
		
		if(Math.abs(lineCoor.get(0).getY() - lineCoor.get(1).getY()) <= GraphObject.EPSILON || 
				Math.abs(lineCoor.get(0).getY() - lineCoor.get(2).getY()) <= GraphObject.EPSILON ||
				Math.abs(lineCoor.get(1).getY() - lineCoor.get(2).getY()) <= GraphObject.EPSILON)
		{
			orientation = 1; //One of the edged is perfect horizontal
		}
		else if(Math.abs(lineCoor.get(0).getX() - lineCoor.get(1).getX()) <= GraphObject.EPSILON ||
				Math.abs(lineCoor.get(0).getX() - lineCoor.get(2).getX()) <= GraphObject.EPSILON ||
				Math.abs(lineCoor.get(1).getX() - lineCoor.get(2).getX()) <= GraphObject.EPSILON)
		{
			orientation = 0; //One of the edged is perfect vertical
		}
		
		return orientation;
	}
	
	private void detectFanoutLines(ArrayList<GeneralPath> lines, ArrayList<GraphObject> arrowLines, ArrayList<VerticalGraphObject> fanoutLines) {
		
		Iterator<GeneralPath> lineIterator = lines.iterator();
		ArrayList<GeneralPath> linesToBeRemoved = new ArrayList<GeneralPath>();
		
		while (lineIterator.hasNext()) {

			// Current line from the iteration
			GeneralPath currLine = lineIterator.next();
			ArrayList<Point2D> lineCoor = new ArrayList<Point2D>();

			float[] coords = new float[6];
			PathIterator pathIterator = currLine.getPathIterator(null);
			while (pathIterator.isDone() == false) {
				pathIterator.currentSegment(coords);
				lineCoor.add(new Point2D.Float(coords[0], coords[1]));
				pathIterator.next();
			}

			int numFanout = 0, numEndFanout = 0;
			if (lineCoor.size() == 2 && Math.abs(lineCoor.get(0).getX() - lineCoor.get(1).getX()) <= GraphObject.EPSILON) 
			{
				//current line is a vertical line
				double maxY = lineCoor.get(0).getY() > lineCoor.get(1).getY() ? lineCoor.get(0).getY() : lineCoor.get(1).getY();
				double minY = lineCoor.get(0).getY() < lineCoor.get(1).getY() ? lineCoor.get(0).getY() : lineCoor.get(1).getY();
				
				minY -= 1; maxY += 1; //expand the range
				
				for (GraphObject currArrowLine : arrowLines) {
					
					if(Math.abs(currArrowLine.getSource().getY() - currArrowLine.getTarget().getY()) > GraphObject.EPSILON){
						continue;
					}
					
					//Current arrow is horizontal arrow
					if(currArrowLine.getSource().getY() < minY || currArrowLine.getSource().getY() > maxY){
						continue;
					}
					
					if(Math.abs(lineCoor.get(0).getX() - currArrowLine.getSource().getX()) <= 1.0 ||
							Math.abs(lineCoor.get(1).getX() - currArrowLine.getSource().getX()) <= 1.0)
					{
						numFanout++;
						if(Math.abs(lineCoor.get(0).getY() - currArrowLine.getSource().getY()) <= 1.0 ||
								Math.abs(lineCoor.get(1).getY() - currArrowLine.getSource().getY()) <= 1.0)
						{
							numEndFanout++;
						}
					}
				}
				
				if(numEndFanout == 2 && numFanout >= 3)
				{
					//This is a fan-out line
					fanoutLines.add(new VerticalGraphObject(lineCoor.get(0), lineCoor.get(1)));
					linesToBeRemoved.add(currLine);
				}
			}//end of if (lineCoor.size() == 2 && Math.abs(lineCoor.get(0).getX() - lineCoor.get(1).getX()) <= GraphObject.EPSILON)
		}//end of while
		
		lines.removeAll(linesToBeRemoved);
		
		//Fan-out lines are vertical lines, fanning out in right side. Convert non-arrow lines terminating to fan-out line as arrow lines
		for(VerticalGraphObject fanoutLine : fanoutLines)
		{
			double fanoutMaxY = fanoutLine.getSource().getY() > fanoutLine.getTarget().getY() ? fanoutLine.getSource().getY() : fanoutLine.getTarget().getY();
			double fanoutMinY = fanoutLine.getSource().getY() < fanoutLine.getTarget().getY() ? fanoutLine.getSource().getY() : fanoutLine.getTarget().getY();
			fanoutMinY -= 1; fanoutMaxY += 1; //expand the range
			
			lineIterator = lines.iterator();
			linesToBeRemoved = new ArrayList<GeneralPath>();
			
			while (lineIterator.hasNext()) {

				// Current line from the iteration
				GeneralPath currLine = lineIterator.next();
				ArrayList<Point2D> lineCoor = new ArrayList<Point2D>();

				float[] coords = new float[6];
				PathIterator pathIterator = currLine.getPathIterator(null);
				while (pathIterator.isDone() == false) {
					pathIterator.currentSegment(coords);
					lineCoor.add(new Point2D.Float(coords[0], coords[1]));
					pathIterator.next();
				}
				
				if (lineCoor.size() == 2 && (Math.abs(lineCoor.get(0).getY() - lineCoor.get(1).getY()) <= GraphObject.EPSILON) //current line is a horizontal line
						&& (lineCoor.get(0).getY() >= fanoutMinY && lineCoor.get(0).getY() <= fanoutMaxY)) //current is within Y bound of fan-out line
				{
					int rightIndex = lineCoor.get(0).getX() > lineCoor.get(1).getX() ? 0 : 1;
					double rightX = lineCoor.get(rightIndex).getX();
					
					if(Math.abs(fanoutLine.getSource().getX() - rightX) <= 1.0)
					{
						linesToBeRemoved.add(currLine);
						arrowLines.add(new GraphObject(lineCoor.get(1 - rightIndex), lineCoor.get(rightIndex)));
					}					
				}
			}//end of while
		}//end of for
		
		lines.removeAll(linesToBeRemoved);
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
	
	private void mergeArrows(ArrayList<GraphObject> arrowLines) {

		ArrayList<GraphObject> toBeDeleted = new ArrayList<GraphObject>();
		
		for (int j = 0; j < arrowLines.size(); j++) {
			
			GraphObject currArrowLine = arrowLines.get(j);
			
			if(toBeDeleted.contains(currArrowLine)) {
				continue;
			}
			
			Point2D arrowSource = currArrowLine.getSource();

			Point2D newArrowSource = null;
			do {
				int i = 0;
				newArrowSource = null;
				
				for (; i < arrowLines.size() && (i != j); i++) {
					
					GraphObject otherArrowLine = arrowLines.get(i);
					
					if(Math.abs(arrowSource.getX() - otherArrowLine.getTarget().getX()) <= 4 &&
							Math.abs(arrowSource.getY() - otherArrowLine.getTarget().getY()) <= 4) {
						newArrowSource = otherArrowLine.getSource();
					}
										
					if(newArrowSource != null) {
						break;
					}
				}
				
				if(newArrowSource != null) {
					arrowSource = newArrowSource;
					toBeDeleted.add(arrowLines.get(i));
				}
			}while(newArrowSource != null);
			
			currArrowLine.setSource(arrowSource);
		}
		
		arrowLines.removeAll(toBeDeleted);
	}
	
	private boolean closeEnough(Point2D p1, Point2D p2) {
		
		if(Math.abs(p1.getX() - p2.getX()) <= 1 &&  Math.abs(p1.getY() - p2.getY()) <= 1) {
			return true;
		}
		
		return false;
	}
}
