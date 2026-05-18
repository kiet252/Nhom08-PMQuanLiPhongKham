package dashboard_fragment.staff_manage_bill.patch_bills_logic;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.Query;

public interface BillUpdatePayloadApiRequest {
    @PATCH("rest/v1/bill")
    Call<Void> updateBillStatus(
            @Query("id") String idFilter,
            @Body BillUpdatePayload payload,
            @Header("Prefer") String preferReturn
    );
}
