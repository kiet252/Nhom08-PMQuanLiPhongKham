package dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.prescription_logic;

import android.text.TextUtils;

import com.example.nhom08_quanlyphongkham.BuildConfig;
import com.google.genai.Client;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PrescriptionRecommendationAssistant {
    private static final String[] MODEL_CANDIDATES = {
            "gemini-2.5-flash",
            "gemini-2.0-flash",
            "gemini-3-flash-preview"
    };

    public interface RecommendationCallback {
        void onSuccess(RecommendationResult result);
        void onError(String message);
    }

    public static class RecommendationItem {
        private final MedicineItem medicine;
        private final int dose;
        private final String frequency;
        private final String duration;
        private final String note;
        private final String reason;

        public RecommendationItem(
                MedicineItem medicine,
                int dose,
                String frequency,
                String duration,
                String note,
                String reason
        ) {
            this.medicine = medicine;
            this.dose = dose;
            this.frequency = frequency;
            this.duration = duration;
            this.note = note;
            this.reason = reason;
        }

        public MedicineItem getMedicine() {
            return medicine;
        }

        public int getDose() {
            return dose;
        }

        public String getFrequency() {
            return frequency;
        }

        public String getDuration() {
            return duration;
        }

        public String getNote() {
            return note;
        }

        public String getReason() {
            return reason;
        }
    }

    public static class RecommendationResult {
        private final List<RecommendationItem> matchedItems;
        private final List<String> unmatchedNames;

        public RecommendationResult(
                List<RecommendationItem> matchedItems,
                List<String> unmatchedNames
        ) {
            this.matchedItems = matchedItems;
            this.unmatchedNames = unmatchedNames;
        }

        public List<RecommendationItem> getMatchedItems() {
            return matchedItems;
        }

        public List<String> getUnmatchedNames() {
            return unmatchedNames;
        }
    }

    private static class GeminiMedicineSuggestion {
        String ten_thuoc;
        String hoat_chat;
        int lieu_dung;
        String tan_suat;
        String thoi_gian;
        String ghi_chu;
        String ly_do;
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Gson gson = new Gson();

    public void recommend(
            String primaryDiagnosis,
            String additionalDiagnosis,
            String clinicalNote,
            List<MedicineItem> medicines,
            RecommendationCallback callback
    ) {
        if (TextUtils.isEmpty(BuildConfig.GEMINI_API_KEY)) {
            callback.onError("Chưa cấu hình GEMINI_API_KEY.");
            return;
        }

        if (TextUtils.isEmpty(primaryDiagnosis) || medicines == null || medicines.isEmpty()) {
            callback.onError("Thiếu chẩn đoán chính hoặc danh mục thuốc.");
            return;
        }

        executor.execute(() -> {
            try {
                Client client = new Client.Builder()
                        .apiKey(BuildConfig.GEMINI_API_KEY)
                        .build();

                String prompt = buildPrompt(
                        primaryDiagnosis,
                        additionalDiagnosis,
                        clinicalNote,
                        medicines
                );

                String text = generateWithFallback(client, prompt);
                if (TextUtils.isEmpty(text)) {
                    callback.onError("Gemini không trả về gợi ý thuốc.");
                    return;
                }

                callback.onSuccess(parseResult(text, medicines));
            } catch (Exception exception) {
                callback.onError("Không thể lấy gợi ý thuốc: " + exception.getMessage());
            }
        });
    }

    private String generateWithFallback(Client client, String prompt) throws Exception {
        Exception lastException = null;

        for (String model : MODEL_CANDIDATES) {
            try {
                var response = client.models.generateContent(
                        model,
                        prompt,
                        null
                );
                String text = response == null ? null : response.text();
                if (!TextUtils.isEmpty(text)) {
                    return text;
                }
            } catch (Exception exception) {
                lastException = exception;
                if (!isRetryableGeminiError(exception)) {
                    throw exception;
                }
            }
        }

        if (lastException != null) {
            throw lastException;
        }
        return "";
    }

    private boolean isRetryableGeminiError(Exception exception) {
        String message = exception.getMessage();
        if (message == null) {
            return false;
        }

        String lower = message.toLowerCase(Locale.getDefault());
        return lower.contains("503")
                || lower.contains("high demand")
                || lower.contains("unavailable")
                || lower.contains("overloaded")
                || lower.contains("deadline")
                || lower.contains("timeout");
    }

    private String buildPrompt(
            String primaryDiagnosis,
            String additionalDiagnosis,
            String clinicalNote,
            List<MedicineItem> medicines
    ) {
        StringBuilder inventory = new StringBuilder();
        int limit = Math.min(medicines.size(), 120);
        for (int i = 0; i < limit; i++) {
            MedicineItem item = medicines.get(i);
            inventory.append("- ten_thuoc: ")
                    .append(safe(item.getTen_thuoc()))
                    .append("; hoat_chat: ")
                    .append(safe(item.getHoat_chat()))
                    .append("; ham_luong: ")
                    .append(safe(item.getHam_luong()))
                    .append("; don_vi: ")
                    .append(safe(item.getDon_vi()))
                    .append("; chuc_nang: ")
                    .append(safe(item.getChuc_nang()))
                    .append("\n");
        }

        return "Ban la tro ly goi y don thuoc tham khao cho bac si phong kham Tai Mui Hong.\n"
                + "Chi goi y thuoc co trong DANH MUC THUOC ben duoi. Khong tu tao ten thuoc moi.\n"
                + "Quyet dinh ke don cuoi cung thuoc ve bac si.\n\n"
                + "CHAN DOAN CHINH: " + safe(primaryDiagnosis) + "\n"
                + "CHAN DOAN PHU: " + safe(additionalDiagnosis) + "\n"
                + "GHI CHU LAM SANG: " + safe(clinicalNote) + "\n\n"
                + "DANH MUC THUOC:\n" + inventory + "\n"
                + "Tra ve JSON thuan, khong markdown, theo dung cau truc:\n"
                + "{ \"medicines\": ["
                + "{ \"ten_thuoc\": \"\", \"hoat_chat\": \"\", \"lieu_dung\": 1, "
                + "\"tan_suat\": \"2 lần/ngày\", \"thoi_gian\": \"7 ngày\", "
                + "\"ghi_chu\": \"\", \"ly_do\": \"\" } ] }\n"
                + "lieu_dung la so nguyen theo don vi cua thuoc. "
                + "tan_suat chi chon mot trong: 1 lần/ngày, 2 lần/ngày, 3 lần/ngày, 4 lần/ngày, Sáng/Tối, Sáng/Trưa/Tối. "
                + "thoi_gian chi chon mot trong: 3 ngày, 5 ngày, 7 ngày, 10 ngày, 14 ngày, 1 tháng. "
                + "Goi y toi da 5 thuoc.";
    }

    private RecommendationResult parseResult(String rawText, List<MedicineItem> medicines) {
        String json = extractJson(rawText);
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonArray array = root.has("medicines") && root.get("medicines").isJsonArray()
                ? root.getAsJsonArray("medicines")
                : new JsonArray();

        List<RecommendationItem> matchedItems = new ArrayList<>();
        List<String> unmatchedNames = new ArrayList<>();
        Set<Integer> addedIds = new LinkedHashSet<>();

        for (JsonElement element : array) {
            if (!element.isJsonObject()) {
                continue;
            }

            GeminiMedicineSuggestion suggestion = gson.fromJson(element, GeminiMedicineSuggestion.class);
            MedicineItem matched = findMedicine(suggestion, medicines);

            if (matched == null) {
                String name = !TextUtils.isEmpty(suggestion.ten_thuoc)
                        ? suggestion.ten_thuoc
                        : suggestion.hoat_chat;
                if (!TextUtils.isEmpty(name)) {
                    unmatchedNames.add(name.trim());
                }
                continue;
            }

            if (addedIds.contains(matched.getId())) {
                continue;
            }
            addedIds.add(matched.getId());

            matchedItems.add(new RecommendationItem(
                    matched,
                    Math.max(1, suggestion.lieu_dung),
                    normalizeFrequency(suggestion.tan_suat),
                    normalizeDuration(suggestion.thoi_gian),
                    safe(suggestion.ghi_chu),
                    safe(suggestion.ly_do)
            ));
        }

        return new RecommendationResult(matchedItems, unmatchedNames);
    }

    private MedicineItem findMedicine(
            GeminiMedicineSuggestion suggestion,
            List<MedicineItem> medicines
    ) {
        String suggestedName = normalize(suggestion.ten_thuoc);
        String suggestedActive = normalize(suggestion.hoat_chat);

        for (MedicineItem medicine : medicines) {
            String medicineName = normalize(medicine.getTen_thuoc());
            if (!TextUtils.isEmpty(suggestedName) && medicineName.equals(suggestedName)) {
                return medicine;
            }
        }

        for (MedicineItem medicine : medicines) {
            String medicineName = normalize(medicine.getTen_thuoc());
            String active = normalize(medicine.getHoat_chat());

            if (!TextUtils.isEmpty(suggestedName)
                    && !TextUtils.isEmpty(medicineName)
                    && (medicineName.contains(suggestedName) || suggestedName.contains(medicineName))) {
                return medicine;
            }

            if (!TextUtils.isEmpty(suggestedActive)
                    && !TextUtils.isEmpty(active)
                    && (active.contains(suggestedActive) || suggestedActive.contains(active))) {
                return medicine;
            }
        }

        return null;
    }

    private String extractJson(String rawText) {
        String text = rawText.trim();
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    private String normalizeFrequency(String value) {
        if (TextUtils.isEmpty(value)) return "2 lần/ngày";
        String normalized = normalize(value);
        if (normalized.contains("1 lan")) return "1 lần/ngày";
        if (normalized.contains("3 lan")) return "3 lần/ngày";
        if (normalized.contains("4 lan")) return "4 lần/ngày";
        if (normalized.contains("sang trua toi")) return "Sáng/Trưa/Tối";
        if (normalized.contains("sang toi")) return "Sáng/Tối";
        return "2 lần/ngày";
    }

    private String normalizeDuration(String value) {
        if (TextUtils.isEmpty(value)) return "7 ngày";
        String normalized = normalize(value);
        if (normalized.contains("3 ngay")) return "3 ngày";
        if (normalized.contains("5 ngay")) return "5 ngày";
        if (normalized.contains("10 ngay")) return "10 ngày";
        if (normalized.contains("14 ngay")) return "14 ngày";
        if (normalized.contains("1 thang") || normalized.contains("30 ngay")) return "1 tháng";
        return "7 ngày";
    }

    private String normalize(String input) {
        if (input == null) return "";
        String value = input.toLowerCase(Locale.getDefault()).trim();
        value = value.replace("đ", "d").replace("Đ", "D");
        String noAccent = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return noAccent.replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
