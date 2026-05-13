package dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.diagnosis_logic;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DiagnosisSeedData {

    public static Map<String, List<DiagnosisOption>> getDiagnosisGroups() {
        Map<String, List<DiagnosisOption>> groups = new LinkedHashMap<>();

        groups.put("Nhóm Bệnh Lý Về Tai", createOptions(
                "Nhóm Bệnh Lý Về Tai",
                "Viêm tai giữa mạn tính",
                "Viêm ống tai ngoài cấp tính",
                "Nút bít ráy tai",
                "Rối loạn tiền đình",
                "Điếc đột ngột"
        ));

        groups.put("Nhóm Bệnh Lý Về Mũi - Xoang", createOptions(
                "Nhóm Bệnh Lý Về Mũi - Xoang",
                "Viêm mũi xoang cấp tính",
                "Polyp mũi",
                "Phì đại cuốn mũi dưới",
                "Chảy máu mũi (chảy máu cam)"
        ));

        groups.put("Nhóm Bệnh Lý Về Họng - Thanh Quản", createOptions(
                "Nhóm Bệnh Lý Về Họng - Thanh Quản",
                "Viêm họng mạn tính",
                "Viêm amidan cấp tính",
                "Viêm VA cấp tính",
                "Viêm VA mạn tính",
                "Trào ngược họng - thanh quản",
                "Viêm thanh quản cấp tính",
                "Viêm thanh quản mạn tính",
                "Hạt xơ / Polyp dây thanh"
        ));

        groups.put("Nhóm Bệnh Lý Dị Vật", createOptions(
                "Nhóm Bệnh Lý Dị Vật",
                "Dị vật tai",
                "Dị vật mũi",
                "Dị vật họng"
        ));

        return groups;
    }

    private static List<DiagnosisOption> createOptions(String groupName, String... items) {
        List<DiagnosisOption> options = new ArrayList<>();
        for (String item : items) {
            options.add(new DiagnosisOption(groupName, item));
        }
        return options;
    }
}
