package parser;


import java.io.IOException;
import java.util.ArrayList;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;

import org.apache.pdfbox.rendering.PageDrawer;
import org.apache.pdfbox.rendering.PageDrawerParameters;


import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;

public class NewPageDrawer extends PageDrawer{
	
//	private int num_line = 0;
	private int count = 0;
	
	ArrayList<GeneralPath> lines = new ArrayList<GeneralPath>();
	ArrayList<GeneralPath> triangles = new ArrayList<GeneralPath>();
	
	public NewPageDrawer(PageDrawerParameters parameters) throws IOException {
		super(parameters);
//		num_line = 0;
	}

	public void drawPage(Graphics2D temp_g, PDRectangle pageSize,RenderingHints renderingHints) throws IOException {
		super.drawPage(temp_g, pageSize);
		
	}

	@Override
	public void strokePath() throws IOException {

		float[] coords = new float[6];
		GeneralPath current_pdf_path = getLinePath();
		PathIterator i = current_pdf_path.getPathIterator(null);
		count=0;
		
		while (i.isDone() == false) {
			count+=1;
			i.currentSegment(coords);
//			System.out.format("x1 : %f x2 : %f \n",coords[0],coords[1]);
			i.next();
		}
		
		if (count == 3 || count == 2) {
//			System.out.format("Number of Coords : %d Lines\n",count);
			lines.add(current_pdf_path);
		}
		
//		num_line+=1;
		current_pdf_path.reset();
	}

	@Override
	public void fillPath(int windingRule) throws IOException {
		float[] coords = new float[6];
		GeneralPath current_pdf_path = getLinePath();
		PathIterator i = current_pdf_path.getPathIterator(null);
		count = 0;
		
		while (i.isDone() == false) {
			count += 1;
			i.currentSegment(coords);
			i.next();
		}
		
		if(count == 4) { 	
//			System.out.format("Number of Coords : %d Triangles\n",count);
			triangles.add(current_pdf_path);
		}
		
		super.fillPath(windingRule);
		//num_line += 1;
	}

	public ArrayList<GeneralPath> getLines() {
		return lines;
	}
	
	public ArrayList<GeneralPath> getTriangles() {
		return triangles;
	}
	
	public PDGraphicsState getstate() {
		return getGraphicsState();
	}
}
