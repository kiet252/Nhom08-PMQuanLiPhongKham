package dashboard_fragment.admin_timekeeping_schedule;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public enum ShiftSlot {
    MORNING("Ca Sáng", "07:30", "11:30", 7, 30, 11, 30, "#4CAF50"),
    AFTERNOON("Ca Chiều", "13:30", "17:30", 13, 30, 17, 30, "#14B8D4"),
    EVENING("Ca Tối", "18:00", "21:00", 18, 0, 21, 0, "#9C27B0");

    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final String label;
    private final String displayStart;
    private final String displayEnd;
    private final int startHour;
    private final int startMinute;
    private final int endHour;
    private final int endMinute;
    private final String accentColor;

    ShiftSlot(String label, String displayStart, String displayEnd,
              int startHour, int startMinute, int endHour, int endMinute, String accentColor) {
        this.label = label;
        this.displayStart = displayStart;
        this.displayEnd = displayEnd;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endHour = endHour;
        this.endMinute = endMinute;
        this.accentColor = accentColor;
    }

    public String getLabel() {
        return label;
    }

    public String getTimeRange() {
        return displayStart + " – " + displayEnd;
    }

    public String getAccentColor() {
        return accentColor;
    }

    public ZonedDateTime buildStart(LocalDate date) {
        return ZonedDateTime.of(date, LocalTime.of(startHour, startMinute), ZONE);
    }

    public ZonedDateTime buildEnd(LocalDate date) {
        return ZonedDateTime.of(date, LocalTime.of(endHour, endMinute), ZONE);
    }

    public String buildStartIso(LocalDate date) {
        return buildStart(date).format(ISO);
    }

    public String buildEndIso(LocalDate date) {
        return buildEnd(date).format(ISO);
    }

    public boolean matchesStartTime(String startTimeIso) {
        if (startTimeIso == null || startTimeIso.isEmpty()) return false;
        try {
            String formatted = startTimeIso.replace(" ", "T");
            LocalTime time;
            try {
                // Xử lý chuỗi có timezone (vd: "2026-06-11T07:30:00+07:00")
                time = ZonedDateTime.parse(formatted).withZoneSameInstant(ZONE).toLocalTime();
            } catch (Exception e1) {
                // Fallback: Supabase trả về không có timezone (vd: "2026-06-11T07:30:00")
                time = java.time.LocalDateTime.parse(formatted).toLocalTime();
            }
            return time.getHour() == startHour && time.getMinute() == startMinute;
        } catch (Exception e) {
            return false;
        }
    }

    public static ShiftSlot[] all() {
        return values();
    }
}
