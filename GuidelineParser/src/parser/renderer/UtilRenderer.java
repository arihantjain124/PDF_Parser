package parser.renderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import parser.graphics.GraphObject;
import parser.json.GraphJsonObject;
import parser.text.RegionWithBound;
import parser.text.WordWithBounds;


public class UtilRenderer {
	
	private BufferedImage image;

	private Graphics2D g2d;
	private float defaultDPI = 72;
	private int dpi;
	
	private float scale;
	
	public UtilRenderer() {
		this.dpi = 72;	
	}
	
	public UtilRenderer(int dpi) {
		this.dpi = dpi;	
	}

	public void intializeImage(float widthPt, float heightPt) {
		this.scale = dpi/defaultDPI;
        
        int widthPx = (int) (Math.max(Math.floor(widthPt * scale), 1) + 200);
        int heightPx = (int) Math.max(Math.floor(heightPt * scale), 1);
        
        this.image = new BufferedImage(widthPx, heightPx, BufferedImage.TYPE_INT_RGB);
        this.g2d = image.createGraphics();
        g2d.setBackground(Color.WHITE);
        g2d.clearRect(0, 0, image.getWidth(), image.getHeight());
        g2d.setColor(Color.BLACK);
	}
	
	public void outputImage(int p, boolean debug) {
        
		String outputPrefix = "jsonexport/image_" + p;
        String imageFormat = "jpg";
        float quality = (float) 0.7;
        boolean success = true;
        String fileName = outputPrefix + "." + imageFormat;
        
        //Write out the image to disk 
        try {
			success &= ImageIOUtil.writeImage(image, fileName, this.dpi, quality);
			if (debug == true) {
	            System.out.println(fileName);
	            System.out.println(success);
	        	
	        }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void drawGraphObject(GraphObject graphLine, Color color) {
		g2d.setColor(color);
		g2d.draw(graphLine.getpath());
	}
	
	public void drawArrayListGraphObject(ArrayList<GraphObject> graphLine, Color color) {
		g2d.setColor(color);
		Iterator<GraphObject> i = graphLine.iterator();
		while (i.hasNext()) {
			g2d.draw(i.next().getpath());
		}
	}
	
	public void drawListGraphObject(List<GraphObject> graphLine, Color color) {
		g2d.setColor(color);
		Iterator<GraphObject> i = graphLine.iterator();
		while (i.hasNext()) {
			g2d.draw(i.next().getpath());
		}
	}

	public void drawWordBounds(List<WordWithBounds> wordbounds, Color color) {
        g2d.setColor(color);
		int numberOfStrings = wordbounds.size();
        for (int i = 0; i < numberOfStrings; i++)
        {
        	g2d.draw (wordbounds.get(i).getbound());
        }
    }
	
	public void drawRegionBounds(List<RegionWithBound> regions , Color color) {
        g2d.setColor(color);
        for (RegionWithBound region : regions)
        {
        	g2d.draw (region.getBound());
        }
    }
	
	public void drawRect(Rectangle2D rect, Color color) {
        g2d.setColor(color);
        g2d.draw(rect);
    }
	
	public void drawGraphJSONObjects(List<GraphJsonObject> graphObjects, int pageNo) throws IOException 
	{
		HashMap<Integer, GraphJsonObject> graphObjMap = new HashMap<Integer, GraphJsonObject>();
		
		for (GraphJsonObject graphObject : graphObjects)
        {
			if(graphObject.getPageNo() == pageNo)
			{
				graphObjMap.put(graphObject.getIndex(), graphObject);
			}
        }
        
    	g2d.setStroke(new BasicStroke(2));
        for (GraphJsonObject graphObject : graphObjMap.values())
        {
    		double x = graphObject.getBound().getCenterX();
        	double y = graphObject.getBound().getCenterY();
        	
        	int idLocX = (int)(graphObject.getBound().getMinX() + 10);
        	int idLocY = (int)(graphObject.getBound().getMaxY() + 11);
        	
        	if(graphObject.getParent() >= 0) 
        	{
        		Rectangle2D.Double bound = new Rectangle2D.Double(graphObject.getBound().getX() + 2, graphObject.getBound().getY() + 2,
        				graphObject.getBound().getWidth() - 4, graphObject.getBound().getHeight() - 4); 
        		
        		g2d.setColor(java.awt.Color.MAGENTA);
            	g2d.draw (bound);
        		g2d.drawString(Integer.toString(graphObject.getIndex()), (int)bound.getCenterX(), (int)bound.getCenterY() + 5);
        	}
        	else
        	{	
        		g2d.setColor(java.awt.Color.RED);
            	g2d.draw (graphObject.getBound());
        		g2d.drawString(Integer.toString(graphObject.getIndex()), idLocX, idLocY);
        	}
        	
        	drawConnections(graphObject, graphObjMap, x, y, idLocX, idLocY, true); //Draw previous connections
        	drawConnections(graphObject, graphObjMap, x, y, idLocX, idLocY, false); //Draw next connections
        }
    }
	
	private void drawConnections(GraphJsonObject graphObject, HashMap<Integer, GraphJsonObject> graphObjMap, double centreX, double centreY,
			int locX, int locY, boolean isPreviousConnection)
	{
		List<Integer> connectionIndices = null;
		List<String> connectionPageKeys = null;
		String connectionPrefix = null;
		java.awt.Color lineColor = null;
		if(isPreviousConnection)
		{
			connectionIndices = graphObject.getPConnections();
			connectionPageKeys = graphObject.getPConnectionsPageKey();
			connectionPrefix = " P:";
			lineColor = java.awt.Color.BLUE;
		}
		else
		{
			connectionIndices = graphObject.getNConnections();
			connectionPageKeys = graphObject.getNConnectionsPageKey();
			connectionPrefix = " N:";
			lineColor = java.awt.Color.GREEN;
		}
		
    	ArrayList<Integer> crossPageConnectionIndices = new ArrayList<Integer>();
    	
    	for (int i = 0; i < connectionIndices.size(); i++) {
    		
    		int connectedRegionIndex = connectionIndices.get(i);
    		if(connectionPageKeys.get(i).equalsIgnoreCase(graphObject.getPageKey())) {
        		
        		double pX = graphObjMap.get(connectedRegionIndex).getBound().getCenterX();
            	double pY = graphObjMap.get(connectedRegionIndex).getBound().getCenterY();
            	           	
            	g2d.setColor(lineColor);
            	if(isPreviousConnection) {
            		g2d.drawLine((int)centreX, (int)centreY, (int)pX, (int)pY - 5);
            	}else {
            		g2d.drawLine((int)centreX, (int)centreY, (int)pX, (int)pY + 5);
            	}
    		}else {
    			crossPageConnectionIndices.add(i);
    		}
    	}
    	
    	StringBuilder crossPageConnectionsStr = new StringBuilder();
    	String curPageKey = null;
    	for(int i : crossPageConnectionIndices) 
    	{
    		if(crossPageConnectionsStr.length() == 0) 
    		{
    			curPageKey = connectionPageKeys.get(i);
    			crossPageConnectionsStr.append(curPageKey + "/" + connectionIndices.get(i));
    		}
    		else 
    		{
    			if(connectionPageKeys.get(i).equalsIgnoreCase(curPageKey))
    			{
	    			crossPageConnectionsStr.append("," + connectionIndices.get(i));
    			}
    			else
    			{
    				curPageKey = connectionPageKeys.get(i);
	    			crossPageConnectionsStr.append(";");
	    			crossPageConnectionsStr.append(curPageKey + "/" + connectionIndices.get(i));
    			}
    		}
    	}
    	
    	if(crossPageConnectionsStr.length() > 0) 
    	{
    		if(crossPageConnectionsStr.length() > 100) {
    			if(isPreviousConnection)
    			{
	    			g2d.drawString(connectionPrefix + crossPageConnectionsStr.substring(0, 100), locX, locY + 12);
	    			g2d.drawString(crossPageConnectionsStr.substring(100), locX, locY + 24);
    			}
    			else
    			{
	    			g2d.drawString(connectionPrefix + crossPageConnectionsStr.substring(0, 100), locX + 20, locY );
	    			g2d.drawString(crossPageConnectionsStr.substring(100), locX + 20, locY + 12);    				
    			}
    		}else {
    			if(isPreviousConnection)
    			{
    				g2d.drawString(connectionPrefix + crossPageConnectionsStr.toString(), locX, locY + 12);
    			}
    			else
    			{
    				g2d.drawString(connectionPrefix + crossPageConnectionsStr.toString(), locX + 20, locY);
    			}
    		}
    	}		
	}
	
	public static void drawGraphJSONObjects(List<GraphJsonObject> graphObjects, int startPage, int endPage) throws IOException
	{
		for (int p = startPage; p <= endPage; ++p)
        {
			UtilRenderer renderer = new UtilRenderer();
			renderer.intializeImage(792, 612);
			
			renderer.drawGraphJSONObjects(graphObjects, p);
			
			renderer.outputImage(p, false);
        }
	}
}
