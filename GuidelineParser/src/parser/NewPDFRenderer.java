package parser;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.RenderDestination;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import parser.NewDataTypes.Wordwithbounds;

public class NewPDFRenderer extends PDFRenderer {
	
    BufferedImage image;
    BufferedImage temp_image;
    Graphics2D g2d;
    float dpi_factor = 72;
    int dpi;
    float scale;
    int page;
    ImageType imageType = ImageType.RGB;
    protected RenderDestination defaultDestination;
    PDGraphicsState state = null;
	
    
	ArrayList<GeneralPath> lines = new ArrayList<GeneralPath>();
	ArrayList<GeneralPath> triangles = new ArrayList<GeneralPath>();
	
	
	public NewPDFRenderer(PDDocument document,int pageIndex,int dpi) {
		super(document);
		this.page = pageIndex;
		this.dpi = dpi;	
	}

	public void intializeImage() {
		this.scale = dpi/dpi_factor;
		PDPage page = pageTree.get(this.page);
        PDRectangle cropbBox = page.getCropBox();
        float widthPt = cropbBox.getWidth();
        float heightPt = cropbBox.getHeight();
        
        int widthPx = (int) Math.max(Math.floor(widthPt * scale), 1);
        int heightPx = (int) Math.max(Math.floor(heightPt * scale), 1);
        
        this.image = new BufferedImage(widthPx, heightPx, BufferedImage.TYPE_INT_RGB);
        this.temp_image = new BufferedImage(widthPx, heightPx, BufferedImage.TYPE_INT_RGB);
        
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
        String fileName = outputPrefix + (page) + "." + imageFormat;
        //Write out the image to disk 
        success &= ImageIOUtil.writeImage(image, fileName, this.dpi, quality);
        System.out.println(fileName);
        System.out.println(success);
	}
		
	
	
	public void getGeometry() throws IOException {
		super.renderImage(this.page, scale, imageType, this.getDefaultDestination());
		PDPage page = pageTree.get(this.page);
        NewPageDrawer drawer = new NewPageDrawer(parameters);
        Graphics2D temp_g = temp_image.createGraphics();
        drawer.drawPage(temp_g, page.getCropBox(),parameters.getRenderingHints()); 
        state = drawer.getGraphicsState();
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
			g2d.setComposite(state.getStrokingJavaComposite());
			g2d.draw(i.next());
		}
	}
	
	public void drawTriangles() {
        g2d.setColor(Color.BLACK);
		Iterator<GeneralPath> i = triangles.iterator();
		while (i.hasNext()) {
			g2d.setComposite(state.getStrokingJavaComposite());
			g2d.draw(i.next());
		}
	}
	public void drawWordBounds(List<Wordwithbounds> wordbounds) throws IOException {
        g2d.setColor(Color.RED);
		int numberOfStrings = wordbounds.size();
        for (int i = 0; i < numberOfStrings; i++)
        {
        	g2d.draw (wordbounds.get(i).bound);
        }
    }
}
