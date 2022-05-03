package parser;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.pdfbox.text.TextPosition;

import parser.text.WordWithBounds;

public class FootNotes {
	
	List<WordWithBounds> line;
	
	public FootNotes()
	{
		this.line = null;
	}
	public HashMap<String,String> footnotes(List<WordWithBounds> line)throws IOException
    {
    	double height0=0;
		double height1=0;
		double heightDiff=0;
		HashMap<String, String> DiffrenceList = new HashMap<String, String>();
		int numberOfStrings = line.size();
		
		for (int i = 0; i < numberOfStrings; i++)
        {

			WordWithBounds word = line.get(i);
            
			List<TextPosition> a=word.getTextPositions();
			
			if(a.size()<=3)
			{
				continue;
			}
            height0=word.getTextPositions().get(0).getFontSizeInPt();
            height1=word.getTextPositions().get(2).getFontSizeInPt();
            char val=word.getText().charAt(2);
            
            
            if(Character.isWhitespace(val)==true)
            {
            	height1=word.getTextPositions().get(3).getFontSizeInPt();
            	
            }
            heightDiff=height1-height0;
            
            
            if(heightDiff==2)
            {
            	String key="";
            	if(Character.isWhitespace(val)==true)
            	{
            		key = "" + word.getText().charAt(0) + word.getText().charAt(1);
            	}
            	else
            	{
            		key = "" + word.getText().charAt(0);
            	}
            	
            	DiffrenceList.put(key,word.getText().substring(2));
            }
            
        }
		
         return DiffrenceList;  
    
    }


}
