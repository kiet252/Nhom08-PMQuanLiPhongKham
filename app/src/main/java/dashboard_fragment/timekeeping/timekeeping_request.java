package dashboard_fragment.timekeeping;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom08_quanlyphongkham.BaseActivity;
import com.example.nhom08_quanlyphongkham.R;
import com.example.nhom08_quanlyphongkham.uilogin.SharedPrefManager;
import com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class timekeeping_request extends BaseActivity {

	private TextInputEditText etViolationDate, etViolationTime, etExplanationReason, etViolationFixedStartTime, etViolationFixedEndTime;
	private RecyclerView recyclerEvidence;
	private MaterialButton btnSubmit;
	private ImageButton btnBack;

	private final List<Uri> evidenceUris = new ArrayList<>();
	private EvidenceAdapter evidenceAdapter;

	private final List<ca_lam_viec> caAllList = new ArrayList<>(); 
	private final List<ca_lam_viec> caFiltered = new ArrayList<>(); 
	private ca_lam_viec selectedCa = null;

	private TimekeepingRepository repository;

	private final ActivityResultLauncher<Intent> pickImagesLauncher = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(), result -> {
				if (result.getResultCode() == RESULT_OK && result.getData() != null) {
					Intent data = result.getData();
					if (data.getClipData() != null) {
						ClipData clip = data.getClipData();
						for (int i = 0; i < clip.getItemCount(); i++) {
							Uri uri = clip.getItemAt(i).getUri();
							if (uri != null) {
								try {
									getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
								} catch (Exception e) {
									Log.e("timekeeping_request", "Permission error: " + e.getMessage());
								}
								evidenceUris.add(uri);
							}
						}
					} else if (data.getData() != null) {
						Uri uri = data.getData();
						try {
							getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
						} catch (Exception e) {
							Log.e("timekeeping_request", "Permission error: " + e.getMessage());
						}
						evidenceUris.add(uri);
					}
					evidenceAdapter.notifyDataSetChanged();
				}
			}
	);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timekeeping_request);

		repository = new TimekeepingRepository(this);

		initViews();
		setupActions();
		loadCaLamViecData();
	}

	private void loadCaLamViecData() {
		try {
			com.example.nhom08_quanlyphongkham.UserProfile local = SharedPrefManager.getInstance(this).getProfile();
			if (local != null && local.getID() != null) {
				repository.getCaLamViecListByStaffId(local.getID()).enqueue(new retrofit2.Callback<java.util.List<ca_lam_viec>>() {
					@Override
					public void onResponse(retrofit2.Call<java.util.List<ca_lam_viec>> call, retrofit2.Response<java.util.List<ca_lam_viec>> response) {
						if (response.isSuccessful() && response.body() != null) {
							caAllList.clear();
							caAllList.addAll(response.body());
							if (caAllList.isEmpty()) {
								Toast.makeText(timekeeping_request.this, "Không có ca làm việc nào để chọn", Toast.LENGTH_SHORT).show();
							}
						} else {
							android.util.Log.e("timekeeping_request", "Response not successful or body is null");
							try {
								String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
								android.util.Log.e("timekeeping_request", "Error body: " + errorBody);
							} catch (Exception e) {
								android.util.Log.e("timekeeping_request", "Could not read error body", e);
							}
							Toast.makeText(timekeeping_request.this, "Lỗi khi lấy dữ liệu ca làm việc (code: " + response.code() + ")", Toast.LENGTH_SHORT).show();
						}
					}

					@Override
					public void onFailure(retrofit2.Call<java.util.List<ca_lam_viec>> call, Throwable t) {
						Toast.makeText(timekeeping_request.this, "Kết nối lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initViews() {
		btnBack = findViewById(R.id.btnBack);
		etViolationDate = findViewById(R.id.etViolationDate);
		etViolationTime = findViewById(R.id.etViolationTime);
		etExplanationReason = findViewById(R.id.etExplanationReason);
		etViolationFixedStartTime = findViewById(R.id.etViolationFixedStartTime);
		etViolationFixedEndTime = findViewById(R.id.etViolationFixedEndTime);
		recyclerEvidence = findViewById(R.id.recyclerEvidence);
		btnSubmit = findViewById(R.id.btnSubmit);

		etViolationDate.setInputType(android.text.InputType.TYPE_NULL);
		etViolationTime.setInputType(android.text.InputType.TYPE_NULL);
		etViolationDate.setFocusable(false);
		etViolationTime.setFocusable(false);

		etViolationFixedStartTime.setInputType(android.text.InputType.TYPE_NULL);
		etViolationFixedEndTime.setInputType(android.text.InputType.TYPE_NULL);
		etViolationFixedStartTime.setFocusable(false);
		etViolationFixedEndTime.setFocusable(false);
		etViolationFixedStartTime.setClickable(true);
		etViolationFixedEndTime.setClickable(true);
		etViolationFixedStartTime.setCursorVisible(false);
		etViolationFixedEndTime.setCursorVisible(false);

		// simple dropdown types
		String[] types = new String[]{"Muộn", "Vắng", "Xin phép", "Khác"};
		ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, types);
		// set adapter


		GridLayoutManager gm = new GridLayoutManager(this, 3);
		recyclerEvidence.setLayoutManager(gm);
		evidenceAdapter = new EvidenceAdapter(this, evidenceUris);
		recyclerEvidence.setAdapter(evidenceAdapter);
	}

	private void setupActions() {
		btnBack.setOnClickListener(v -> {
			finish();
		});

		etViolationDate.setOnClickListener(v -> showDatePicker());
		etViolationTime.setOnClickListener(v -> showShiftPicker());
		etViolationFixedStartTime.setOnClickListener(v -> showTimePickerForStart());
		etViolationFixedEndTime.setOnClickListener(v -> showTimePickerForEnd());

		btnSubmit.setOnClickListener(v -> {
			String date = etViolationDate.getText() != null ? etViolationDate.getText().toString().trim() : "";
			String shift = etViolationTime.getText() != null ? etViolationTime.getText().toString().trim() : "";
			String reason = etExplanationReason.getText() != null ? etExplanationReason.getText().toString().trim() : "";

			if (date.isEmpty() || shift.isEmpty() || reason.isEmpty() || evidenceUris.isEmpty()) {
				Toast.makeText(this, "Vui lòng không để trống các trường, bao gồm minh chứng.", Toast.LENGTH_LONG).show();
				return;
			}

			sendEvidence();
		});
	}

	private void sendEvidence() {
		ProgressDialog pd = new ProgressDialog(this);
		pd.setMessage("Đang tải minh chứng lên...");
		pd.setCancelable(false);
		pd.show();

		ExecutorService executor = Executors.newSingleThreadExecutor();
		Handler handler = new Handler(Looper.getMainLooper());

		executor.execute(() -> {
			List<String> uploadedFileNames = new ArrayList<>();
			OkHttpClient client = new OkHttpClient();
			com.example.nhom08_quanlyphongkham.UserProfile profile = SharedPrefManager.getInstance(this).getProfile();
			String staffId = profile.getID();

			try {
				for (int i = 0; i < evidenceUris.size(); i++) {
					Uri uri = evidenceUris.get(i);
					InputStream is = getContentResolver().openInputStream(uri);
					if (is == null) continue;

					byte[] imageBytes = getBytes(is);
					is.close();

					String fileName = staffId + "_" + System.currentTimeMillis() + "_" + i + ".jpg";
					String uploadUrl = SupabaseClientProvider.SUPABASE_URL + "storage/v1/object/evidence/" + fileName;

					Request request = new Request.Builder()
							.url(uploadUrl)
							.post(RequestBody.create(imageBytes, MediaType.parse("image/jpeg")))
							.addHeader("apikey", SupabaseClientProvider.SUPABASE_ANON_KEY)
							.addHeader("Authorization", "Bearer " + SupabaseClientProvider.SUPABASE_ANON_KEY)
							.build();

					try (Response response = client.newCall(request).execute()) {

						String body = response.body() != null
								? response.body().string()
								: "null";

						Log.e("SUPABASE",
								"URL = " + uploadUrl +
										"\nCODE = " + response.code() +
										"\nMESSAGE = " + response.message() +
										"\nBODY = " + body);

						if (response.isSuccessful()) {
							// Build public URL for accessing the image
							String publicImageUrl = SupabaseClientProvider.SUPABASE_URL + "storage/v1/object/public/evidence/" + fileName;
							uploadedFileNames.add(publicImageUrl);
							Log.e("SUPABASE", "Public URL: " + publicImageUrl);
						} else {
							throw new IOException(
									"Lỗi upload: " + response.code() +
											"\n" + body
							);
						}
					}
				}

				handler.post(() -> {
					pd.dismiss();
					sendRequest(uploadedFileNames);
				});

			} catch (Exception e) {
				handler.post(() -> {
					pd.dismiss();
					Toast.makeText(this, "Lỗi khi gửi minh chứng: " + e.getMessage(), Toast.LENGTH_LONG).show();
					Log.e("timekeeping_request", "Evidence upload error", e);
				});
			}
		});
	}

	private void sendRequest(List<String> uploadedFileNames) {
		String reason = etExplanationReason.getText().toString().trim();
		String startTimeHHmm = etViolationFixedStartTime.getText().toString().trim();
		String endTimeHHmm = etViolationFixedEndTime.getText().toString().trim();
		String dateStr = etViolationDate.getText().toString().trim();
		com.example.nhom08_quanlyphongkham.UserProfile profile = SharedPrefManager.getInstance(this).getProfile();

		try {
			// Convert HH:mm times to full ISO 8601 timestamptz with proper date handling
			String[] timestamps = convertToTimestamptz(dateStr, startTimeHHmm, endTimeHHmm);
			String startTimestamptz = timestamps[0];
			String endTimestamptz = timestamps[1];

			fix_request request = new fix_request();
			request.setStaff_id(profile.getID());
			if (selectedCa != null) {
				request.setId_ca_lam(selectedCa.getId());
			}
			request.setLi_do(reason);
			request.setThoi_gian_vao_ca(startTimestamptz);
			request.setThoi_gian_ra_ca(endTimestamptz);
			request.setMinh_chung(uploadedFileNames);
			
			Log.e("sendRequest", "startTime: " + startTimestamptz);
			Log.e("sendRequest", "endTime: " + endTimestamptz);

		repository.createTimekeepingRequest(request).enqueue(new retrofit2.Callback<ResponseBody>() {
			@Override
			public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
				if (response.isSuccessful()) {
					Toast.makeText(timekeeping_request.this, "Gửi yêu cầu thành công!", Toast.LENGTH_SHORT).show();
					finish();
				} else {
					Toast.makeText(timekeeping_request.this, "Lỗi gửi yêu cầu (code: " + response.code() + ")", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {
				Toast.makeText(timekeeping_request.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
			}
		});
		} catch (Exception e) {
			Log.e("sendRequest", "Error converting timestamps", e);
			Toast.makeText(this, "Lỗi xử lý thời gian: " + e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	private byte[] getBytes(InputStream inputStream) throws IOException {
		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
		int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];

		int len = 0;
		while ((len = inputStream.read(buffer)) != -1) {
			byteBuffer.write(buffer, 0, len);
		}
		return byteBuffer.toByteArray();
	}

	/**
	 * Convert date + HH:mm times to ISO 8601 timestamptz format (YYYY-MM-DDTHH:mm:ss+07:00)
	 * Handles overnight shifts: if endTime < startTime, adds 1 day to the end date.
	 *
	 * @param dateStr Date in format "dd/MM/yyyy" (as displayed) or "yyyy-MM-dd" (internal)
	 * @param startTimeHHmm Start time in format "HH:mm"
	 * @param endTimeHHmm End time in format "HH:mm"
	 * @return Array of 2 strings: [startTimestamptz, endTimestamptz]
	 */
	private String[] convertToTimestamptz(String dateStr, String startTimeHHmm, String endTimeHHmm) throws Exception {
		// Parse the input date and times
		LocalDate date = parseDate(dateStr);
		LocalTime startTime = LocalTime.parse(startTimeHHmm, DateTimeFormatter.ofPattern("HH:mm"));
		LocalTime endTime = LocalTime.parse(endTimeHHmm, DateTimeFormatter.ofPattern("HH:mm"));

		// Create LocalDateTime for start (same day)
		LocalDateTime startDateTime = LocalDateTime.of(date, startTime);

		// Handle overnight logic: if endTime < startTime, add 1 day to end date
		LocalDate endDate = date;
		if (endTime.isBefore(startTime)) {
			endDate = date.plusDays(1);
		}
		LocalDateTime endDateTime = LocalDateTime.of(endDate, endTime);

		// Convert to OffsetDateTime with GMT+7 timezone
		ZoneOffset gmt7 = ZoneOffset.of("+07:00");
		OffsetDateTime startOffsetDateTime = startDateTime.atOffset(gmt7);
		OffsetDateTime endOffsetDateTime = endDateTime.atOffset(gmt7);

		// Format to ISO 8601 timestamptz
		String startTimestamptz = startOffsetDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
		String endTimestamptz = endOffsetDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

		return new String[]{startTimestamptz, endTimestamptz};
	}

	/**
	 * Parse date from either "dd/MM/yyyy" (display format) or "yyyy-MM-dd" (db format)
	 */
	private LocalDate parseDate(String dateStr) throws Exception {
		if (dateStr.contains("/")) {
			// Format: dd/MM/yyyy
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
			return LocalDate.parse(dateStr, formatter);
		} else {
			// Format: yyyy-MM-dd
			return LocalDate.parse(dateStr);
		}
	}

	private void showDatePicker() {
		final Calendar c = Calendar.getInstance();
		int y = c.get(Calendar.YEAR);
		int m = c.get(Calendar.MONTH);
		int d = c.get(Calendar.DAY_OF_MONTH);
		DatePickerDialog dp = new DatePickerDialog(this, (DatePicker view, int year, int month, int dayOfMonth) -> {
			Calendar sel = Calendar.getInstance();
			sel.set(year, month, dayOfMonth);
			SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
			String dateStr = fmt.format(sel.getTime());
			etViolationDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(sel.getTime()));

			caFiltered.clear();
			for (ca_lam_viec ca : caAllList) {
				String st = ca.getStart_time();
				if (st == null) continue;
				if (st.startsWith(dateStr)) {
					caFiltered.add(ca);
				}
			}

			// Sort shifts in ascending order (earliest first)
			caFiltered.sort(new Comparator<ca_lam_viec>() {
				@Override
				public int compare(ca_lam_viec ca1, ca_lam_viec ca2) {
					String st1 = ca1.getStart_time();
					String st2 = ca2.getStart_time();
					if (st1 == null || st2 == null) return 0;
					return st1.compareTo(st2); // ascending order (earliest first)
				}
			});

			// Auto-fill with the earliest shift if available
			selectedCa = null;
			etViolationTime.setText("");

			if (!caFiltered.isEmpty()) {
				// First element is the earliest shift after sorting
				selectedCa = caFiltered.get(0);
				etViolationTime.setText(selectedCa.getDisplayTime());
			}

			// Update UI to show available shifts for selected date
		}, y, m, d);
		dp.show();
	}



	private void showTimePickerForStart() {
		final Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);

		android.app.TimePickerDialog timePicker = new android.app.TimePickerDialog(this,
				(view, hourOfDay, minuteOfHour) -> {
					String timeStr = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
					etViolationFixedStartTime.setText(timeStr);
				}, hour, minute, true);
		timePicker.show();
	}

	private void showTimePickerForEnd() {
		final Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);

		android.app.TimePickerDialog timePicker = new android.app.TimePickerDialog(this,
				(view, hourOfDay, minuteOfHour) -> {
					String timeStr = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
					etViolationFixedEndTime.setText(timeStr);
				}, hour, minute, true);
		timePicker.show();
	}

	private void showShiftPicker() {
		if (caFiltered.isEmpty()) {
			Toast.makeText(this, "Vui lòng chọn ngày hoặc không có ca trong ngày này.", Toast.LENGTH_SHORT).show();
			return;
		}

		// Create custom adapter with dividers and non-bold text
		ShiftPickerAdapter adapter = new ShiftPickerAdapter(this, caFiltered);

		new androidx.appcompat.app.AlertDialog.Builder(this)
				.setTitle("Chọn ca làm (" + caFiltered.size() + " ca)")
				.setAdapter(adapter, (dialog, which) -> {
					selectedCa = caFiltered.get(which);
					etViolationTime.setText(selectedCa.getDisplayTime());
				})
				.setNegativeButton("Hủy", null)
				.show();
	}

	// Custom adapter for shift picker with dividers
	private static class ShiftPickerAdapter extends ArrayAdapter<ca_lam_viec> {
		ShiftPickerAdapter(Context context, List<ca_lam_viec> items) {
			super(context, 0, items);
		}

		@NonNull
		@Override
		public View getView(int position, View convertView, @NonNull ViewGroup parent) {
			LinearLayout layout = new LinearLayout(getContext());
			layout.setOrientation(LinearLayout.VERTICAL);

			// Create TextView for shift item
			TextView textView = new TextView(getContext());
			textView.setText(getItem(position).getDisplayTime());
			textView.setTextSize(16);
			textView.setTextColor(0xFF1E293B); // dark color, not bold
			int padding = (int) (16 * getContext().getResources().getDisplayMetrics().density);
			textView.setPadding(padding, padding, padding, padding);

			layout.addView(textView);

			// Add divider if not last item
			if (position < getCount() - 1) {
				View divider = new View(getContext());
				divider.setBackgroundColor(0xFFE2E8F0); // light gray
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT, 1);
				divider.setLayoutParams(params);
				layout.addView(divider);
			}

			return layout;
		}
	}

	// Adapter for evidence images + add button
	private static class EvidenceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
		private static final int TYPE_IMAGE = 1;
		private static final int TYPE_ADD = 2;
		private final List<Uri> items;
		private final Context ctx;

		EvidenceAdapter(Context ctx, List<Uri> items) {
			this.ctx = ctx;
			this.items = items;
		}

		@Override
		public int getItemViewType(int position) {
			if (position < items.size()) return TYPE_IMAGE;
			return TYPE_ADD; // last
		}

		@Override
		public int getItemCount() {
			return items.size() + 1; // images + add button
		}

		@NonNull
		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			if (viewType == TYPE_IMAGE) {
				ImageView iv = new ImageView(ctx);
				int pad = dpToPx(ctx, 4);
				iv.setPadding(pad, pad, pad, pad);
				iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
				int size = parent.getMeasuredWidth() / 3 - dpToPx(ctx, 12);
				RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(size, size);
				iv.setLayoutParams(lp);
				return new ImageVH(iv);
			} else {
				// add button
				LayoutInflater inflater = LayoutInflater.from(ctx);
				View v = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
				TextView tv = v.findViewById(android.R.id.text1);
				tv.setText("+ Thêm");
				tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
				tv.setBackgroundColor(0xFFECEFF1);
				int pad = dpToPx(ctx, 8);
				tv.setPadding(pad, pad, pad, pad);
				return new AddVH(v);
			}
		}

		@Override
		public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
			if (getItemViewType(position) == TYPE_IMAGE) {
				Uri uri = items.get(position);
				ImageVH vh = (ImageVH) holder;
				try {
					vh.imageView.setImageURI(uri);
				} catch (Exception e) {
					vh.imageView.setImageResource(android.R.drawable.ic_menu_report_image);
				}
				vh.imageView.setOnLongClickListener(v -> {
					// remove on long press
					items.remove(position);
					notifyDataSetChanged();
					return true;
				});
			} else {
				AddVH avh = (AddVH) holder;
				avh.itemView.setOnClickListener(v -> {
					// launch gallery intent
					Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
					intent.addCategory(Intent.CATEGORY_OPENABLE);
					intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
					intent.setType("image/*");
					// use activity from context if possible
					if (ctx instanceof timekeeping_request) {
						((timekeeping_request) ctx).pickImagesLauncher.launch(intent);
					} else if (ctx instanceof android.app.Activity) {
						// fallback
						((android.app.Activity) ctx).startActivityForResult(intent, 1001);
					}
				});
			}
		}

		private static int dpToPx(Context c, int dp) {
			float density = c.getResources().getDisplayMetrics().density;
			return Math.round(dp * density);
		}

		static class ImageVH extends RecyclerView.ViewHolder {
			ImageView imageView;
			ImageVH(@NonNull View itemView) {
				super(itemView);
				imageView = (ImageView) itemView;
			}
		}

		static class AddVH extends RecyclerView.ViewHolder {
			AddVH(@NonNull View itemView) { super(itemView); }
		}
	}

}
