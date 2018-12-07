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

import java.io.*;
import java.util.Vector;
import java.util.Scanner;
import java.awt.*;
import java.awt.Color;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.JComponent;
import javax.swing.border.*;

class BouncingLines extends JPanel {

	// Constants
	static final String HiscoreFileName = "hiscore.txt";		
	static final String APP_TITLE = "Flash v0.4";			
	static final Font myFont = new Font ("Courier New", 1, 40);		
	static final int m_interval  = 35;  // Milliseconds between updates
	
	static final int WinPercentInit = 50;
	static final int WinPercentStep = 10;
	static final int WinPercentMax = 90;	
	
	// Variables	
	int xSize, ySize;	   // Main panel size		
	
	// flags
	boolean GAME_OVER,GAME_WIN;
	boolean help = false;
	boolean pause = false;
	boolean test = false;	
	boolean vonalHuzas, flagVonalGomb, flagCtrlGomb;	
	int vonalHuzasMode;	// lassu vagy gyors
	
	private int vx0,vy0,vx1,vy1, vir;	// Start and end points -- vonal bezaras, kezdo irany 
	
	// Buffers
	Pixmap bgenemy;	// enemies layer		
	Pixmap pixmap;	// player path layer	
	Player player;
	Vector <Enemy> enemies;
	
	// Game display text 
	String msgx="";
	int msgxx,msgxy,msgxc;	//  to fade;
		
	// score
	int m_time,time;		// game time in ms and seconds 
	int level,winPercent;	
	int hiscore=-1, score, totalFilled,TotalPlaces;
	int orange = Pixmap.getIntFromColor(Color.ORANGE);
	
	public void Menu_InitSettings(JFrame jf, int width, int height) {

		xSize = width;
		ySize = height;
		
		this.setPreferredSize( new Dimension(width,height));	
		jf.setSize(xSize,ySize);		
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
		jf.setLayout(new BorderLayout());
		jf.add(this,BorderLayout.CENTER);		
		jf.pack();
		jf.setVisible(true);				
	}
	
	public BouncingLines(JFrame jf, int width,int height) {
		super();

		Menu_InitSettings(jf, width,height); 
						
		ResetGame();
				
		// window resize handler
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent evt) {
				Component c = (Component)evt.getSource();
				Dimension newSize = c.getSize();
				//xSize = newSize.width;
				//ySize = newSize.height;
				c.repaint();		
			}
		});
		
		// handle keyboard
		jf.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode()) {				
				case KeyEvent.VK_SHIFT:	flagVonalGomb = true; break;
				case KeyEvent.VK_CONTROL:flagCtrlGomb  = true; break;			
				case KeyEvent.VK_H:  	help = !help; Enemy.flagDisplayInfo = !Enemy.flagDisplayInfo; break;
				case KeyEvent.VK_P: 	pause = !pause; break;
				case KeyEvent.VK_T: 	test = !test; break;									
				case KeyEvent.VK_S: 	pixmap.save("Pixmap.jpg"); break;
				case KeyEvent.VK_UP:	player.mov(Player.DIR_UP); break;
				case KeyEvent.VK_RIGHT: player.mov(Player.DIR_RIGHT); break;	
				case KeyEvent.VK_DOWN:	player.mov(Player.DIR_DOWN); break;
				case KeyEvent.VK_LEFT:	player.mov(Player.DIR_LEFT); break;				
				case KeyEvent.VK_ENTER:	if(GAME_OVER && GAME_WIN) NextGame(); else ResetGame(); break;
				case KeyEvent.VK_ESCAPE: System.exit(0); break;
				}	
			}
			@Override
			public void keyReleased(KeyEvent e) {
				switch(e.getKeyCode()) {
				case KeyEvent.VK_SHIFT:		flagVonalGomb = false; break;
				case KeyEvent.VK_CONTROL:	flagCtrlGomb = false; break;					
				}
			}
			@Override
			public void keyTyped(KeyEvent e) {}
		});

		ActionListener counter = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				repaint();
		}};
		new Timer(m_interval, counter).start();
	}
	
	public void FreeObjects() {		
		bgenemy= null;
		pixmap = null;
		player = null;
		if(enemies!=null) {
			enemies.clear();
			enemies = null;
		}
	}
	
	private void newline() {
		flagVonalGomb=false;
		flagCtrlGomb=false;		
		vonalHuzas=false;
		vonalHuzasMode=0;
		vx0=-1;
		vy0=-1;
	}
	
	public void speedupEnemies() {		
		for(int i=0; i<enemies.size(); i++) 
			enemies.get(i).speedUp();
	}
	
	public void ResetGame() {
		
		FreeObjects();		
		
		// jatek ujra indul 				
		level = 1;
		winPercent = WinPercentInit;
		TotalPlaces = xSize/Player.PlayerSize * ySize/Player.PlayerSize;
				
		GAME_WIN = false;
		GAME_OVER = false;
		
		msgx="";	
		time = m_time= 0;
				
		hiscore = getHiscore();		
		score = 0;
		totalFilled = 0;		

		// vonalhuzas init
		newline();	
				
//		Create game objects
//		bgenemy = new Pixmap(xSize,ySize);		
		pixmap = new Pixmap(xSize,ySize);			
		player = new Player(pixmap, Color.BLUE);		
		
//		Create enemies
		Enemy.init(pixmap,15);			// Bitmap to render enemies
		enemies = new Vector<Enemy>();
		enemies.add( new Enemy( Enemy.RandomColorNotBlue(),10 ) );
	
		pixmap.drawFrame( Color.WHITE );
	}
	
	public void NextGame() {
		
		// kovetkezo szint
		GAME_WIN = false;
		GAME_OVER = false;		
		
		msgx="";				
		time = m_time= 0;
		
		totalFilled = 0;		
						
		// vonalhuzas init
		newline();
			
		level++;
		
		winPercent += WinPercentStep;
		if(winPercent > WinPercentMax)
			winPercent = WinPercentMax;		
		
		player.reset();
		for(int i=0;i<enemies.size();i++)
			enemies.get(i).reset();
		
		enemies.add( new Enemy( Enemy.RandomColorNotBlue(),10 ) );
		speedupEnemies();
		
		pixmap.clear();
		pixmap.drawFrame( Color.WHITE );		

	}
	
	public void UpdateAll() {	
		
		/* Update timer */
		m_time += m_interval;
		if(m_time > 1000) {
			m_time = 0;
			time++;
		}
			
		/* Check Game Win */		
		if((int)(100.0* totalFilled / TotalPlaces) > winPercent || enemies.size()==0){
			GAME_WIN = true;
			GAME_OVER = true;
			return;
		}
				
		if((time%10)==0) {
			speedupEnemies();
		}
		
		int color = player.update();
		if (color==orange) 
			 player.stop();
		else if(color==Pixmap.green) { // belement a sajat huzott vonalba 
			// kiveve ha visszafele ment bele...
			if(player.visszafele()) {
				speedupEnemies();
				pixmap.delcol( Pixmap.green );	// le kell torolni az eddigieket
				newline(); // vonal new start
			} else {
				GAME_OVER = true;			
				return;
			}
		} 
		
		/* update enemies, check collision */
		boolean safe = pixmap.alapvonal(player.x,player.y);			
	  	for(int i=0; i<enemies.size(); i++) {
			Enemy enemy=enemies.get(i);
			enemy.update();
			if(enemy.died()) {
				enemies.remove(i);
				return;				
			} else if( !safe && enemies.get(i).hit(player) ) {
				GAME_OVER=true;
				return;				
			}
		}
	}
	
	public void paintComponent(Graphics g) {
		String msg;
		
		g.setColor(Color.GREEN);
		g.setFont( myFont );
		
		if(GAME_OVER) GameOver(g); else 						
		if(pause) Pause(g); else { 
		//if(test)  Test(g); else 
			
			UpdateAll();

			if(vonalHuzas && vonalHuzasMode>0) {
				// vonal elengedese 				
				if(vonalHuzasMode==1) {
					if(flagVonalGomb==false) {
						pixmap.delcol( Pixmap.green );	// le kell torolni az eddigieket
						newline(); // vonal new start
					}
				} 
				else if(vonalHuzasMode==2) {						
					if(flagCtrlGomb==false) {
					 	pixmap.delcol( Pixmap.green );	// le kell torolni az eddigieket
						newline(); // vonal new start
					}
				}
				// vonal bezarasa ..					
			}
			
			/*
			backgr.clear();
			backgr.merge(pixmap);
			backgr.merge(bgenemy);
			backgr.copyTo( g );				
			bgenemy.clear();
			*/
			pixmap.copyTo(g);
			
			// render actors				
			if(pixmap.alapvonal(player.x,player.y)) {				
				
				if(vonalHuzas && (vx0>=0 && vy0>=0)) {
					
						//vonal bezarasa ..
												
						vx1=player.x;
						vy1=player.y;
						
						if(vx1!=vx0 || vy1!=vy0){ // volt terulet
														
							// Az eddigi zold vonal a bezart terulet resze lesz
							int count=pixmap.overcol(Pixmap.green, Pixmap.white); 
							count +=pixmap.filler( vir, vx0,vy0, vx1,vy1 ,help);
							
							// Start message display
							msgx="POINTS +"+count;
							msgxc= 2 * m_interval;
							msgxx=xSize/3;
							msgxy=ySize/3;
							
							totalFilled += count;
							
							// lassu vagy gyors gombbal tortent?
							if(vonalHuzasMode==1)
								score += totalFilled;
							else if (vonalHuzasMode==2)
								score += totalFilled*2;
							
							newline(); // new start							
						}
						
				} 
				
				// vonalhuzas kezdes lehetoseg 2017.01.24
				vonalHuzas=(flagVonalGomb || flagCtrlGomb); 
				if(flagVonalGomb) vonalHuzasMode = 1; else 
				if(flagCtrlGomb)  vonalHuzasMode = 2; else	
								  vonalHuzasMode = 0;
				player.render( g, false, help );			
				
			} else { /* a jatekos nincs az alapvonalon */				
				
				// vonalhuzas indulo pozicio 	
				if(vonalHuzas && (vx0<0||vy0<0)) { // eloszor.. elhagyta az alapvonalat						
					vx0=player.x;
					vy0=player.y;
					vir=player.irany;		// indulo irany					
				}
				player.render( g, vonalHuzas, help );								
			}

			// Render enemies
			for(int i=0; i<enemies.size(); i++) 
				enemies.get(i).render( g, help );
			
			// Text Display 
			int x=20,y=4*Player.PlayerSize, lH=y;
			g.drawString("TIMER "+time,x,y);	y+=lH;
			g.drawString("LEVEL "+level,x,y);	y+=lH;
			g.drawString("SCORE "+score,x,y);	y+=lH;
			g.drawString("HIGHS "+hiscore,x,y);	y+=lH;
/*			g.drawString("FILLT "+totalFilled,x,y);
			if(flagVonalGomb) {g.drawString("SHIFT",x,y);	y+=lH;}
			if(flagCtrlGomb) {g.drawString("CTRL",x,y);	y+=lH;}
			if(vonalHuzas) {g.drawString("VONAL",x,y);	y+=lH;}*/						
			if(msgxc-->=0) g.drawString(msgx,msgxx++,msgxy++); else msgx="";			
		}				
	}
	
	void GameOver(Graphics g) {		
		String msg1,msg2="SCORE "+score;	
		if(GAME_WIN) msg1="YOU WIN"; else msg1="GAME OVER";
		
		if(hiscore < 0) 
			hiscore=getHiscore();
		
		if(score > hiscore) {
			hiscore = score;
			putHiscore(hiscore);
			msg2="*** NEW HIGH SCORE "+hiscore+" ***";
		}
			
		for(int j=0; j<50; j++) {				
			g.setColor( Enemy.RandomColor() );
			g.drawRect( j, j, xSize-2*j, ySize-2*j );
			// text 
			g.drawString("*** "+msg1+" ***",xSize/3,ySize/2); 
			g.drawString(msg2,xSize/3,ySize/2+ Player.PlayerSize*8); 
			g.drawString("ENTER = NEW GAME",xSize/3,ySize/2 + Player.PlayerSize*12);
			g.drawString("EXIT  = ESC",xSize/3,ySize/2 + Player.PlayerSize*16);
		}		
	}	
	
	void Pause(Graphics g) {	
		int xx=0,yy=0;
		int n=TotalPlaces * winPercent / 100;
		
		g.drawString("PAUSE - P",xSize/2,ySize/2);		
		
		// fill winpercent of table
		g.setColor( Color.BLUE );
		g.drawString("PIXELS="+n,xSize/2,ySize-Player.PlayerSize);
			
		while(n-->0) {
			g.drawRect( xx, yy, Player.PlayerSize, Player.PlayerSize );
			xx += Player.PlayerSize;
			if(xx > xSize) {
				yy += Player.PlayerSize;
				xx=0;						
			}
		}
		
		// number of filled
		n=totalFilled;
		g.setColor( Color.YELLOW );
		g.drawString("FILLED="+n,xSize/3,ySize-Player.PlayerSize);
		
		xx=yy=0;				
		while(n-->0) {
			g.drawRect( xx, yy, Player.PlayerSize, Player.PlayerSize );
			xx += Player.PlayerSize;
			if(xx > xSize) {						
				yy += Player.PlayerSize;
				xx=0;
			}
		}								
	}
	
	/* high score */
	
	int getHiscore() {
		int theScore=-1;
		try {
			Scanner scanner = new Scanner(new File(HiscoreFileName));
			theScore = scanner.nextInt();
		} catch(FileNotFoundException e) {
			System.out.println("ERROR HISCORE FILE MISSING "+HiscoreFileName);
			System.exit(0);
		}
		return theScore;
	}
	
	boolean putHiscore(int theScore) {
		try {
			FileWriter wr = new FileWriter(HiscoreFileName);
			wr.write(theScore + "");
			wr.close();				
		} catch(IOException e) {
			System.out.println("ERROR WRITING HISCORE TO "+HiscoreFileName);
			return false;
		}
		return true;
	}
	
	public static void main(String[] args) {			
		int screenWidth=-1, screenHeight=-1;
		if(args.length > 1) {
			System.out.println("Args: "+args.length);
			try {
				screenWidth  = Integer.parseInt( args[0] );
				screenHeight = Integer.parseInt( args[1] );				
			}
			catch (NumberFormatException e) {
				screenWidth  = 800;
				screenHeight = 500;
			}
		}
		
		JFrame frame = new JFrame(APP_TITLE);		
		Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
		if(screenWidth < 1 || screenHeight < 1) {
			screenWidth=screenSize.width/2;
			screenHeight=screenSize.height/2;
		}
		System.out.println("Screen: "+screenWidth+" x "+screenHeight);
		if((screenWidth % Player.PlayerSize)!=0 || (screenHeight % Player.PlayerSize)!=0) {
			System.out.println("Screen size error");
			System.exit(-1);
		}
		
		// Local variables refered from inner class must be final or effectively final
		final int ScreenWidth=screenWidth;
		final int ScreenHeight=screenHeight;
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new BouncingLines(frame, ScreenWidth,ScreenHeight);
			}
		});	
	}
}