package parser.graphics;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import parser.text.RegionWithBound;

public class VerticalGraphObject extends GraphObject {
	
	private ArrayList<RegionWithBound> leftAssociatedRegions = null;
	private ArrayList<RegionWithBound> rightAssociatedRegions = null;
	private ArrayList<RegionWithBound> betweenAssociatedRegions = null; 
	
	public enum RegionType { LEFT, RIGHT, BETWEEN, ALL }

	public VerticalGraphObject(GeneralPath currLine) {
		super(currLine);
		this.leftAssociatedRegions = new ArrayList<RegionWithBound>();
		this.rightAssociatedRegions = new ArrayList<RegionWithBound>();
		this.betweenAssociatedRegions = new ArrayList<RegionWithBound>();
	}
	
	public VerticalGraphObject(Point2D source, Point2D target) {
		super(source, target);
		this.leftAssociatedRegions = new ArrayList<RegionWithBound>();
		this.rightAssociatedRegions = new ArrayList<RegionWithBound>();
		this.betweenAssociatedRegions = new ArrayList<RegionWithBound>();
	}
	
	public void addLeftAssociatedRegion(RegionWithBound region) {
		if(!leftAssociatedRegions.contains(region)) {
			leftAssociatedRegions.add(region);
		}
	}
	
	public void addRightAssociatedRegion(RegionWithBound region) {
		if(!rightAssociatedRegions.contains(region)) {
			rightAssociatedRegions.add(region);
		}
	}
	
	public void addBetweenAssociatedRegion(RegionWithBound region) {
		if(!betweenAssociatedRegions.contains(region)) {
			betweenAssociatedRegions.add(region);
		}
	}
	
	public ArrayList<RegionWithBound> getAssociatedRegions(RegionType type) {
		ArrayList<RegionWithBound> associatedRegions = new ArrayList<RegionWithBound>();
		
		switch (type) {
		case LEFT:
			associatedRegions.addAll(leftAssociatedRegions);
			break;
		case RIGHT:
			associatedRegions.addAll(rightAssociatedRegions);
			break;
		case BETWEEN:
			associatedRegions.addAll(betweenAssociatedRegions);	
			break;
		case ALL:
			associatedRegions.addAll(leftAssociatedRegions);
			associatedRegions.addAll(rightAssociatedRegions);
			associatedRegions.addAll(betweenAssociatedRegions);
			break;
		default:
			break;
		}
		
		return associatedRegions;
	}
}
