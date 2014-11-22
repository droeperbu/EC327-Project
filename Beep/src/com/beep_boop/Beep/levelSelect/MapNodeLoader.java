package com.beep_boop.Beep.levelSelect;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;

public class MapNodeLoader
{
	///-----Interfaces-----
	// None

	///-----Members-----
	private static final String TAG = "mapNodeLoader";
	
	/** XML namespace used in the file. */
	private static final String NAMESPACE = null;
	/** Tag for the node array */
	private static final String TAG_NODES_ARRAY = "nodes";
	/** Tag for an individual node */
	private static final String TAG_NODE = "node";
	/** Tag for an individual node�s location X */
	private static final String TAG_NODE_LOCATION_X = "locX";
	/** Tag for an individual node�s location Y */
	private static final String TAG_NODE_LOCATION_Y = "locY";
	/** Tag for an individual node�s level KEY*/
	private static final String TAG_NODE_LEVEL_KEY = "levelKey";

	///-----Constructors-----
	// None

	///-----Functions-----
	/** Returns an ArrayList of {@link MapNodes} loaded from an input stream.
	 * @param aIn - The input stream to read from
	 * @return ArrayList<MapNode> - Array of parsed map nodes
	 */
	public static ArrayList<MapNode> parseFile(InputStream aIn)
	{
		try 
		{
			XmlPullParser aParser = Xml.newPullParser();
			aParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			aParser.setInput(aIn, null);
			aParser.nextTag();
			return parseNodes(aParser);
		} 
		catch(XmlPullParserException e)
		{
			Log.e(TAG, "The XmlPullParserException was caught.");
		}
		catch(IOException i)
		{
			Log.e(TAG, "The IOException was caught.");
		}
		finally 
		{
			try
			{
				aIn.close();
			}
			catch(IOException i)
			{
				Log.e(TAG, "The IOException was caught from the aIn.close().");
			}
		}
		
		return null;
	}

	/** Reads all the nodes
	 * @param aParser - XML parser to read from
	 * @return ArrayList<MapNode> - Array of {@link MapNode}s
	 */

	private static ArrayList<MapNode> parseNodes(XmlPullParser aParser) throws XmlPullParserException, IOException
	{
		aParser.require(XmlPullParser.START_TAG, NAMESPACE, TAG_NODES_ARRAY);
		ArrayList<MapNode> nodeList = new ArrayList<MapNode>(); 
		//Create a new node until we hit the </nodes> end tag 
		while(aParser.next() != XmlPullParser.END_TAG)
		{
			//Call the parseNode function to create a new MapNode from each segment of the data
			MapNode mNode = parseNode(aParser);
			//Add the newly created MapNode into our array of MapNodes
			nodeList.add(mNode);
		}	
		aParser.require(XmlPullParser.END_TAG, NAMESPACE, TAG_NODES_ARRAY);
		return nodeList;
	}


	private static MapNode parseNode(XmlPullParser aParser) throws XmlPullParserException, IOException
	{
		
		aParser.require(XmlPullParser.START_TAG, NAMESPACE, TAG_NODE);
		float xVal = readFloat(aParser,TAG_NODE_LOCATION_X);
		float yVal = readFloat(aParser,TAG_NODE_LOCATION_Y);
		String levelKey = readString(aParser, TAG_NODE_LEVEL_KEY);
		aParser.require(XmlPullParser.END_TAG, NAMESPACE, TAG_NODE);
		//Create and return a new MapNode created with the parsed data
		return new MapNode(xVal,yVal,levelKey);
	}

	private static float readFloat(XmlPullParser aParser, String aInsideTag) throws XmlPullParserException, IOException
	{
		aParser.require(XmlPullParser.START_TAG, NAMESPACE, aInsideTag);
		String val = aParser.getText();
		float floatVal = Float.parseFloat(val); 
		aParser.require(XmlPullParser.END_TAG, NAMESPACE, aInsideTag);
		return floatVal;
	}

	private static String readString(XmlPullParser aParser, String aInsideTag) throws XmlPullParserException, IOException
	{
		aParser.require(XmlPullParser.START_TAG, NAMESPACE, aInsideTag);
		String stringRead = aParser.getText();
		aParser.require(XmlPullParser.END_TAG, NAMESPACE, aInsideTag);
		return stringRead; 
	}

}