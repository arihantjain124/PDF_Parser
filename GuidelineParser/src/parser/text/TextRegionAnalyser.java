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
			
			double minDistFromSource = Double.MAX_VALUE, minDistFromTarget = Double.MAX_VALUE, minDistFromTarget2nd = Double.MAX_VALUE;
			int minDistFromSourceIndex = -1, minDistFromTargetIndex = -1, minDistFromTargetIndex2nd = -1;
			
			for(int regionIndex = 0; regionIndex < regions.size(); regionIndex++) {
				
				RegionWithBound region = regions.get(regionIndex);
				
				double distFromSource = distanceBoxPoint(source, region.getBound());
				if(distFromSource < minDistFromSource) {
					minDistFromSource =  distFromSource;
					minDistFromSourceIndex = regionIndex;
				}
					
				double distFromTarget = distanceBoxPoint(target, region.getBound());
				if(distFromTarget < minDistFromTarget) {
					
					minDistFromTarget2nd =  minDistFromTarget;//First min becoms second min
					minDistFromTargetIndex2nd = minDistFromTargetIndex;
					
					minDistFromTarget =  distFromTarget;//Update first min
					minDistFromTargetIndex = regionIndex;
				}else if(distFromTarget < minDistFromTarget2nd) {
					minDistFromTarget2nd =  distFromTarget;
					minDistFromTargetIndex2nd = regionIndex;
				}
			}
			
			if(minDistFromSourceIndex >= 0 && minDistFromTargetIndex >= 0) {
				
				if(minDistFromSourceIndex == minDistFromTargetIndex) {
					//System.out.println("Same region as source and target for page " + p);
					minDistFromTargetIndex = minDistFromTargetIndex2nd;
				}
				
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
	
	public static void generateChildRegions(List<RegionWithBound> regionList, int indexOffset) {
		
		int curRegionCount = regionList.size();
		for(int i = 0; i < curRegionCount; i++) {
			
			RegionWithBound parentRegion = regionList.get(i);
			List<WordWithBounds> contentLines = parentRegion.getContentLines();					
			int numLines = contentLines.size();
			
			boolean hasBulletedLines = false;
			List<WordWithBounds> curChildList = new ArrayList<WordWithBounds>();
			
			for(int l = 0; l < numLines; l++) {
				WordWithBounds curLine = contentLines.get(l);
				
				if(curLine.getText().startsWith("\u2022")) {
					hasBulletedLines = true;
					
					if(curChildList.size() > 0) {
						createChildRegion(curChildList, parentRegion, i, regionList, indexOffset);
						curChildList.clear();
					}
					
				}
				
				curChildList.add(curLine);
			}
			
			if(hasBulletedLines) {
				if(curChildList.size() > 0) {// Create a new child node using the remaining lines					
					createChildRegion(curChildList, parentRegion, i, regionList, indexOffset);
					curChildList.clear();
				}
				parentRegion.resetContentLine();
			}
			
		}
		
	}
	
	private static void createChildRegion(List<WordWithBounds> contentList, RegionWithBound parentRegion, int parentIndex, 
			List<RegionWithBound> mainRegionList, int indexOffset) {
		
		RegionWithBound newRegion = new RegionWithBound(contentList, parentIndex + indexOffset);
		newRegion.setPageKey(parentRegion.getPageKey());
		newRegion.setPageNo(parentRegion.getPageNo());
		int newRegionIndex = mainRegionList.size();
		mainRegionList.add(newRegion);
		parentRegion.addChildRegion(newRegionIndex + indexOffset);
	}
}
