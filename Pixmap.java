/* 
Bouncing Lines
Copyright (C) 2018 by webhauser@gmail.com

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/> 
*/

import java.util.ArrayList;
import java.awt.*;
import java.awt.image.*;
import java.awt.Point;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;

public class Pixmap {
	
	// Const int colors
	public final static int black = Pixmap.getIntFromColor(0,0,0);
	public final static int white = Pixmap.getIntFromColor(255,255,255);
	public final static int green = Pixmap.getIntFromColor(0,255,0);	
	public final static int red   = Pixmap.getIntFromColor(255,0,0);
	public final static int blue  = Pixmap.getIntFromColor(0,0,255);	
			
	
	public int xSize, ySize;
	private BufferedImage pixels;
	
	// Side codes
	public static final int bit_Left = 1;
	public static final int bit_Right= 2;
	public static final int bit_Top = 4;
	public static final int bit_Bottom = 8;	
	
	/* cdab 
	   0000 : - Not outside
	   0011 : * Impossible	   
	   0101 : + Top Left
	   0110 : + Top Right
	   0111 : * IMPOSSIBLE
	   1100 : * Impossible	   
	   1111 : * Impossible
	   
	ab:10 
	cd:01
	private static int getbit(int x, int n) {
		while(n-- > 0) x>>=1;
		return( x & 1);
	}
	
	*/
	
	public static int getIntFromColor( Color col ) {
		return getIntFromColor(col.getRed(),col.getGreen(),col.getBlue());
	}
	
	public static int getIntFromColor(int Red, int Green, int Blue){
		Red = (Red << 16) & 0x00FF0000; //Shift red 16-bits and mask out other stuff
		Green = (Green << 8) & 0x0000FF00; //Shift Green 8-bits and mask out other stuff
		Blue = Blue & 0x000000FF; //Mask out anything not blue.
		return 0xFF000000 | Red | Green | Blue; //0xFF000000 for 100% Alpha. Bitwise OR everything together.
	}

	public static Color getColorFromInt(int col) {
		int b=col & 255;
		int g=(col>>8)&255;
		int r=(col>>16)&255;
		return new Color(r,g,b);
	}
	
	public Pixmap(int xSize, int ySize) {
		this.xSize = xSize;
		this.ySize = ySize;
		pixels = new BufferedImage(xSize,ySize, BufferedImage.TYPE_INT_RGB);
	}
	
	public void copyTo(Graphics g) {
		g.drawImage(pixels, 0,0, null);
	}
	
	boolean inRect(int x, int y) {
		return( x>=0 && y>=0 && x<xSize && y<ySize );
	}
	
	public void line(int x0, int y0, int x1,int y1, Color col) {
		Graphics g = pixels.getGraphics();
		g.setColor(col);
		g.drawLine(x0,y0,x1,y1);		
	}
	
	public void rect(int x, int y, int width, int height, Color col) {
		Graphics g = pixels.getGraphics();
		g.setColor(col);
		g.fillRect(x,y,width,height);		
	}
	
	public boolean alapvonal(int x,int y) {	
		return (getPixel(x,y)==Pixmap.white);
	}
	
	
	public void plot(int x, int y, Color c) {
		rect(x,y,Player.PlayerSize,Player.PlayerSize,c);
	}
	
	void drawFrame( Color color ) {
		// left, right
		for(int y=0;y<ySize;y+=Player.PlayerSize) {
			plot(0,y,color);
			plot(xSize-Player.PlayerSize,y,color);
		}
		// top, bottom
		for(int x=0;x<xSize;x+=Player.PlayerSize) {
			plot(x,0,color);
			plot(x,ySize-Player.PlayerSize,color);
		}
	}	
	
	public void setPixel(int x, int y, int col) {
		if(!inRect(x,y)) return;
		pixels.setRGB(x,y,col);		
	}
	
	public int getPixel(int x, int y) {
		if(!inRect(x,y)) return 0;
		return pixels.getRGB(x,y);
	}
		
	public void clear() {
		rect(0,0,xSize,ySize,Color.BLACK);
	}
	
	public void merge(Pixmap a) {		
		int x,y,col;
		for(y=0;y<ySize;y++) {
			for(x=0;x<xSize;x++) {
				if(pixels.getRGB(x,y)==black) {
					col=a.getPixel(x,y);
					pixels.setRGB(x,y,col);
				}
			}
		}
	}
	
	public int overcol(int c1, int c2) {
		int count=0;
		Color c=getColorFromInt(c2);
		for(int y=0;y<ySize;y+=Player.PlayerSize) 
			for(int x=0;x<xSize;x+=Player.PlayerSize) 
				if(pixels.getRGB(x,y)==c1) {
					plot(x,y,c);				
					count++;
				}
				
		return count;
	}
	
	public int delcol(int col) {		
		return overcol(col,black);
	}
	
	private int doFill(int x,int y, int szini) {
		int c, count=1, count2=0;
		int xx,yy,off=Player.PlayerSize;			
		
		Color szin=getColorFromInt(szini);
		plot(x,y,szin);
				
		while(count > count2) {
			count2 = count;
			for(y=0; y<ySize; y+=Player.PlayerSize) {
				for(x=0; x<xSize; x+=Player.PlayerSize) {
					if(getPixel(x,y)==szini) {
						// van-e black szomszedja
						xx=x+off; yy=y;	// right
						c = getPixel(xx,yy); if(c==black) {++count; plot(xx,yy,szin);}
						xx=x-off; yy=y;	// left
						c = getPixel(xx,yy); if(c==black) {++count; plot(xx,yy,szin);}
						xx=x; yy=y-off;// up
						c = getPixel(xx,yy); if(c==black) {++count; plot(xx,yy,szin);}
						xx=x; yy=y+off;// down
						c = getPixel(xx,yy); if(c==black) {++count; plot(xx,yy,szin);}
					}
				}
			}			
		}
		return count;
	}
	
	
	// Game function, fal=white
	public int filler(int vir, int x1,int y1, int x2,int y2, boolean help)  {		
		int innerFill=getIntFromColor(Color.ORANGE);
		int cx1,cy1,count1=0;
		int cx2,cy2,count2=0;
		
		cx1=cx2=x1;
		cy1=cy2=y1;
		
		// kezdo irany
		switch(vir) {
		case 0:// Key Up - from bottom
		case 2:// Key Down - from up
			cx1+=Player.PlayerSize;
			cx2-=Player.PlayerSize;
			break;
		case 1: // Key Right - started from left
		case 3:// Key Left
			cy1+=Player.PlayerSize;
			cy2-=Player.PlayerSize;			
			break;
		}
	
		count1=doFill(cx1,cy1, red); delcol(red);
		count2=doFill(cx2,cy2, red); delcol(red);
		System.out.println("c1="+count1+" c2="+count2);	// debug
		
		// Display candidate points for the fill start
		if(count1 < count2) {			
			 count1 = doFill(cx1,cy1, innerFill); 
			 if(help) plot( cx1, cy1, Color.RED );
		} else {
			count1 = doFill(cx2,cy2, innerFill);
			if(help) plot( cx2, cy2, Color.RED );	
		}

		return count1;
	}
	
	public boolean save(String fname) {
		File f = new File(fname);
		try {
			if (!ImageIO.write(pixels, "JPEG", f)) {
				System.out.println("Unexpected error writing image: "+fname);
				return false;
			}
		} catch(IOException e) {return false;}
		return true;
	}

	public Image getImage() {return this.pixels;}	
	public BufferedImage getBufferedImage() {return this.pixels;}
}