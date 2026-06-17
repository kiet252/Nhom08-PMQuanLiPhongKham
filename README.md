# Nhóm 8 quản lý phòng khám

## Cấu hình API key Supabase

API key của Supabase không được để trong file nguồn và không được commit lên GitHub. Ứng dụng sẽ lấy key từ `local.properties` hoặc từ biến môi trường khi build.

## Lấy API key Gemini

Vào Google AI studio -> Chọn Get API key -> Copy API key rồi paste vào `local.properties`. Ứng dụng sẽ lấy key từ đây mà thao tác. Gemini api key không để trong file nguồn nhé.

### Cách thêm key trên máy của bạn

1. Mở file `local.properties` ở thư mục gốc của dự án.
2. Giữ nguyên dòng `sdk.dir=...` nếu đã có sẵn.
3. Thêm một dòng mới ngay dưới dòng 'sdk.dir=...' như sau:

```properties
SUPABASE_ANON_KEY=sb_publishable_your_key_here
```
4. Thêm một dòng mới ngay dưới dòng SUPABASE_ANON_KEY như sau:
```properties
GEMINI_API_KEY=your_api_key_here
```
5. Sync Gradle lại trong Android Studio. 
6. Chạy ứng dụng như bình thường.
