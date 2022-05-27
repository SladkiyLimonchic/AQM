package com.example.airqualitymonitoring;

import com.google.gson.annotations.SerializedName;

public class WebResponse {

    public static class Coord {
        @SerializedName("lon") public double lon;
        @SerializedName("lat") public double lat;
    }

    public static class Main {
        @SerializedName("aqi") public int aqi;
    }

    public static class Components {
            @SerializedName("co")    public double co;
            @SerializedName("no")    public double no;
            @SerializedName("no2")   public double no2;
            @SerializedName("o3")    public double o3;
            @SerializedName("so2")   public double so2;
            @SerializedName("pm2_5") public double pm2_5;
            @SerializedName("pm10")  public double pm10;
            @SerializedName("nh3")   public double nh3;
    }

    public static class Entry {
        @SerializedName("main")       public Main main;
        @SerializedName("components") public Components components;
        @SerializedName("dt")         public int dt;
    }

    @SerializedName("coord") public Coord coord;
    @SerializedName("list")  public Entry[] list;

}
