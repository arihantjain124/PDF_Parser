package parser.text;


import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

public class GuidelineTextStripper extends PDFTextStripper{

	private List<WordWithBounds> wordbounds = new LinkedList<WordWithBounds>();
	private int pageIndex ;
	private int dpi=72;
	
	private final Map<String, Rectangle2D> regionArea = new HashMap<String, Rectangle2D>();
	
	public GuidelineTextStripper(int pageIndex) throws IOException {
		super();
		this.pageIndex = pageIndex;
	}
	
	private void positionToBound(String text, List<TextPosition> positions) {
        
		int numberofchar = positions.size();
        float x = 0;
        float threshold = (float) 10;
        int j = 0;
        
        for (int i = 0; i < numberofchar; i++)
        {
        	if (i == 0) {
                x = positions.get(0).getX();
                j = 0;
                threshold = (float) (10);
        	}
        	else if (positions.get(i).getX() - x > threshold) {
        		
            	wordbounds.add(new WordWithBounds(text.substring(j, i), positions.subList(j, i)));
            	j = i;
            }
        	else if (i == numberofchar - 1) {
        		wordbounds.add(new WordWithBounds(text.substring(j, i+1), positions.subList(j, i+1)));
        	}
        	x = positions.get(i).getX();
        }
        
	}
	
	@Override
	public void writeLine(List<WordWithTextPositions> line) throws IOException
    {
		super.writeLine(line);
		
		int numberOfStrings = line.size();
        for (int i = 0; i < numberOfStrings; i++)
        {
            WordWithTextPositions word = line.get(i);
            wordbounds.add(new WordWithBounds(word.getText(), word.getTextPositions()));
        }

    }
	
	@Override
	protected void writePage() throws IOException
    {
		super.writePage();
//		NewPDFRenderer drawer = new NewPDFRenderer(document,pageIndex,dpi);
//		drawer.drawWordBounds(pageIndex,wordbounds);
//		drawer.rendergeometry(pageIndex);
//		drawer.OutputImage();
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
}