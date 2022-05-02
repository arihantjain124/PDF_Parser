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
	
	public void OutputImage() throws IOException {
        
		String outputPrefix = "mark1_page_";
        String imageFormat = "jpg";
        float quality = (float) 0.7;
        boolean success = true;
        String fileName = outputPrefix + (page + 1) + "." + imageFormat;
        
        //Write out the image to disk 
        success &= ImageIOUtil.writeImage(image, fileName, this.dpi, quality);
        System.out.println(fileName);
        System.out.println(success);
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
	
	public void drawLines() {
        g2d.setColor(Color.BLACK);
		Iterator<GeneralPath> i = lines.iterator();
		while (i.hasNext()) {
			g2d.draw(i.next());
		}
	}
	
	public void drawGraphObject(ArrayList<GraphObject> graphLine) {
		g2d.setColor(Color.RED);
		Iterator<GraphObject> i = graphLine.iterator();
		
		while (i.hasNext()) {
			g2d.draw(i.next().getpath());
		}
	}
	
	public void drawTriangles() throws IOException  {
        g2d.setColor(Color.BLACK);
		Iterator<GeneralPath> i = triangles.iterator();
		while (i.hasNext()) 
			g2d.draw(i.next());
		
		}
	
	

	public void drawWordBounds(List<WordWithBounds> wordbounds) throws IOException {
        g2d.setColor(Color.RED);
		int numberOfStrings = wordbounds.size();
        for (int i = 0; i < numberOfStrings; i++)
        {
        	g2d.draw (wordbounds.get(i).getbound());
        }
    }
	
	public void drawRegionBounds(List<RegionWithBound> regions) throws IOException {
        g2d.setColor(Color.RED);
        for (RegionWithBound region : regions)
        {
        	g2d.draw (region.getBound());
        }
    }
	public void drawRegionOfInterest() throws IOException {
		String[] regionOfInterest = ConfigProperty.getProperty("page.main-content.region").split("[,]");
        Rectangle mainContentRect = new Rectangle(Integer.valueOf(regionOfInterest[0]),Integer.valueOf(regionOfInterest[1]),Integer.valueOf(regionOfInterest[2]),Integer.valueOf(regionOfInterest[3]));
        g2d.draw (mainContentRect);
	}
}
