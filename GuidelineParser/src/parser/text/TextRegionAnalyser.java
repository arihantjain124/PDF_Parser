package parser.text;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import parser.graphics.GraphObject;

public class TextRegionAnalyser {
	
	private static boolean isOverlapsInX(Rectangle2D bounds1, Rectangle2D bounds2)
	{
		double bounds1Xstart = bounds1.getX();
        double bounds1Xend = bounds1Xstart + bounds1.getWidth();

        double bounds2Xstart = bounds2.getX();
        double bounds2Xend = bounds2Xstart + bounds2.getWidth();

        // No horizontal overlap
        if (bounds2Xend < bounds1Xstart || bounds2Xstart > bounds1Xend)
        {
            return false;
        }
        
		return true;
	}
	
	private static int getOverlappingRegion(ArrayList<RegionWithBound> boundList, Rectangle2D bound) {
		int regionIndex = -1;
		
		for(int i = 0; i < boundList.size(); i++) {
			
			Rectangle2D curBound = boundList.get(i).getBound();
			if(isOverlapsInX(curBound, bound)) {
				
				//Check vertical gaps
		        double bounds1Ystart = curBound.getY();
		        double bounds1Yend = bounds1Ystart + curBound.getHeight();
		        
		        double bounds2Ystart = bound.getY();
		        double bounds2Yend = bounds2Ystart + bound.getHeight();
		        
		        double verticalGapHeight;
		        if (bounds2Yend < bounds1Ystart || bounds2Ystart > bounds1Yend) {
		        	//No overlap in Y
		        	if(bounds2Yend < bounds1Ystart) {
		        		verticalGapHeight = bounds1Ystart - bounds2Yend;
		        	}else {
		        		verticalGapHeight = bounds2Ystart - bounds1Yend;
		        	}
		        }else {
		        	verticalGapHeight = 0;
		        }
		        
		        if(verticalGapHeight < bound.getHeight()) {
		        	regionIndex = i;
		        	break;
		        }
			}
		}
		
		return regionIndex;
	}

	public static List<RegionWithBound> getRegions(List<WordWithBounds> linesWithBounds){
		
		ArrayList<RegionWithBound> regionBoundList = new ArrayList<RegionWithBound>();
		
		for(WordWithBounds line: linesWithBounds) {
			
			Rectangle2D curLineBound = line.getbound();
			
			int overlappingRegionIndex = getOverlappingRegion(regionBoundList, curLineBound); //Check if line is overlapping with any existing regions
			if(overlappingRegionIndex >= 0) {
				
				RegionWithBound region = regionBoundList.get(overlappingRegionIndex);
				Rectangle2D oldRegionBound = region.getBound();
				Rectangle2D newRegionBound = (Rectangle2D.Float) oldRegionBound.createUnion(curLineBound);
				
				region.setBound(newRegionBound);
				region.addContentLine(line);
				//regionBoundList.set(overlappingRegionIndex, newRegionBound);
				
			}else {
				//Create a new region
				regionBoundList.add(new RegionWithBound(curLineBound, line));
			}
		}
		
		return regionBoundList;
	}
	
	public static void generateTextRegionAssociation(List<GraphObject> arrows, List<RegionWithBound> regions) {
		
		for(GraphObject arrow : arrows) {
			Point2D source = arrow.getSource();
			Point2D target = arrow.getTarget();
			
			double minDistFromSource = Double.MAX_VALUE, minDistFromTarget = Double.MAX_VALUE;
			int minDistFromSourceIndex = -1, minDistFromTargetIndex = -1;
			
			for(int regionIndex = 0; regionIndex < regions.size(); regionIndex++) {
				
				RegionWithBound region = regions.get(regionIndex);
				
				double distFromSource = distanceBoxPoint(source, region.getBound());
				if(distFromSource < minDistFromSource) {
					minDistFromSource =  distFromSource;
					minDistFromSourceIndex = regionIndex;
				}
					
				double distFromTarget = distanceBoxPoint(target, region.getBound());
				if(distFromTarget < minDistFromTarget) {
					minDistFromTarget =  distFromTarget;
					minDistFromTargetIndex = regionIndex;
				}
			}
			
			if(minDistFromSourceIndex >= 0 && minDistFromTargetIndex >= 0) {
				regions.get(minDistFromSourceIndex).addNextRegion(minDistFromTargetIndex);
				regions.get(minDistFromTargetIndex).addPrevRegion(minDistFromSourceIndex);
			}
		}
	}
	
	private static double distanceBoxPoint(Point2D point, Rectangle2D rect) {
			
		if (point.getX() < rect.getMinX()) {
			
			if (point.getY() < rect.getMinY()) {
				return hypot(rect.getMinX() - point.getX(), rect.getMinY() - point.getY());
			}
			
			if (point.getY() <= rect.getMaxY()) {
				return rect.getMinX() - point.getX();
			}
			
			return hypot(rect.getMinX() - point.getX(), rect.getMaxY() - point.getY());
			
		} else if (point.getX() <= rect.getMaxX()) {
			
			if (point.getY() < rect.getMinY()) {
				return rect.getMinY() - point.getY();
			}
			
			if (point.getY() <= rect.getMaxY()) {
				return 0;
			}
			
			return point.getY() - rect.getMaxY();
		} else {
			if (point.getY() < rect.getMinY()) {
				return hypot(rect.getMaxX() - point.getX(), rect.getMinY() - point.getY());
			}
			
			if (point.getY() <= rect.getMaxY()) {
				return point.getX() - rect.getMaxX();
			}
			
			return hypot(rect.getMaxX() - point.getX(), rect.getMaxY() - point.getY());
		}
	}
	
	private static double hypot(double x, double y) {
		return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
	}
}
