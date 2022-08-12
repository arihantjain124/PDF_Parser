package parser.renderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import parser.config.ConfigProperty;
import parser.graphics.GraphObject;
import parser.graphics.VerticalGraphObject;
import parser.text.RegionWithBound;
import parser.text.WordWithBounds;


public class GuidelinePageRenderer extends PDFRenderer {
	
	private BufferedImage image;

	private Graphics2D g2d;
	private float defaultDPI = 72;
	private int dpi;
	
	private float scale;
	private int page;
	private ImageType imageType = ImageType.RGB;
    
    private ArrayList<GeneralPath> lines = new ArrayList<GeneralPath>();
    private ArrayList<GeneralPath> triangles = new ArrayList<GeneralPath>();
	
	
	public GuidelinePageRenderer(PDDocument document,int pageIndex,int dpi) {
		super(document);
		this.page = pageIndex;
		this.dpi = dpi;	
	}

	public void intializeImage() {
		this.scale = dpi/defaultDPI;
		PDPage page = pageTree.get(this.page);
        PDRectangle cropbBox = page.getCropBox();
        float widthPt = cropbBox.getWidth();
        float heightPt = cropbBox.getHeight();
        
        int widthPx = (int) Math.max(Math.floor(widthPt * scale), 1);
        int heightPx = (int) Math.max(Math.floor(heightPt * scale), 1);
        
        this.image = new BufferedImage(widthPx, heightPx, BufferedImage.TYPE_INT_RGB);
        this.g2d = image.createGraphics();
        g2d.setBackground(Color.WHITE);
        g2d.clearRect(0, 0, image.getWidth(), image.getHeight());
        g2d.setColor(Color.BLACK);
//        PDRectangle pageSize=page.getCropBox();
//        g2d.translate(0, pageSize.getHeight());
//        g2d.scale(1, -1);
//        g2d.translate(-pageSize.getLowerLeftX(), -pageSize.getLowerLeftY());
	}
	
	public void OutputImage(boolean debug) throws IOException {
        
		String outputPrefix = "mark1_page_";
        String imageFormat = "jpg";
        float quality = (float) 0.7;
        boolean success = true;
        String fileName = outputPrefix + (page + 1) + "." + imageFormat;
        
        //Write out the image to disk 
        success &= ImageIOUtil.writeImage(image, fileName, this.dpi, quality);
        if (debug == true) {
            System.out.println(fileName);
            System.out.println(success);
        	
        }
	}
		
	
	
	public void getGeometry() throws IOException {
		
		super.renderImage(this.page, scale, imageType, this.getDefaultDestination());
		PDPage page = pageTree.get(this.page);

        GuidelinePageDrawer drawer = new GuidelinePageDrawer(parameters);
        int widthPx = (int) Math.max(Math.floor(page.getCropBox().getWidth() * scale), 1);
        int heightPx = (int) Math.max(Math.floor(page.getCropBox().getHeight() * scale), 1);
        
        BufferedImage tempImage = new BufferedImage(widthPx, heightPx, BufferedImage.TYPE_INT_RGB);
        Graphics2D tempGraphics = tempImage.createGraphics();
        drawer.drawPage(tempGraphics, page.getCropBox(),parameters.getRenderingHints()); 
        
        triangles = drawer.getTriangles();
        lines = drawer.getLines();
        
	}
	
	public ArrayList<GeneralPath> getLines(){
		return lines;
	}
	
	public ArrayList<GeneralPath> getTriangles(){
		return triangles;
	}
	
	public void drawLines(Color color) {
        g2d.setColor(color);
		Iterator<GeneralPath> i = lines.iterator();
		while (i.hasNext()) {
			g2d.draw(i.next());
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
	
	public void drawArrayListVerticalGraphObject(ArrayList<VerticalGraphObject> graphLine, Color color) {
		g2d.setColor(color);
		Iterator<VerticalGraphObject> i = graphLine.iterator();
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
	
	public void drawTriangles(Color color) throws IOException  {
        g2d.setColor(color);
		Iterator<GeneralPath> i = triangles.iterator();
		while (i.hasNext()) 
			g2d.draw(i.next());
		
		}
	
	

	public void drawWordBounds(List<WordWithBounds> wordbounds, Color color) throws IOException {
        g2d.setColor(color);
		int numberOfStrings = wordbounds.size();
        for (int i = 0; i < numberOfStrings; i++)
        {
        	g2d.draw (wordbounds.get(i).getbound());
        }
    }
	
	public void drawRegionBounds(List<RegionWithBound> regions , Color color) throws IOException {
        g2d.setColor(color);
        for (RegionWithBound region : regions)
        {
        	g2d.draw (region.getBound());
        }
    }
	
	public void drawRegionBoundsWithRelations(List<RegionWithBound> regions , Color color) throws IOException {
        
        for (RegionWithBound region : regions)
        {
        	g2d.setColor(color);
        	g2d.draw (region.getBound());
        	
        	double x = region.getBound().getCenterX();
        	double y = region.getBound().getCenterY();
        	
        	for (int prevRegionIndex : region.getPrevRegions()) {
        		
        		double pX = regions.get(prevRegionIndex).getBound().getCenterX();
            	double pY = regions.get(prevRegionIndex).getBound().getCenterY() - 5;
            	
            	g2d.setColor(java.awt.Color.BLUE);
            	g2d.drawLine((int)x, (int)y, (int)pX, (int)pY);
        	}
        	
        	for (int nextRegionIndex : region.getNextRegions()) {
        		
        		double pX = regions.get(nextRegionIndex).getBound().getCenterX();
            	double pY = regions.get(nextRegionIndex).getBound().getCenterY() + 5;
            	
            	g2d.setColor(java.awt.Color.GREEN);
            	g2d.drawLine((int)x, (int)y, (int)pX, (int)pY);
        	}
        }
    }
	
	public void drawRegionOfInterest(Color color) throws IOException {
        g2d.setColor(color);
		String[] regionOfInterest = ConfigProperty.getProperty("page.main-content.region").split("[,]");
        Rectangle mainContentRect = new Rectangle(Integer.valueOf(regionOfInterest[0]),Integer.valueOf(regionOfInterest[1]),Integer.valueOf(regionOfInterest[2]),Integer.valueOf(regionOfInterest[3]));
        g2d.draw (mainContentRect);
	}
	
	public void drawHeadingRegion(Color color) throws IOException {
        g2d.setColor(color);
		String[] regionOfInterest = ConfigProperty.getProperty("page.heading.region").split("[,]");
        Rectangle mainContentRect = new Rectangle(Integer.valueOf(regionOfInterest[0]),Integer.valueOf(regionOfInterest[1]),Integer.valueOf(regionOfInterest[2]),Integer.valueOf(regionOfInterest[3]));
        g2d.draw (mainContentRect);
	}
}
