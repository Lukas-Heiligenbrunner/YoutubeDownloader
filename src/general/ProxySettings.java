package general;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class ProxySettings {
    /**
     * set proxy settings for system
     * @param authUser username
     * @param authPassword password
     * @param host hostname
     * @param port port of proxy
     */
    public static void setProxy(String authUser, String authPassword, String host, String port) {
        Authenticator.setDefault(
                new Authenticator() {
                    @Override
                    public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                authUser, authPassword.toCharArray());
                    }
                }
        );

        //set https proxy
        System.setProperty("https.proxyUser", authUser);
        System.setProperty("https.proxyPassword", authPassword);
        System.setProperty("https.proxyHost", host);
        System.setProperty("https.proxyPort", port);

        //set http proxy
        System.setProperty("http.proxyUser", authUser);
        System.setProperty("http.proxyPassword", authPassword);
        System.setProperty("http.proxyHost", host);
        System.setProperty("http.proxyPort", port);

        System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
    }

    /**
     * disable proxy for the whole application
     */
    public static void disbleProxy(){

        //clear https proxy
        System.clearProperty("https.proxyUser");
        System.clearProperty("https.proxyPassword");
        System.clearProperty("https.proxyHost");
        System.clearProperty("https.proxyPort");

        //clear http proxy
        System.clearProperty("http.proxyUser");
        System.clearProperty("http.proxyPassword");
        System.clearProperty("http.proxyHost");
        System.clearProperty("http.proxyPort");

        System.clearProperty("jdk.http.auth.tunneling.disabledSchemes");
    }
}
