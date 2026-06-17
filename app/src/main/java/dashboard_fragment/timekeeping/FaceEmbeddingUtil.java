package dashboard_fragment.timekeeping;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class FaceEmbeddingUtil {

    public static final int MODEL_INPUT_SIZE = 160;
    public static final int EMBEDDING_SIZE = 128;
    private static final float MARGIN_RATIO = 0.25f;

    /** Load model TFLite từ assets */
    public static MappedByteBuffer loadModelFile(Context context, String modelFile) throws IOException {
        android.content.res.AssetFileDescriptor afd = context.getAssets().openFd(modelFile);
        FileInputStream is = new FileInputStream(afd.getFileDescriptor());
        try {
            FileChannel fileChannel = is.getChannel();
            long startOffset = afd.getStartOffset();
            long declaredLength = afd.getDeclaredLength();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        } finally {
            try { is.close(); } catch (Exception ignored) {}
        }
    }

    /**
     * Crop khuôn mặt với margin + ép thành hình vuông, căn giữa theo bounding box gốc.
     * Trả về null nếu vùng crop không hợp lệ.
     */
    public static Bitmap cropFaceWithMargin(Bitmap bmp, Rect box) {
        int boxW = box.right - box.left;
        int boxH = box.bottom - box.top;
        int marginX = (int) (boxW * MARGIN_RATIO);
        int marginY = (int) (boxH * MARGIN_RATIO);

        int left = box.left - marginX;
        int top = box.top - marginY;
        int right = box.right + marginX;
        int bottom = box.bottom + marginY;

        // Ép thành hình vuông, căn giữa
        int w = right - left;
        int h = bottom - top;
        int size = Math.max(w, h);
        int centerX = (left + right) / 2;
        int centerY = (top + bottom) / 2;

        left = centerX - size / 2;
        top = centerY - size / 2;
        right = left + size;
        bottom = top + size;

        // Clamp trong giới hạn bitmap
        left = Math.max(0, left);
        top = Math.max(0, top);
        right = Math.min(bmp.getWidth(), right);
        bottom = Math.min(bmp.getHeight(), bottom);

        if (right - left <= 0 || bottom - top <= 0) return null;

        return Bitmap.createBitmap(bmp, left, top, right - left, bottom - top);
    }

    /**
     * Chạy TFLite FaceNet với prewhiten normalization (mean/std của ảnh),
     * giúp ổn định hơn với thay đổi ánh sáng so với (val-128)/128 cố định.
     */
    public static float[] runModel(Interpreter tflite, Bitmap bitmap) {
        if (tflite == null) return null;

        int inputSize = MODEL_INPUT_SIZE;
        int[] intValues = new int[inputSize * inputSize];
        bitmap.getPixels(intValues, 0, inputSize, 0, 0, inputSize, inputSize);

        int totalValues = intValues.length * 3;
        float[] rawValues = new float[totalValues];
        double sum = 0.0;
        int idx = 0;
        for (int val : intValues) {
            float r = (val >> 16) & 0xFF;
            float g = (val >> 8) & 0xFF;
            float b = val & 0xFF;
            rawValues[idx++] = r;
            rawValues[idx++] = g;
            rawValues[idx++] = b;
            sum += r + g + b;
        }
        double mean = sum / totalValues;

        double sumSq = 0.0;
        for (float v : rawValues) sumSq += (v - mean) * (v - mean);
        double std = Math.sqrt(sumSq / totalValues);
        double stdAdj = Math.max(std, 1.0 / Math.sqrt(totalValues));

        ByteBuffer input = ByteBuffer.allocateDirect(4 * totalValues);
        input.order(ByteOrder.nativeOrder());
        for (float v : rawValues) {
            input.putFloat((float) ((v - mean) / stdAdj));
        }
        input.rewind();

        float[][] output = new float[1][EMBEDDING_SIZE];
        tflite.run(input, output);
        return output[0];
    }

    /** Fallback khi không có TFLite model (chỉ để app không crash, độ chính xác thấp) */
    public static List<Double> computeFaceVectorFallback(Bitmap bmp) {
        try {
            Bitmap small = Bitmap.createScaledBitmap(bmp, 16, 8, true);
            List<Double> vec = new ArrayList<>(EMBEDDING_SIZE);
            for (int y = 0; y < 8; y++) {
                for (int x = 0; x < 16; x++) {
                    int px = small.getPixel(x, y);
                    int r = (px >> 16) & 0xff;
                    int g = (px >> 8) & 0xff;
                    int b = px & 0xff;
                    double gray = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0;
                    vec.add(gray * 2.0 - 1.0);
                }
            }
            return vec;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void l2Normalize(float[] v) {
        double sum = 0.0;
        for (float f : v) sum += f * f;
        double norm = Math.sqrt(sum);
        if (norm == 0) return;
        for (int i = 0; i < v.length; i++) v[i] /= norm;
    }

    public static double euclideanDistance(float[] a, float[] b) {
        double s = 0.0;
        int len = Math.min(a.length, b.length);
        for (int i = 0; i < len; i++) {
            double diff = a[i] - b[i];
            s += diff * diff;
        }
        return Math.sqrt(s);
    }

    /**
     * Pipeline đầy đủ: crop margin -> resize -> embedding -> L2 normalize.
     * Trả về null nếu fail ở bất kỳ bước nào.
     */
    public static float[] getEmbedding(Interpreter tflite, Bitmap fullBitmap, Rect faceBox) {
        Bitmap faceBmp = cropFaceWithMargin(fullBitmap, faceBox);
        if (faceBmp == null) return null;

        Bitmap resized = Bitmap.createScaledBitmap(faceBmp, MODEL_INPUT_SIZE, MODEL_INPUT_SIZE, true);

        float[] emb;
        if (tflite != null) {
            emb = runModel(tflite, resized);
        } else {
            List<Double> fallback = computeFaceVectorFallback(resized);
            if (fallback == null) return null;
            emb = new float[EMBEDDING_SIZE];
            for (int i = 0; i < Math.min(EMBEDDING_SIZE, fallback.size()); i++) {
                emb[i] = fallback.get(i).floatValue();
            }
        }

        if (emb == null || emb.length != EMBEDDING_SIZE) return null;
        l2Normalize(emb);
        return emb;
    }

    /** Parse vector dạng JSON array hoặc "[a,b,c]" / "a,b,c" từ string lưu trong DB */
    public static List<Double> parseVector(String raw) {
        if (raw == null) return null;
        try {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<List<Double>>(){}.getType();
            return gson.fromJson(raw, type);
        } catch (Exception ex) {
            try {
                String s = raw.replaceAll("[\\[\\]]", "");
                String[] parts = s.split(",");
                List<Double> result = new ArrayList<>();
                for (String p : parts) {
                    if (p.trim().isEmpty()) continue;
                    result.add(Double.parseDouble(p.trim()));
                }
                return result;
            } catch (Exception ex2) {
                return null;
            }
        }
    }
}