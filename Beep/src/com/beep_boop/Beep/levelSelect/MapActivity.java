package com.beep_boop.Beep.levelSelect;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.beep_boop.Beep.LevelManager;
import com.beep_boop.Beep.LevelManager.LevelStateListener;
import com.beep_boop.Beep.R;
import com.beep_boop.Beep.levelSelect.MapView.NodeClickListener;

public class MapActivity extends Activity implements NodeClickListener, LevelStateListener
{
	///-----Member Variables-----
	/** Holds a reference to the map view */
	private MapView mMapView; 
	
	///-----Activity Life Cycle-----
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		//subscribe to level state updates
		LevelManager.addLevelStateListener(this);
		
		//get the map view from XML
		mMapView = (MapView)findViewById(R.id.mapActivity_mapView);
		//setup the map view
		this.setupMapView();
	}
	
	private void setupMapView()
	{
		//load the nodes
		ArrayList<MapNode> nodeList = null;
		InputStream in = null;
		try 
		{
			in = getResources().openRawResource(R.raw.nodes_test_file);
			nodeList = MapNodeLoader.parseFile(in);
		}
		catch (Exception i)
		{
			Log.e("fileinput", "The IOException was caught.");
		}
		finally 
		{
			if (in != null)
			{
				try
				{
					in.close();
				}
				catch (IOException e)
				{
					
				}
			}
		}
		
		//add the nodes to the map view
		this.mMapView.addNodes(nodeList); 
		
		//set the node click listener
		this.mMapView.setListener(this);
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		//unsubscribe to level state updates
		LevelManager.removeLevelStateListener(this);
	}

	///-----NodeClickListener methods-----
	public boolean mapViewUserCanClickNode(MapView aMapView, MapNode aNode)
	{
		boolean result = true;
		
		//@TODO - add game logic
		
		return result;
	}
	
	public void mapViewUserDidClickNode(MapView aMapView, MapNode aNode)
	{
		//@TODO - switch to next level
		
		//@TEMP - switches state of level
		LevelManager.setLevelComplete(aNode.getLevelKey(), !LevelManager.getIsLevelComplete(aNode.getLevelKey()));
	}
	
	///-----NodeDataSource methods-----
	public boolean mapViewIsNodeDone(MapView aMapView, MapNode aNode)
	{
		return LevelManager.getIsLevelComplete(aNode.getLevelKey());
	}
	
	///-----LevelStateListener methods-----
	public void stateDidChangeForLevel(String aLevelKey, boolean aState)
	{
		this.mMapView.updateStateForNodeWithKey(aLevelKey, aState);
	}
}