package ru.deaper.dvpopov.wifigrabber;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class WifiTester extends Activity {

    Comparator<ScanResult> comparator;
    private class TheTask extends AsyncTask<String,String,String>
    {
        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            //super.onPostExecute(String result);
            // update textview here
            //textView.setText("Server message is "+result);
            Context context = getApplicationContext();
            CharSequence text = "Hello toast!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, result, duration);
            toast.show();
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try
            {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost method = new HttpPost(params[0]);
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                wifiList = mainWifi.getScanResults();
                nameValuePairs.add(new BasicNameValuePair("longtitude", Double.toString(lastKnownLocation.getLongitude())));
                nameValuePairs.add(new BasicNameValuePair("latitude", Double.toString(lastKnownLocation.getLatitude())));
                nameValuePairs.add(new BasicNameValuePair("accuracy", Double.toString(lastKnownLocation.getAccuracy())));

                for(int i = 0; i < wifiList.size(); i++){
                    nameValuePairs.add(new BasicNameValuePair("SSID[]", wifiList.get(i).SSID));
                    nameValuePairs.add(new BasicNameValuePair("BSSID[]", wifiList.get(i).BSSID));
                    nameValuePairs.add(new BasicNameValuePair("freq[]", new Integer(wifiList.get(i).frequency).toString()));
                    nameValuePairs.add(new BasicNameValuePair("params[]", wifiList.get(i).capabilities));
                    nameValuePairs.add(new BasicNameValuePair("level[]", new Integer(wifiList.get(i).level).toString()));

//                    sb.append(new Integer(i+1).toString() + ".");
//                    sb.append((wifiList.get(i)).toString());
//                    sb.append("\\n");
                }

//                nameValuePairs.add(new BasicNameValuePair("id", "12345"));
//                nameValuePairs.add(new BasicNameValuePair("stringdata", "AndDev is Cool!"));
                method.setEntity(new UrlEncodedFormEntity(nameValuePairs));


                HttpResponse response = httpclient.execute(method);
                HttpEntity entity = response.getEntity();
                if(entity != null){
                    return EntityUtils.toString(entity);
                }
                else{
                    return "No string.";
                }
            }
            catch(Exception e){
                return "Network problem";
            }

        }
    }


    TextView mainText;
    WifiManager mainWifi;
    WifiReceiver receiverWifi;
    List<ScanResult> wifiList;
    StringBuilder sb = new StringBuilder();
    ArrayList<HashMap<String, String>> arraylist = new ArrayList<HashMap<String, String>>();
    Location lastKnownLocation;

    public void onCreate(Bundle savedInstanceState) {
        comparator = new Comparator<ScanResult>() {
            @Override
            public int compare(ScanResult lhs, ScanResult rhs) {
                return (lhs.level <rhs.level ? 1 : (lhs.level==rhs.level ? 0 : -1));
            }
        };

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_tester);
        mainText = (TextView) findViewById(R.id.mainText);
        mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (mainWifi.isWifiEnabled() == false)
        {
            Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled", Toast.LENGTH_LONG).show();
           mainWifi.setWifiEnabled(true);
        }


        receiverWifi = new WifiReceiver();
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mainWifi.startScan();
        mainText.setText("\nStarting Scan...\n");
        Button button = (Button) findViewById(R.id.button);
        button.setEnabled(false);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Refresh");
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        mainWifi.startScan();
        mainText.setText("Starting Scan");
        return super.onMenuItemSelected(featureId, item);
    }

    protected void onPause() {
        unregisterReceiver(receiverWifi);
        super.onPause();
    }

    protected void onResume() {
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    public void onClick(View view) {
// Acquire a reference to the system Location Manager

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

/*
//
// Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.

            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

// Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
*/
        String locationProvider = LocationManager.NETWORK_PROVIDER;
// Or use LocationManager.GPS_PROVIDER

        lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);

        Context context = getApplicationContext();
        CharSequence text = "Hello toast!";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, new Double(lastKnownLocation.getLatitude()).toString(), duration);
        toast.show();
        AsyncTask task = new TheTask().execute("http://deaper.ru/gpsapp/data.php");
        //postData();

    }


    class WifiReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            sb = new StringBuilder("Networs: \n");
            wifiList = mainWifi.getScanResults();
            Collections.sort(wifiList, comparator);
            int j=0;
            for(int i = 0; i < wifiList.size(); i++){
                sb.append(new Integer(i+1).toString() + ".");
                int q = 2*(wifiList.get(i).level+100);
                sb.append(wifiList.get(i).SSID+ " " + q);
                sb.append("\n");
                j++;
             }
            if (j>0) {         Button button = (Button) findViewById(R.id.button);
                button.setEnabled(true);
            }
            mainText.setText(sb);
        }
    }
}
