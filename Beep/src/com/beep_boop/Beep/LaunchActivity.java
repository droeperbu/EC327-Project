package com.beep_boop.Beep;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.beep_boop.Beep.R;
import com.beep_boop.Beep.game.PlayScreenParser;
import com.beep_boop.Beep.game.WordHandler;
import com.beep_boop.Beep.levelSelect.MapActivity;
import com.beep_boop.Beep.levels.LevelManager;

public class LaunchActivity extends Activity 
{
	///-----Member Variables-----
	/** Holds a reference to THIS for use in listeners */
	private LaunchActivity THIS = this;
	/** Holds a reference to a image view */
	private ImageView logo_image_view;
	/** Holds a reference to a image view */
	private ImageView text_image_view;
	
	private TextView mLoadingTextView;

	private boolean mLevelsLoaded = false, mWordsLoaded = false;
	private float mLevelsPercent = 0.0f, mWordsPercent = 0.0f;

	///-----Activity Life Cycle-----
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launch);

		//grab the image views from XML
		logo_image_view = (ImageView) findViewById(R.id.launchActivity_logoImageView);
		text_image_view = (ImageView) findViewById(R.id.launchActivity_textImageView);
		mLoadingTextView = (TextView) findViewById(R.id.launchActivity_loadingText);

		new LoadLevelsTask().execute(this);
		new LoadWordsTask().execute(this);

		//load the fade in animation
		Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.animator.anim_fadein);
		//start the animation
		logo_image_view.startAnimation(fadeInAnimation);
		text_image_view.startAnimation(fadeInAnimation);
		//set the listener
		fadeInAnimation.setAnimationListener(new Animation.AnimationListener() 
		{

			@Override
			public void onAnimationStart(Animation animation) 
			{
				// do nothing

			}

			@Override
			public void onAnimationRepeat(Animation animation) 
			{
				// do nothing, does not repeat

			}

			@Override
			public void onAnimationEnd(Animation animation)
			{
				//do nothing
			}
		});
	}



	private class LoadLevelsTask extends AsyncTask<Context, Void, Void>
	{
		protected Void doInBackground(Context... contexts)
		{
			LevelManager.load(contexts[0]);
			return null;
		}

		protected void onProgressUpdate(Void... voids)
		{
			updateLoadingProgress();
		}

		protected void onPostExecute(Void result)
		{
			mLevelsLoaded = true;
			mLevelsPercent = 1.0f;

			checkDone();
		}
	}

	private class LoadWordsTask extends AsyncTask<Context, String, Void> implements PlayScreenParser.StatusUpdate
	{
		protected Void doInBackground(Context... contexts)
		{
			WordHandler.load(contexts[0], this);
			return null;
		}

		protected void onProgressUpdate(String... words)
		{
			updateLoadingProgress();
		}

		protected void onPostExecute(Void result)
		{
			mWordsLoaded = true;
			mWordsPercent = 1.0f;

			checkDone();
		}

		@Override
		public void parserStatusUpdate(int aIndex, String aWord)
		{
			mWordsPercent = aIndex / 5685.0f;
			publishProgress(aWord);
		}
	}
	
	private void updateLoadingProgress()
	{
		float percent = (this.mLevelsPercent + this.mWordsPercent) / 2 * 100;
		this.mLoadingTextView.setText(getString(R.string.launchActivity_loadingText) + " " + percent);
	}

	private void checkDone()
	{
		if (mWordsLoaded && mLevelsLoaded)
		{
			//load the fade out animation
			Animation fadeOutAnimation = AnimationUtils.loadAnimation(THIS, R.animator.anim_fadeout);
			//start the animation
			logo_image_view.startAnimation(fadeOutAnimation);
			text_image_view.startAnimation(fadeOutAnimation);
			//set the listener
			fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() 
			{

				@Override
				public void onAnimationStart(Animation animation) 
				{
					// do nothing

				}

				@Override
				public void onAnimationRepeat(Animation animation) 
				{
					// do nothing, does not repeat

				}

				@Override
				public void onAnimationEnd(Animation animation)
				{
					//make sure the image view doesn't reappear after the animation is done
					logo_image_view.setAlpha(0.0f);
					text_image_view.setAlpha(0.0f);

					//transition to map page
					Intent toMap = new Intent(THIS, MapActivity.class);
					startActivity(toMap);

					//quit
					finish();
				}
			});
		}
	}
}