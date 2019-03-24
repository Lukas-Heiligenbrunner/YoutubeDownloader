package api.spotify.login;

interface LoginListener {
    void onLoginSuccess(String key);
    void onLoginError();
}
