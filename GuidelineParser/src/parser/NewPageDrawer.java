package parser;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.PageDrawer;
import org.apache.pdfbox.rendering.PageDrawerParameters;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripper.WordWithTextPositions;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.apache.pdfbox.util.Matrix;

import parser.NewDataTypes.Line;

import org.apache.pdfbox.text.TextPosition;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;

public class NewPageDrawer extends PageDrawer{
	
	private int num_line=0;
	private int count=0;
	private Graphics2D g;
	ArrayList<Line> PageLines = new ArrayList<Line>();
	
	
	public NewPageDrawer(PageDrawerParameters parameters) throws IOException {
		super(parameters);
		num_line=0;
	}

	public void drawPage(Graphics2D g_temp,Graphics2D g2d, PDRectangle pageSize,RenderingHints renderingHints) throws IOException {

		this.g=g2d;
        g2d.translate(0, pageSize.getHeight());
        g2d.scale(1, -1);
        g2d.translate(-pageSize.getLowerLeftX(), -pageSize.getLowerLeftY());
		super.drawPage(g_temp, pageSize);
		
	}
	
	@Override
	public void strokePath() throws IOException {
		
		GeneralPath temp = getLinePath();
		boolean linedraw=true;
		PathIterator i = temp.getPathIterator(null);
		float[] coords = new float[6];
		float[] prev_coords = new float[6];
		count=0;
		while (i.isDone() == false) {
			count+=1;
			i.currentSegment(coords);
			i.next();
		}
		System.out.format("Line Number : %d , Number of Coords : %d \n",num_line,count);
		if (count==2) {
			i = temp.getPathIterator(null);
			i.currentSegment(coords);
			i.next();
			i.currentSegment(prev_coords);
			Line curr_line=null;
			curr_line=new Line(prev_coords[0],prev_coords[1],coords[0],coords[1]);
            if(linedraw==true) {
			this.g.setComposite(getGraphicsState().getStrokingJavaComposite());
			this.g.draw(curr_line.path);
            }
		}
		num_line+=1;
        temp.reset();
	}

	@Override
	public void fillPath(int windingRule) throws IOException {
		
		float[] coords = new float[6];
		GeneralPath temp = getLinePath();
		int pointCount = 0;			
		PathIterator i = temp.getPathIterator(null);
		while (i.isDone() == false) {
			pointCount+=1;
			i.currentSegment(coords);
			i.next();
		}
		if(pointCount >= 3 && pointCount <= 4) {
			this.g.setComposite(getGraphicsState().getStrokingJavaComposite());
			this.g.draw(temp);
//			System.out.format("Line Number : %d , Number of Coords : %d Triangles\n",num_line,pointCount);
			i = temp.getPathIterator(null);
			while (i.isDone() == false) {
				pointCount+=1;
				int pathType = i.currentSegment(coords);
//	            System.out.format("pathType = %d x = %f ,y = %f \n", pathType, coords[0],coords[1]);
				i.next();
			}
		}

		super.fillPath(windingRule);
		num_line+=1;
	}
}
