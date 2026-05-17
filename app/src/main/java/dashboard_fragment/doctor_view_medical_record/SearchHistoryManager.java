package dashboard_fragment.doctor_view_medical_record;

import java.util.ArrayList;
import java.util.List;

import dashboard_fragment.staff_add_update_patient.PatientProfile;

public class SearchHistoryManager {
    private static SearchHistoryManager instance;
    private final List<PatientProfile> recentSearches;

    private SearchHistoryManager() {
        recentSearches = new ArrayList<>();
    }

    public static synchronized SearchHistoryManager getInstance() {
        if (instance == null) {
            instance = new SearchHistoryManager();
        }
        return instance;
    }

    public List<PatientProfile> getRecentSearches() {
        return recentSearches;
    }

    public void addSearch(PatientProfile patient) {
        if (patient == null || patient.getId() == null) return;
        
        for (int i = 0; i < recentSearches.size(); i++) {
            if (patient.getId().equals(recentSearches.get(i).getId())) {
                return;
            }
        }
        
        recentSearches.add(patient);
    }
}
