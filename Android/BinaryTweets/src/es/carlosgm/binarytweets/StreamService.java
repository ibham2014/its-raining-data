package es.carlosgm.binarytweets;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.OAuth2Token;
import twitter4j.conf.ConfigurationBuilder;
import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class StreamService extends IntentService implements StatusListener {
	
	/**
	 * Consumer key and secret key to connect to the API
	 */
	private String TWITTER_CONSUMER_KEY = "";
    private String TWITTER_CONSUMER_SECRET = "";
    
    /**
     * URL for the Arduino REST API
     */
    
    static String urlREST = "http://yuncloud.local/";
    
    /**
     * Twitter configuration
     */
    private ConfigurationBuilder builder;
	
    public String mSearchTerm = "#interactivosbham";
	static public String tweet = "test";
	static public String getTweet() {
		return tweet;
	}
	
	/**
	 * TODO: Flag for the streaming
	 */
	public boolean keepStreaming = true;
	public boolean isKeepStreaming() {
		return keepStreaming;
	}
	public void setKeepStreaming(boolean keepStreaming) {
		this.keepStreaming = keepStreaming;
	}
	
	/**
	 * Custom Intent to communicate to the UI
	 */
	Intent mIntent = new Intent(Constants.ACTION_URI);	
	
	public StreamService() {
		super("StreamService");
		
		builder = new ConfigurationBuilder();
        builder.setApplicationOnlyAuthEnabled(true);
        
        StreamService.urlREST = Constants.API_URL;
        
        this.TWITTER_CONSUMER_KEY = Constants.TWITTER_CONSUMER_KEY;
        this.TWITTER_CONSUMER_SECRET = Constants.TWITTER_CONSUMER_SECRET;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d("STREAM", "Reading tweets...");
		try {
			Twitter twitter = new TwitterFactory(builder.build()).getInstance();
        	twitter.setOAuthConsumer(TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET);
        	
        	OAuth2Token token = twitter.getOAuth2Token();
        	twitter.setOAuth2Token(token);        	
        	
    		Query query = new Query(mSearchTerm);
    		query.setCount(Constants.NUM_TWEETS);
    		QueryResult result;
    		do {
                result = twitter.search(query);
                List<Status> tweets = result.getTweets();
                for (Status tweet : tweets) {
                    StreamService.tweet = tweet.getText();
                    Log.d("STREAM", "Tweet read:" + tweet.getText());
                    
                    mIntent.putExtra(Constants.DATA_URI, "<b>@" +tweet.getUser().getName() +":</b> "+ tweet.getText());
    	    		LocalBroadcastManager.getInstance(this).sendBroadcast(mIntent);
                    
					this.sendBinary(tweet.getText());
					
					mIntent.putExtra(Constants.DATA_URI, "It's raining data\n#interactivosbham");
    	    		LocalBroadcastManager.getInstance(this).sendBroadcast(mIntent);
                }
            } while (/**(query = result.nextQuery()) != null ||**/ this.keepStreaming);
    	} catch (TwitterException te) {
            te.printStackTrace();
    	}
	}

	@Override
	public void onException(Exception arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDeletionNotice(StatusDeletionNotice arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onScrubGeo(long arg0, long arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStallWarning(StallWarning arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatus(Status arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTrackLimitationNotice(int arg0) {
		// TODO Auto-generated method stub
		
	}
	
	private void sendBinary(String tweet) {
		
		/**
		 * Turn the String into binary
		 */	

		
		String finalUrl = urlREST+"arduino/byte/";
		int numBytes = 8;
		byte[] bytes = tweet.getBytes();
	    StringBuilder binary = new StringBuilder();
	    
	    int k = 0;
	    for (byte b : bytes)
	    {
	    	int val = b;
	    	for (int i = 0; i < 8; i++)
	    	{
	    		binary.append((val & 128) == 0 ? 0 : 1);
	    		val <<= 1;
	    	}
	    	
	    	finalUrl = finalUrl + b + "/";
	    	
	    	k ++;
	    	if(k==numBytes) {
		    	try {
					this.getUrl(finalUrl);
					Log.d("STREAM", "IO success: " + finalUrl);
				} catch (IOException e) {
					Log.e("STREAM", "IO error: " + finalUrl);
				}
		    	
		    	try {
					Thread.sleep(Constants.TIME_SLEEP);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		    	k = 0;
		    	finalUrl = urlREST+"arduino/byte/";
	    	}
	    }
	    
	    String s = binary.toString();
	    Log.d("STREAM", "Binary tweet: " + s);
	    
	    
	    
	    /**
	     * Sending bits rather than bytes
	     */    
	    /***
	    
	    
	    int firstPin = 12;
	    int lastPin = 12;
	    int pin = firstPin;
	    for (int i = 0; i < s.length(); i++) {
        	if(s.charAt(i) == '0') {
	    		Log.d("STREAM", "Sending 0 to pin " + Integer.toString(pin));
	    		try {
					this.getUrl(urlREST+"arduino/digital/"+ Integer.toString(pin) +"/0");
				} catch (IOException e) {
					Log.e("STREAM", "Unable to send IO to API, check connection");
				}
	    		mIntent.putExtra(Constants.DATA_URI, false);
	    		LocalBroadcastManager.getInstance(this).sendBroadcast(mIntent);
	    	}
	    	else if(s.charAt(i) == '1') {
	    		Log.d("STREAM", "Sending 1 to pin " + Integer.toString(pin));
	    		try {
					this.getUrl(urlREST+"arduino/digital/"+ Integer.toString(pin) +"/1");
				} catch (IOException e) {
					Log.e("STREAM", "Unable to send IO to API, check connection");
				}
	    		mIntent.putExtra(Constants.DATA_URI, true);
	    		LocalBroadcastManager.getInstance(this).sendBroadcast(mIntent);
	    	}
	    	else {
	    		Log.e("STREAM", "Read neither 0 nor 1");
	    	}
        	pin++;
        	if(pin > lastPin)
        		pin = firstPin;
	    }
	    **/
	}
	
	/**
	 * Function to do a GET on an URL	
	 * @param myurl
	 * @return
	 * @throws IOException
	 */
	private String getUrl(String myurl) throws IOException {
	    InputStream is = null;
	    // Only display the first 100000 characters of the retrieved
	    // web page content.
	    int len = 100000;
	        
	    try {
	        URL url = new URL(myurl);
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setReadTimeout(10 * 1000);
	        conn.setConnectTimeout(15 * 1000);
	        conn.setRequestMethod("GET");
	        conn.setDoInput(true);
	        // Starts the query
	        conn.connect();
	        int response = conn.getResponseCode();
	        Log.d("HttpResponse", "The response is: " + response);
	        is = conn.getInputStream();

	        // Convert the InputStream into a string
	        String contentAsString = readIt(is, len);
	        return contentAsString;
	        
	    // Makes sure that the InputStream is closed after the app is
	    // finished using it.
	    } finally {
	        if (is != null) {
	            is.close();
	        } 
	    }
	}
	
	/**
	 * Converts an input stream into a String
	 * @param stream
	 * @param len
	 * @return
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	
	public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
	    Reader reader = null;
	    reader = new InputStreamReader(stream, "UTF-8");        
	    char[] buffer = new char[len];
	    reader.read(buffer);
	    return new String(buffer);
	}

}
