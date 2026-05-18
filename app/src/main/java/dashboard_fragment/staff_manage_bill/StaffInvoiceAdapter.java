package dashboard_fragment.staff_manage_bill;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom08_quanlyphongkham.R;
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StaffInvoiceAdapter extends RecyclerView.Adapter<StaffInvoiceAdapter.InvoiceViewHolder> {

    private List<StaffInvoiceItem> invoices = new ArrayList<>();
    private OnInvoiceClickListener listener;

    public interface OnInvoiceClickListener {
        void onInvoiceClick(StaffInvoiceItem invoice);
    }

    public void setOnInvoiceClickListener(OnInvoiceClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<StaffInvoiceItem> newInvoices) {
        invoices = newInvoices != null ? newInvoices : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InvoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.staff_item_invoice, parent, false);
        return new InvoiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvoiceViewHolder holder, int position) {
        StaffInvoiceItem invoice = invoices.get(position);

        holder.tvPatientName.setText(invoice.getPatientName());
        holder.tvInvoiceDetails.setText(invoice.getId() + " - " + invoice.getDate());
        holder.tvAmount.setText(formatAmount(invoice.getAmount()));

        if (invoice.isPaid()) {
            bindPaidState(holder, invoice);
        } else {
            bindUnpaidState(holder);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onInvoiceClick(invoice);
            }
        });
    }

    private void bindPaidState(InvoiceViewHolder holder, StaffInvoiceItem invoice) {
        holder.statusIconContainer.setCardBackgroundColor(0xFFE8F5E9);
        holder.imgStatus.setImageResource(R.drawable.ic_check_circle);
        holder.imgStatus.setColorFilter(0xFF4CAF50);

        holder.tvAmount.setTextColor(0xFF2D7A2F);
        holder.tvStatusLabel.setText("Đã thanh toán");
        holder.tvStatusLabel.setTextColor(0xFF4CAF50);
        holder.tvStatusLabel.setBackgroundResource(R.drawable.bg_status_paid);

        holder.layoutPaymentMethod.setVisibility(View.VISIBLE);
        String method = invoice.getPaymentMethod();
        holder.tvPaymentMethod.setText(method != null && !method.isEmpty() ? method : "--");
    }

    private void bindUnpaidState(InvoiceViewHolder holder) {
        holder.statusIconContainer.setCardBackgroundColor(0xFFFFF3E0);
        holder.imgStatus.setImageResource(R.drawable.ic_clock);
        holder.imgStatus.setColorFilter(0xFFFF9800);

        holder.tvAmount.setTextColor(0xFFF97316);
        holder.tvStatusLabel.setText("Chưa thanh toán");
        holder.tvStatusLabel.setTextColor(0xFFFF9800);
        holder.tvStatusLabel.setBackgroundResource(R.drawable.bg_status_unpaid);

        holder.layoutPaymentMethod.setVisibility(View.GONE);
    }

    private String formatAmount(long amount) {
        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
        return fmt.format(amount) + "đ";
    }

    @Override
    public int getItemCount() {
        return invoices.size();
    }

    static class InvoiceViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView statusIconContainer;
        final ImageView imgStatus;
        final TextView tvPatientName;
        final TextView tvInvoiceDetails;
        final TextView tvAmount;
        final TextView tvStatusLabel;
        final LinearLayout layoutPaymentMethod;
        final TextView tvPaymentMethod;

        InvoiceViewHolder(@NonNull View itemView) {
            super(itemView);
            statusIconContainer = itemView.findViewById(R.id.statusIconContainer);
            imgStatus = itemView.findViewById(R.id.imgStatus);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvInvoiceDetails = itemView.findViewById(R.id.tvInvoiceDetails);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvStatusLabel = itemView.findViewById(R.id.tvStatusLabel);
            layoutPaymentMethod = itemView.findViewById(R.id.layoutPaymentMethod);
            tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
        }
    }
}
