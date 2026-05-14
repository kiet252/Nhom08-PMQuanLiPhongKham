package dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.prescription_logic;

public class MedicineItem {
    private int id;
    private String ten_thuoc;
    private String hoat_chat;
    private String ham_luong;
    private String don_vi;
    private int ton_kho;
    private String chuc_nang;
    private double don_gia;

    private boolean selected;

    public int getId() {
        return id;
    }

    public String getTen_thuoc() {
        return ten_thuoc;
    }

    public String getHoat_chat() {
        return hoat_chat;
    }

    public String getHam_luong() {
        return ham_luong;
    }

    public String getDon_vi() {
        return don_vi;
    }

    public int getTon_kho() {
        return ton_kho;
    }

    public String getChuc_nang() {
        return chuc_nang;
    }

    public double getDon_gia() {
        return don_gia;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getSubtitle() {
        String hamLuongText = ham_luong == null ? "" : ham_luong.trim();
        String hoatChatText = hoat_chat == null ? "" : hoat_chat.trim();

        if (!hamLuongText.isEmpty() && !hoatChatText.isEmpty()) {
            return hamLuongText + " " + hoatChatText;
        }
        if (!hamLuongText.isEmpty()) {
            return hamLuongText;
        }
        if (!hoatChatText.isEmpty()) {
            return hoatChatText;
        }
        return "";
    }
    public String getFunctionText() {
        if (chuc_nang == null || chuc_nang.trim().isEmpty()) {
            return "Chức năng: --";
        }
        return "Chức năng: " + chuc_nang.trim();
    }

    public String getStockText() {
        String donViText = don_vi == null || don_vi.trim().isEmpty() ? "đơn vị" : don_vi.trim();
        return "TK: " + ton_kho + " " + donViText;
    }

}
