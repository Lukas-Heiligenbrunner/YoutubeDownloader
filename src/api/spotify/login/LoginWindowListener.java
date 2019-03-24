package api.spotify.login;

interface LoginWindowListener {
    void onLoginSuccess(String key);
    void onLoginError();
}
