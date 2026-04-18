package dashboard_fragment.manage_examination_form;

import java.util.List;

import dashboard_fragment.create_examination_form.ExaminationForm;

public class ExaminationFormGroup {
    private final String date;
    private final List<ExaminationForm> forms;

    public ExaminationFormGroup(String date, List<ExaminationForm> forms) {
        this.date = date;
        this.forms = forms;
    }

    public String getDate() { return date; }
    public List<ExaminationForm> getForms() { return forms; }
}
