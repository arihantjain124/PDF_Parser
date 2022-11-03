package parser.text;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.pdfbox.text.TextPosition;

public class FootnoteAnalyser {
	
	static class Pair{
		public int start;
		public int end;
		
		public Pair(int start, int end) {
			this.start = start;
			this.end = end;
		}
	}
	
	private static final int MAX_FOOTNOTE_LENGTH = 3; //TODO: Add in configuration
	private static final int FOOTNOTE_HEIGHT_DIFF = 1; //TODO: Add in configuration

	public static HashMap<String, FootnoteDetails> analyseFootnotes(List<WordWithBounds> line) throws IOException {
		
		HashMap<String, FootnoteDetails> footnoteDefinitions = new HashMap<String, FootnoteDetails>();
		int numberOfStrings = line.size();
		
		ArrayList<Integer> footnoteLineIndices = new ArrayList<Integer>();

		StringBuilder previousLineFootnoteDefKey = new StringBuilder();
		for (int i = 0; i < numberOfStrings; i++) {

			WordWithBounds curLine = line.get(i);
			
			int footnoteLength = getFootnoteSupersciptLength(curLine);
			if(footnoteLength >= 0) {
				String key = curLine.getText().substring(0, footnoteLength);
				String footnoteText = curLine.getText().substring(footnoteLength + 1);
				
				FootnoteDetails footnoteDetails = new FootnoteDetails(footnoteText);
				footnoteDefinitions.put(key, footnoteDetails);
				
				
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
		        	String oldText = footnoteDefinitions.get(key).getFootNoteText();
		        	String footnoteText = oldText + curLineText;
		        	
		        	FootnoteDetails footnoteDetails = new FootnoteDetails(footnoteText);
		        	footnoteDefinitions.put(key, footnoteDetails);
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
		
		int nonFootnoteCharactersIndex = whitespaceIndex + 1;
		while(nonFootnoteCharactersIndex < curLineTextPositions.size()) {
			
			//Find next non space character which will be used below for height difference calculation.
			if(!Character.isWhitespace(curLineTextPositions.get(nonFootnoteCharactersIndex).getUnicode().charAt(0))) {
				break;
			}
			nonFootnoteCharactersIndex++;
		}

		double heightDiff = 0;
		if(nonFootnoteCharactersIndex < curLineTextPositions.size()) {
			double nonFootnoteCharactersHeight = curLineTextPositions.get(nonFootnoteCharactersIndex).getFontSizeInPt();
			heightDiff = nonFootnoteCharactersHeight - firstCharacterHeight;
		}
		
		if (heightDiff >= FOOTNOTE_HEIGHT_DIFF) {
			return whitespaceIndex;
		}
		
		return -1;
	}
	
	public static void analyzeAllFootNoteReferences(List<RegionWithBound> allRegionList,
			String pageKey, HashMap<String, FootnoteDetails> docFootnotes) {

		for (RegionWithBound region : allRegionList) {
			ArrayList<String> footnoteRefList = analyzeFootNoteReferences(region.getContentLines(), false, pageKey, docFootnotes);
			region.addFootnoteRefs(footnoteRefList);
		}
	}
	
	public static void analyzeAllFootNoteReferences(HashMap<String, List<RegionWithBound>> labelsHashMap, 
			String pageKey, HashMap<String, FootnoteDetails> docFootnotes) {
		
		for(String key : labelsHashMap.keySet()) {
			
			List<RegionWithBound> regions = labelsHashMap.get(key);
			
			for(RegionWithBound region : regions) {
				ArrayList<String> footnoteRefList = analyzeFootNoteReferences(region.getContentLines(), true, pageKey, docFootnotes);
				region.addFootnoteRefs(footnoteRefList);
			}
		}
	}
	
	public static ArrayList<String> analyzeFootNoteReferences(List<WordWithBounds> lines, boolean removeFootnote,
			String pageKey, HashMap<String, FootnoteDetails> docFootnotes)
	{
		
		HashSet<String> footnoteRefSet = new HashSet<String>();
		for(WordWithBounds line : lines)
		{
			float[] fontSizeArray = getFontSizeArray(line);
			
			int[] sortedArray = argsort(fontSizeArray, true); //First few elements stores the index of Footnote
			
			int i = 0;
			for(; i < sortedArray.length - 1; i++) {
				
				if(fontSizeArray[sortedArray[i]] < fontSizeArray[sortedArray[i + 1]]) {
					
					float y1 = line.getTextPositions().get(sortedArray[i]).getTextMatrix().getTranslateY();//0,0 in lower left.
					float y2 = line.getTextPositions().get(sortedArray[i + 1]).getTextMatrix().getTranslateY();//0,0 in lower left.
					
					if(y1 > y2) {//Footnote references are at slightly higher positions.
						break;
					}
				}
			}
			
			if( i >= (sortedArray.length - 1)) {
				continue;//no footnote reference in this line
			}
			
			//Footnotes positions are in sortedArray; from 0 to i. Sort the positions in ascending order
			int[] footnoteIndexes = new int[i + 1];
			System.arraycopy(sortedArray, 0, footnoteIndexes, 0, i + 1);
			Arrays.sort(footnoteIndexes);
			
			ArrayList<Pair> ranges = new ArrayList<Pair>();
			int startIndex = 0, endIndex = 0;
			while(endIndex < (footnoteIndexes.length -1)) {
				
				if((sortedArray[endIndex + 1] - sortedArray[endIndex]) > 1) {
					//new range has started
					Pair range = new Pair(sortedArray[startIndex], sortedArray[endIndex]);
					ranges.add(range);
					startIndex = endIndex + 1;
				}
				
				endIndex++;
			}
			
			Pair range = new Pair(sortedArray[startIndex], sortedArray[endIndex]);
			ranges.add(range);
			
			StringBuilder strBuilder = new StringBuilder(line.getText());
			for(int l = ranges.size() - 1; l >=0; l--) {
				Pair curRange = ranges.get(l);
				
				StringBuilder footNoteSubStrBuilder = new StringBuilder();
				for (int m = curRange.start; m <= curRange.end; m++)
				{
					footNoteSubStrBuilder.append(line.getTextPositions().get(m).getUnicode());//line.getText() & line.getTextPositions() may not be in sync because of ligatures.
				}
				
				String footNoteSubStr = footNoteSubStrBuilder.toString().trim();
				//String footNoteSubStr = line.getText().substring(curRange.start, curRange.end + 1).tirm();
				
				if(footNoteSubStr.isEmpty()) {
					continue;
				}
				
				String[] footNotes = footNoteSubStr.trim().split(",");
				List<String> footNoteList = new ArrayList<String>();
				
				for(int fnIndex = 0; fnIndex < footNotes.length; fnIndex++) {
					String footNote = footNotes[fnIndex].trim();
					
					if(footNote.contains("-")) {
						
						//Looks like it's range of integers
						String[] intRange = footNote.split("-");
						int startNum = Integer.parseInt(intRange[0].trim());
						int endNum = Integer.parseInt(intRange[1].trim());
						for(int rangeNum = startNum; rangeNum <= endNum; rangeNum++) {
							
							String pageLevelFootnoteKey = pageKey + "/" + rangeNum;
							if(docFootnotes.containsKey(pageLevelFootnoteKey)) {
								footNoteList.add(pageLevelFootnoteKey);
							}else {
								footNoteList.add(Integer.toString(rangeNum));
							}
						}
						
					}else {
					
						String pageLevelFootnoteKey = pageKey + "/" + footNote;
						if(docFootnotes.containsKey(pageLevelFootnoteKey)) {
							footNoteList.add(pageLevelFootnoteKey);
						}else {
							footNoteList.add(footNote);
						}
					}
				}
				footnoteRefSet.addAll(footNoteList);
				
				int startInText = strBuilder.indexOf(footNoteSubStr, curRange.start);//line.getText() & line.getTextPositions() may not be in sync because of ligatures. Hence re-calculate the indices.
				int endInText = startInText + (curRange.end - curRange.start);
				
				if(startInText >= 0) {
					if(removeFootnote) {
						strBuilder.replace(startInText, endInText + 1, "");
					}else {
						strBuilder.replace(startInText, endInText + 1, "{" + String.join(",", footNoteList) + "}");
					}
				}
				
				//strBuilder.replace(curRange.start, curRange.end + 1, "{" + footNoteSubStr.trim() + "}");
				//System.out.println(footNoteSubStr);
			}
			
			String newText = strBuilder.toString();
			//System.out.println(newText);
			line.setText(newText);
		}
		
		return (new ArrayList<String>(footnoteRefSet));
	}
	
	private static float[] getFontSizeArray(WordWithBounds line)
	{
		float[] fontSizes = new float[line.getTextPositions().size()];
		for (int i = 0; i < line.getTextPositions().size(); i++)
		{
			float size = line.getTextPositions().get(i).getFontSizeInPt();
			fontSizes[i] = size;
		}
		
		return fontSizes;
	}
	
	private static int[] argsort(final float[] a, final boolean ascending) {
        Integer[] indexes = new Integer[a.length];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = i;
        }
        
        Arrays.sort(indexes, new Comparator<Integer>() {
            @Override
            public int compare(final Integer i1, final Integer i2) {
                return (ascending ? 1 : -1) * Float.compare(a[i1], a[i2]);
            }
        });
        
        return asArray(indexes);
    }
	
	@SafeVarargs
	private static <T extends Number> int[] asArray(final T... a) {
        int[] b = new int[a.length];
        for (int i = 0; i < b.length; i++) {
            b[i] = a[i].intValue();
        }
        return b;
    }
}
