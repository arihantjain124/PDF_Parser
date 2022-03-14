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
import org.apache.pdfbox.text.TextPosition;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;

public class NewPageDrawer extends PageDrawer{
	
	Graphics g;
	public NewPageDrawer(PageDrawerParameters parameters) throws IOException {
		super(parameters);
		// TODO Auto-generated constructor stub
	}
	
	public void drawPage(Graphics g_temp,Graphics g2d, PDRectangle pageSize,RenderingHints renderingHints) throws IOException {

		this.g=g2d;
		super.drawPage(g_temp, pageSize);
		
	}
	public void strokePath() throws IOException {
//		super.strokePath();
		GeneralPath temp=getLinePath();
//		System.out.println(temp);
		((Graphics2D) this.g).draw(temp);
	}
	
	public void fillPath(int windingRule) throws IOException {
//		super.fillPath(windingRule);
	}
    public void showAnnotation(PDAnnotation annotation) throws IOException{
//    	super.showAnnotation(annotation);
    }
}
