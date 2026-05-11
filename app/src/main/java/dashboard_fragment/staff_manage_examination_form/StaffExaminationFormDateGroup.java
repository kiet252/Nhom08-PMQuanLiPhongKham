package dashboard_fragment.staff_manage_examination_form;

import java.util.List;

import dashboard_fragment.staff_manage_examination_form.get_all_ex_form_logic.ExaminationFormWithPatientDto;

public class StaffExaminationFormDateGroup {
    private final String date;
    private final List<ExaminationFormWithPatientDto> forms;

    public StaffExaminationFormDateGroup(String date, List<ExaminationFormWithPatientDto> forms) {
        this.date = date;
        this.forms = forms;
    }

    public String getDate() { return date; }
    public List<ExaminationFormWithPatientDto> getForms() { return forms; }
}
