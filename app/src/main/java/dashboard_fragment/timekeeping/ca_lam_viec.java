package dashboard_fragment.timekeeping;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Locale;

public class ca_lam_viec {

    private String id;

    private String start_time;
    private String end_time;

    public String getId() {
        return id;
    }
    public String getStart_time() {
        return start_time;
    }

    public String getEnd_time() {
        return end_time;
    }
    public String getDisplayTime() {
        return formatTime(start_time) + " - " + formatTime(end_time);
    }
    private String formatTime(String timestamp) {
        if (timestamp == null) return "--:--";
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return output.format(input.parse(timestamp));
        } catch (Exception e) {
            return timestamp;
        }
    }

}
