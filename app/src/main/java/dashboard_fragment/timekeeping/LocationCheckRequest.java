package dashboard_fragment.timekeeping;

import com.google.gson.annotations.SerializedName;

public class LocationCheckRequest {
    @SerializedName("user_lat") public double userLat;
    @SerializedName("user_lng") public double userLng;
}