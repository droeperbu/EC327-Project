package com.beep_boop.Beep.game;

import java.util.ArrayList;
import java.util.Set;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.beep_boop.Beep.R;

public class PlayView extends View
{
	///-----Members-----
	/** Holds the tag used for logging */
	private static final String TAG = "PlayView";
	
	private PointF[] mDrawPoints;
	private float[] mDrawThetas;
	private int mStartWordIndex;
	private int mNumberOfWordsToDraw = 10;
	private ArrayList<String> mWords = new ArrayList<String>();
	
	private PointF[] mStartPoints;
	private float[] mStartThetas;
	private float mAnimationPercent;
	private Paint mTextPaint;
	
	/** Holds the minimum distance the finger must move to be considered a scroll */
	private static final float mMinScrollDelta = 5.0f;
	/** Holds the last registered point of the touch in screen coords */
	private PointF mLastTouchPoint = new PointF();
	/** Holds whether or not we are scrolling */
	private boolean mScrolling; 
	
	private static final float SCROLL_SCALAR = 2.0f;
	
	///-----Constructors-----
	public PlayView(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PlayView, 0, 0);
		try
		{
			
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
		this.mTextPaint = new Paint();
		this.mTextPaint.setTextSize(20.0f);
		this.mTextPaint.setColor(0x000000);
		
		//does a circle pattern
		ArrayList<PointF> startPoints = new ArrayList<PointF>();
		ArrayList<Float> startThetas = new ArrayList<Float>();
		float delta = (float)Math.PI / (this.mNumberOfWordsToDraw + 1);
		float theta = (float)Math.PI/2;
		float radius = 0.5f;
		for (int i = 0; i < this.mNumberOfWordsToDraw + 1; i++,  theta -= delta)
		{
			startPoints.add(new PointF(radius * (float)Math.sin(theta), radius * (float)Math.cos(theta)));
			startThetas.add(theta);
		}
		this.setStarts(startPoints, startThetas);
	}
	
	public void setWords(Set<String> aWords)
	{
		if (aWords != null)
		{
			this.mWords.clear();
			this.mWords.addAll(aWords);
		}
	}

	///-----Functions-----
	//overridden view method
	protected void onDraw(Canvas canvas)
	{
		//draw background
		this.drawBackground(canvas);
	}

	//draws the background of the map
	private void drawBackground(Canvas canvas)
	{
		
	}
	
	
	private void setStarts(ArrayList<PointF> aPoints, ArrayList<Float> aThetas)
	{
		this.mNumberOfWordsToDraw = aPoints.size() - 1;
		this.mStartPoints = new PointF[aPoints.size()];
		this.mStartThetas = new float[aThetas.size()];
		
		for (int i = 0; i < aPoints.size(); i++)
		{
			this.mStartPoints[i] = aPoints.get(i);
			this.mStartThetas[i] = aThetas.get(i);
		}
	}
	
	private void calculateDrawPointsAndThetas()
	{
		for (int i = 0; i < this.mStartPoints.length - 1; i++)
		{
			PointF currentPoint = this.mStartPoints[i];
			PointF nextPoint = this.mStartPoints[i + 1];
			
			float deltaX = (nextPoint.x - currentPoint.x) * this.mAnimationPercent;
			float deltaY = (nextPoint.y - currentPoint.y) * this.mAnimationPercent;
			float deltaTheta = (this.mStartThetas[i + 1] - this.mStartThetas[i]) * this.mAnimationPercent;
			
			this.mDrawPoints[i] = new PointF(currentPoint.x + deltaX, currentPoint.y + deltaY);
			this.mDrawThetas[i] = this.mDrawThetas[i] + deltaTheta;
		}
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
			Log.e(PlayView.TAG, "Unknown motion event type: " + event.getAction());
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
			//increment the origin by the delta
			this.scroll(-deltaY * PlayView.SCROLL_SCALAR);
		}
		else
		{
			//check if we've exceeded the minimum scroll distance
			if (Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2)) >= Math.pow(PlayView.mMinScrollDelta, 2))
			{
				//if so, we are scrolling
				this.mScrolling = true;
			}
		}
	}
	
	private void scroll(float aIncrement)
	{
		//increment the percent
		this.mAnimationPercent += aIncrement;
		
		//bound the percent
		if (this.mAnimationPercent > 1.0f)
		{
			this.mAnimationPercent -= 1.0f;
			this.mStartWordIndex++;
			if (this.mStartWordIndex >= this.mWords.size())
			{
				this.mStartWordIndex = this.mWords.size();
			}
		}
		else if (this.mAnimationPercent < 0.0f)
		{
			this.mAnimationPercent += 1.0f;
			this.mStartWordIndex--;
			if (this.mStartWordIndex < 0)
			{
				this.mStartWordIndex = 0;
			}
		}
		
		//calculate the draw points
		this.calculateDrawPointsAndThetas();
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
			//@TODO - click
		}

		//touch ended, reset all variables
		this.resetTouchVariables();
	}
	
	//resets all touch variables
	private void resetTouchVariables()
	{
		//reset variables here as needed
		this.mScrolling = false;
		this.mLastTouchPoint = new PointF(-1.0f, -1.0f);
	}

}
