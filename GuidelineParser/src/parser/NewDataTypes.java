package parser;

import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.text.TextPosition;

public class NewDataTypes {
	
	public static class GraphObject
    {
		GeneralPath path=new GeneralPath();
		ArrayList<Float> Targety = new ArrayList<Float>();
		ArrayList<Float> Targetx = new ArrayList<Float>();
		GraphObject(GeneralPath Path){
			this.path=Path;
		}
		public void setTarget(float x,float y) {
			Targetx.add(x);
			Targety.add(x);
		}
		public void getTarget() {
			
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
