package parser.text;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

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
	
	private static int getOverlappingRegion(ArrayList<Rectangle2D> boundList, Rectangle2D bound) {
		int regionIndex = -1;
		
		for(int i = 0; i < boundList.size(); i++) {
			
			Rectangle2D curBound = boundList.get(i);
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

	public static List<Rectangle2D> getRegions(List<WordWithBounds> linesWithBounds){
		
		ArrayList<Rectangle2D> regionBoundList = new ArrayList<Rectangle2D>();
		
		for(WordWithBounds line: linesWithBounds) {
			
			Rectangle2D curLineBound = line.getbound();
			
			int overlappingRegionIndex = getOverlappingRegion(regionBoundList, curLineBound); //Check if line is overlapping with any existing regions
			if(overlappingRegionIndex >= 0) {
				
				Rectangle2D oldRegionBound = regionBoundList.get(overlappingRegionIndex);
				Rectangle2D newRegionBound = (Rectangle2D.Float) oldRegionBound.createUnion(curLineBound);
				
				regionBoundList.set(overlappingRegionIndex, newRegionBound);
				
			}else {
				//Create a new region
				regionBoundList.add(curLineBound);
			}
		}
		
		return regionBoundList;
	}
}
