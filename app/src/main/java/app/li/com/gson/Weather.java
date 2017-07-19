package app.li.com.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Lee on 2017/7/14.
 */

public class Weather {

    public String status;

    public AQI aqi;

    public Basic basic;

    public Now now;

    public Suggestion suggestion;

    @SerializedName("daily_forecast")
    public List<Forecast>forecastList;


}
