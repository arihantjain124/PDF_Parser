package parser.text;

import java.awt.geom.Rectangle2D;
import java.util.List;

import org.apache.pdfbox.text.TextPosition;

//TODO: For NCCN guideline, PDFBox is returning a single word for entire line. So,this is actually a line. Rename the class to LineWithBounds
public class WordWithBounds {

	private String text;
	private List<TextPosition> textPositions;
	private Rectangle2D bound = new Rectangle2D.Float();
    
    public WordWithBounds(String text, List<TextPosition> positions)
    {
        this.text = text;
        this.textPositions = positions;
        
        int numberofchar = textPositions.size();
        double x = textPositions.get(0).getX();
        double y = textPositions.get(0).getY();
        double w = textPositions.get(numberofchar-1).getX() + textPositions.get(numberofchar-1).getWidthDirAdj();
        double h = 0;
        
        for (int i = 0; i < numberofchar; i++)
        {
        	if (h < textPositions.get(i).getHeight()) 
        	{
        		h = textPositions.get(i).getHeight();
        	}
        }
        bound.setRect(x, y - h, w - x, h);
//      System.out.format("%s , %f, %s ,%f %s %d \n",text,x,text.charAt(0),w,text.charAt(numberofchar-1),numberofchar);
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
