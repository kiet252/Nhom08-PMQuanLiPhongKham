package dashboard_fragment.change_password_request;

public class UpdatePasswordRequest {
    private String password;

    public UpdatePasswordRequest(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }
}