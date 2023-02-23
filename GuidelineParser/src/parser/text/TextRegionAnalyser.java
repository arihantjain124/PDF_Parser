package parser.text;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDNamedDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import parser.graphics.GraphObject;
import parser.graphics.GraphProcessing;
import parser.graphics.VerticalGraphObject;
import parser.graphics.VerticalGraphObject.RegionType;
import parser.renderer.UtilRenderer;

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
	
	public static int extractLinks(PDDocument document, int pageNum, String compare) throws IOException {

		PDPage page = document.getPage(pageNum - 1);

		PDFTextStripperByArea stripper = new PDFTextStripperByArea();
		List<PDAnnotation> annotations = page.getAnnotations();
		
		// first setup text extraction regions
		for (int j = 0; j < annotations.size(); j++) {
			PDAnnotation annot = annotations.get(j);
			if (annot instanceof PDAnnotationLink) {
				PDAnnotationLink link = (PDAnnotationLink) annot;
				PDRectangle rect = link.getRectangle();
				// need to reposition link rectangle to match text space
				float x = rect.getLowerLeftX();
				float y = rect.getUpperRightY();
				float width = rect.getWidth();
				float height = rect.getHeight();
				int rotation = page.getRotation();
				if (rotation == 0) {
					PDRectangle pageSize = page.getMediaBox();
					y = pageSize.getHeight() - y;
				} else if (rotation == 90) {
					// do nothing
				}

				Rectangle2D.Float awtRect = new Rectangle2D.Float(x - 10, y, width + 10, height);
				stripper.addRegion("" + j, awtRect);
			}
		}

		stripper.extractRegions(page);

		for (int j = 0; j < annotations.size(); j++) {
			PDAnnotation annot = annotations.get(j);
			
			if (annot instanceof PDAnnotationLink) {
				
				PDAnnotationLink link = (PDAnnotationLink) annot;
				PDAction action = link.getAction();
				String urlText = stripper.getTextForRegion("" + j);
				
				if (action instanceof PDActionURI) {
					//PDActionURI uri = (PDActionURI) action; //do nothing
				} else if (action instanceof PDActionGoTo) {
					
					PDActionGoTo gta = (PDActionGoTo) action;
					if (gta.getDestination() instanceof PDPageDestination) {	
						//PDPageDestination pd = (PDPageDestination) gta.getDestination(); //do nothing
					} else if (gta.getDestination() instanceof PDNamedDestination) {
						
						PDPageDestination pd = document.getDocumentCatalog()
								.findNamedDestinationPage((PDNamedDestination) gta.getDestination());
						if (pd != null) {
							if (urlText.trim().contains(compare)) {
								return (pd.retrievePageNumber() + 1);
							}
						}
					}
				}
			}
		}
		return 0;
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
		        
		        if(verticalGapHeight < 0.85*bound.getHeight()) { // 0.85 is a heuristic number
		        	regionIndex = i;
		        	break;
		        }
			}
		}
		
		return regionIndex;
	}

	//Group lines to create regions
	public static List<RegionWithBound> getRegions(List<WordWithBounds> linesWithBounds, ArrayList<GeneralPath> graphLines, ArrayList<VerticalGraphObject> fanoutLines,
			String pageKey, int p){
		
		ArrayList<RegionWithBound> regionBoundList = new ArrayList<RegionWithBound>();
		
		for(WordWithBounds line: linesWithBounds) {
			
			Rectangle2D curLineBound = line.getbound();
			if(line.getText().trim().isEmpty()) {
				//System.out.println("Ignore Empty Lines.");
				continue;
			}
			
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
		
		for(VerticalGraphObject fanoutLine : fanoutLines) 
		{
			Rectangle2D verticalLineBound = fanoutLine.getpath().getBounds2D();
			Rectangle2D newRegionBound = new Rectangle2D.Double(verticalLineBound.getX() - 1, verticalLineBound.getY(), 
					verticalLineBound.getWidth() + 1, verticalLineBound.getHeight());
			RegionWithBound newRegion = new RegionWithBound(newRegionBound);
			newRegion.setPageKey(pageKey);
			newRegion.setPageNo(p);
			newRegion.setIsImaginary(true);
			regionBoundList.add(newRegion);
		}
		
		analyzeRegionsUsingVerticalLines(regionBoundList, graphLines, pageKey, p); 
		return regionBoundList;
	}
	
	private static void analyzeRegionsUsingVerticalLines(ArrayList<RegionWithBound> regions, ArrayList<GeneralPath> graphLines, 
			String pageKey, int p) {
		
		GraphProcessing graphProc = new GraphProcessing();
		ArrayList<VerticalGraphObject> verticalLines = graphProc.getVerticalLines(graphLines);
		
		UtilRenderer renderer = new UtilRenderer();
		renderer.intializeImage(792, 612);
		
		ArrayList<RegionWithBound> newRegionsToBeAdded = new ArrayList<RegionWithBound>();
		ArrayList<RegionWithBound> oldRegionsToBeDeleted = new ArrayList<RegionWithBound>();
		
		//Step 1: Use vertical lines to break regions if required.
		for(int regionIndex = 0; regionIndex < regions.size(); regionIndex++) {
			
			RegionWithBound region = regions.get(regionIndex);
			Rectangle2D regionBound = new Rectangle2D.Double(region.getBound().getX(), region.getBound().getY(), 
					region.getBound().getWidth() - 2, region.getBound().getHeight());
			
			ArrayList<VerticalGraphObject> leftLines = new ArrayList<VerticalGraphObject>();
			ArrayList<VerticalGraphObject> rightLines = new ArrayList<VerticalGraphObject>();
			ArrayList<VerticalGraphObject> betweenLines = new ArrayList<VerticalGraphObject>();
			
			for(int lineIndex = 0; lineIndex < verticalLines.size(); lineIndex++) {
				
				VerticalGraphObject verticalLine = verticalLines.get(lineIndex);
				
				if(isOverlapInY(regionBound, verticalLine.getpath().getBounds2D())) {
					
					if((regionBound.getMinX() >= verticalLine.getSource().getX()) 
							&& (regionBound.getMinX() - verticalLine.getSource().getX()) <= 10) {//10 is a heuristic number
						
						leftLines.add(verticalLine);
					}else if((verticalLine.getSource().getX() >= regionBound.getMaxX()) 
							&& (verticalLine.getSource().getX() - regionBound.getMaxX()) <= 10) {
						
						rightLines.add(verticalLine);
					}else if((verticalLine.getSource().getX() >= regionBound.getMinX()) 
							&& (verticalLine.getSource().getX() <= regionBound.getMaxX())) {
						
						betweenLines.add(verticalLine);
					}
				}
			}
			
			ArrayList<RegionWithBound> newRegions = null;
			if((leftLines.size() == 1) && (rightLines.size() == 1)) {
				
				//Use smaller line to break.
				if(leftLines.get(0).getpath().getBounds2D().getHeight() < rightLines.get(0).getpath().getBounds2D().getHeight()) {
					newRegions = breakRegionUsingVerticalLines(region, leftLines);
				}else {
					newRegions = breakRegionUsingVerticalLines(region, rightLines);
				}
			
			}else if((leftLines.size() == 1) && (betweenLines.size() == 1)) {
				
				//Use smaller line to break.
				if(leftLines.get(0).getpath().getBounds2D().getHeight() < betweenLines.get(0).getpath().getBounds2D().getHeight()) {
					newRegions = breakRegionUsingVerticalLines(region, leftLines);
				}else {
					newRegions = breakRegionUsingVerticalLines(region, betweenLines);
				}
				
			}else if(leftLines.size() > rightLines.size()) {
				//renderer.drawRect(regionBound, java.awt.Color.RED);
				
				newRegions = breakRegionUsingVerticalLines(region, leftLines);
				
			}else if(rightLines.size() > leftLines.size()) {
				//renderer.drawRect(regionBound, java.awt.Color.BLUE);
				newRegions = breakRegionUsingVerticalLines(region, rightLines);
			}
			
			if(newRegions != null && newRegions.size() > 1) {
				newRegionsToBeAdded.addAll(newRegions);
				oldRegionsToBeDeleted.add(region);
			}
		}
		
		regions.removeAll(oldRegionsToBeDeleted);
		regions.addAll(newRegionsToBeAdded);
		
		//Step 2: Get associated regions of vertical lines. 
		for(int regionIndex = 0; regionIndex < regions.size(); regionIndex++) {
			
			RegionWithBound region = regions.get(regionIndex);
			Rectangle2D regionBound = new Rectangle2D.Double(region.getBound().getX(), region.getBound().getY(), 
					region.getBound().getWidth() - 2, region.getBound().getHeight());
			
			for(int lineIndex = 0; lineIndex < verticalLines.size(); lineIndex++) {
				
				VerticalGraphObject verticalLine = verticalLines.get(lineIndex);
				
				if(isOverlapInY(regionBound, verticalLine.getpath().getBounds2D())) {
					
					if((regionBound.getMinX() >= verticalLine.getSource().getX()) 
							&& (regionBound.getMinX() - verticalLine.getSource().getX()) <= 10) //10 is a heuristic number
					{
						//Associate the region with the current vertical line. Line is left to the region
						verticalLine.addLeftAssociatedRegion(region);
					}
					else if((verticalLine.getSource().getX() >= regionBound.getMaxX()) 
							&& (verticalLine.getSource().getX() - regionBound.getMaxX()) <= 25) //25 is a heuristic number. Right line tends to be far away than left line
					{	
						//Associate the region with the current vertical line. Line is right to the region
						verticalLine.addRightAssociatedRegion(region);
					}
					else if((verticalLine.getSource().getX() >= regionBound.getMinX()) 
							&& (verticalLine.getSource().getX() <= regionBound.getMaxX())) 
					{
						//Associate the region with the current vertical line. Line is in between the region
						verticalLine.addBetweenAssociatedRegion(region);
					}
				}
			}
		}
		
		//Step 3: Create imaginary nodes.
		for(VerticalGraphObject verticalLine : verticalLines) {
			
			double lineHeight = verticalLine.getpath().getBounds2D().getHeight();
			
			ArrayList<RegionWithBound> associatedRegions = verticalLine.getAssociatedRegions(RegionType.ALL);
			boolean isCoveredByRegion = false;
			for(RegionWithBound region : associatedRegions) {
				double regionHeight = region.getBound().getHeight();
				if(Math.abs(lineHeight - regionHeight) <= 0.2*lineHeight) {//line is almost covered by a single region
					isCoveredByRegion = true;
					break;
				}
			}
			
			if(!isCoveredByRegion) {
				renderer.drawGraphObject(verticalLine, java.awt.Color.RED);
				
				Rectangle2D verticalLineBound = verticalLine.getpath().getBounds2D();
				Rectangle2D newRegionBound = new Rectangle2D.Double(verticalLineBound.getX() - 1, verticalLineBound.getY(), 
						verticalLineBound.getWidth() + 1, verticalLineBound.getHeight());
				RegionWithBound newRegion = new RegionWithBound(newRegionBound);
				newRegion.setPageKey(pageKey);
				newRegion.setPageNo(p);
				newRegion.setIsImaginary(true);
				int newRegionIndex = regions.size();
				regions.add(newRegion);
				
				ArrayList<RegionWithBound> leftAssociatedRegions = verticalLine.getAssociatedRegions(RegionType.LEFT);
				for(RegionWithBound associatedRegion : leftAssociatedRegions) 
				{			
					int associatedRegionIndex = regions.indexOf(associatedRegion);
					newRegion.addNextRegion(associatedRegionIndex);//Line is left to the region
					associatedRegion.addPrevRegion(newRegionIndex);
				}
				
				ArrayList<RegionWithBound> rightAssociatedRegions = verticalLine.getAssociatedRegions(RegionType.RIGHT);
				for(RegionWithBound associatedRegion : rightAssociatedRegions) 
				{					
					int associatedRegionIndex = regions.indexOf(associatedRegion);
					newRegion.addPrevRegion(associatedRegionIndex);//Line is right to the region
					associatedRegion.addNextRegion(newRegionIndex);
				}
			}			
		}
		//renderer.drawRegionBounds(regions, java.awt.Color.RED);	
		//renderer.outputImage(p, true);
	}
	
	private static ArrayList<RegionWithBound> breakRegionUsingVerticalLines(RegionWithBound region, ArrayList<VerticalGraphObject> verticalLines) {
		
		ArrayList<RegionWithBound> newRegions = new ArrayList<RegionWithBound>();
		
		ArrayList<WordWithBounds> allContentLines = new ArrayList<WordWithBounds>();
		allContentLines.addAll(region.getContentLines());
		
		for(VerticalGraphObject verticalLine : verticalLines) {
			
			Rectangle2D verticalLineBound = verticalLine.getpath().getBounds2D();
			
			ArrayList<WordWithBounds> curContentLines = new ArrayList<WordWithBounds>();
			for(WordWithBounds contentLine : allContentLines) {
				if(isOverlapInY(contentLine.getbound(), verticalLineBound)) {
					curContentLines.add(contentLine);
				}
			}
			
			if(!curContentLines.isEmpty()) {
				RegionWithBound newRegion = new RegionWithBound(curContentLines, -1);
				newRegions.add(newRegion);
				
				allContentLines.removeAll(curContentLines);
			}
		}
		
		if(!allContentLines.isEmpty()) {
			RegionWithBound newRegion = new RegionWithBound(allContentLines, -1);
			newRegions.add(newRegion);
		}
		
		return newRegions;
	}
	
	private static boolean isOverlapInY(Rectangle2D bound1, Rectangle2D bound2) {
        double bounds1Ystart = bound1.getY();
        double bounds1Yend = bounds1Ystart + bound1.getHeight();
        
        double bounds2Ystart = bound2.getY();
        double bounds2Yend = bounds2Ystart + bound2.getHeight();
        
        if (bounds2Yend < bounds1Ystart || bounds2Ystart > bounds1Yend) {
        	//No overlap in Y
        	return false;
        }else {
        	
        	Rectangle2D intersectionRect = bound1.createIntersection(bound2);
        	double minHeight = Math.min(bound1.getHeight(), bound2.getHeight());
        	
        	if(intersectionRect.getHeight() >= 0.1*minHeight) { //At least 10% overlap
        		return true;
        	}else {
        		return false;
        	}
        }
	}
	
	public static void generateTextRegionAssociation(List<GraphObject> arrows, List<RegionWithBound> regions) {
		
		for(GraphObject arrow : arrows) {
			Point2D source = arrow.getSource();
			Point2D target = arrow.getTarget();
			
			double minDistFromSource = Double.MAX_VALUE, minDistFromTarget = Double.MAX_VALUE;
			int minDistFromSourceIndex = -1, minDistFromTargetIndex = -1;
			
			double minDistFromSource2nd = Double.MAX_VALUE, minDistFromTarget2nd = Double.MAX_VALUE;
			int minDistFromSourceIndex2nd = -1, minDistFromTargetIndex2nd = -1;
			
			for(int regionIndex = 0; regionIndex < regions.size(); regionIndex++) {
				
				RegionWithBound region = regions.get(regionIndex);
				
				double distFromSource = distanceBoxPoint(source, region.getBound());
				if(distFromSource < minDistFromSource) {
					
					minDistFromSource2nd = minDistFromSource;
					minDistFromSourceIndex2nd = minDistFromSourceIndex;
					
					minDistFromSource =  distFromSource;
					minDistFromSourceIndex = regionIndex;
				}else if(distFromSource < minDistFromSource2nd) {
					minDistFromSource2nd = distFromSource;
					minDistFromSourceIndex2nd = regionIndex;
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
				
				if(Math.abs(minDistFromSource - minDistFromSource2nd) <= 1 && regions.get(minDistFromSourceIndex2nd).isImaginary()) {
					//Give preference to imaginary in this case as both are close to source and imaginary didn't exist in PDF
					minDistFromSourceIndex = minDistFromSourceIndex2nd;
				}
				
				if(Math.abs(minDistFromTarget - minDistFromTarget2nd) <= 1 && regions.get(minDistFromTargetIndex2nd).isImaginary()) {
					//Give preference to imaginary in this case as both are close to target and imaginary didn't exist in PDF
					minDistFromTargetIndex = minDistFromTargetIndex2nd;
				}
				
				boolean isHorizontal = Math.abs(source.getY() - target.getY()) <= GraphObject.EPSILON;
				if(isHorizontal ) {
					
					Rectangle2D firstNearestRegionBound = regions.get(minDistFromSourceIndex).getBound();
					Rectangle2D secondNearestRegionBound = regions.get(minDistFromSourceIndex2nd).getBound();
					
					if((firstNearestRegionBound.getMinY() > source.getY() ||  firstNearestRegionBound.getMaxY() < source.getY()) && 
						(secondNearestRegionBound.getMinY() <= source.getY() &&  secondNearestRegionBound.getMaxY() >= source.getY())) {
						
						//Horizontal arrow intersecting second nearest region, but not first nearest region.
						minDistFromSourceIndex = minDistFromSourceIndex2nd;
					}
				}
				
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
	
	public static void generateChildRegions(List<RegionWithBound> regionList) {
		
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
						createChildRegion(curChildList, parentRegion, parentRegion.getIndex(), regionList);
						curChildList.clear();
					}
					
				}
				
				curChildList.add(curLine);
			}
			
			if(hasBulletedLines) {
				if(curChildList.size() > 0) {// Create a new child node using the remaining lines					
					createChildRegion(curChildList, parentRegion, parentRegion.getIndex(), regionList);
					curChildList.clear();
				}
				parentRegion.resetContentLine();
			}
			
		}
		
	}
	
	private static void createChildRegion(List<WordWithBounds> contentList, RegionWithBound parentRegion, int parentIndex, 
			List<RegionWithBound> mainRegionList) {
		
		RegionWithBound newRegion = new RegionWithBound(contentList, parentIndex);
		newRegion.setPageKey(parentRegion.getPageKey());
		newRegion.setPageNo(parentRegion.getPageNo());
		int newRegionIndex = mainRegionList.size();
		newRegion.setIndex(newRegionIndex);
		mainRegionList.add(newRegion);
		parentRegion.addChildRegion(newRegionIndex);
	}
}
