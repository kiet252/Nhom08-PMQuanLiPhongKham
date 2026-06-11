package dashboard_fragment.admin_timekeeping_schedule;

import android.content.Context;

import com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class TimekeepingScheduleRepository {

    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final TimekeepingApiService apiService;

    public TimekeepingScheduleRepository(Context context) {
        apiService = SupabaseClientProvider.getClient(context).create(TimekeepingApiService.class);
    }

    public Call<List<TimekeepingItem>> getShiftsForWeek(LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        ZonedDateTime rangeStart = weekStart.atStartOfDay(ZONE);
        ZonedDateTime rangeEnd = weekEnd.atTime(23, 59, 59).atZone(ZONE);

        String andFilter = String.format(
                "(start_time.gte.%s,start_time.lte.%s)",
                rangeStart.format(ISO),
                rangeEnd.format(ISO)
        );

        return apiService.getShiftsInRange(
                andFilter,
                "id,start_time,end_time,status,user_id,profiles(id,ho_ten,chuc_vu,anh_dai_dien)",
                "start_time.asc"
        );
    }

    public Call<ResponseBody> createShift(String userId, ShiftSlot slot, LocalDate date) {
        Map<String, Object> body = new HashMap<>();
        body.put("user_id", userId);
        body.put("start_time", slot.buildStartIso(date));
        body.put("end_time", slot.buildEndIso(date));
        body.put("status", "Chưa checkin");
        return apiService.createShift("return=minimal", body);
    }

    public Call<ResponseBody> deleteShift(long id) {
        return apiService.deleteShift("eq." + id);
    }
}
