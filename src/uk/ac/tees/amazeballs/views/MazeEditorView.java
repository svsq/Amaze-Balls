package uk.ac.tees.amazeballs.views;

import uk.ac.tees.amazeballs.maze.FloorTile;
import uk.ac.tees.amazeballs.maze.TileFactory;
import uk.ac.tees.amazeballs.maze.TileType;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class MazeEditorView extends MazeGridView {

	public MazeEditorView(Context context) {
		super(context);
		this.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return handleViewTouched(event);
			}
		});
	}
	
	public MazeEditorView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return handleViewTouched(event);
			}
		});
	}

	private boolean handleViewTouched(MotionEvent event) {
		// We only want to know when a square was touched.
		if (event.getActionMasked() != MotionEvent.ACTION_DOWN) {
			return true;
		}
		
		// Normalize the coordinates touched into grid coordinates.
		int gridPositionTouchedX = (int)Math.floor(((event.getX() - gridOffset_x) / getTilesize()));
		int gridPositionTouchedY = (int)Math.floor(((event.getY() - gridOffset_y) / getTilesize()));
		
		// Ignore any touches that are within our view area but outside the displayed grid.
		if (gridPositionTouchedX < 0 || 
			gridPositionTouchedY < 0 || 
			gridPositionTouchedX >= getMaze().getWidth() ||
			gridPositionTouchedY >= getMaze().getHeight()) {
			return true;
		}
		
		handleTileTouched(gridPositionTouchedX, gridPositionTouchedY);

		return true;
	}
	
	private void handleTileTouched(int x, int y) {
		// Prevent the edges of the maze being modified
		if (getMaze().isTileAtAnEdge(x, y)) {
			return;
		}
		
		// Toggle the tile.
		if (getMaze().getTileAt(x, y) instanceof FloorTile) {
			getMaze().setTileAt(x, y, TileFactory.createTile(TileType.Wall));
		} else {
			getMaze().setTileAt(x, y, TileFactory.createTile(TileType.Floor));
		}
		
		// Repaint the view
		invalidate();
		
		Log.d(getClass().getName(), "(" + x + "," + y + ")");
	}
	
}
