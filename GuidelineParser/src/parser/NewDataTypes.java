package parser;

import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.apache.pdfbox.text.TextPosition;

public class NewDataTypes {
	
	public static class Line
    {
		GeneralPath path=new GeneralPath();
		float[] coord = new float[4];
		Line(float x1,float y1,float x2,float y2){
			coord[0]=x1;
			coord[1]=y1;
			coord[2]=x2;
			coord[3]=y2;
			this.path.moveTo(x1, y1);
			this.path.lineTo(x2, y2);
			this.path.closePath();
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
