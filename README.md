# Ứng dụng quản lí phòng khám (Clinic Management System)

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square)
![Language](https://img.shields.io/badge/Language-Java-ED8B00?style=flat-square)
![Backend](https://img.shields.io/badge/Backend-Supabase-3ECF8E?style=flat-square)
![Build System](https://img.shields.io/badge/Build--System-Gradle__Kotlin__DSL-02303A?style=flat-square)

Một ứng dụng di động trên nền tảng Android (Java) giúp số hóa toàn diện quy trình vận hành và quản lý tại phòng khám y tế. Dự án tích hợp các công nghệ hiện đại như nhận diện khuôn mặt (AI), bản đồ số, quét mã QR/OCR căn cước công dân và trợ lý thông minh (Gemini AI).

---

## 1. Danh sách thành viên (Nhóm 08)

| STT | Họ và tên | MSSV | Vai trò |
| :---: | :--- | :---: | :---: |
| 1 | Phạm Anh Khoa | 24520835 | Nhóm trưởng |
| 2 | Nguyễn Hoàng Anh Khoa | 24520827 | Thành viên |
| 3 | Cao Tuấn Kiệt | 24520894 | Thành viên |
| 4 | Trần Quốc Khánh | 24520801 | Thành viên |

---

## 2. Mục đích đồ án

Ứng dụng hướng tới giải quyết các bất cập trong quản lý truyền thống bằng cách:
- Số hóa quy trình thủ công: Giảm thiểu sai sót giấy tờ, tối ưu hóa thời gian tiếp đón và xử lý phiếu khám.
- Hỗ trợ chuyên môn: Giúp Bác sĩ dễ dàng theo dõi lịch sử bệnh án, kê đơn thuốc điện tử và chỉ định cận lâm sàng.
- Tối ưu vận hành: Quản trị viên nắm bắt kịp thời tình hình doanh thu, chấm công nhân viên và điều phối tài nguyên phòng khám (thuốc, thiết bị).

---

## 3. Công nghệ sử dụng (Tech Stack)

| Thành phần | Công nghệ / Thư viện |
| :--- | :--- |
| Nền tảng & Ngôn ngữ | Android SDK (API 26+), Java, Gradle Kotlin DSL (.gradle.kts) |
| Giao diện (UI/UX) | Material Components, XML Layouts, ViewPager2, RecyclerView |
| Backend & Database | Supabase (PostgreSQL, Realtime, Storage, Auth) |
| Kết nối mạng (API) | Retrofit 2, OkHttp3 |
| Xử lý hình ảnh | Coil (Image Loading), CameraX |
| Trí tuệ nhân tạo (AI) | Google ML Kit (Face Detection), TensorFlow Lite, Gemini API |
| Bản đồ & Định vị | WebView + Leaflet JS + OpenStreetMap / CartoDB tiles |

---

## 4. Hướng dẫn cấu hình và Bảo mật API Key

Để bảo mật thông tin, các API Key của Supabase và Gemini không được để trong file nguồn và không được phép commit lên GitHub. Ứng dụng sẽ lấy các thông tin này từ file local.properties khi build.

### Hướng dẫn lấy Gemini API Key
Vào Google AI Studio -> Chọn "Get API key" -> Sao chép API key để sử dụng cho bước cấu hình bên dưới.

### Cách thêm key trên máy cá nhân
1. Mở file `local.properties` tại thư mục gốc của dự án.
2. Giữ nguyên dòng `sdk.dir=...` nếu đã có sẵn.
3. Thêm các dòng cấu hình mới vào ngay dưới như sau:

```properties
# Cấu hình kết nối đến Supabase Backend
SUPABASE_ANON_KEY=sb_publishable_your_key_here

# Cấu hình API key của Trợ lý ảo Gemini AI
GEMINI_API_KEY=your_api_key_here
