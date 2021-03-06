package uk.ac.tees.amazeballs;

import android.graphics.Point;
import uk.ac.tees.amazeballs.maze.MazeNew;
import uk.ac.tees.amazeballs.maze.MazeWorld;
import uk.ac.tees.amazeballs.maze.MazeWorld.Ball;
import uk.ac.tees.amazeballs.maze.MazeWorldCamera;
import uk.ac.tees.amazeballs.views.MazeViewport;


/**
 * A class for managing a game.
 * 
 * @author Alex Mullen (J9858839)
 *
 */
public class GameController {

	private static final int NORMAL_BALL_SPEED = 3;
	private static final int ICE_BALL_SPEED = 7;
	private static final int RAIN_BALL_SPEED = 1;

	private static final double TILT_SENSITIVITY = 0.75;

	public volatile float lastAccelerometerReading_x;
	public volatile float lastAccelerometerReading_y;

	private final MazeWorld mazeWorld;
	private final MazeWorldCamera mazeWorldCamera;
	private final MazeViewport mazeViewport;

	private int ballSpeed = NORMAL_BALL_SPEED;
	private int keysCarrying;
	boolean touchingRain;
	boolean touchingIce;
	
	private boolean finished;

	public GameController(MazeNew maze, MazeViewport mazeVewport, Point startPosition) {
		this.mazeViewport = mazeVewport;
		mazeWorld = new MazeWorld(maze, 20);
		mazeWorldCamera = new MazeWorldCamera(mazeWorld, 0, 0,
				10 * mazeWorld.tilesize, 
				17 * mazeWorld.tilesize);
		mazeViewport.setCamera(mazeWorldCamera);

		// Create and initialize the ball
		int ballSize = (int) (mazeWorld.tilesize * 0.8);
		int ballStartOffset = (mazeWorld.tilesize - ballSize) / 2;
		int ballStart_x = startPosition.x * mazeWorld.tilesize + ballStartOffset;
		int ballStart_y = startPosition.y * mazeWorld.tilesize + ballStartOffset;
		Ball ball = new Ball(ballStart_x, ballStart_y, ballSize);
		mazeWorld.ball = ball;
	}

	/**
	 * Performs one update of the game state.
	 */
	public void update() {
		moveBall();

		scrollScreen();

		processTouchingTiles();
	}

	/**
	 * Move the ball depending on the tilt of the device.
	 */
	private void moveBall() {
		Ball ball = mazeWorld.ball;
		
		// Get the accelerometer reading
		if (lastAccelerometerReading_y > TILT_SENSITIVITY) {
			ball.position_y += ballSpeed;
			// Resolve collisions
			while (ballHasCollided()) {
				ball.position_y--;
			}
		}
		if (lastAccelerometerReading_y < -TILT_SENSITIVITY) {
			ball.position_y -= ballSpeed;
			// Resolve collisions
			while (ballHasCollided()) {
				ball.position_y++;
			}
		}
		if (lastAccelerometerReading_x > TILT_SENSITIVITY) {
			ball.position_x -= ballSpeed;
			// Resolve collisions
			while (ballHasCollided()) {
				ball.position_x++;
			}
		}
		if (lastAccelerometerReading_x < -TILT_SENSITIVITY) {
			ball.position_x += ballSpeed;
			// Resolve collisions
			while (ballHasCollided()) {
				ball.position_x--;
			}
		}
	}

	/**
	 * Gets whether the ball has collided with a wall or door.
	 * 
	 * @return true if the ball intersects a wall or door tile by 1 point or
	 *         more.
	 */
	private boolean ballHasCollided() {
		int gridPosTouchedX;
		int gridPosTouchedY;
		MazeNew maze = mazeWorld.maze;
		Ball ball = mazeWorld.ball;
		int ballSize = ball.size;
		int tileSize = mazeWorld.tilesize;
		int tileTouched;

		// Check top left corner of ball
		gridPosTouchedX = (int) (ball.position_x) / tileSize;
		gridPosTouchedY = (int) (ball.position_y) / tileSize;
		tileTouched = maze.getTileAt(gridPosTouchedX, gridPosTouchedY);
		if (tileTouched == MazeNew.WALL_TILE || tileTouched == MazeNew.DOOR_TILE) {
			return true;
		}

		// Check top right corner of ball
		gridPosTouchedX = (int) (ball.position_x + (ballSize - 1)) / tileSize;
		gridPosTouchedY = (int) (ball.position_y) / tileSize;
		tileTouched = maze.getTileAt(gridPosTouchedX, gridPosTouchedY);
		if (tileTouched == MazeNew.WALL_TILE || tileTouched == MazeNew.DOOR_TILE) {
			return true;
		}

		// Check bottom right corner of ball
		gridPosTouchedX = (int) (ball.position_x + (ballSize - 1)) / tileSize;
		gridPosTouchedY = (int) (ball.position_y + (ballSize - 1)) / tileSize;
		tileTouched = maze.getTileAt(gridPosTouchedX, gridPosTouchedY);
		if (tileTouched == MazeNew.WALL_TILE || tileTouched == MazeNew.DOOR_TILE) {
			return true;
		}

		// Check bottom left corner of ball
		gridPosTouchedX = (int) (ball.position_x) / tileSize;
		gridPosTouchedY = (int) (ball.position_y + (ballSize - 1)) / tileSize;
		tileTouched = maze.getTileAt(gridPosTouchedX, gridPosTouchedY);
		if (tileTouched == MazeNew.WALL_TILE || tileTouched == MazeNew.DOOR_TILE) {
			return true;
		}

		// No collisions
		return false;
	}

	/**
	 * Scroll to another part of the maze if the ball is within a certain
	 * distance from the view edge.
	 */
	private void scrollScreen() {
		int mazeAreaHeight = mazeWorldCamera.getHeight();
		int mazeAreaWidth = mazeWorldCamera.getWidth();

		// Convert the balls world coordinates into view/camera coordinates
		int viewBallPositionX = (mazeWorldCamera.world.ball.position_x - mazeWorldCamera.getLeft());
		int viewBallPositionY = (mazeWorldCamera.world.ball.position_y - mazeWorldCamera.getTop());

		// Potentially scroll the screen down
		if ((float) viewBallPositionY / mazeAreaHeight >= 0.75) {
			mazeWorldCamera.moveDown(ballSpeed);
		}

		// Potentially scroll the screen up
		if ((float) viewBallPositionY / mazeAreaHeight <= 0.25) {
			mazeWorldCamera.moveUp(ballSpeed);
		}

		// Potentially scroll the screen right
		if ((float) viewBallPositionX / mazeAreaWidth >= 0.75) {
			mazeWorldCamera.moveRight(ballSpeed);
		}

		// Potentially scroll the screen left
		if ((float) viewBallPositionX / mazeAreaWidth <= 0.25) {
			mazeWorldCamera.moveLeft(ballSpeed);
		}
	}

	/**
	 * Handles what happens when certain tiles are touched.
	 */
	private void handleTouchedTile(int touchedGrid_x, int touchedGrid_y, int type) {
		// Reset these
		touchingRain = false;
		touchingIce = false;
		ballSpeed = NORMAL_BALL_SPEED;
		
		processDoorsNearby(touchedGrid_x, touchedGrid_y);

		switch (type) {
			case MazeNew.CHEST_TILE:
				mazeWorld.maze.setTileAt(touchedGrid_x, touchedGrid_y, MazeNew.FLOOR_TILE);
				break;
			case MazeNew.KEY_TILE:
				// Increment the number of keys we have then replace the key tile
				// with a floor tile
				keysCarrying++;
				mazeWorld.maze.setTileAt(touchedGrid_x, touchedGrid_y, MazeNew.FLOOR_TILE);
				break;
			case MazeNew.GOAL_TILE:
				// End the game
				finished = true;
				return;
			case MazeNew.ICE_TILE:
				if (!touchingRain) {
					touchingIce = true;
					ballSpeed = ICE_BALL_SPEED;
				}
				break;
			case MazeNew.PENALTY_TILE:
				// Apply the penalty
	
				mazeWorld.maze.setTileAt(touchedGrid_x, touchedGrid_y, MazeNew.FLOOR_TILE);
				break;
			case MazeNew.RAIN_TILE:
				touchingIce = false;
				touchingRain = true;
				ballSpeed = RAIN_BALL_SPEED;
				break;
			default:
				// Ignore other tiles
				break;
		}
	}

	/**
	 * Processes each tile the ball is currently touching.
	 */
	private void processTouchingTiles() {
		int gridPosTouchedX;
		int gridPosTouchedY;
		MazeNew maze = mazeWorld.maze;
		Ball ball = mazeWorld.ball;
		int ballSize = ball.size;
		int tileSize = mazeWorld.tilesize;

		// Check top left corner of ball
		gridPosTouchedX = (int) (ball.position_x) / tileSize;
		gridPosTouchedY = (int) (ball.position_y) / tileSize;
		handleTouchedTile(gridPosTouchedX, gridPosTouchedY, maze
				.getTileAt(gridPosTouchedX, gridPosTouchedY));

		// Check top right corner of ball
		gridPosTouchedX = (int) (ball.position_x + (ballSize - 1)) / tileSize;
		gridPosTouchedY = (int) (ball.position_y) / tileSize;
		handleTouchedTile(gridPosTouchedX, gridPosTouchedY, maze
				.getTileAt(gridPosTouchedX, gridPosTouchedY));

		// Check bottom right corner of ball
		gridPosTouchedX = (int) (ball.position_x + (ballSize - 1)) / tileSize;
		gridPosTouchedY = (int) (ball.position_y + (ballSize - 1)) / tileSize;
		handleTouchedTile(gridPosTouchedX, gridPosTouchedY, maze
				.getTileAt(gridPosTouchedX, gridPosTouchedY));

		// Check bottom left corner of ball
		gridPosTouchedX = (int) (ball.position_x) / tileSize;
		gridPosTouchedY = (int) (ball.position_y + (ballSize - 1)) / tileSize;
		handleTouchedTile(gridPosTouchedX, gridPosTouchedY, maze
				.getTileAt(gridPosTouchedX, gridPosTouchedY));
	}

	/**
	 * Checks and handles any doors that could be near the ball
	 * 
	 * @param x the grid x position to check
	 * @param y the grid y position to check
	 */
	private void processDoorsNearby(int x, int y) {
		MazeNew maze = mazeWorld.maze;
		// Above
		if (y - 1 >= 0) {
			if (maze.getTileAt(x, (y - 1)) == MazeNew.DOOR_TILE) {
				handleDoorNearby(x, (y - 1));
			}
		}
		// Below
		if (y + 1 < maze.height) {
			if (maze.getTileAt(x, (y + 1)) == MazeNew.DOOR_TILE) {
				handleDoorNearby(x, (y + 1));
			}
		}
		// Left
		if (x - 1 >= 0) {
			if (maze.getTileAt((x - 1), y) == MazeNew.DOOR_TILE) {
				handleDoorNearby((x - 1), y);
			}
		}
		// Right
		if (x + 1 < maze.width) {
			if (maze.getTileAt((x + 1), y) == MazeNew.DOOR_TILE) {
				handleDoorNearby((x + 1), y);
			}
		}
	}

	/**
	 * Checks if the player has a key and opens (removes) the door.
	 * 
	 * @param x the grid x position of the door
	 * @param y the grid y position of the door
	 */
	private void handleDoorNearby(int x, int y) {
		// "Use" any picked up keys to unlock any doors near
		if (keysCarrying > 0) {
			keysCarrying--;
			mazeWorld.maze.setTileAt(x, y, MazeNew.FLOOR_TILE);
		}
	}

	/**
	 * Returns whether a game is finished or not. A game is finished
	 * when the ball has touched a "Goal" tile.
	 * 
	 * @return
	 */
	public boolean isFinished() {
		return finished;
	}
}
