

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripper.WordWithTextPositions;

import java.io.BufferedInputStream;

public class NewPDFTextStripper extends PDFTextStripper{
	public NewPDFTextStripper() throws IOException {
		super();
	}
	public static class Wordwithbounds
    {
        String text;
        List<TextPosition> textPositions;
        List<Float> posX=new ArrayList<Float>();  
        List<Float> posY=new ArrayList<Float>();  
        List<Float> endX=new ArrayList<Float>();  
        List<Float> endY=new ArrayList<Float>();  
        
        
        Wordwithbounds(WordWithTextPositions word)
        {
            text = word.getText();
            textPositions = word.getTextPositions();
            Iterator<TextPosition> textIter = textPositions.iterator();
            while (textIter.hasNext())
            {
                TextPosition pos = textIter.next();
                posX.add(pos.getX());
                posY.add(pos.getY());
                endX.add(pos.getEndX());
                endY.add(pos.getEndY());
            }   
        }

        public String getText()
        {
            return text;
        }

        public List<TextPosition> getTextPositions()
        {
            return textPositions;
        }
    }
	protected void writeLine(List<WordWithTextPositions> line) throws IOException
    {
		super.writeLine(line);
		List<Wordwithbounds> wordbounds=new LinkedList<Wordwithbounds>();
		
		int numberOfStrings = line.size();
        for (int i = 0; i < numberOfStrings; i++)
        {
            WordWithTextPositions word = line.get(i);
            wordbounds.add(new Wordwithbounds(word));
        }
        
    	Wordwithbounds temp= wordbounds.get(0);
    	System.out.print("posX = ");
    	System.out.println(temp.posX);
    	System.out.print("posY = ");
    	System.out.println(temp.posY);
    	System.out.print("endX = ");
    	System.out.println(temp.endX);
    	System.out.print("endY = ");
    	System.out.println(temp.endY);
    	System.out.print("text = ");
    	System.out.println(temp.text);
    	System.out.println("\n\n");
    	//System.out.println(String.format("x %f, y %f , endx %f, end y %f , text %s ",temp.posX,temp.posY,temp.endX,temp.endY,temp.text));
    }
	
}
