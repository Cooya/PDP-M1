package blindgps.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.StrictMode;

import java.io.IOException;

// class that allows to obtain the network connection status
public class NetworkConnectivityTest extends AsyncTask<Void, Void, Boolean> {
    private OnNetworkTestResponseListener listener;
    private ProgressDialog progressDialog;

    // determine if the app is connected to Internet network
    public NetworkConnectivityTest(OnNetworkTestResponseListener listener) {
        this.listener = listener;

        // authorize network requests on the main thread
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // check network connection (wifi or mobile network)
        ConnectivityManager cm = (ConnectivityManager) ((Context) this.listener).getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if(netInfo == null || !netInfo.isConnectedOrConnecting()) // none network connection
            this.listener.onNetworkTestResponse(false);
        else
            execute();
    }

    @Override
    protected void onPreExecute() {
        // display a progress dialog during the ping
        this.progressDialog = new ProgressDialog((Context) this.listener);
        this.progressDialog.setMessage("Checking network connection, please wait...");
        this.progressDialog.setIndeterminate(true);
        this.progressDialog.setCancelable(false);
        this.progressDialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        // execute a ping command to Google server to be sure to be connected
        try {
            Process process = Runtime.getRuntime().exec("ping -c 1 -W 5 www.google.com"); // timeout of 5 seconds
            return process.waitFor() == 0;
        } catch(IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        this.progressDialog.dismiss();
        this.listener.onNetworkTestResponse(result);
    }
}