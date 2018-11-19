package com.example.smk.airnow.Data;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Air {
    /*
    AsyncTask의 Params은 전달되는 값의 타입
        Progress는 작업의 진행 정도를 나타내는 값의 타입
        Result는 작업의 결과를 나타내는 값의 타입.
        필요하지 않은 타입 있다면 Void로 표시

    AsyncTask 클래스의 메소드
        doInBackground() 메소드는 쓰레드에서 실행
        doInBackground()가 반환하는 값은 onPostExecute()로 보내진다.
        */

    private static final String OPENAPI_URL =
            "http://openapi.airkorea.or.kr/openapi/services/rest/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty?serviceKey=%s&numOfRows=1&pageSize=1&pageNo=1&startPage=1&stationName=%s&dataTerm=DAILY&ver=1.3&_returnType=json";
    private static final String OPENAPI_KEY = "J5k%2BhjEPEge4TVEDSTI%2BGqqFI704PrdGgDyPIZSihmrnKh3L8YOt4Nj7zgxtI70hWi%2B1e0T2dIaXwdDyx4Rtrg%3D%3D";

    public static class placeIdTask extends AsyncTask<String, Void, JSONObject> {
        public AsyncResponse delegate = null;//Call back interface
        public placeIdTask(AsyncResponse asyncResponse) {
            delegate = asyncResponse;//Assigning call back interfacethrough constructor
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject jsonAir = null;
            try {
                jsonAir = getAir(params[0]);
            } catch (Exception e) {
                Log.d("Error", e.toString());
            }
            return jsonAir;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if(json != null){
                    JSONObject details = json.getJSONArray("list").getJSONObject(0);

                    String dataTime = details.getString("dataTime");
                    String pm25 = details.getString("pm25Value");

                    delegate.processFinish(dataTime, pm25);

                }
            } catch (JSONException e) {
                Log.d("Error", e.toString());
            }
        }
    }

    public static JSONObject getAir(String station){
        try {
            URL url = new URL(String.format(OPENAPI_URL, OPENAPI_KEY, station));
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
        void processFinish(String output1, String output2);
    }
}
