package com.beep_boop.Beep.about;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.beep_boop.Beep.MyApplication;
import com.beep_boop.Beep.R;
import com.beep_boop.Beep.stars.StarryBackgroundView;

public class AboutActivity extends Activity
{
	private StarryBackgroundView mStarBackground;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		

		this.mStarBackground = (StarryBackgroundView) findViewById(R.id.aboutActivity_background);
		
		TextView aboutBlurb = (TextView) findViewById(R.id.aboutActivity_aboutText);
		aboutBlurb.setTypeface(MyApplication.MAIN_FONT);

		ImageButton backButton = (ImageButton) findViewById(R.id.aboutActivity_backButton);
		backButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				finish();
				overridePendingTransition(R.animator.anim_activity_bottom_in, R.animator.anim_activity_bottom_out);
			}
		});
	}

	@Override
	protected void onStop()
	{
		super.onStop();

		

	}
	@Override
	protected void onRestart(){
		super.onRestart();

		//.mServ.resumeMusic();
		

	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if (this.mStarBackground != null)
		{
			this.mStarBackground.destroy();
		}
	}
}
