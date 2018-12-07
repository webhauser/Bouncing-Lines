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

import java.awt.*;

public class Enemy {
	// const
	private final static int MAX_LINES = 20;	
	// static
	private static Pixmap pixmap;	
	private static int xSize, ySize;	
	private static int numLines;	
	
	//debug
	public static boolean flagDisplayInfo = false;	
	public static int flagDisplayInfoI = -1;		
	
	// vars		
	private Color color;
	private int speed, speedt;
	private int first;
	private int dx1,dx2,dy1,dy2;	
	private int[] x1;
    private int[] y1;
    private int[] x2;
    private int[] y2;
	
	
	public static Color RandomColor() {
		return new Color((int)(Math.random()*256),
						 (int)(Math.random()*256),
						 (int)(Math.random()*256));
	}
	
	public static Color RandomColorNotBlue() {
		Color col;
		do {
			col=null;
			col=RandomColor();
		} while(col==Color.BLUE);
		return col;
	}
	
	public static boolean isPointInRect(int x,int y, int x1,int y1,int x2,int y2) {
		int xx1=Math.min(x1,x2);
		int xx2=Math.max(x1,x2);
		int yy1=Math.min(y1,y2);
		int yy2=Math.max(y1,y2);				
		return ( x>=xx1 && x<=xx2 && y>=yy1 && y<=yy2 );		
	}
	
	/*
	double m=(double)(y2-y1)/(x2-x1);
	double a=y-y1;
	double b=m*(x-x1);
	double eps=0.05;
	return Math.abs(a-b);
	//return (Math.abs(a-b) < eps);		
	*/	
	private static double pointLineDist(int x, int y, int x1, int y1, int x2, int y2) {	
		double px=x,py=y;
		double ax=x1,ay=y1;		
		double nx=x2-x1;
		double ny=y2-y1;
		double d=java.lang.Math.sqrt(nx*nx+ny*ny);
		nx=nx/d; 
		ny=ny/d;			// n 		
			
		double vx=(ax-px);	//  (a-p)  
		double vy=(ay-py);		
		d = vx*nx + vy*ny;	
		
		vx = (ax-px)-nx*d;	//(a-p) - (a-p) . n  vector
		vy = (ay-py)-ny*d;

//		d=java.lang.Math.sqrt(vx*vx+vy*vy);
		d=(vx*vx+vy*vy);
		return d;
	}
		
	public static void init( Pixmap pix, int lines ) {
		pixmap = pix;
		xSize = pix.xSize;
		ySize = pix.ySize;
		pixmap.clear();
		
		numLines=lines;
		if(numLines > MAX_LINES) numLines = MAX_LINES;		
	}
	
	public Enemy( Color color) {
		this(color, (int)( 3 * Math.random() )) ;
	}
	
	public Enemy( Color color, int sp) {
		this.color = color;
		this.first = 0;
		this.speed = sp;
		reset();		
	}
	
	public void reset() {
		
		speedt = 0;
	
		x1 = new int[numLines];
		y1 = new int[numLines];
		x2 = new int[numLines];
		y2 = new int[numLines];
 
		/* initialise the first line */
		
		x1[0] = xSize/2 - (int)(Math.random()*10);
		y1[0] = ySize/2 + (int)(Math.random()*10);
		x2[0] = xSize/2 - (int)(Math.random()*10);
		y2[0] = ySize/2 + (int)(Math.random()*10);
		
		/* initialise all the other lines */
	   for ( int i = 1; i < numLines; i++ ) {
			x1[i] = x1[0];
			y1[i] = y1[0];
			x2[i] = x2[0];
			y2[i] = y2[0];
		}
		
		dx1 =  (int)( 3 * Math.random() ) + 2;
		dx2 = -(int)( 3 * Math.random() ) - 2;
		dy1 =  (int)( 3 * Math.random() ) + 2;		
		dy2 = -(int)( 3 * Math.random() ) - 2;
		
	}
	
	/* render to graphics */
	public void render( Graphics g, boolean help ) {
		for(int i=0; i<numLines; i++) {	
			Color col=(i==first ? Color.YELLOW : color); 
			g.setColor( col );
			g.drawLine( x1[i],y1[i],x2[i],y2[i] );
		}	
		// DEBUG
		if(help) RenderInfo2( g );	
		if(flagDisplayInfo==true) {			
			int i=flagDisplayInfoI;
			int x0=Math.min(x1[i],x2[i]);
			int y0=Math.min(y1[i],y2[i]);
			int width =x1[i] > x2[i] ? x1[i]-x2[i] : x2[i]-x1[i];
			int height=y1[i] > y2[i] ? y1[i]-y2[i] : y2[i]-y1[i];
			g.setColor( Color.GREEN );
			g.drawRect(x0,y0,width,height);
			flagDisplayInfo = false;
		}	
	}
		
	/* render to pixmap */
	public void render() {				
		for(int i=0; i<numLines; i++) {	
			Color col=(i==first ? Color.YELLOW : color); 
			renderLine( x1[i],y1[i],x2[i],y2[i], Pixmap.getIntFromColor(col.getRed(),col.getGreen(),col.getBlue()) );
		}		
	}
	
	public void speedUp() {
		if(speed>0) speed--; speedt=0;
	}
	
	private boolean pxf(int x,int y) {
		return pixmap.getPixel(x,y)!=Pixmap.white;
	}
	
	private boolean pxg(int x,int y) {		
		return pixmap.getPixel(x,y)!=Pixmap.black;
	}
	
	public void update() {
				
		if(speedt++ < speed) 
			return; 
		else speedt = 0;
		
		// update the first line 
		
		int line = first;
		
		--first; if(first<0) first=numLines-1;
		
		x1[first]=x1[line];
		x2[first]=x2[line];
		y1[first]=y1[line];
		y2[first]=y2[line];
		
		if(x1[first] + dx1 < xSize-Player.PlayerSize && pxf(x1[first] + dx1,y1[first])) x1[first] += dx1; else dx1= -(int)( 3 * Math.random() ) - 2;
		if(x1[first] + dx1 >= Player.PlayerSize && pxf(x1[first] + dx1,y1[first])) x1[first] += dx1; else dx1=  (int)( 3 * Math.random() ) + 2;
		if(y1[first] + dy1 < ySize-Player.PlayerSize && pxf(x1[first],y1[first] + dy1)) y1[first] += dy1; else dy1= -(int)( 3 * Math.random() ) - 2;
		if(y1[first] + dy1 >= Player.PlayerSize && pxf(x1[first],y1[first] + dy1)) y1[first] += dy1; else dy1=  (int)( 3 * Math.random() ) + 2;
		if(x2[first] + dx2 < xSize-Player.PlayerSize && pxf(x2[first] + dx2,y2[first])) x2[first] += dx2; else dx2= -(int)( 3 * Math.random() ) - 2;
		if(x2[first] + dx2 >= Player.PlayerSize && pxf(x2[first] + dx2,y2[first])) x2[first] += dx2; else dx2=  (int)( 3 * Math.random() ) + 2;
		if(y2[first] + dy2 < ySize-Player.PlayerSize && pxf(x2[first],y2[first] + dy2)) y2[first] += dy2; else dy2= -(int)( 3 * Math.random() ) - 2;
		if(y2[first] + dy2 >= Player.PlayerSize && pxf(x2[first],y2[first] + dy2)) y2[first] += dy2; else dy2=  (int)( 3 * Math.random() ) + 2;
		
		Point p=lineCheckP(x1[first],y1[first],x2[first],y2[first], Pixmap.white);
		if( p!=null ) {			
			dx1 = -dx1;
			dx2 = -dx2;
			dy1 = -dy1;
			dy2 = -dy2;
			//pixmap.plot(p.x-p.x%Player.PlayerSize, p.y-p.y%Player.PlayerSize, Color.BLACK);
		}
	}
	
	public boolean died() {
		 return pxg(x1[first],y1[first]) &&  pxg(x2[first],y2[first]);
	}
	
	public boolean hit(Player p) {		
		for(int i=0; i<numLines; i++) {
			if( lineCheck(x1[i],y1[i],x2[i],y2[i], Pixmap.green) ) 
				return true;
			
			if(Enemy.isPointInRect(p.x,p.y, x1[i],y1[i],x2[i],y2[i])) {				
				Enemy.flagDisplayInfoI= i;
				if(pointLineDist(p.x,p.y, x1[i],y1[i],x2[i],y2[i]) < 1.0) 				
					return true;
			}
		}
		return false;
	}
	
	/* read from pixmap  */
	private Point lineCheckP(int x,int y,int x2, int y2, int needColor) {
		int col;
		int w = x2 - x ;
		int h = y2 - y ;
		int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0 ;
		if (w<0) dx1 = -1 ; else if (w>0) dx1 = 1 ;
		if (h<0) dy1 = -1 ; else if (h>0) dy1 = 1 ;
		if (w<0) dx2 = -1 ; else if (w>0) dx2 = 1 ;
		int longest = Math.abs(w) ;
		int shortest = Math.abs(h) ;
		if (!(longest>shortest)) {
			longest = Math.abs(h) ;
			shortest = Math.abs(w) ;
			if (h<0) dy2 = -1 ; else if (h>0) dy2 = 1 ;
			dx2 = 0 ;            
		}
		int numerator = longest >> 1 ;
		for (int i=0;i<=longest;i++) {
			col = pixmap.getPixel(x, y);
			if(col==needColor) 
				return new Point(x,y);
			numerator += shortest ;
			if (!(numerator<longest)) {
				numerator -= longest ;
				x += dx1 ;
				y += dy1 ;
			} else {
				x += dx2 ;
				y += dy2 ;
			}
		}
		return null; 
	}
	
	// check for a color in line
	private boolean lineCheck(int x,int y,int x2, int y2, int needColor) {
		return lineCheckP(x,y,x2,y2,needColor)!=null;
	}
	
	/* render to pixmap - line */ 
	public void renderLine( int x,int y,int x2, int y2, int col) {		
		int w = x2 - x ;
		int h = y2 - y ;
		int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0 ;
		if (w<0) dx1 = -1 ; else if (w>0) dx1 = 1 ;
		if (h<0) dy1 = -1 ; else if (h>0) dy1 = 1 ;
		if (w<0) dx2 = -1 ; else if (w>0) dx2 = 1 ;
		int longest = Math.abs(w) ;
		int shortest = Math.abs(h) ;
		if (!(longest>shortest)) {
			longest = Math.abs(h) ;
			shortest = Math.abs(w) ;
			if (h<0) dy2 = -1 ; else if (h>0) dy2 = 1 ;
			dx2 = 0 ;            
		}
		int numerator = longest >> 1 ;
		for (int i=0;i<=longest;i++) {
			pixmap.setPixel(x, y, col);			
			numerator += shortest ;
			if (!(numerator<longest)) {
				numerator -= longest ;
				x += dx1 ;
				y += dy1 ;
			} else {
				x += dx2 ;
				y += dy2 ;
			}
		}
	}
	
	public void RenderInfo(Graphics g) {
		int i=first;
		String msg="dx1="+dx1+" dy1="+dy1;
		g.setColor(Color.GREEN);
		g.drawString(msg,x1[i],y1[i]); msg="dx2="+dx2+" dy2="+dy2;
		g.drawString(msg,x2[i],y2[i]); 
	}

	public void RenderInfo2(Graphics g) {
		int i=first;
		String msg="x1="+x1[first]+" y1="+y1[first];
		g.setColor(Color.GREEN);
		g.drawString(msg,x1[i],y1[i]); msg="x2="+x2[first]+" y2="+y2[first];
		g.drawString(msg,x2[i],y2[i]); 
	}	
}