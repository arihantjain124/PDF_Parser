package parser.page;

import java.io.IOException;
import java.util.HashMap;


import org.apache.pdfbox.pdmodel.PDDocument;


import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDNamedDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineNode;

public class ExtractBookMark {
	
	private HashMap <String, Integer> bookmarkmap= new HashMap <String, Integer>();

	public ExtractBookMark(PDDocument document, PDOutlineNode bookmark, String indentation)// extract book mark
	{
		
	}

	

public HashMap <String, Integer> getBookmark(PDDocument document,PDOutlineNode bookmark, String indentation) throws IOException
    {
        PDOutlineItem current = bookmark.getFirstChild();
        while( current != null )
        {
           
             
            if (current.getAction() instanceof PDActionGoTo)
            {
                PDActionGoTo gta = (PDActionGoTo) current.getAction();
                if (gta.getDestination() instanceof PDPageDestination)
                {
                    PDPageDestination pd = (PDPageDestination) gta.getDestination();
                    bookmarkmap.put(current.getTitle(),(pd.retrievePageNumber() + 1));
                    
                }
                else if (gta.getDestination() instanceof PDNamedDestination)
                {
                    PDPageDestination pd = document.getDocumentCatalog().findNamedDestinationPage((PDNamedDestination) gta.getDestination());
                    if (pd != null)
                    {
                    	bookmarkmap.put(current.getTitle(),(pd.retrievePageNumber() + 1));
                       
                    }
                }
            }
            
           
            getBookmark( document, current, indentation + "    " );
            current = current.getNextSibling();
        }
        return bookmarkmap;
        
    }

}
