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

public class Player {
	// const	
	public final static int PlayerSize = 10;	// in pixels
	public final static int DIR_UP=0;			// moving directions
	public final static int DIR_RIGHT=1;
	public final static int DIR_DOWN=2;
	public final static int DIR_LEFT=3;
	
	private static Pixmap pixmap;
	
	private Color color;	
	private int dx, dy;
	private int px, py;			// elozo pozicio
	public int x, y;
	public int previrany, irany; 	// pozicio, indulo irany
	
	public Player(Pixmap pixmap, Color color) {
		
		this.pixmap = pixmap;
		this.color = color;
		
		reset();
	}
	
	public void reset() {
		
		// Az alapvonalrol indul
		this.x = pixmap.xSize / PlayerSize / 2 * PlayerSize;
		this.y = pixmap.ySize - PlayerSize;
		
		this.px=this.py-1;		
		this.dx=this.dy=0;
		this.irany=this.previrany=-1;
	}
	
	private void move(int i, int j) {
		dx=i*PlayerSize;
		dy=j*PlayerSize;
	}
	
	public void mov(int i) {
		previrany=irany;
		irany=i;
		switch(irany) {
			case DIR_UP: move(0,-1); break;
			case DIR_RIGHT:	move(+1,0); break;
			case DIR_DOWN: move(0,+1); break;					
			case DIR_LEFT: move(-1,0); break;
		}
	}
	
	public boolean visszafele() {		
		switch(irany) {
			case DIR_UP: 	if(previrany==DIR_DOWN) return true; break;
			case DIR_RIGHT: if(previrany==DIR_LEFT) return true; break;
			case DIR_DOWN: 	if(previrany==DIR_UP) 	return true; break;
			case DIR_LEFT: 	if(previrany==DIR_RIGHT) return true; break;
		}
		return false;
	}
	
	public void stop() {
		x=px;
		y=py;
		dx=dy=0;
	}

	// elmozdul a jatekos az uj helyre, ha kiment akkor megall
	// Player collision with special pixels
	public int update(){
		
		px = x; 
		py = y;

		x += dx;
		y += dy;
		
		// check bounds
		if(x<0)	{x=0;dx=0;}
		if(y<0) {y=0;dy=0;}
		if(x>pixmap.xSize-PlayerSize) {x=pixmap.xSize-PlayerSize;dx=0;}	
		if(y>pixmap.ySize-PlayerSize) {y=pixmap.ySize-PlayerSize;dy=0;}
		
		return pixmap.getPixel(x,y);
	}

	// render player to front and back buffers
	public void render(Graphics g, boolean vonal, boolean help){
		g.setColor(color);
		g.fillRect(x,y,PlayerSize,PlayerSize);
		if(vonal) pixmap.rect(x,y,PlayerSize,PlayerSize, Color.GREEN);		
		if(help) g.drawString("x="+x+" y="+y, x,y);
	}	

}