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

public class NewPDFTextStripper extends PDFTextStripper{
	

	List<Wordwithbounds> wordbounds=new LinkedList<Wordwithbounds>();
	
	public NewPDFTextStripper() throws IOException {
		super();
	}
	

	public void postiontobound(String text, List<TextPosition> positions) {

        
        
        int numberofchar = positions.size();
        float x=0;
        float fontsize=0;
        float threshold=(float) 5;
        int j=0;
        
        for (int i = 0; i < numberofchar; i++)
        {
        	if (i==0) {
                x=positions.get(0).getX();
                j=0;
                fontsize=positions.get(0).getFontSize();
                threshold=(float) (fontsize*1.2);
        	}
        	else if (positions.get(i).getX()-x >fontsize+threshold) {
        		System.out.println(text.substring(j, i));
        		System.out.println(positions.subList(j, i));
        		
            	wordbounds.add(new Wordwithbounds(text.substring(j, i), positions.subList(j, i)));
            	j=i;
            }
        	else if (i==numberofchar-1) {
        		wordbounds.add(new Wordwithbounds(text.substring(j, i), positions.subList(j, i)));
        	}
            x=positions.get(i).getX();
        }
        
	}
	
	public static class Wordwithbounds
    {
        String text;
        List<TextPosition> textPositions;
        Rectangle2D bound = new Rectangle2D.Float();
        float sum;
        
        
        Wordwithbounds(String text, List<TextPosition> positions)
        {
        	double x;
            double y;
            double w;
            double h;
            this.text = text;
            this.textPositions = positions;
            int numberofchar = textPositions.size();
            x=textPositions.get(0).getX();
            y=textPositions.get(0).getY();
            w=numberofchar*textPositions.get(0).getWidth();
            h=textPositions.get(0).getHeight();
            bound.setRect(x, y-h, w, h);
//            System.out.println(text);
//            System.out.println(w);
//            System.out.println("\n");
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
            //wordbounds.add(new Wordwithbounds(word.getText(),word.getTextPositions()));
            postiontobound(word.getText(),word.getTextPositions());
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
//            System.out.println(wordbounds.get(i).text);
//            System.out.println("\n");
        }
        
        String fileName = outputPrefix + (page) + "." + imageFormat;
        System.out.println(fileName);
        success &= ImageIOUtil.writeImage(image, fileName, dpi, quality);
    }
	
}
