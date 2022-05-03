package parser.renderer;


import java.io.IOException;
import java.util.ArrayList;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.PageDrawer;
import org.apache.pdfbox.rendering.PageDrawerParameters;


import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;

public class GuidelinePageDrawer extends PageDrawer{
	
//	private int num_line = 0;
	private int count = 0;
	
	private ArrayList<GeneralPath> lines = new ArrayList<GeneralPath>();
	private ArrayList<GeneralPath> triangles = new ArrayList<GeneralPath>();
	
	AffineTransform pageLevelTransform = null; //To transform origin from lower left to upper left
	
	public GuidelinePageDrawer(PageDrawerParameters parameters) throws IOException {
		super(parameters);
//		num_line = 0;
	}

	public void drawPage(Graphics2D graphics, PDRectangle pageSize,RenderingHints renderingHints) throws IOException {
		
		pageLevelTransform = new AffineTransform(1, 0, 0, -1, 0, pageSize.getHeight());
		
		super.drawPage(graphics, pageSize);
		
	}
	
	private GeneralPath getTransformedPath() {
		GeneralPath clonedPath = (GeneralPath) getLinePath().clone();
		clonedPath.transform(pageLevelTransform);
		return clonedPath;
	}

	@Override
	public void strokePath() throws IOException {
		float[] coords = new float[6];
		GeneralPath currentPdfPath = getLinePath();
		PathIterator i = currentPdfPath.getPathIterator(null);
		count=0;
		
		while (i.isDone() == false) {
			count+=1;
			i.currentSegment(coords);
//			System.out.format("x1 : %f x2 : %f \n",coords[0],coords[1]);
			i.next();
		}
		
		if (count == 3 || count == 2) {
//			System.out.format("Number of Coords : %d Lines\n",count);
			lines.add(getTransformedPath());
		}
		
//		num_line+=1;
		currentPdfPath.reset();
	}

	@Override
	public void fillPath(int windingRule) throws IOException {
		float[] coords = new float[6];
		GeneralPath currentPdfPath = getLinePath();
		PathIterator i = currentPdfPath.getPathIterator(null);
		count = 0;
		
		while (i.isDone() == false) {
			count += 1;
			i.currentSegment(coords);
			i.next();
		}
		
		if(count == 4) { 	
//			System.out.format("Number of Coords : %d Triangles\n",count);
			triangles.add(getTransformedPath());
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
	
}
