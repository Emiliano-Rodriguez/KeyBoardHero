package application.model;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

import application.controller.GameController;
import application.controller.GameThreeController;
import application.controller.MainController;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

/**
 * @author Team Garbage
 * 
 * This class is the model code for GamemodeThree
 * GamemodeThree runs the major interactions between the player, the enemies, and the game
 * methods in this class keep track of lives, score, whether enemies needs to be spawned or removed, and
 * whether the game is playable, paused, or has ended   
 *
 */
public class GamemodeThree {
	private GameThreeController controller;
	private int counter = 0;
	private CopyOnWriteArrayList<Asteroid> asteroids;
	private boolean paused = false;
	private boolean gameOver = false;
	
	//Sets up the sounds that happen when a shark dies
	String musicFile = "src/soundTrack/poof.mp3";     // For example
	Media sound1 = new Media(new File(musicFile).toURI().toString());
	MediaPlayer poof = new MediaPlayer(sound1);
	
	
	public GamemodeThree(GameThreeController controller) { //constructor
		this.controller = controller;
		WordReader randomWord = new WordReader();		
		WordReader.getRandomTimer();
		asteroids = new CopyOnWriteArrayList<Asteroid>();
		asteroids.add(new Asteroid(1300, WordReader.getRandomSpawn(), WordReader.getRandomWord()));
		
		new AnimationTimer() {

			@Override
			public void handle(long arg0) {
				update();
				draw(controller.canvas.getGraphicsContext2D());			
			}
			
		}.start();
	}
	
	/**
	 * checkWord checks whether the word the player has typed matches with the words on the enemies. 
	 * If it does it sets the boolean variable tru to true, it increases the players score, and it adds a bonus 
	 * to the scoreModifier.
	 * Else the scoreModifier is reset so a bonus score isn't applied to missed words
	 * 
	 * @param input //the word the player types
	 */
	public void checkWord(String input) {
		//System.out.println("Looking for word " + input);
		for(Asteroid asteroid: asteroids) {
			if(asteroid.getWord().toLowerCase().equals(input.toLowerCase())) {
				asteroid.setStatus(true);
				
				//G.B. score
				MainController.score.set(MainController.score.get() + (int)(MainController.scoreModifier * MainController.POINTS_PER_WORD));
				MainController.scoreModifier += MainController.BONUS_MODIFIER;
			
			} else {
				MainController.scoreModifier = 1.0f; //reset the Bonus
			}
		}
	}
	
	/**
	 * update tracks whether an enemy has gone past the player
	 * if so the player looses a life and the scoreModifier bonus is reset
	 * if lives decrease down to 0 the gameOver method is called
	 * if no sharks are on screen a shark is spawned
	 * also difficulty values are updated here
	 * 
	 */
	public void update() {
		if(paused) return;
		counter++;
		if(asteroids.isEmpty() == false) {
			for(Asteroid asteroid: asteroids) {
				if(asteroid.getX() < -300) { //shark left screen, lose life
					asteroids.remove(asteroid);
					
					//G.B. enemy goes past player
					MainController.scoreModifier = 1.0f; //reset the Bonus
					
					//G.B. lives
					MainController.lives = MainController.lives - 1;
					if(MainController.lives <= 0) {
						MainController.lives = 0; //stops removing lives
						gameOver(); //call fail state/game stop method
					}
				}
				asteroid.update();
			}
		}
		else {
			asteroids.add(new Asteroid(1300, WordReader.getRandomSpawn(), WordReader.getRandomWord()));
			counter = 1;
		}
		if(counter % Math.rint(MainController.DIFFICULTY_VALUE) == 0 ) {
			asteroids.add(new Asteroid(1300, WordReader.getRandomSpawn(), WordReader.getRandomWord()));
			counter = 1;
			WordReader.getRandomTimer();
			}
	}
	
	/**
	 * draws the enemies and adds death effects when an enemy is removed
	 * once an enemy has been removed, tru is set back to false
	 * lives are also drawn in this method, number of hearts drawn depend on the number of lives the player has  
	 * 
	 * @param gc
	 */
	public void draw(GraphicsContext gc) {
		gc.clearRect(0, 0, 1280, 720);
		int i = 0;
		for(Asteroid asteroid: asteroids) {
			asteroid.draw(gc);
			while(asteroid.getStatus() == true) {
				//add explosion
				asteroid.explosion(gc);
				poof.play();
				
				
				if(i == 2) {
					asteroids.remove(asteroid);
					asteroid.setStatus(false); 
					
				}
			i++;
			poof.stop();
			}
			
			if(MainController.lives == 3) {
				asteroid.heart1(gc);
				asteroid.heart2(gc);
				asteroid.heart3(gc);

			}
			
			if(MainController.lives == 2) {
				asteroid.heart1(gc);
				asteroid.heart2(gc);
			}
			
			if(MainController.lives == 1) {
				asteroid.heart1(gc);
			}
		}
	}

	/**
	 * if set to true, this method pauses the game
	 * 
	 * @param paused
	 */
	public void setPaused(boolean paused) {
		this.paused = paused;
		
	}
	
	/**
	 * if set to true, this method ends the game
	 * 
	 */
	public void gameOver() {
		gameOver = true;
		controller.gameOver();
	}

}
