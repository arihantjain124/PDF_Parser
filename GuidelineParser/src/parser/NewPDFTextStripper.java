package parser;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripper.WordWithTextPositions;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.apache.pdfbox.text.TextPosition;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

public class NewPDFTextStripper extends PDFTextStripper{
	

	List<Wordwithbounds> wordbounds=new LinkedList<Wordwithbounds>();
	
	public NewPDFTextStripper() throws IOException {
		super();
	}
	

	
	
	public static class Wordwithbounds
    {
        String text;
        List<TextPosition> textPositions;
        Rectangle2D bound = new Rectangle2D.Float();
        float sum;
        
        
        Wordwithbounds(WordWithTextPositions word)
        {
        	double x;
            double y;
            double w;
            double h;
            text = word.getText();
            textPositions = word.getTextPositions();
            int numberofchar = textPositions.size();
            x=textPositions.get(0).getX();
            y=textPositions.get(0).getY();
            w=numberofchar*textPositions.get(0).getIndividualWidths()[0];;
            h=textPositions.get(0).getHeight();
            bound.setRect(x, y-h, w, h);
            System.out.println(text);
            System.out.println(w);
            System.out.println("\n");
        }

        public String getText()
        {
            return text;
        }

        public List<TextPosition> getTextPositions()
        {
            return textPositions;
        }
        public Rectangle2D getbound()
        {
            return bound;
        }
    }
	protected void writeLine(List<WordWithTextPositions> line) throws IOException
    {
		super.writeLine(line);
		
		int numberOfStrings = line.size();
        for (int i = 0; i < numberOfStrings; i++)
        {
            WordWithTextPositions word = line.get(i);
            wordbounds.add(new Wordwithbounds(word));
        }
//        System.out.println(wordbounds.get(0).getTextPositions().get(0).getIndividualWidths()[0]);
//        System.out.println("\n");
    }
	protected void writePage() throws IOException
    {
		super.writePage();
		
		int page =13;
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
        BufferedImage image = renderer.renderImageWithDPI(page, dpi, imageType);
    	Graphics2D g2d = image.createGraphics();
		
		int numberOfStrings = wordbounds.size();
        for (int i = 0; i < numberOfStrings; i++)
        {

            g2d.setColor(Color.RED);

            g2d.draw (wordbounds.get(i).bound);
            System.out.println(wordbounds.get(i).text);
            System.out.println("\n");
        }
        
        String fileName = outputPrefix + (page) + "." + imageFormat;
        System.out.println(fileName);
        success &= ImageIOUtil.writeImage(image, fileName, dpi, quality);
    }
	
}
