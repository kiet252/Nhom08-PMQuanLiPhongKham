# Nhóm 8 quản lý phòng khám

## Cấu hình API key Supabase

API key của Supabase không được để trong file nguồn và không được commit lên GitHub. Ứng dụng sẽ lấy key từ `local.properties` hoặc từ biến môi trường khi build.

### Cách thêm key trên máy của bạn

1. Mở file `local.properties` ở thư mục gốc của dự án.
2. Giữ nguyên dòng `sdk.dir=...` nếu đã có sẵn.
3. Thêm một dòng mới ngay dưới dòng 'sdk.dir=...' như sau:

```properties
SUPABASE_ANON_KEY=sb_publishable_your_key_here
```

4. Sync Gradle lại trong Android Studio.
5. Chạy ứng dụng như bình thường.
