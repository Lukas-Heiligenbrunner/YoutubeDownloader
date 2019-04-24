package safe;

import general.Logger;

import java.io.*;

public class Settings implements  Serializable{
    public Settings(){

    }

    private static Settings mysettings = Settings.loadSettings();

    //proxy settings
    private boolean proxyEnabled = false;
    private String proxyUser;
    private String proxyPass;
    private String proxyHost="proxy.htl-steyr.ac.at";
    private String proxyPort="8082";

    //Download Settings
    private String downloadPath = System.getProperty("user.home");


    //getters

    public boolean isProxyEnabled() {
        return proxyEnabled;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public String getProxyPort() {
        return proxyPort;
    }

    public String getProxyUser() {
        return proxyUser;
    }

    public String getProxyPass() {
        return proxyPass;
    }

    public String getDownloadPath() {
        return downloadPath;
    }


    //setters

    public void setProxyUser(String proxyUser) {
        this.proxyUser = proxyUser;
    }

    public void setProxyPass(String proxyPass) {
        this.proxyPass = proxyPass;
    }

    public void setProxyEnabled(boolean proxyEnabled) {
        this.proxyEnabled = proxyEnabled;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public void setProxyPort(String proxyPort) {
        this.proxyPort = proxyPort;
    }

    public void setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
    }


    /**
     * safe the settings object to disk
     */
    public void safeSettings(){
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("settings.txt"));
            out.writeObject(this);
            out.close();
            new Logger().log("safed settings",Logger.INFO,2);
        } catch (IOException e) {
            e.printStackTrace();
            new Logger().log("root path might not be writeable!",Logger.ERROR,2);
        }
    }

    /**
     * load the settings object from disk
     * @return Settings object
     */
    private static Settings loadSettings(){
        Settings mysettings;
        new Logger().log("loaded file from Disk",Logger.INFO);
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream("settings.txt"));
            mysettings = (Settings) in.readObject();
            in.close();
        } catch (Exception e){
            mysettings = new Settings();
            mysettings.safeSettings();
            new Logger().log("created new settings file",Logger.INFO);
        }
            return mysettings;
    }

    /**
     * get always the same Settings object
     * @return Settings object
     */
    public static Settings getSettings(){
        return mysettings;
    }
}
