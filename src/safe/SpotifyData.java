package safe;

import general.Logger;

import java.io.*;

public class SpotifyData implements Serializable{

    private static SpotifyData myspotify = SpotifyData.loadData();

    //Spotify data
    private String refreshToken="";
    private String key="";
    private long expireSeconds=0;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getExpireSeconds() {
        return expireSeconds;
    }

    public void setExpireSeconds(long expireSeconds) {
        this.expireSeconds = expireSeconds;
    }

    public void safeData(){
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("spotify.txt"));
            out.writeObject(this);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            new Logger().log("root path might not be writeable!",Logger.ERROR);
        }
    }

    private static SpotifyData loadData(){
        SpotifyData myspotify = null;
        new Logger().log("loaded spotify file from Disk",Logger.INFO,2);
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream("spotify.txt"));
            myspotify = (SpotifyData) in.readObject();
            in.close();
        } catch (Exception e){
            myspotify = new SpotifyData();
            myspotify.safeData();
            new Logger().log("created new spotify file",Logger.INFO,2);
        } finally {
            return myspotify;
        }
    }

    public static SpotifyData getData(){
        return myspotify;
    }
}
