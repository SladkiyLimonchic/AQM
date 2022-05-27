package com.example.airqualitymonitoring;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AirActivity extends AppCompatActivity {

    private Activity context;

    private final OkHttpClient client = new OkHttpClient();
    private boolean retrieving = false;
    private final String appID = "c1eaf5ae36d34c2d0156412f24c22e80";

    private TextView coordinatesTextView;
    private TextView co_TextView;
    private TextView no_TextView;
    private TextView no2_TextView;
    private TextView o3_TextView;
    private TextView so2_TextView;
    private TextView pm2_5_TextView;
    private TextView pm10_TextView;
    private TextView nh3_TextView;
    private TextView gradeTextView;

    private ImageView gradeImageView;

    private ViewGroup cardViewGroup;
    private ViewGroup messageViewGroup;

    private final String[] grades = new String[] {
            "Хорошо",
            "Выше среднего",
            "Нормально",
            "Ниже среднего",
            "Плохо",
    };

    private final int[] grade_images_ids = new int[] {
            R.drawable.grade1,
            R.drawable.grade2,
            R.drawable.grade3,
            R.drawable.grade4,
            R.drawable.grade5,
    };

    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_air);

        coordinatesTextView = findViewById(R.id.text_location);
        co_TextView         = findViewById(R.id.text_view_co);
        no_TextView         = findViewById(R.id.text_view_no);
        no2_TextView        = findViewById(R.id.text_view_no2);
        o3_TextView         = findViewById(R.id.text_view_o3);
        so2_TextView        = findViewById(R.id.text_view_so2);
        pm2_5_TextView      = findViewById(R.id.text_view_pm2_5);
        pm10_TextView       = findViewById(R.id.text_view_pm10);
        nh3_TextView        = findViewById(R.id.text_view_nh3);
        gradeTextView       = findViewById(R.id.text_view_grade);
        gradeImageView      = findViewById(R.id.image_grade);
        cardViewGroup       = findViewById(R.id.group_card);
        messageViewGroup    = findViewById(R.id.group_message);

        context = this;

        findViewById(R.id.button_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.button_update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RetrieveData();
            }
        });

        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Предоставьте разрешения к геолокации, чтобы использовать местоположение!", Toast.LENGTH_SHORT).show();
            int code = 0;
            requestPermissions(new String[] { Manifest.permission.ACCESS_COARSE_LOCATION }, code);
            finish();
        }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Включите местоположение, чтобы использовать приложение!", Toast.LENGTH_SHORT).show();
            finish();
        }

        RetrieveData();
    }

    @SuppressLint("SetTextI18n")
    private void assignData(WebResponse data) {
        WebResponse.Coord coord = data.coord;
        coordinatesTextView.setText("[" + coord.lat + "] [" + coord.lon + "]");

        WebResponse.Entry entry = data.list[0];
        WebResponse.Components components = entry.components;

        co_TextView        .setText(components.co    + "μg/m3");
        no_TextView        .setText(components.no    + "μg/m3");
        no2_TextView       .setText(components.no2   + "μg/m3");
        o3_TextView        .setText(components.o3    + "μg/m3");
        so2_TextView       .setText(components.so2   + "μg/m3");
        pm2_5_TextView     .setText(components.pm2_5 + "μg/m3");
        pm10_TextView      .setText(components.pm10  + "μg/m3");
        nh3_TextView       .setText(components.nh3   + "μg/m3");

        int aqi = entry.main.aqi;
        gradeTextView .setText(grades[aqi - 1]);
        gradeImageView.setImageDrawable(AppCompatResources.getDrawable(context, grade_images_ids[aqi - 1]));
    }

    private void toast(String text) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(@NonNull Location location) {
            new RetrievingThread(location.getLatitude(), location.getLongitude(), this).start();
        }
    }

    private class RetrievingThread extends Thread {

        private final double latitude, longitude;
        private final LocationListener listener;

        public RetrievingThread(double latitude, double longitude, LocationListener listener) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.listener = listener;
        }

        // https://square.github.io/okhttp/recipes/
        @Override
        public void run() {
            try {
                Request request = new Request.Builder()
                        .url("https://api.openweathermap.org/data/2.5/air_pollution?lat=" + latitude + "&lon=" + longitude + "&appid=" + appID)
                        .build();
                Response response = client.newCall(request).execute();
                String responseString = response.body().string();
                WebResponse webResponse = new Gson().fromJson(responseString, WebResponse.class);
                Log.d("RESPONSE", responseString);
                assignData(webResponse);
            } catch (Exception e) {
                toast("Ошибка получения информации о воздухе!");
                Log.e("RETRIEVE_AQI", e.toString());
                finish();
            }
            retrieving = false;
            locationManager.removeUpdates(listener);
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cardViewGroup.setVisibility(View.VISIBLE);
                    messageViewGroup.setVisibility(View.GONE);
                }
            });
        }

    }

    private void RetrieveData() {
        if (!retrieving && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            retrieving = true;
            cardViewGroup.setVisibility(View.GONE);
            messageViewGroup.setVisibility(View.VISIBLE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new MyLocationListener());
        }
    }

}