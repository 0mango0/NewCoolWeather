package app.li.com.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Lee on 2017/7/14.
 */

public class Now {
    @SerializedName("tmp")
    public String temperature;
    @SerializedName("cond")
    public More more;

    public class More{
        @SerializedName("txt")
        public String info;
    }


}
