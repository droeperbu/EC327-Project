package com.beep_boop.Beep.launch;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;

import com.beep_boop.Beep.R;

public class LaunchActivity extends Activity 
{
	private ImageView logo_image_view;
	private ImageView text_image_view;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launch);
		
		logo_image_view = (ImageView) findViewById(R.id.launchActivity_logoImageView);
		text_image_view = (ImageView) findViewById(R.id.launchActivity_textImageView);
		
		
	}
}