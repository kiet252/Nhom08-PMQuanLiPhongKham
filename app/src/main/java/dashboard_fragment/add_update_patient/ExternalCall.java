package dashboard_fragment.add_update_patient;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

//Đánh dấu ngay trên đầu một phương thức rằng nó sẽ được gọi từ bên ngoài class
@Retention(RetentionPolicy.SOURCE)
@interface ExternalCall {}
