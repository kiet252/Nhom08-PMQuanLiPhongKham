package dashboard_fragment.admin_location_setting;

import com.google.gson.annotations.SerializedName;

public class ClinicLocation {
    @SerializedName("id")
    private Integer id;

    @SerializedName("latitude")
    private Double latitude;

    @SerializedName("longitude")
    private Double longitude;

    @SerializedName("max_distance_meters")
    private Integer max_distance_meters;

    @SerializedName("related_fixed_time")
    private String related_fixed_time;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Integer getMax_distance_meters() { return max_distance_meters; }
    public void setMax_distance_meters(Integer max_distance_meters) { this.max_distance_meters = max_distance_meters; }

    public String getRelated_fixed_time() { return related_fixed_time; }
    public void setRelated_fixed_time(String related_fixed_time) { this.related_fixed_time = related_fixed_time; }
}