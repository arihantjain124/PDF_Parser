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
import parser.NewDataTypes.Wordwithbounds;

public class NewPDFTextStripper extends PDFTextStripper{
	

	List<Wordwithbounds> wordbounds=new LinkedList<Wordwithbounds>();
	int pageIndex = 20;
	int dpi=72;
	
	public NewPDFTextStripper() throws IOException {
		super();
	}
	

	public void postiontobound(String text, List<TextPosition> positions) {
        int numberofchar = positions.size();
        float x=0;
        float threshold=(float) 10;
        int j=0;
        
        for (int i = 0; i < numberofchar; i++)
        {
        	if (i==0) {
                x=positions.get(0).getX();
                j=0;
                threshold=(float) (10);
        	}
        	else if (positions.get(i).getX()-x >threshold) {
        		
            	wordbounds.add(new Wordwithbounds(text.substring(j, i), positions.subList(j, i)));
            	j=i;
            }
        	else if (i==numberofchar-1) {
        		wordbounds.add(new Wordwithbounds(text.substring(j, i+1), positions.subList(j, i+1)));
        	}
        	x=positions.get(i).getX();
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
		NewPDFRenderer drawer = new NewPDFRenderer(document,pageIndex,dpi);
		//Expending the code of Writepage from PDFtextStripper to render a image to visualily verify bounding boxes
		drawer.DrawWordBounds(pageIndex,wordbounds);
//		drawer.OutputImage();
//		renderer.renderImageWithDPI(page, dpi, imageType);
		drawer.rendergeometry(pageIndex,wordbounds);
		drawer.OutputImage();
}
}