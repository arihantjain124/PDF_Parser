package parser.text;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.pdfbox.text.TextPosition;

public class FootnoteAnalyser {
	
	private static final int MAX_FOOTNOTE_LENGTH = 3; //TODO: Add in configuration
	private static final int FOOTNOTE_HEIGHT_DIFF = 2; //TODO: Add in configuration

	public static HashMap<String, String> analyseFootnotes(List<WordWithBounds> line) throws IOException {
		
		HashMap<String, String> footnoteDefinitions = new HashMap<String, String>();
		int numberOfStrings = line.size();
		
		ArrayList<Integer> footnoteLineIndices = new ArrayList<Integer>();

		StringBuilder previousLineFootnoteDefKey = new StringBuilder();
		for (int i = 0; i < numberOfStrings; i++) {

			WordWithBounds curLine = line.get(i);
			
			int footnoteLength = getFootnoteSupersciptLength(curLine);
			if(footnoteLength >= 0) {
				String key = curLine.getText().substring(0, footnoteLength);
				String footnoteText = curLine.getText().substring(footnoteLength + 1);
				
				footnoteDefinitions.put(key, footnoteText);
				
				previousLineFootnoteDefKey.setLength(0);
				previousLineFootnoteDefKey.append(key);
				
				footnoteLineIndices.add(i);
			}else {
				
				if(previousLineFootnoteDefKey.length() <= 0 ) {
					continue;
				}
				
				Rectangle2D bounds1 = curLine.getbound();
				Rectangle2D bounds2 = line.get(i - 1).getbound();
				
				double bounds1Xstart = bounds1.getX();
		        double bounds1Xend = bounds1Xstart + bounds1.getWidth();

		        double bounds2Xstart = bounds2.getX();
		        double bounds2Xend = bounds2Xstart + bounds2.getWidth();
		        
		        if (bounds2Xend < bounds1Xstart || bounds2Xstart > bounds1Xend) //No horizontal overlap
		        {
		        	previousLineFootnoteDefKey.setLength(0);
		        	continue;
		        }
		        	
	        	//Check vertical gap height
		        double bounds1Ystart = bounds1.getY();
		        double bounds1Yend = bounds1Ystart + bounds1.getHeight();
		        
		        double bounds2Ystart = bounds2.getY();
		        double bounds2Yend = bounds2Ystart + bounds2.getHeight();
		        
		        double verticalGapHeight = 0;
		        if (bounds2Yend < bounds1Ystart || bounds2Ystart > bounds1Yend) {
		        	//No overlap in Y
		        	if(bounds2Yend < bounds1Ystart) {
		        		verticalGapHeight = bounds1Ystart - bounds2Yend;
		        	}else {
		        		verticalGapHeight = bounds2Ystart - bounds1Yend;
		        	}
		        }
		        
		        if(verticalGapHeight < (2 * bounds2.getHeight()) ) { //TODO: Using a factor of 2 here. But need to re-check. 
		        	
		        	String curLineText = curLine.getText();
		        	
		        	String key = previousLineFootnoteDefKey.toString();
		        	String oldText = footnoteDefinitions.get(key);
		        	String footnoteText = oldText + curLineText;
		        	
		        	footnoteDefinitions.put(key, footnoteText);
		        	footnoteLineIndices.add(i);
		        	
		        }else {
		        	previousLineFootnoteDefKey.setLength(0);
		        }
			}
		}
		
		if(!footnoteLineIndices.isEmpty()) {
			
			Collections.sort(footnoteLineIndices, Collections.reverseOrder());
			for(int index : footnoteLineIndices) {
				line.remove(index);
			}
		}

		return footnoteDefinitions;

	}
	
	private static int getFootnoteSupersciptLength(WordWithBounds curLine) {
		
		List<TextPosition> curLineTextPositions = curLine.getTextPositions();

		if (curLineTextPositions.size() < 3) {
			return -1; //Can't be footnote definition. Footnote definition lines should have at least 3 characters.
		}
		
		int whitespaceIndex = -1;
		for( int i = 1; i <= MAX_FOOTNOTE_LENGTH && i < curLineTextPositions.size(); i++) {
			
			if(Character.isWhitespace(curLineTextPositions.get(i).getUnicode().charAt(0))) {
				whitespaceIndex = i;//Footnote lines has a space after MAX_FOOTNOTE_LENGTH
				break;
			}
		}
		
		if(whitespaceIndex < 0) {
			return -1;
		}
		
		double firstCharacterHeight = curLineTextPositions.get(0).getFontSizeInPt();
		for( int i = 1; i < whitespaceIndex; i++) {
			
			//All characters till whitespace should have same size
			if(curLineTextPositions.get(i).getFontSizeInPt() != firstCharacterHeight) {
				//not a footnote.
				return -1;
			}
		}

		double heightDiff = 0;
		if((whitespaceIndex + 1) < curLineTextPositions.size()) {
			double nonFootnoteCharactersHeight = curLineTextPositions.get(whitespaceIndex + 1).getFontSizeInPt();
			heightDiff = nonFootnoteCharactersHeight - firstCharacterHeight;
		}
		
		if (heightDiff >= FOOTNOTE_HEIGHT_DIFF) {
			return whitespaceIndex;
		}
		
		return -1;
	}
}
