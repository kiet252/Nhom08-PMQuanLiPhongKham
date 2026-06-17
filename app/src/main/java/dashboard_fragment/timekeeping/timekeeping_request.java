package dashboard_fragment.timekeeping;

import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AutoCompleteTextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom08_quanlyphongkham.BaseActivity;
import com.example.nhom08_quanlyphongkham.R;
import com.example.nhom08_quanlyphongkham.uilogin.SharedPrefManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class timekeeping_request extends BaseActivity {

	private TextInputEditText etViolationDate, etViolationTime, etExplanationReason;
	private AutoCompleteTextView actvViolationType; // actual widget in layout
	private RecyclerView recyclerEvidence;
	private MaterialButton btnSubmit;

	private final List<Uri> evidenceUris = new ArrayList<>();

	private EvidenceAdapter evidenceAdapter;

	private final List<ca_lam_viec> caAllList = new ArrayList<>(); // loaded all shifts for staff
	private final List<ca_lam_viec> caFiltered = new ArrayList<>(); // filtered by selected date
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
								getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
								evidenceUris.add(uri);
							}
						}
					} else if (data.getData() != null) {
						Uri uri = data.getData();
						getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
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

		// Load all shifts for the current user (store into caAllList)
		try {
			com.example.nhom08_quanlyphongkham.UserProfile local = SharedPrefManager.getInstance(this).getProfile();
			if (local != null && local.getID() != null) {
				repository.getCaLamViecList(local.getID()).enqueue(new retrofit2.Callback<java.util.List<ca_lam_viec>>() {
					@Override
					public void onResponse(retrofit2.Call<java.util.List<ca_lam_viec>> call, retrofit2.Response<java.util.List<ca_lam_viec>> response) {
						if (response.isSuccessful() && response.body() != null) {
							caAllList.clear();
							caAllList.addAll(response.body());
						}
					}

					@Override
					public void onFailure(retrofit2.Call<java.util.List<ca_lam_viec>> call, Throwable t) {
						// ignore silently
					}
				});
			}
		} catch (Exception e) { e.printStackTrace(); }
	}

	private void initViews() {
		actvViolationType = findViewById(R.id.actvViolationType);
		etViolationDate = findViewById(R.id.etViolationDate);
		etViolationTime = findViewById(R.id.etViolationTime);
		etExplanationReason = findViewById(R.id.etExplanationReason);
		recyclerEvidence = findViewById(R.id.recyclerEvidence);
		btnSubmit = findViewById(R.id.btnSubmit);

		// simple dropdown types
		String[] types = new String[]{"Muộn", "Vắng", "Xin phép", "Khác"};
		ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, types);
		// set adapter
		try {
			actvViolationType.setAdapter(typeAdapter);
		} catch (Exception ignored) {}

		// RecyclerView setup
		GridLayoutManager gm = new GridLayoutManager(this, 3);
		recyclerEvidence.setLayoutManager(gm);
		evidenceAdapter = new EvidenceAdapter(this, evidenceUris);
		recyclerEvidence.setAdapter(evidenceAdapter);
	}

	private void setupActions() {
		etViolationDate.setOnClickListener(v -> showDatePicker());
		etViolationTime.setOnClickListener(v -> showShiftPicker());

		btnSubmit.setOnClickListener(v -> {
			// validate all fields
			String type = actvViolationType.getText() != null ? actvViolationType.getText().toString().trim() : "";
			String date = etViolationDate.getText() != null ? etViolationDate.getText().toString().trim() : "";
			String shift = etViolationTime.getText() != null ? etViolationTime.getText().toString().trim() : "";
			String reason = etExplanationReason.getText() != null ? etExplanationReason.getText().toString().trim() : "";

			if (type.isEmpty() || date.isEmpty() || shift.isEmpty() || reason.isEmpty() || evidenceUris.isEmpty()) {
				Toast.makeText(this, "Vui lòng không để trống các trường, bao gồm minh chứng.", Toast.LENGTH_LONG).show();
				return;
			}

			// Temporary: just show toast completed
			Toast.makeText(this, "Hoàn thành", Toast.LENGTH_SHORT).show();
		});
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

			// filter caAllList by date (matching start_time's date prefix yyyy-MM-dd)
			caFiltered.clear();
			for (ca_lam_viec ca : caAllList) {
				String st = ca.getStart_time();
				if (st == null) continue;
				if (st.startsWith(dateStr)) {
					caFiltered.add(ca);
				}
			}
			// reset selected ca and shift display (only show after date selected)
			selectedCa = null;
			etViolationTime.setText("");
		}, y, m, d);
		dp.show();
	}

	private void showShiftPicker() {
		if (caFiltered.isEmpty()) {
			Toast.makeText(this, "Vui lòng chọn ngày hoặc không có ca trong ngày này.", Toast.LENGTH_SHORT).show();
			return;
		}
		String[] items = new String[caFiltered.size()];
		for (int i = 0; i < caFiltered.size(); i++) items[i] = caFiltered.get(i).getDisplayTime();

		new androidx.appcompat.app.AlertDialog.Builder(this)
				.setTitle("Chọn ca làm")
				.setItems(items, (dialog, which) -> {
					selectedCa = caFiltered.get(which);
					etViolationTime.setText(selectedCa.getDisplayTime());
				})
				.show();
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
