package parser.text;


import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

public class GuidelineTextStripper extends PDFTextStripper{

	private List<WordWithBounds> wordbounds = new LinkedList<WordWithBounds>();
	private List<WordWithBounds> capitalWordbounds = new LinkedList<WordWithBounds>();
	private int pageIndex ;
	private int dpi=72;
	
	private static final int WIDTH_FACTOR_HEURISTICS_FOR_GAP = 4;
	
	private final Map<String, Rectangle2D> regionArea = new HashMap<String, Rectangle2D>();
	
	public GuidelineTextStripper(int pageIndex) throws IOException {
		super();
		this.pageIndex = pageIndex;
	}
	
	private void positionToBound(String text, List<TextPosition> positions) {
        
		int numberOfTextPos = positions.size();
		
        StringBuilder lineBuilder = new StringBuilder();
        List<TextPosition> wordPositions = new ArrayList<TextPosition>();
        
        for (int i = 0; i < numberOfTextPos; i++)
        {
        	if(wordPositions.isEmpty()) {
        		lineBuilder.append(positions.get(i).getUnicode());
                wordPositions.add(positions.get(i));
        		continue;
        	}
        	
        	TextPosition curTextPos = positions.get(i);
        	TextPosition prevTextPos = positions.get(i - 1);
        	
        	float gapSize = -1;
        	if(curTextPos.getEndX() >  prevTextPos.getEndX()) {
        		
        		gapSize = curTextPos.getEndX() -  prevTextPos.getEndX();
        		
        	}else if (wordPositions.get(0).getX() > curTextPos.getEndX()) {
        		
        		gapSize = wordPositions.get(0).getX() - curTextPos.getEndX();
        	}
        	
        	if(curTextPos.getUnicode().trim().length() > 0 && (gapSize > WIDTH_FACTOR_HEURISTICS_FOR_GAP * curTextPos.getWidth())) {
        		
        		if(Character.isUpperCase((lineBuilder.charAt(0)))) {
        			capitalWordbounds.add(new WordWithBounds(lineBuilder.toString(), wordPositions));//Create a new WordWithBounds up to previous text position.
        		}
        		wordbounds.add(new WordWithBounds(lineBuilder.toString(), wordPositions));//Create a new WordWithBounds up to previous text position.
        		
                lineBuilder = new StringBuilder();
                wordPositions.clear();
                i--; //re-process the cur text position;
        		
        	}else {
        		lineBuilder.append(curTextPos.getUnicode());
                wordPositions.add(curTextPos);
        	}
        }

		if(Character.isUpperCase((lineBuilder.charAt(0)))) {
			capitalWordbounds.add(new WordWithBounds(lineBuilder.toString(), wordPositions));//Create a new WordWithBounds up to previous text position.
		}
        wordbounds.add(new WordWithBounds(lineBuilder.toString(), wordPositions));
	}
	
	@Override
    protected void startPage(PDPage page) throws IOException
    {
		super.startPage(page);
		wordbounds.clear();
		capitalWordbounds.clear();
    }
	
	@Override
	public void writeLine(List<WordWithTextPositions> line) throws IOException
    {
		super.writeLine(line);
		
		int numberOfStrings = line.size();
        for (int i = 0; i < numberOfStrings; i++)
        {
            WordWithTextPositions word = line.get(i);
            positionToBound(word.getText(), word.getTextPositions());
        }

    }
	
	@Override
	protected void writePage() throws IOException
    {
		super.writePage();
    }
	
	@Override
    protected void processTextPosition(TextPosition text)
    {
		if(regionArea.isEmpty()) {
			super.processTextPosition(text);
			return;
		}
		
        for (Map.Entry<String, Rectangle2D> regionAreaEntry : regionArea.entrySet())
        {
            Rectangle2D rect = regionAreaEntry.getValue();
            if (rect.contains(text.getX(), text.getY()))
            {
                super.processTextPosition(text);
            }
        }
    }
	
   /**
     * Add a new region to group text by.
     *
     * @param regionName The name of the region.
     * @param rect The rectangle area to retrieve the text from. The y-coordinates are java
     * coordinates (y == 0 is top), not PDF coordinates (y == 0 is bottom).
     */
    public void addRegion( String regionName, Rectangle2D rect )
    {
        regionArea.put( regionName, rect );
    }
	
	public List<WordWithBounds> getWordBounds(){
		return wordbounds;
		
	}
	
	public List<WordWithBounds> getCapitalWordBounds(){
		return capitalWordbounds;
		
	}
}