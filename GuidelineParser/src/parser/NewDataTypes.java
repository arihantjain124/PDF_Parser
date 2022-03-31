package parser;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.text.TextPosition;

public class NewDataTypes {
	
	public static class GraphObject
    {
		GeneralPath path=new GeneralPath();
		ArrayList<Point2D> Targets = new ArrayList<Point2D>();
		
		GraphObject(GeneralPath Path){
			this.path=Path;
		}
		
		public void addTarget(float x,float y) {
			Point2D currPoint = new Point2D.Float(x,y); 
			Targets.add(currPoint);
		}
		
		public void addTarget(Point2D currPoint) {
			Targets.add(currPoint);
		}
		
		public ArrayList<Point2D> getTarget() {
			
			return Targets;
			
		}
		
		public GeneralPath getpath()
        {
            return path;
        }
    }
	
	public static class Wordwithbounds
    {
        String text;
        List<TextPosition> textPositions;
        Rectangle2D bound = new Rectangle2D.Float();
        float sum;
        
        
        Wordwithbounds(String text, List<TextPosition> positions)
        {
        	double x,y,w,h;
            this.text = text;
            this.textPositions = positions;
            int numberofchar = textPositions.size();
            x = textPositions.get(0).getX();
            y = textPositions.get(0).getY();
            w = textPositions.get(numberofchar-1).getX();
            h=0;
            for (int i = 0; i < numberofchar; i++)
            {
            	if (h<textPositions.get(i).getHeight()) 
            	{
            		h=textPositions.get(i).getHeight();
            	}
            }
            bound.setRect(x, y-h, w-x+5, h);
//          System.out.format("%s , %f, %s ,%f %s %d \n",text,x,text.charAt(0),w,text.charAt(numberofchar-1),numberofchar);
        }

        public String getText()
        {
            return text;
        }

        public List<TextPosition> getTextPositions()
        {
            return textPositions;
        }
        public Rectangle2D getbound()
        {
            return bound;
        }
    }
}
