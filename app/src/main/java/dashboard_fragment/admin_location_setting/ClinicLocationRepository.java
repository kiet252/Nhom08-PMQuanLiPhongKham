package dashboard_fragment.admin_location_setting;


import android.content.Context;

import com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class ClinicLocationRepository {

    private final ClinicLocationApiService api;

    public ClinicLocationRepository(Context context) {
        api = SupabaseClientProvider.getClient(context)
                .create(ClinicLocationApiService.class);
    }

    // Lấy vị trí hiện tại (lấy record đầu tiên)
    public Call<List<ClinicLocation>> getLocation() {
        return api.getLocations();
    }

    // Tạo mới
    public Call<List<ClinicLocation>> createLocation(double lat, double lng, int radiusMeters) {
        ClinicLocation loc = new ClinicLocation();
        loc.setLatitude(lat);
        loc.setLongitude(lng);
        loc.setMax_distance_meters(radiusMeters);
        return api.createLocation(loc);
    }

    // Cập nhật theo id
    public Call<List<ClinicLocation>> updateLocation(int id, double lat, double lng, int radiusMeters) {
        Map<String, Object> body = new HashMap<>();
        body.put("latitude", lat);
        body.put("longitude", lng);
        body.put("max_distance_meters", radiusMeters);
        return api.updateLocation("eq." + id, body);
    }
}