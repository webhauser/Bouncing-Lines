# Bouncing-Lines
This project is a Java arcade game, where there is a player and enemies on a territory. The aim of the game is enclose more than the half area of the territory while avoiding the enemy contact. The game uses keyboard to control the player. The player moves with the Cursor keys and starts enclosing an area with a line using the Shift key. As the level is completed more enemies come. Try to beat the high score!

<img src="./Screenshot.jpg" Alt="Bouncing Lines, Java arcade game by webhauser.com" />

## How to compile?

```bash
git clone https://github.com/webhauser/Bouncing-Lines.git
cd Bouncing-Lines
javac BouncingLines.java Player.java Enemy.java Pixmap.java
```

## How to run?

```bash
java BouncingLines 800 400
```

The command above opens a 800x400 sized game window on the screen. 