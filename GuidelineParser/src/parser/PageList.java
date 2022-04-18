package parser;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.HashMap;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripperByArea;

public class PageList {
	
	//Rectangle rect = new Rectangle( 700, 560, 92, 52 );
	PDDocument doc= null;
	
	public PageList(PDDocument document)
	{
		this.doc = document; 
	}
	
	public String ExtractTextByArea(PDDocument doc,Rectangle rect,int PageIndex)throws IOException
	{
		//HashMap<Integer,String> PageListmap = new HashMap<Integer,String>();
		PDFTextStripperByArea stripper = new PDFTextStripperByArea();
		stripper.setSortByPosition( true );
		stripper.addRegion( "class1", rect );
		PDPage firstPage = doc.getPage(PageIndex-1);
		stripper.extractRegions( firstPage );
		
		
        return stripper.getTextForRegion( "class1" );
	}
}
