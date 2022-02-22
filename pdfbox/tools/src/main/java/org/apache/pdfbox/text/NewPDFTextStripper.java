package org.apache.pdfbox.text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.pdfbox.text.PDFTextStripper.WordWithTextPositions;

import java.io.BufferedInputStream;

public class NewPDFTextStripper extends PDFTextStripper{
	public NewPDFTextStripper() throws IOException {
		super();
		// TODO Auto-generated constructor stub
	}
	public static class Wordwithbounds
    {
        String text;
        List<TextPosition> textPositions;
        List<Float> posX=new ArrayList<Float>();  
        List<Float> posY=new ArrayList<Float>();  
        
        
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
	@SuppressWarnings("null")
	protected void writeLine(List<WordWithTextPositions> line) throws IOException
    {
		super.writeLine(line);
		List<Wordwithbounds> wordbounds = null;
		
		System.out.println("Here");
		int numberOfStrings = line.size();
        for (int i = 0; i < numberOfStrings; i++)
        {
            WordWithTextPositions word = line.get(i);
            wordbounds.add(new Wordwithbounds(word));
        }
    }
	
}
