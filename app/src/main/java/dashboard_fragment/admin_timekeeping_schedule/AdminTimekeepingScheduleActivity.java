package dashboard_fragment.admin_timekeeping_schedule;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom08_quanlyphongkham.BaseActivity;
import com.example.nhom08_quanlyphongkham.R;
import com.example.nhom08_quanlyphongkham.UserProfile;
import com.example.nhom08_quanlyphongkham.uilogin.ProfileRepository;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminTimekeepingScheduleActivity extends BaseActivity {

    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private TimekeepingScheduleRepository repository;
    private ProfileRepository profileRepository;

    private LocalDate weekStart;
    private int selectedDayIndex;
    private final List<TimekeepingItem> weekShifts = new ArrayList<>();
    private final List<ShiftStaffAdapter.StaffOption> allStaff = new ArrayList<>();

    private TextView tvWeekRange;
    private TextView tvStatAssigned;
    private TextView tvStatPresent;
    private TextView tvStatPending;
    private TextView tvStatAbsent;
    private ProgressBar progressBar;
    private WeekDayAdapter weekDayAdapter;

    private final View[] shiftCards = new View[3];
    private final RecyclerView[] shiftLists = new RecyclerView[3];
    private final TextView[] shiftEmptyTexts = new TextView[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_timekeeping_schedule);

        repository = new TimekeepingScheduleRepository(this);
        profileRepository = new ProfileRepository(this);

        weekStart = LocalDate.now(ZONE).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        selectedDayIndex = LocalDate.now(ZONE).getDayOfWeek().getValue() - 1;

        bindViews();
        setupWeekDayStrip();
        setupShiftCards();
        setupHeaderActions();
        loadStaff();
        loadWeekShifts();
    }

    private void bindViews() {
        tvWeekRange = findViewById(R.id.tvScheduleWeekRange);
        tvStatAssigned = findViewById(R.id.tvStatAssigned);
        tvStatPresent = findViewById(R.id.tvStatPresent);
        tvStatPending = findViewById(R.id.tvStatPending);
        tvStatAbsent = findViewById(R.id.tvStatAbsent);
        progressBar = findViewById(R.id.progressSchedule);
    }

    private void setupWeekDayStrip() {
        RecyclerView rvDays = findViewById(R.id.rvScheduleDays);
        weekDayAdapter = new WeekDayAdapter();
        rvDays.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvDays.setAdapter(weekDayAdapter);
        weekDayAdapter.setOnDaySelectedListener((position, date) -> {
            selectedDayIndex = position;
            refreshDayView();
        });
        updateWeekDayStrip();
    }

    private void setupShiftCards() {
        int[] cardIds = {R.id.cardShiftMorning, R.id.cardShiftAfternoon, R.id.cardShiftEvening};
        int[] listIds = {R.id.rvShiftMorningStaff, R.id.rvShiftAfternoonStaff, R.id.rvShiftEveningStaff};
        int[] emptyIds = {R.id.tvShiftMorningEmpty, R.id.tvShiftAfternoonEmpty, R.id.tvShiftEveningEmpty};
        ShiftSlot[] slots = ShiftSlot.all();

        for (int i = 0; i < 3; i++) {
            shiftCards[i] = findViewById(cardIds[i]);
            shiftLists[i] = findViewById(listIds[i]);
            shiftEmptyTexts[i] = findViewById(emptyIds[i]);
            shiftLists[i].setLayoutManager(new LinearLayoutManager(this));
            shiftLists[i].setNestedScrollingEnabled(false);

            final ShiftSlot slot = slots[i];
            shiftCards[i].setOnClickListener(v -> showAssignBottomSheet(slot));
        }
    }

    private void setupHeaderActions() {
        findViewById(R.id.btnBackSchedule).setOnClickListener(v -> finish());
        findViewById(R.id.btnRefreshSchedule).setOnClickListener(v -> loadWeekShifts());
        findViewById(R.id.btnPrevWeek).setOnClickListener(v -> {
            weekStart = weekStart.minusWeeks(1);
            selectedDayIndex = 0;
            updateWeekDayStrip();
            loadWeekShifts();
        });
        findViewById(R.id.btnNextWeek).setOnClickListener(v -> {
            weekStart = weekStart.plusWeeks(1);
            selectedDayIndex = 0;
            updateWeekDayStrip();
            loadWeekShifts();
        });
    }

    private void updateWeekDayStrip() {
        List<LocalDate> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            days.add(weekStart.plusDays(i));
        }
        weekDayAdapter.setDays(days, selectedDayIndex);
        updateWeekRangeLabel();
    }

    private void updateWeekRangeLabel() {
        LocalDate weekEnd = weekStart.plusDays(6);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault());
        tvWeekRange.setText(weekStart.format(fmt) + " — " + weekEnd.format(fmt));
    }

    private void loadStaff() {
        profileRepository.getListProfile("neq.Quản trị viên", "eq.Đang hoạt động")
                .enqueue(new Callback<List<UserProfile>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<UserProfile>> call,
                                           @NonNull Response<List<UserProfile>> response) {
                        allStaff.clear();
                        if (response.isSuccessful() && response.body() != null) {
                            for (UserProfile p : response.body()) {
                                allStaff.add(new ShiftStaffAdapter.StaffOption(
                                        p.getID(),
                                        p.getHo_ten() != null ? p.getHo_ten() : "Nhân viên",
                                        p.getChuc_vu() != null ? p.getChuc_vu() : ""
                                ));
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<UserProfile>> call, @NonNull Throwable t) {
                        Toast.makeText(AdminTimekeepingScheduleActivity.this,
                                "Không thể tải danh sách nhân viên", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadWeekShifts() {
        setLoading(true);
        repository.getShiftsForWeek(weekStart).enqueue(new Callback<List<TimekeepingItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<TimekeepingItem>> call,
                                   @NonNull Response<List<TimekeepingItem>> response) {
                setLoading(false);
                weekShifts.clear();
                if (response.isSuccessful() && response.body() != null) {
                    weekShifts.addAll(response.body());
                } else {
                    Toast.makeText(AdminTimekeepingScheduleActivity.this,
                            "Không thể tải lịch làm việc", Toast.LENGTH_SHORT).show();
                }
                refreshDayView();
            }

            @Override
            public void onFailure(@NonNull Call<List<TimekeepingItem>> call, @NonNull Throwable t) {
                setLoading(false);
                Toast.makeText(AdminTimekeepingScheduleActivity.this,
                        "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
                refreshDayView();
            }
        });
    }

    private void refreshDayView() {
        LocalDate selectedDate = weekStart.plusDays(selectedDayIndex);
        List<TimekeepingItem> dayShifts = filterShiftsForDate(selectedDate);

        updateStats(dayShifts);
        ShiftSlot[] slots = ShiftSlot.all();
        List<TimekeepingItem> unmatchedItems = new ArrayList<>(dayShifts);
        List<List<TimekeepingItem>> slotItemsList = new ArrayList<>();
        for (int i = 0; i < slots.length; i++) {
            List<TimekeepingItem> slotItems = filterShiftsForSlot(dayShifts, slots[i]);
            slotItemsList.add(slotItems);
            unmatchedItems.removeAll(slotItems);
        }

        if (!unmatchedItems.isEmpty() && hasNoVisibleShiftItems(slotItemsList)) {
            slotItemsList.get(0).addAll(unmatchedItems);
        }

        for (int i = 0; i < slots.length; i++) {
            renderShiftSection(i, slots[i], slotItemsList.get(i));
        }
    }

    private boolean hasNoVisibleShiftItems(List<List<TimekeepingItem>> slotItemsList) {
        for (List<TimekeepingItem> items : slotItemsList) {
            if (!items.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void updateStats(List<TimekeepingItem> dayShifts) {
        int assigned = dayShifts.size();
        int present = 0;
        int pending = 0;
        int absent = 0;
        for (TimekeepingItem item : dayShifts) {
            String status = item.getStatus();
            if ("Vắng".equals(status)) absent++;
            else if ("Chưa checkin".equals(status)) pending++;
            else present++;
        }
        tvStatAssigned.setText(String.valueOf(assigned));
        tvStatPresent.setText(String.valueOf(present));
        tvStatPending.setText(String.valueOf(pending));
        tvStatAbsent.setText(String.valueOf(absent));
    }

    private void renderShiftSection(int index, ShiftSlot slot, List<TimekeepingItem> items) {
        RecyclerView list = shiftLists[index];
        TextView empty = shiftEmptyTexts[index];

        if (items.isEmpty()) {
            list.setVisibility(View.GONE);
            empty.setVisibility(View.VISIBLE);
            empty.setText("Chưa phân ca — chạm để thêm nhân viên");
        } else {
            empty.setVisibility(View.GONE);
            list.setVisibility(View.VISIBLE);
            ShiftStaffAdapter adapter = new ShiftStaffAdapter();
            adapter.setMode(ShiftStaffAdapter.Mode.ASSIGNED);
            adapter.setOnStaffActionListener(new ShiftStaffAdapter.OnStaffActionListener() {
                @Override
                public void onRemove(TimekeepingItem item) {
                    if (!"Chưa checkin".equals(item.getStatus())) {
                        Toast.makeText(AdminTimekeepingScheduleActivity.this,
                                "Chỉ xóa được ca chưa checkin", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    removeShift(item, null, slot, null, null, null, null, null);
                }
                @Override
                public void onAdd(ShiftStaffAdapter.StaffOption staff) {}
            });
            // HẾT
            adapter.setAssignedItems(items);
            list.setAdapter(adapter);
        }
    }

    private void showAssignBottomSheet(ShiftSlot slot) {
        LocalDate selectedDate = weekStart.plusDays(selectedDayIndex);
        List<TimekeepingItem> assigned = filterShiftsForSlot(filterShiftsForDate(selectedDate), slot);

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_assign_shift, null);
        dialog.setContentView(sheetView);

        TextView tvShiftTitle = sheetView.findViewById(R.id.tvSheetShiftTitle);
        TextView tvShiftDate = sheetView.findViewById(R.id.tvSheetShiftDate);
        TextView tvAssignedHeader = sheetView.findViewById(R.id.tvAssignedHeader);
        TextView tvAvailableHeader = sheetView.findViewById(R.id.tvAvailableHeader);
        RecyclerView rvAssigned = sheetView.findViewById(R.id.rvAssignedStaff);
        RecyclerView rvAvailable = sheetView.findViewById(R.id.rvAvailableStaff);
        View layoutAssignedEmpty = sheetView.findViewById(R.id.layoutAssignedEmpty);
        tvShiftTitle.setText(slot.getLabel() + "  " + slot.getTimeRange());
        tvShiftTitle.setTextColor(Color.parseColor(slot.getAccentColor()));

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", new Locale("vi", "VN"));
        tvShiftDate.setText(selectedDate.format(dateFmt));

        rvAssigned.setLayoutManager(new LinearLayoutManager(this));
        rvAvailable.setLayoutManager(new LinearLayoutManager(this));

        final ShiftStaffAdapter[] assignedAdapterRef = new ShiftStaffAdapter[1];
        final ShiftStaffAdapter[] availableAdapterRef = new ShiftStaffAdapter[1];

        ShiftStaffAdapter.OnStaffActionListener actionListener = new ShiftStaffAdapter.OnStaffActionListener() {
            @Override
            public void onRemove(TimekeepingItem item) {
                if (!"Chưa checkin".equals(item.getStatus())) {
                    Toast.makeText(AdminTimekeepingScheduleActivity.this,
                            "Chỉ xóa được ca chưa checkin", Toast.LENGTH_SHORT).show();
                    return;
                }
                removeShift(item, dialog, slot, assignedAdapterRef[0], availableAdapterRef[0],
                        tvAssignedHeader, tvAvailableHeader, layoutAssignedEmpty);
            }

            @Override
            public void onAdd(ShiftStaffAdapter.StaffOption staff) {
                addShift(staff, slot, selectedDate, dialog, assignedAdapterRef[0], availableAdapterRef[0],
                        tvAssignedHeader, tvAvailableHeader, layoutAssignedEmpty);
            }
        };

        ShiftStaffAdapter assignedAdapter = new ShiftStaffAdapter();
        assignedAdapter.setMode(ShiftStaffAdapter.Mode.ASSIGNED);
        assignedAdapter.setOnStaffActionListener(actionListener);
        assignedAdapterRef[0] = assignedAdapter;
        rvAssigned.setAdapter(assignedAdapter);

        ShiftStaffAdapter availableAdapter = new ShiftStaffAdapter();
        availableAdapter.setMode(ShiftStaffAdapter.Mode.AVAILABLE);
        availableAdapter.setOnStaffActionListener(actionListener);
        availableAdapterRef[0] = availableAdapter;
        rvAvailable.setAdapter(availableAdapter);

        refreshBottomSheetLists(assigned, assignedAdapter, availableAdapter,
                tvAssignedHeader, tvAvailableHeader, layoutAssignedEmpty);

        dialog.show();
    }

    private void refreshBottomSheetLists(List<TimekeepingItem> assigned,
                                         ShiftStaffAdapter assignedAdapter,
                                         ShiftStaffAdapter availableAdapter,
                                         TextView tvAssignedHeader,
                                         TextView tvAvailableHeader,
                                         View layoutAssignedEmpty) {
        assignedAdapter.setAssignedItems(assigned);
        Set<String> assignedIds = new HashSet<>();
        for (TimekeepingItem item : assigned) {
            if (item.getUserId() != null) assignedIds.add(item.getUserId());
        }

        List<ShiftStaffAdapter.StaffOption> available = new ArrayList<>();
        for (ShiftStaffAdapter.StaffOption staff : allStaff) {
            if (!assignedIds.contains(staff.getId())) {
                available.add(staff);
            }
        }
        availableAdapter.setAvailableStaff(available);

        tvAssignedHeader.setText("ĐÃ PHÂN CA (" + assigned.size() + ")");
        tvAvailableHeader.setText("NHÂN VIÊN CÓ THỂ THÊM (" + available.size() + ")");
        layoutAssignedEmpty.setVisibility(assigned.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void addShift(ShiftStaffAdapter.StaffOption staff, ShiftSlot slot, LocalDate date,
                          BottomSheetDialog dialog, ShiftStaffAdapter assignedAdapter,
                          ShiftStaffAdapter availableAdapter, TextView tvAssignedHeader,
                          TextView tvAvailableHeader, View layoutAssignedEmpty) {
        LocalDate selectedDate = weekStart.plusDays(selectedDayIndex);
        List<TimekeepingItem> dayShifts = filterShiftsForDate(selectedDate);
        for (TimekeepingItem existing : dayShifts) {
            if (staff.getId().equals(existing.getUserId()) && slot.matchesStartTime(existing.getStartTime())) {
                Toast.makeText(this, "Nhân viên đã được phân ca này", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        setLoading(true);
        repository.createShift(staff.getId(), slot, selectedDate).enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<okhttp3.ResponseBody> call,
                                   @NonNull Response<okhttp3.ResponseBody> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(AdminTimekeepingScheduleActivity.this,
                            "Đã phân ca cho " + staff.getName(), Toast.LENGTH_SHORT).show();

                    TimekeepingItem localItem = TimekeepingItem.createLocal(
                            staff.getId(),
                            staff.getName(),
                            staff.getRole(),
                            slot.buildStartIso(selectedDate),
                            slot.buildEndIso(selectedDate)
                    );
                    weekShifts.add(localItem);
                    refreshDayView();

                    loadWeekShiftsAndRefreshSheet(dialog, slot, assignedAdapter, availableAdapter,
                            tvAssignedHeader, tvAvailableHeader, layoutAssignedEmpty);
                } else {
                    Toast.makeText(AdminTimekeepingScheduleActivity.this,
                            "Không thể tạo ca (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<okhttp3.ResponseBody> call, @NonNull Throwable t) {
                setLoading(false);
                Toast.makeText(AdminTimekeepingScheduleActivity.this,
                        "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeShift(TimekeepingItem item, BottomSheetDialog dialog, ShiftSlot slot,
                             ShiftStaffAdapter assignedAdapter, ShiftStaffAdapter availableAdapter,
                             TextView tvAssignedHeader, TextView tvAvailableHeader,
                             View layoutAssignedEmpty) {
        setLoading(true);
        repository.deleteShift(item.getId()).enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<okhttp3.ResponseBody> call,
                                   @NonNull Response<okhttp3.ResponseBody> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(AdminTimekeepingScheduleActivity.this,
                            "Đã xóa ca làm việc", Toast.LENGTH_SHORT).show();
                    if (dialog != null && dialog.isShowing()) {
                        loadWeekShiftsAndRefreshSheet(dialog, slot, assignedAdapter, availableAdapter,
                                tvAssignedHeader, tvAvailableHeader, layoutAssignedEmpty);
                    }
                    else
                    {
                        weekShifts.remove(item);
                        refreshDayView();
                    }
                } else {
                    Toast.makeText(AdminTimekeepingScheduleActivity.this,
                            "Không thể xóa ca (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<okhttp3.ResponseBody> call, @NonNull Throwable t) {
                setLoading(false);
                Toast.makeText(AdminTimekeepingScheduleActivity.this,
                        "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadWeekShiftsAndRefreshSheet(BottomSheetDialog dialog, ShiftSlot slot,
                                               ShiftStaffAdapter assignedAdapter,
                                               ShiftStaffAdapter availableAdapter,
                                               TextView tvAssignedHeader, TextView tvAvailableHeader,
                                               View layoutAssignedEmpty) {
        repository.getShiftsForWeek(weekStart).enqueue(new Callback<List<TimekeepingItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<TimekeepingItem>> call,
                                   @NonNull Response<List<TimekeepingItem>> response) {
                weekShifts.clear();
                if (response.isSuccessful() && response.body() != null) {
                    weekShifts.addAll(response.body());
                }
                refreshDayView();
                if (dialog.isShowing()) {
                    LocalDate selectedDate = weekStart.plusDays(selectedDayIndex);
                    List<TimekeepingItem> assigned = filterShiftsForSlot(
                            filterShiftsForDate(selectedDate), slot);
                    refreshBottomSheetLists(assigned, assignedAdapter, availableAdapter,
                            tvAssignedHeader, tvAvailableHeader, layoutAssignedEmpty);
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<TimekeepingItem>> call, @NonNull Throwable t) {
                refreshDayView();
            }
        });
    }

    private List<TimekeepingItem> filterShiftsForDate(LocalDate date) {
        List<TimekeepingItem> result = new ArrayList<>();
        for (TimekeepingItem item : weekShifts) {
            if (matchesDate(item.getStartTime(), date) || matchesDate(item.getEndTime(), date)) {
                result.add(item);
            }
        }
        return result;
    }

    private List<TimekeepingItem> filterShiftsForSlot(List<TimekeepingItem> dayShifts, ShiftSlot slot) {
        List<TimekeepingItem> result = new ArrayList<>();
        for (TimekeepingItem item : dayShifts) {
            if (slot.matchesTime(item.getStartTime()) || slot.matchesTime(item.getEndTime())) {
                result.add(item);
            }
        }
        return result;
    }

    private LocalDate parseDate(String iso) {
        if (iso == null || iso.isEmpty()) return null;
        try {
            String formattedIso = iso.replace(" ", "T");
            try {
                return ZonedDateTime.parse(formattedIso)
                        .withZoneSameInstant(ZONE).toLocalDate();
            } catch (Exception e1) {
                return java.time.LocalDateTime.parse(formattedIso).toLocalDate();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean matchesDate(String iso, LocalDate date) {
        LocalDate parsedDate = parseDate(iso);
        if (date.equals(parsedDate)) {
            return true;
        }
        LocalDate utcDate = parseDateAssumingUtc(iso);
        return date.equals(utcDate);
    }

    private LocalDate parseDateAssumingUtc(String iso) {
        if (iso == null || iso.isEmpty()) return null;
        try {
            String formattedIso = iso.replace(" ", "T");
            return java.time.LocalDateTime.parse(formattedIso)
                    .atZone(java.time.ZoneOffset.UTC)
                    .withZoneSameInstant(ZONE)
                    .toLocalDate();
        } catch (Exception e) {
            return null;
        }
    }
    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
