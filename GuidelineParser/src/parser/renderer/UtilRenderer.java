package parser.renderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import parser.graphics.GraphObject;
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
        
		String outputPrefix = "renderer_image_" + p;
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
}
