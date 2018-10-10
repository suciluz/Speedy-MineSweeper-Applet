import java.awt.*;
import java.applet.*;
import javax.swing.*;
/*Welcome to MineSweeper!
 *You need to click on a tile in 5 seconds
 *Find all the mines before you step on a mine to win the game!
 */

public class MSV4 extends Applet implements Runnable {

	//Needed for multi-threading and animations that use Thread.sleep.
	Thread myThread=null;

	//Different fonts.
	Font basic = new Font("SansSerif", Font.BOLD, 25);
	Font title = new Font("Century", Font.BOLD, 30);
	Font description = new Font("Courier", Font.BOLD, 20);
	Font mineFont = new Font("Courier", Font.BOLD, 60);

	//Creation of 2D array for the minefield.
	int[][] mines = new int[10][10];

	//Declaring various variables.
	Image offScreen;
	Graphics offG;
	Image background, headerBackground, startMenu, startButton, restartButton, mine, mine2, mine3, mine4, victory, gameOverPic, timeUp;
	AudioClip backgroundMusic, explosion, victorySound, gameOver, startMenuMusic, tileClick, standingOvation;

	int numMines;	//Number of mines.
	int x1, y1, buttonWidth, buttonHeight;	//These are for the start button.
	int count;	//This count is used for counting the number of tiles flipped so that player can win the game.
	int timer;	//This is the timer.
	int i;		//Used for modulus delaying.
	int game;	//For differentiating the screens.
	int adjMines; //This is for counting number of adjacent mines around a tile.
	boolean once;	//Making sure the start menu runs only once.
	boolean once2;	//Similar function.
	String input;	//Input for the number of mines.

    public void init() {
    	//Using fakescreen.
    	offScreen = createImage(700,800);
		offG = offScreen.getGraphics();

		//Getting pictures & music
		background = getImage(getCodeBase(), "rsz_gold.jpg");
		headerBackground = getImage(getCodeBase(), "headerBackground.jpg");
		startMenu = getImage(getCodeBase(), "startMenu.jpg");
		startButton = getImage(getCodeBase(), "startButton.gif");
		restartButton = getImage(getCodeBase(), "restart.png");
		mine = getImage(getCodeBase(), "mineicon.png");
		mine2 = getImage(getCodeBase(), "mine2.png");
		mine3 = getImage(getCodeBase(), "mine3.png");
		mine4 = getImage(getCodeBase(), "mine4.png");
		victory = getImage(getCodeBase(), "victory.jpg");
		gameOverPic = getImage(getCodeBase(), "gameOver.jpg");
		timeUp = getImage(getCodeBase(), "timeUp.jpg");
		backgroundMusic = getAudioClip(getCodeBase(), "backgroundMusic.wav");
		explosion = getAudioClip(getCodeBase(), "explosion.wav");
		victorySound = getAudioClip(getCodeBase(), "victory.wav");
		gameOver = getAudioClip(getCodeBase(), "gameOver.wav");
		startMenuMusic = getAudioClip(getCodeBase(), "startMenuMusic.wav");
		tileClick = getAudioClip(getCodeBase(), "tileClick.wav");
		standingOvation = getAudioClip(getCodeBase(), "standingOvation.wav");

		//Tracking pictures.
		MediaTracker tracker = new MediaTracker(this);
		tracker.addImage(background, 0);
		tracker.addImage(headerBackground, 0);
		tracker.addImage(startMenu, 0);
		tracker.addImage(startButton, 0);
		tracker.addImage(restartButton, 0);
		tracker.addImage(mine, 0);
		tracker.addImage(mine2, 0);
		tracker.addImage(mine3, 0);
		tracker.addImage(mine4, 0);
		tracker.addImage(victory, 0);
		tracker.addImage(gameOverPic, 0);
		tracker.addImage(timeUp, 0);

		while(tracker.checkAll(true) != true){}
		if (tracker.isErrorAny()){
			JOptionPane.showMessageDialog(null, "Trouble loading pictures.");
		}

		//Set cursor to a pickaxe cursor.
		setCursor(Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon("mineCursor.png").getImage(),new Point(0,0), "custom cursor"));

		//Initializations for the variables.
		x1 = 0;
		y1 = 0;
		buttonWidth = startButton.getWidth(this);
		buttonHeight = startButton.getHeight(this);
		timer = 5;
		i = 0;
		game = 0;
		adjMines = 0;
		once = true;
		once2 = true;
    }

    //Called by the web browser before the init method and is used to start a multi-threaded applet.
	public void start() {
		if (myThread==null) {
			myThread = new Thread(this);
			myThread.start();
		}
	}

	//Needed in multi-threaded Applets to stop the animation thread.
	public void stop() {
		if (myThread != null) {
			myThread = null;
		}
	}

	/*Needed to prevent computer from automatically clearing the entire screen everytime the paint method is called.
	 *This prevents the	screen from flashing.*/
    public void update(Graphics g) {
		paint(g);
	}

	//Copy the fake screen to the real screen.
	public void paint(Graphics g) {
    	g.drawImage(offScreen,0,0,this);
    }

	//This is where the game runs.
    public void run() {
		while(true) {
			//This while loop runs until timer becomes 0.
			while (timer>=0) {
				if (game == 0 && once) {
					drawStartMenu();
					startMenuMusic.play();
					once = false;	//This ensures that the start menu only gets drawn once. Without this, the screen flashes.
				}
				delay(1);

				//This part draws the timer on top of the minefield. A combination of modulus and delay counts five seconds.
				if(game == 1) {
					i++;
					if (i % 20 == 0) {
						offG.setFont(basic);
						offG.drawImage(headerBackground, 0, 0, this);
						offG.setColor(Color.black);
						offG.drawString("Speedy MineSweeper!!!", 200, 35);
						offG.drawString("# of Safe Tiles Left: "+(100-numMines-count), 60, 70);	//Shows the number of safe tiles left to flip.
						offG.drawString("/", 348, 70);
						offG.drawString("Remaining Time: "+timer, 385, 70); //Shows the remaining time.
						timer--;
						repaint();
					}
					delay(50);
				}
			}
			//In this case, the player has lost the game because the timer has gone below zero.
			gameLost(0);
		}
    }

	//This is for interaction with the mouse.
   	public boolean mouseDown(Event evt, int x, int y) {
		offG.setFont(mineFont);
   		if(x>200 && x<200+buttonWidth && y>600 && y<600+buttonHeight && game==0) {	//When the start button is clicked, user is required to input number of mines.
   			input = JOptionPane.showInputDialog("Enter the number of mines you want to deploy!");
    		numMines = Integer.parseInt(input);
    		while(numMines<1 || numMines>99) {
    			input = JOptionPane.showInputDialog("Invalid number of mines. Enter a number between 0 and 100!");
    			numMines = Integer.parseInt(input);
    		}
    		populateMines(mines,numMines);
    		JOptionPane.showMessageDialog(null, numMines+" mines detected in the field!!!");
			game = 1;	//Sets the game to main mode.
			startMenuMusic.stop();
			backgroundMusic.loop();
			drawMineField();
   		}
		else if(game==1) {	//Main game starts.
			if(x>0 && x<700 && y>100 && y<800) {	//If the user clicks inside the mine field.

				//Used mathematical relationships between the tile pixels and 2D array indexes to prevent the creation of innumerous if statements.
				if(mines[x/70][(y-100)/70]==-1) {
					offG.drawImage(mine,(x/70)*70, (100+((y-100)/70)*70), this);
					repaint();
					explosion.play();
					gameLost(1);	//Loses if steps on a mine.

				} else if(mines[x/70][(y-100)/70]==0) {
					adjMines = adjacentMines(mines, x/70, (y-100)/70);	//Gets the adjacent number of mines around the tile flipped.
					offG.drawString(""+adjMines, (x/70)*70+17, 100+((y-100)/70+1)*70-14);
					repaint();
					tileClick.play();
					mines[x/70][(y-100)/70] = 1;	//Changes the array indexes of flipped tiles from 0 to 1 so that it can be counted for checking the number of remaining tiles left.
					timer=5;	//Resets the time to 5 seconds.
				}
			}
   		}
   		else if(game==2) {	//Restarts the game.
   			if(x>200 && x<200+buttonWidth && y>600 && y<600+buttonHeight) {	//Restarts if the restart button is clicked.
   				game = 0;
   				once = true;
   				once2 = true;
   				timer = 5;
				i = 0;
				clearMines();
				standingOvation.stop();
   			}
   		}
		if(tilesFlipped()==100-numMines) {
			gameWon(); //Wins if all the safe tiles are fipped.
		}

   		return true;
   	}

	//Delays the screen.
	public void delay(int milliSecs) {
		try {
			myThread.sleep(milliSecs);
		} catch(Exception e) {}
	}

	//This method populates the empty 2D arrays. 0 will be safe tiles, -1 will be mines.
	public void populateMines( int[][] mines, int n )  {
		for ( int i=0 ; i<n; i++) {	//For loop runs until n so that n number of mines can be created.
			int row = (int) (mines.length*Math.random());	//Uses the fact that Math.random() produces a value between 0 and 1 and multiplies it by the length, which is 10, to get a random index.
			int col = (int) (mines[0].length*Math.random());	//Same logic here.
			if (mines[row][col] == 0) {
				mines[row][col] = -1;
			} else {	//This condition is to prevent the same index being assigned a mine twice.
				i--;
			}
		}
	}

	//This method returns the number of adjacent mines around the tile.
	public int adjacentMines(int[][]mines, int r, int c) {
		int adjacentMines=0;
		for(int i=r-1; i<=r+1; i++) {	//These for loops traverse the indexes around a given index to calculate the number of mines around it.
			for(int j=c-1; j<=c+1; j++) {
				if (i>=0 && i<10 && j>=0 && j<10) {	//This condition is to prevent array index out of bounds error when checking for indexes that are at the edges.
					if(mines[i][j]==-1) {
						adjacentMines++;
					}
				}
			}
		}

		return adjacentMines;
	}

	//Checks the number of safe tiles left to flip.
	public int tilesFlipped () {
		count=0;
		for(int i=0; i<mines.length; i++) {
			for(int j=0; j<mines[i].length; j++) {
				if(mines[i][j]==1) {
					count++;
				}
			}
		}
		return count;
	}

	//This method resets the minefield for game restart
	public void clearMines() {
		for(int i=0; i<mines.length; i++) {
			for(int j=0; j<mines[i].length; j++) {
				mines[i][j]=0;
			}
		}
	}

	//This method draws the start menu.
	public void drawStartMenu() {
		offG.drawImage(startMenu, 0, 0, this);
		offG.setColor(Color.black);
		offG.drawImage(startButton, 197, 600, this);
		offG.drawImage(mine2, 240, 40, this);
		offG.drawImage(mine3, 325, 40, this);
		offG.drawImage(mine4, 400, 40, this);
		offG.setFont(title);
		offG.drawString("Welcome to", 262, 300);
		offG.drawString("Speedy MineSweeper!", 184, 340);
		offG.setFont(description);
		offG.drawString("Flip all the safe tiles to win!", 170, 400);
		offG.drawString("You are given 5 seconds for", 185, 430);
		offG.drawString("each flip, so think quick!", 194, 460);
		offG.drawString("You can choose the # of mines!", 170, 490);
		repaint();
	}

	//This method draws the mine field on the applet
	public void drawMineField() {
   		offG.drawImage(background, 0, 100, this);
   		offG.drawImage(headerBackground, 0, 0, this);
		int a=70, b=100, c=70, d=800;
		for(int i=0; i<10; i++) {
		  	offG.drawLine(a,b,c,d);
		  	a+=70;
		    c+=70;
		}
	    int a2=0, b2=100, c2=700, d2=100;
	    for(int i=0; i<10; i++) {
	    	offG.drawLine(a2,b2,c2,d2);
		   	b2+=70;
	    	d2+=70;
	    }
	    repaint();
	}

	//This method is called when user wins the game.
	public void gameWon() {
		game = 2;
		offG.drawImage(victory, 0, 0, this);
		offG.drawImage(restartButton, 197, 600, this);
		repaint();
		backgroundMusic.stop();
		victorySound.play();
		delay(100);
		standingOvation.play();
		JOptionPane.showMessageDialog(null, "You have won!!! Noone has ever done that!");
	}

	//This method is called when either the user steps on a mine or the time is up.
	public void gameLost(int lost) {
		game = 2;
		if(once2) {	//This is to prevent gameLost from repeating infinitely when it gets called when time is up.
			backgroundMusic.stop();
			offG.drawImage(gameOverPic, 0, 0, this);
			gameOver.play();
			if(lost==0) {	//This differentiates between step on a mine and time up.
				offG.drawImage(timeUp, 197, 300, this);
			}
			offG.drawImage(restartButton, 197, 600, this);
			once2 = false;
		}
		repaint();
	}

}