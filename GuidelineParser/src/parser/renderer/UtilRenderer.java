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
        
        int widthPx = (int) Math.max(Math.floor(widthPt * scale), 1);
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
        	
        	if(graphObject.getParent() > 0) 
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
        	
        	StringBuilder interPagePConnections = new StringBuilder();
        	for (int prevRegionIndex : graphObject.getPConnections()) {
        		
        		if(graphObjMap.containsKey(prevRegionIndex)) {
	        		
	        		double pX = graphObjMap.get(prevRegionIndex).getBound().getCenterX();
	            	double pY = graphObjMap.get(prevRegionIndex).getBound().getCenterY() - 5;
	            	
	            	g2d.setColor(java.awt.Color.BLUE);
	            	g2d.drawLine((int)x, (int)y, (int)pX, (int)pY);
        		}else {
        			interPagePConnections.append(prevRegionIndex);
        			interPagePConnections.append(";");
        		}
        	}
        	
        	if(interPagePConnections.length() > 0) {
        		g2d.drawString(" P:"+interPagePConnections.toString(), idLocX + 20, idLocY);
        	}
        	
        	StringBuilder interPageNConnections = new StringBuilder();
        	for (int nextRegionIndex : graphObject.getNConnections()) {
        		
        		if(graphObjMap.containsKey(nextRegionIndex)) {
	        		double pX = graphObjMap.get(nextRegionIndex).getBound().getCenterX();
	            	double pY = graphObjMap.get(nextRegionIndex).getBound().getCenterY() + 5;
	            	
	            	g2d.setColor(java.awt.Color.GREEN);
	            	g2d.drawLine((int)x, (int)y, (int)pX, (int)pY);
        		}else {
        			interPageNConnections.append(nextRegionIndex);
        			interPageNConnections.append(";");
        		}
        	}
        	
        	if(interPageNConnections.length() > 0) {
        		g2d.drawString(" N:"+interPageNConnections.toString(), idLocX + 20, idLocY);
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
