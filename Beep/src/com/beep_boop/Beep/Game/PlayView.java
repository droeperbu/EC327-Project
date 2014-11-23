package com.beep_boop.Beep.Game;

import java.util.ArrayList;

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
	private int mNumberOfWordsToDraw;
	private ArrayList<String> mWords;
	
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
		
	}

	//handles all touch moved events
	private void touchMoved(MotionEvent aEvent)
	{

	}

	//handles all touch up events
	private void touchUp(MotionEvent aEvent)
	{
		
	}
}
