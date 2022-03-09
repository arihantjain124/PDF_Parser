package parser;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import parser.NewPDFTextStripper.Wordwithbounds;


public class NewPDFRenderer extends PDFRenderer {

	public NewPDFRenderer(PDDocument document) {
		super(document);
//		System.out.print(this.document);
		// TODO Auto-generated constructor stub
	}
	public void draw(List<Wordwithbounds> wordbounds) throws IOException {
		int page =20;
        boolean subsampling = false;
        int dpi=72;
        ImageType imageType = null;
        imageType = ImageType.RGB;
        PDFRenderer renderer = new PDFRenderer(document);
        renderer.setSubsamplingAllowed(subsampling);
        String imageFormat = "jpg";
        String outputPrefix = "mark1_page_";
        float quality = (float) 0.7;
        boolean success = true;
//        BufferedImage image = renderer.renderImageWithDPI(page, dpi, imageType);
        BufferedImage image = new BufferedImage(800,600,BufferedImage.TYPE_INT_RGB);;
        Graphics2D g2d = image.createGraphics();
		//Above code is mostly related to image rendering and bounding boxes are generated in writeline function
    	
		int numberOfStrings = wordbounds.size();
        for (int i = 0; i < numberOfStrings; i++)
        {

            g2d.setColor(Color.RED);
            g2d.draw (wordbounds.get(i).bound);
            //Iterate through each wordbound object and draw its corresponding bounding box over the page 
        }
        
        
        String fileName = outputPrefix + (page) + "." + imageFormat;
//        System.out.println(fileName);
        success &= ImageIOUtil.writeImage(image, fileName, dpi, quality);
        //Write out the image to disk 
    }

}
