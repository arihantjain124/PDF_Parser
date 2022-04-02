package parser.text;


import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

public class GuidelineTextStripper extends PDFTextStripper{

	private List<WordWithBounds> wordbounds = new LinkedList<WordWithBounds>();
	private int pageIndex ;
	private int dpi=72;
	
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
	
	protected void writePage() throws IOException
    {
		super.writePage();
//		NewPDFRenderer drawer = new NewPDFRenderer(document,pageIndex,dpi);
//		drawer.drawWordBounds(pageIndex,wordbounds);
//		drawer.rendergeometry(pageIndex);
//		drawer.OutputImage();
}
	
	public List<WordWithBounds> getWordBounds(){
		return wordbounds;
		
	}
}