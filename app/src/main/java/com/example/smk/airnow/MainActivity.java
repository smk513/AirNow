package com.example.smk.airnow;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smk.airnow.Data.Air;
import com.example.smk.airnow.Data.Station;
import com.example.smk.airnow.Location.GeoPoint;
import com.example.smk.airnow.Location.GeoTrans;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TextView tvGEO_X, tvGEO_Y;
    private TextView tvTM_X, tvTM_Y;
    private TextView tvAddress;
    private TextView tvStation;
    private TextView lblPM25, tvPM25;

    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION },
                    200
            );
        } else {
            Toast.makeText(getApplicationContext(), "테스트", Toast.LENGTH_LONG).show();

            tvGEO_X = (TextView) findViewById(R.id.tvGEO_X); tvGEO_Y = (TextView) findViewById(R.id.tvGEO_Y);
            tvTM_X = (TextView) findViewById(R.id.tvTM_X); tvTM_Y = (TextView) findViewById(R.id.tvTM_Y);
            tvAddress = (TextView) findViewById(R.id.tvAddress);
            tvStation = (TextView) findViewById(R.id.tvStation);
            lblPM25 = (TextView) findViewById(R.id.lblPM25); tvPM25 = (TextView) findViewById(R.id.tvPM25);

            GeoPoint geoPoint = GET_LOCATION();
            if (geoPoint.geoX() != 0) {
                tvGEO_X.setText(String.valueOf(geoPoint.geoX())); tvGEO_Y.setText(String.valueOf(geoPoint.geoY()));
                tvTM_X.setText(String.valueOf(geoPoint.tmX())); tvTM_Y.setText(String.valueOf(geoPoint.tmY()));
                tvAddress.setText(getAddress(this, Double.valueOf(tvGEO_X.getText().toString()), Double.valueOf(tvGEO_Y.getText().toString())));
                GetStation(geoPoint.tmX(), geoPoint.tmY());
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults) {
        if (requestCode == 200) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                tvGEO_X = (TextView) findViewById(R.id.tvGEO_X); tvGEO_Y = (TextView) findViewById(R.id.tvGEO_Y);
                tvTM_X = (TextView) findViewById(R.id.tvTM_X); tvTM_Y = (TextView) findViewById(R.id.tvTM_Y);
                tvAddress = (TextView) findViewById(R.id.tvAddress);
                tvStation = (TextView) findViewById(R.id.tvStation);
                lblPM25 = (TextView) findViewById(R.id.lblPM25); tvPM25 = (TextView) findViewById(R.id.tvPM25);

                GeoPoint geoPoint = GET_LOCATION();
                if (geoPoint.geoX() != 0) {
                    tvGEO_X.setText(String.valueOf(geoPoint.geoX())); tvGEO_Y.setText(String.valueOf(geoPoint.geoY()));
                    tvTM_X.setText(String.valueOf(geoPoint.tmX())); tvTM_Y.setText(String.valueOf(geoPoint.tmY()));
                    tvAddress.setText(getAddress(this, Double.valueOf(tvGEO_X.getText().toString()), Double.valueOf(tvGEO_Y.getText().toString())));
                    GetStation(geoPoint.tmX(), geoPoint.tmY());
                }
            } else {
                this.finish();
            }
        }
    }

    private void GetStation(double tmX, double tmY) {
        Station.placeIdTask asyncTask = new Station.placeIdTask(new Station.AsyncResponse() {
            public void processFinish(String Station) {
                tvStation.setText(Station);
                GetAir(Station);
            }
        });
        asyncTask.execute(Double.toString(tmX), Double.toString(tmY));
    }

    private void GetAir(String station) {
        Air.placeIdTask asyncTask = new Air.placeIdTask(new Air.AsyncResponse() {
            public void processFinish(String dataTime, String PM25) {
                lblPM25.setText("미세먼지 (" + dataTime + ")");
                tvPM25.setText(PM25 + "㎍/㎥");
            }
        });
        asyncTask.execute(station);
    }

    private GeoPoint GET_LOCATION() {
        GeoPoint result = new GeoPoint(0, 0, 0, 0);

        // LocationManager 객체 생성 (LOCATION_SERVICE 사용)
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        boolean GPS_ENABLED = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (GPS_ENABLED  == false){
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        // GPSListener 객체 생성
        GPSListener gpsListener = new GPSListener();
        long minTime = 10000;
        float minDistance = 0;

        try {
            // GPS를 이용한 위치 요청
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    minTime,
                    minDistance,
                    gpsListener);

            // 네트워크를 이용한 위치 요청
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    minTime,
                    minDistance,
                    gpsListener);

            // 위치요청을 한 상태에서 위치추적되는 동안 먼저 최근 위치를 조회
            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation != null) {
                Double longitude = lastLocation.getLongitude();
                Double latitude = lastLocation.getLatitude();
                GeoPoint GEO = new GeoPoint(longitude, latitude);
                GeoPoint TM = GeoTrans.convert(GeoTrans.GEO, GeoTrans.TM, GEO);

                result = new GeoPoint(GEO.X(), GEO.Y(), TM.X(), TM.Y());
            }
        } catch(SecurityException ex) {
            ex.printStackTrace();
        }
        return result;
    }


    // LocationListener 정의
    private class GPSListener implements LocationListener {
        // LocationManager 에서 위치정보가 변경되면 호출
        public void onLocationChanged(Location location) {
            Toast.makeText(getApplicationContext(), "위치 변경됨.", Toast.LENGTH_LONG).show();

            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();
            GeoPoint GEO = new GeoPoint(longitude, latitude);
            GeoPoint TM = GeoTrans.convert(GeoTrans.GEO, GeoTrans.TM, GEO);

            tvGEO_X.setText(String.valueOf(GEO.X())); tvGEO_Y.setText(String.valueOf(GEO.Y()));
            tvTM_X.setText(String.valueOf(TM.X())); tvTM_Y.setText(String.valueOf(TM.Y()));
            tvAddress.setText(getAddress(getApplicationContext(), GEO.X(), GEO.Y()));
            GetStation(TM.X(), TM.Y());

            locationManager.removeUpdates(this);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    public static String getAddress(Context mContext,double lng, double lat) {
        String nowAddress ="현재 위치를 확인 할 수 없습니다.";
        Geocoder geocoder = new Geocoder(mContext, Locale.KOREA);
        List<Address> address;
        try {
            if (geocoder != null) {
                // 세번째 파라미터는 좌표에 대해 주소를 리턴 받는 갯수로
                // 한좌표에 대해 두개이상의 이름이 존재할수있기에 주소배열을 리턴받기 위함
                address = geocoder.getFromLocation(lat, lng, 1);

                if (address != null && address.size() > 0) {
                    // 주소 받아오기
                    String currentLocationAddress = address.get(0).getAddressLine(0).toString();
                    nowAddress  = currentLocationAddress;
                }
            }

        } catch (IOException e) {
            Toast.makeText(mContext, "주소를 가져 올 수 없습니다.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        return nowAddress;
    }
}