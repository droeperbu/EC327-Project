package com.beep_boop.Beep.levelSelect;

import java.util.ArrayList;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.beep_boop.Beep.R;

public class MapView extends View
{
	///-----Interfaces-----
	public interface NodeClickListener
	{
		public boolean mapViewUserCanClickNode(MapView aMapView, MapNode aNode);
		public void mapViewUserDidClickNode(MapView aMapView, MapNode aNode);
	}

	public interface NodeStatusDataSource
	{
		public boolean mapViewIsNodeDone(MapView aMapView, MapNode aNode);
	}

	///-----Members-----
	/** Holds the tag used for logging */
	private static final String TAG = "MapView";

	/** Holds the listener who handles node clicks */
	private NodeClickListener mListener = null;
	/** Holds the data source */
	private NodeStatusDataSource mDataSource = null;

	/** Holds all the nodes on the map */
	private ArrayList<MapNode> mNodes = new ArrayList<MapNode>();
	/** Holds the status of all the nodes */
	private ArrayList<Boolean> mNodeStates = new ArrayList<Boolean>();
	/** Holds the currently selected node */
	private int mSelectedNode = -1;

	/** Holds the view origin in map space */
	private PointF mOrigin = new PointF(0.0f, 0.0f);

	/** Holds the bounds the origin can take in map space */
	private RectF mOriginBounds;

	/** Holds the minimum distance the finger must move to be considered a scroll */
	private static final float mMinScrollDelta = 5.0f;
	/** Holds the last registered point of the touch in screen coords */
	private PointF mLastTouchPoint = new PointF();
	/** Holds whether or not we are scrolling */
	private boolean mScrolling; 
	/** Holds the maximum distance the finger can be from a node to click it*/
	private float mMaxNodeClickDistance = 0.05f;

	/** Holds the amount of the map on the screen width wise */
	private float MAP_ON_SCREEN_WIDTH;
	/** Holds the amount of the map on the screen height wise */
	private float MAP_ON_SCREEN_HEIGHT = 1.0f;

	private float mScaleX, mScaleY;
	/** Hold the image to be drawn in the background */
	private Bitmap mBackgroundImage; // This may need to be broken up into multiple images, in which case an array should be used

	/** Holds the OFF node image */
	private Bitmap mNodeImageOff;
	/** Holds the ON node image */
	private Bitmap mNodeImageOn;
	private int mNodeHalfSizeX, mNodeHalfSizeY;
	/** Holds an overlay for the selected node  */
	private Bitmap mSelectedNodeOverlay;
	private int mOverlayHalfSizeX, mOverlayHalfSizeY;
	/** Holds the current state of the node */
	private float mSelectedNodeState;
	/** Holds the time it takes to transition between the off and on node image for the selected node */
	private int mAnimationLength;
	private Paint mNodeOnPaint, mNodeOffPaint;
	private ValueAnimator mNodeAnimator;
	
	private static final float SCROLL_SCALAR = 2.0f;

	///-----Constructors-----
	public MapView(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MapView, 0, 0);
		try
		{
			float mapWidth = a.getFloat(R.styleable.MapView_mapWidthOnScreen, 1.0f);
			this.MAP_ON_SCREEN_WIDTH = mapWidth;
			Drawable nodeOffImage = a.getDrawable(R.styleable.MapView_nodeOffImage);
			this.mNodeImageOff = ((BitmapDrawable) nodeOffImage).getBitmap();
			Drawable nodeOnImage = a.getDrawable(R.styleable.MapView_nodeOnImage);
			this.mNodeImageOn = ((BitmapDrawable) nodeOnImage).getBitmap();
			Drawable backgroundImage = a.getDrawable(R.styleable.MapView_backgroundImage);
			this.mBackgroundImage = ((BitmapDrawable) backgroundImage).getBitmap();
			Drawable nodeOverlayImage = a.getDrawable(R.styleable.MapView_nodeSelectedOverlay);
			this.mSelectedNodeOverlay = ((BitmapDrawable) nodeOverlayImage).getBitmap();
			this.mAnimationLength = a.getInteger(R.styleable.MapView_animationLength, 100);
			this.mMaxNodeClickDistance = a.getFloat(R.styleable.MapView_nodeClickDistance, 0.05f);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			a.recycle();
		}
		
		this.init();
	}
	
	private void init()
	{
		this.mNodeOnPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.mNodeOffPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		
		// Create a new value animator that will use the range 0 to 1
		this.mNodeAnimator = ValueAnimator.ofFloat(0, 1);
		// It will take XXXms for the animator to go from 0 to 1
		this.mNodeAnimator.setDuration(this.mAnimationLength);
		// Callback that executes on animation steps. 
		this.mNodeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
		    @Override
		    public void onAnimationUpdate(ValueAnimator animation)
		    {
		    	mSelectedNodeState = ((Float) (animation.getAnimatedValue())).floatValue();
		    	//mNodeOffPaint.setAlpha((int)(255 * (1.0f - mSelectedNodeState)));
		    	mNodeOnPaint.setAlpha((int)(255 * mSelectedNodeState));
		    	requestRedraw();
		    }
		});
		this.mNodeAnimator.setRepeatCount(ValueAnimator.INFINITE);
		this.mNodeAnimator.setRepeatMode(ValueAnimator.REVERSE);
		this.mNodeAnimator.start();
	}
	
	@Override
	public void onDetachedFromWindow()
	{
		super.onDetachedFromWindow();
		
		//clean up the animator
		this.mNodeAnimator.cancel();
		this.mNodeAnimator = null;
	}

	///-----Functions-----
	//Implements LevelDoneListener interface

	//sets the listener
	public void setListener(NodeClickListener aListener)
	{
		this.mListener = aListener;
	}

	//sets the data source
	public void setDataSource(NodeStatusDataSource aDataSource)
	{
		this.mDataSource = aDataSource;
	}

	//adds a node
	public void addNode(MapNode aNode)
	{
		//add the node
		this.mNodes.add(aNode);
		//recalculate the bounding box
		this.calculateOriginBounds();
		//update all states
		this.updateStates();
	}

	//adds multiple nodes
	public void addNodes(ArrayList<MapNode> aNodeArray)
	{
		//add all the nodes
		this.mNodes.addAll(aNodeArray);
		//recalculate the bounding box
		this.calculateOriginBounds();
		//update all states
		this.updateStates();
	}
	
	//gets the states for a node
	public void updateStateForNodeWithKey(String aLevelKey, boolean aState)
	{
		//look for the node
		for (int i = 0; i < this.mNodes.size(); i++)
		{
			MapNode node = this.mNodes.get(i);
			//check if it's the right node
			if (node.getLevelKey().equals(aLevelKey))
			{
				//save the state
				this.mNodeStates.set(i, aState);
				//break
				break;
			}
		}
	}
	
	//gets the states for all nodes
	public void updateStates()
	{
		//remove all old states
		this.mNodeStates.clear();
		//enumerate through all nodes
		for (MapNode node : this.mNodes)
		{
			//get state from data source
			boolean state = this.getStateForNode(node);
			//save data
			this.mNodeStates.add(state);
		}
	}

	//gets the state of a node from the DataSource
	private boolean getStateForNode(MapNode aNode)
	{
		boolean result = false;
		if (this.mDataSource != null)
		{
			result = this.mDataSource.mapViewIsNodeDone(this, aNode);
		}
		else
		{
			Log.w(MapView.TAG, "Datasource is null");
		}
		return result;
	}

	//sets the selected node
	public void setSelectedNode(int aIndex)
	{
		//set the selected node index
		this.mSelectedNode = aIndex;
	}

	//calculates the max and min origin bounds
	private void calculateOriginBounds()
	{
		float minX = 0.0f, minY = 0.0f;
		float maxX = 1.0f - this.MAP_ON_SCREEN_WIDTH;
		float maxY = 1.0f - this.MAP_ON_SCREEN_HEIGHT;
		
		if (maxX < minX)
		{
			maxX = minX;
		}
		if (maxY < minY)
		{
			maxY = minY;
		}

		//set the new bounds
		this.mOriginBounds = new RectF(minX, maxY, maxX, minY);
	}

	//ensures the view�s origin is within the bounds
	private void boundOrigin()
	{
		//bound in the x direction
		if (this.mOrigin.x < this.mOriginBounds.left)
		{
			this.mOrigin.x = this.mOriginBounds.left;
		}
		else if (this.mOrigin.x > this.mOriginBounds.right)
		{
			this.mOrigin.x = this.mOriginBounds.right;
		}

		//bound in the y direction
		if (this.mOrigin.y < this.mOriginBounds.bottom)
		{
			this.mOrigin.y = this.mOriginBounds.bottom;
		}
		else if (this.mOrigin.y > this.mOriginBounds.top)
		{
			this.mOrigin.y = this.mOriginBounds.top;
		}
	}

	//sets the origin of the view
	public void setOrigin(PointF aOrigin)
	{
		//set the origin
		this.mOrigin = aOrigin;
		//make sure the origin is within bounds
		this.boundOrigin();
		//redraw
		this.requestRedraw();
	}

	//centers the view on the node
	private void centerOnNode(int aIndex)
	{
		//get the map we want to center on
		MapNode centerOn = this.mNodes.get(aIndex);
		//calculate the origin to center on it
		PointF centered = new PointF(centerOn.getX() - this.MAP_ON_SCREEN_WIDTH/2, centerOn.getY() - this.MAP_ON_SCREEN_HEIGHT/2);
		//set the origin
		this.setOrigin(centered);
	}

	//overridden view method
	protected void onDraw(Canvas canvas)
	{
		
		canvas.save();
		
		canvas.scale(this.mScaleX, this.mScaleY);
		//draw background
		this.drawBackground(canvas);
		//draw all the nodes on top
		this.drawNodesWithinView(canvas);
		canvas.restore();
	}

	//draws the background of the map
	private void drawBackground(Canvas canvas)
	{
		if (this.mBackgroundImage != null)
		{
			PointF screen = this.convertToScreenSpace(0.0f, 1.0f);
			canvas.drawBitmap(this.mBackgroundImage, screen.x, screen.y, null);
		}
	}

	//draws all the nodes that are within the bounds of the screen
	private void drawNodesWithinView(Canvas canvas)
	{
		//enumerate through all map nodes
		for (int i = 0; i < this.mNodes.size(); i++)
		{
			MapNode node = this.mNodes.get(i);
			//check if it's on screen in the X direction
			if (Math.abs(node.getX() - this.mOrigin.x) < this.MAP_ON_SCREEN_WIDTH * 1.5f)
			{
				//check if it's on screen in the Y direction
				if (Math.abs(node.getY() - this.mOrigin.y) < this.MAP_ON_SCREEN_HEIGHT * 1.5f)
				{
					//draw it
					PointF screenDrawCenter = this.convertToScreenSpace(node.getX(), node.getY());

					if (i == this.mSelectedNode)
					{
						canvas.drawBitmap(this.mNodeImageOff, screenDrawCenter.x - this.mNodeHalfSizeX, screenDrawCenter.y - this.mNodeHalfSizeY, this.mNodeOffPaint);
						canvas.drawBitmap(this.mNodeImageOn, screenDrawCenter.x - this.mNodeHalfSizeX, screenDrawCenter.y - this.mNodeHalfSizeY, this.mNodeOnPaint);
						if (this.mSelectedNodeOverlay != null)
							canvas.drawBitmap(this.mSelectedNodeOverlay, screenDrawCenter.x - this.mOverlayHalfSizeX, screenDrawCenter.y - this.mOverlayHalfSizeY, null);
					}
					else
					{
						//get which bitmap to use for this node
						boolean state = this.mNodeStates.get(i).booleanValue();
						Bitmap useToDraw = (state ? this.mNodeImageOn : this.mNodeImageOff);
						canvas.drawBitmap(useToDraw, screenDrawCenter.x - this.mNodeHalfSizeX, screenDrawCenter.y - this.mNodeHalfSizeY, null);
					}
				}
			}
		}
	}

	//tells the map view to update all of the node�s states and redraw
	private void requestRedraw()
	{
		invalidate();
		requestLayout();
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		
		this.mScaleX =  w / (float)(this.MAP_ON_SCREEN_WIDTH * this.mBackgroundImage.getWidth());
		this.MAP_ON_SCREEN_HEIGHT = this.MAP_ON_SCREEN_WIDTH * (h / (float)w);
		this.mScaleY = h / (float)(this.MAP_ON_SCREEN_HEIGHT * this.mBackgroundImage.getHeight());
		
		this.mNodeHalfSizeX = (int)(this.mNodeImageOff.getWidth() * this.MAP_ON_SCREEN_WIDTH * this.mScaleX);
		this.mNodeHalfSizeY = (int)(this.mNodeImageOff.getHeight() * this.MAP_ON_SCREEN_HEIGHT * this.mScaleY);
		
		this.calculateOriginBounds();
	}

	//gets touch events for view
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		//result holds if the touch was processed by the view, which is all cases is yes
		boolean result = true;
		if (event.getAction() == MotionEvent.ACTION_DOWN)
		{
			//handle touch down
			this.touchDown(event);
		}
		else if (event.getAction() == MotionEvent.ACTION_MOVE)
		{
			//handle touch moved
			this.touchMoved(event);
		}
		else if (event.getAction() == MotionEvent.ACTION_UP)
		{
			//handle touch up
			this.touchUp(event);
		}
		else
		{
			Log.e(MapView.TAG, "Unknown motion event type: " + event.getAction());
			result = false;
		}

		return result;
	}

	//handles all touch down events
	private void touchDown(MotionEvent aEvent)
	{
		this.mLastTouchPoint.x = aEvent.getX();
		this.mLastTouchPoint.y = aEvent.getY();
	}

	//handles all touch moved events
	private void touchMoved(MotionEvent aEvent)
	{
		float deltaX = aEvent.getX() - this.mLastTouchPoint.x;
		float deltaY = aEvent.getY() - this.mLastTouchPoint.y;

		//check if we are already scrolling
		if (this.mScrolling)
		{
			//increment the last touch point
			this.mLastTouchPoint.x += deltaX;
			this.mLastTouchPoint.y += deltaY;
			//calculate the delta movement in map space
			float scaledX = (deltaX / this.getWidth()) * this.MAP_ON_SCREEN_WIDTH;
			float scaledY = (deltaY / this.getHeight()) * this.MAP_ON_SCREEN_HEIGHT;
			//increment the origin by the delta
			this.incrementOrigin(-scaledX * MapView.SCROLL_SCALAR, scaledY * MapView.SCROLL_SCALAR);
		}
		else
		{
			//check if we've exceeded the minimum scroll distance
			if (Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2)) >= Math.pow(MapView.mMinScrollDelta, 2))
			{
				//if so, we are scrolling
				this.mScrolling = true;
			}
		}
	}

	//increments the origin
	private void incrementOrigin(float aX, float aY)
	{
		//increment origin
		this.mOrigin.x += aX;
		this.mOrigin.y += aY;
		//make sure the origin is within bounds
		this.boundOrigin();
		//redraw
		this.requestRedraw();
	}

	//handles all touch up events
	private void touchUp(MotionEvent aEvent)
	{
		//check if we are scrolling
		if (this.mScrolling)
		{
			//do nothing
		}
		else
		{
			//if we're not, convert the touch point to map space
			PointF mapSpace = this.convertToMapSpace(aEvent.getX(), aEvent.getY());

			//get the nearest node
			MapNode nodeNearLocation = this.getNodeNearPoint(mapSpace);
			if (nodeNearLocation != null)
			{
				//there is a node to click
				//check if we can click the node
				boolean canClick = false;
				if (this.mListener != null)
				{
					canClick = this.mListener.mapViewUserCanClickNode(this, nodeNearLocation);
				}
				else
				{
					Log.w(MapView.TAG, "Listener is null");
				}
				
				if (canClick)
				{
					//user can click node
					Log.v(MapView.TAG, "User clicked node with key: " + nodeNearLocation.getLevelKey());
					//click the node
					if (this.mListener != null)
					{
						this.mListener.mapViewUserDidClickNode(this, nodeNearLocation);
					}
					else
					{
						Log.w(MapView.TAG, "Listener is null");
					}
					//set this node as the selected node
					this.mSelectedNode = this.mNodes.indexOf(nodeNearLocation);
					//tell the view to center on that node
					this.centerOnNode(this.mSelectedNode);
				}
				else
				{
					//can't click node
					Log.v(MapView.TAG, "User cannot click node with key: " + nodeNearLocation.getLevelKey());

					//@TODO - play sound?
				}
			}
			else
			{
				//no node to click, do nothing
			}
		}

		//touch ended, reset all variables
		this.resetTouchVariables();
	}

	//converts a point in touch space to map space
	private PointF convertToMapSpace(float aX, float aY)
	{
		float scaledX = (aX / this.getWidth()) * this.MAP_ON_SCREEN_WIDTH  + this.mOrigin.x;
		float scaledY = (1.0f - (aY / this.getHeight())) * this.MAP_ON_SCREEN_HEIGHT + this.mOrigin.y;
		return new PointF(scaledX, scaledY);
	}

	private PointF convertToScreenSpace(float aX, float aY)
	{
		float scaledX = (aX - this.mOrigin.x) * this.getWidth() / this.MAP_ON_SCREEN_WIDTH / this.mScaleX;
		float scaledY = (this.MAP_ON_SCREEN_HEIGHT - aY + this.mOrigin.y) * this.getHeight() / this.MAP_ON_SCREEN_HEIGHT / this.mScaleY;
		return new PointF(scaledX, scaledY);
	}

	//find a node near the location. If there isn�t a node, returns null
	private MapNode getNodeNearPoint(PointF aPoint)
	{
		MapNode result = null;
		for (MapNode node : this.mNodes)
		{
			double distance = Math.sqrt(Math.pow(aPoint.x - node.getX(), 2) + Math.pow(aPoint.y- node.getY(), 2));
			if (Math.abs(distance) <= this.mMaxNodeClickDistance)
			{
				result = node;
				break;
			}
		}

		return result;
	}

	//resets all touch variables
	private void resetTouchVariables()
	{
		//reset variables here as needed
		this.mScrolling = false;
		this.mLastTouchPoint = new PointF(-1.0f, -1.0f);
	}

}
