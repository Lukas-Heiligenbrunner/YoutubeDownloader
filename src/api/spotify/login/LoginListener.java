package api.spotify.login;

public interface LoginListener {
    void onLoginSuccess();
    void onLoginError(String message);
}
