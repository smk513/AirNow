package com.example.smk.airnow.Data;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Station {
    private static final String OPENAPI_URL =
            "http://openapi.airkorea.or.kr/openapi/services/rest/MsrstnInfoInqireSvc/getNearbyMsrstnList?serviceKey=%s&tmX=%s&tmY=%s&_returnType=json";
    private static final String OPENAPI_KEY = "J5k%2BhjEPEge4TVEDSTI%2BGqqFI704PrdGgDyPIZSihmrnKh3L8YOt4Nj7zgxtI70hWi%2B1e0T2dIaXwdDyx4Rtrg%3D%3D";

    public static class placeIdTask extends AsyncTask<String, Void, JSONObject> {
        public AsyncResponse delegate = null;//Call back interface
        public placeIdTask(AsyncResponse asyncResponse) {
            delegate = asyncResponse;//Assigning call back interfacethrough constructor
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject jsonStation = null;
            try {
                jsonStation = getNearbyMsrstn(params[0], params[1]);
            } catch (Exception e) {
                Log.d("Error", "Cannot process JSON results", e);
            }
            return jsonStation;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if(json != null){
                    JSONObject details = json.getJSONArray("list").getJSONObject(0);
                    String stationName = details.getString("stationName");
                    delegate.processFinish(stationName);
                }
            } catch (JSONException e) {
                Log.d("Error", e.toString());
            }
        }
    }

    public static JSONObject getNearbyMsrstn(String lat, String lon){
        try {
            URL url = new URL(String.format(OPENAPI_URL, OPENAPI_KEY, lat, lon));
            HttpURLConnection connection =
                    (HttpURLConnection)url.openConnection();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            StringBuffer json = new StringBuffer(1024);
            String tmp="";
            while((tmp=reader.readLine())!=null)
                json.append(tmp).append("\n");
            reader.close();

            JSONObject data = new JSONObject(json.toString());
            return data;
        }catch(Exception e){
            return null;
        }
    }

    public interface AsyncResponse {
        void processFinish(String output1);
    }
}
