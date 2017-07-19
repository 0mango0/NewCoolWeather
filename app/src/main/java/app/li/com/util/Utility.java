package app.li.com.util;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import app.li.com.db.City;
import app.li.com.db.County;
import app.li.com.db.Province;
import app.li.com.gson.Weather;

/**
 * Created by Lee on 2017/7/13.
 */

public class Utility {

    /**
     * 解析和处理服务器返回的省级数据
     * @param response
     * @return
     */
    public static boolean handleProvinceResponse(String response){

        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allProvince=new JSONArray(response);
                for (int i = 0; i < allProvince.length(); i++) {
                    JSONObject provinceObject=allProvince.getJSONObject(i);
                    Province province=new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }

                return true;

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

        return false;
    }

    /**
     * 解析和处理服务器返回的市级数据
     * @param response
     * @param provinceId
     * @return
     */
    public static boolean handleCityResponse(String response,int provinceId){

        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allCities=new JSONArray(response);
                for (int i = 0; i < allCities.length(); i++) {

                    JSONObject cityObject=allCities.getJSONObject(i);
                    City city=new City();

                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    Log.v("TAG","========cityObject.getString(\"name\")=======>"+cityObject.getString("name"));
                    Log.v("TAG","========cityObject.getInt(\"id\")=======>"+cityObject.getInt("id"));
                    Log.v("TAG","========provinceId=======>"+provinceId);

                    city.save();
                }

                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * 解析和处理服务器返回的市级数据
     * @param response
     * @param cityId
     * @return
     */
    public  static  boolean handleCoutyResponse(String response,int cityId){

        if (!TextUtils.isEmpty(response)){

            try {
                JSONArray allCouties=new JSONArray(response);
                for (int i = 0; i < allCouties.length(); i++) {

                    JSONObject countyObject=allCouties.getJSONObject(i);
                    County county=new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    Log.v("TAG","========countyObject.getString=======>"+countyObject.getString("name"));
                    Log.v("TAG","========countyObject.getString=======>"+countyObject.getString("weather_id"));
                    Log.v("TAG","========cityId=======>"+cityId);
                    county.save();

                }

                return true;

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        return false;
    }


    /**
     * 将返回的JSON数据解析成Weather实体
     * @param response
     * @return
     */
    public static Weather handleWeatherResponese(String response){


        try {
            JSONObject jsonObject=new JSONObject(response);
            JSONArray jsonArray=jsonObject.getJSONArray("HeWeather");
            String weatherContent=jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,Weather.class);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }


}
