package parser;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.PageDrawer;
import org.apache.pdfbox.rendering.PageDrawerParameters;
import org.apache.pdfbox.rendering.RenderDestination;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.apache.pdfbox.rendering.PageDrawerParameters;

import parser.NewDataTypes.Wordwithbounds;
import parser.NewPageDrawer;

public class NewPDFRenderer extends PDFRenderer {
	
    BufferedImage image;
    BufferedImage image1;
    Graphics2D g2d;
    float dpi_factor=72;
    int dpi;
    float scale;
    int page;
    ImageType imageType = ImageType.RGB;
    protected RenderDestination defaultDestination;
    
    
	public NewPDFRenderer(PDDocument document,int pageIndex,int dpi) {
		
		super(document);
		
		this.scale=dpi/dpi_factor;
		PDPage page = pageTree.get(pageIndex);
        PDRectangle cropbBox = page.getCropBox();
        float widthPt = cropbBox.getWidth();
        float heightPt = cropbBox.getHeight();
        int widthPx = (int) Math.max(Math.floor(widthPt * scale), 1);
        int heightPx = (int) Math.max(Math.floor(heightPt * scale), 1);
        this.image = new BufferedImage(widthPx, heightPx, BufferedImage.TYPE_INT_RGB);
        this.image1 = new BufferedImage(widthPx, heightPx, BufferedImage.TYPE_INT_RGB);
        this.g2d=image.createGraphics();
        g2d.setBackground(Color.WHITE);
        g2d.clearRect(0, 0, image.getWidth(), image.getHeight());
        this.page = pageIndex;
		this.dpi=dpi;
        g2d.setColor(Color.RED);
		
		
	}
	

	
	public void OutputImage() throws IOException {
		
        String outputPrefix = "mark1_page_";
        String imageFormat = "jpg";
        float quality = (float) 0.7;
        boolean success = true;
        boolean subsampling = false;
        PDFRenderer renderer = new PDFRenderer(document);
        renderer.setSubsamplingAllowed(subsampling);

        String fileName = outputPrefix + (page) + "." + imageFormat;
        //Write out the image to disk 
        success &= ImageIOUtil.writeImage(image, fileName, this.dpi, quality);
        System.out.println(fileName);
        System.out.println(success);
	}
	
	
	
	public void DrawWordBounds(int pageIndex,List<Wordwithbounds> wordbounds) throws IOException {
		int numberOfStrings = wordbounds.size();
        for (int i = 0; i < numberOfStrings; i++)
        {
            g2d.draw (wordbounds.get(i).bound);
        }
    }

	
	
	
	public void rendergeometry(int pageIndex,List<Wordwithbounds> wordbounds) throws IOException {
		
		super.renderImage(pageIndex, scale, imageType, this.getDefaultDestination());
		PDPage page = pageTree.get(pageIndex);
        NewPageDrawer drawer = new NewPageDrawer(parameters);
        Graphics2D temp_g=image1.createGraphics();
        drawer.drawPage(temp_g,g2d, page.getCropBox(),parameters.getRenderingHints()); 
	}
	
}
