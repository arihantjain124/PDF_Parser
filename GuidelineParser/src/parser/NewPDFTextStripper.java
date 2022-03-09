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
import parser.NewPDFRenderer;

public class NewPDFTextStripper extends PDFTextStripper{
	

	List<Wordwithbounds> wordbounds=new LinkedList<Wordwithbounds>();
	
	public NewPDFTextStripper() throws IOException {
		super();
	}
	

	public void postiontobound(String text, List<TextPosition> positions) {

        
        int numberofchar = positions.size();
        float x=0;
        float fontsize=0;
        float threshold=(float) 10;
        int j=0;
        
        for (int i = 0; i < numberofchar; i++)
        {
        	fontsize=positions.get(i).getFontSize();
//        	System.out.format("%f, %s \n",positions.get(i).getX(),text.charAt(i));
        	if (i==0) {
                x=positions.get(0).getX();
                j=0;
                threshold=(float) (10);
        	}
        	else if (positions.get(i).getX()-x >threshold) {
//        		System.out.println(text.substring(j, i));
//        		System.out.println(positions.subList(j, i));
        		
            	wordbounds.add(new Wordwithbounds(text.substring(j, i), positions.subList(j, i)));
            	j=i;
            }
        	else if (i==numberofchar-1) {
//        		System.out.println(text.substring(j, i+1));
//        		System.out.println(positions.subList(j, i+1));
        		wordbounds.add(new Wordwithbounds(text.substring(j, i+1), positions.subList(j, i+1)));
        	}
//        	System.out.format("%f, %s ,%f \n",fontsize,text.charAt(i),positions.get(i).getX()-x);
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
        	double x,y,w,h;
            this.text = text;
            this.textPositions = positions;
            int numberofchar = textPositions.size();
            x = textPositions.get(0).getX();
            y = textPositions.get(0).getY();
            w = textPositions.get(numberofchar-1).getX();
            h=0;
            for (int i = 0; i < numberofchar; i++)
            {
            	if (h<textPositions.get(i).getHeight()) 
            	{
            		h=textPositions.get(i).getHeight();
            	}
            }
            bound.setRect(x, y-h, w-x+5, h);
//          System.out.format("%s , %f, %s ,%f %s %d \n",text,x,text.charAt(0),w,text.charAt(numberofchar-1),numberofchar);
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
	public void writeLine(List<WordWithTextPositions> line) throws IOException
    {
		super.writeLine(line);
		
		int numberOfStrings = line.size();
        for (int i = 0; i < numberOfStrings; i++)
        {
            WordWithTextPositions word = line.get(i);
            postiontobound(word.getText(),word.getTextPositions());
        }

    }
	
	protected void writePage() throws IOException
    {
		super.writePage();
		NewPDFRenderer drawer = new NewPDFRenderer(document);
		//Expending the code of Writepage from PDFtextStripper to render a image to visualily verify bounding boxes
		drawer.draw(wordbounds);
	
}
}