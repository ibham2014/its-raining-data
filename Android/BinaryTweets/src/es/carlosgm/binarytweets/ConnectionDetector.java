package es.carlosgm.binarytweets;

import android.content.Context;
/**
 * Code from http://www.androidhive.info/2012/09/android-twitter-oauth-connect-tutorial/
 */


import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionDetector {
	
	private Context _context;
	  
    public ConnectionDetector(Context context){
        this._context = context;
    }
  
    /**
     * Checking for all possible Internet providers
     * **/
    public boolean isConnectingToInternet(){
        ConnectivityManager connectivity = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
          if (connectivity != null)
          {
              NetworkInfo[] info = connectivity.getAllNetworkInfo();
              if (info != null)
                  for (int i = 0; i < info.length; i++)
                      if (info[i].getState() == NetworkInfo.State.CONNECTED)
                      {
                          return true;
                      }
          }
          return false;
    }

}
