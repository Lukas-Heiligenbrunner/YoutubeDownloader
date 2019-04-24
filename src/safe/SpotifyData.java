package safe;

import general.Logger;

import java.io.*;

public class SpotifyData implements Serializable{

    private static SpotifyData myspotify = SpotifyData.loadData();

    //Spotify data
    private String refreshToken="";
    private String key="";
    private long expireSeconds=0;

    /**
     * get Refresh Token
     * @return refresh token
     */
    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * set Refresh Token
     * @param refreshToken returns refresh token
     */
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    /**
     * get the current key
     * @return key
     */
    public String getKey() {
        return key;
    }

    /**
     * set the key
     * @param key set key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * get expireation time
     * @return time in seconds
     */
    public long getExpireSeconds() {
        return expireSeconds;
    }

    /**
     * set key expire time
     * @param expireSeconds time in seconds
     */
    public void setExpireSeconds(long expireSeconds) {
        this.expireSeconds = expireSeconds;
    }

    /**
     * safe object to disk
     */
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

    /**
     * load data from disk
     * @return SpotifyData Objects
     */
    private static SpotifyData loadData(){
        SpotifyData myspotify;
        new Logger().log("loaded spotify file from Disk",Logger.INFO,2);
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream("spotify.txt"));
            myspotify = (SpotifyData) in.readObject();
            in.close();
        } catch (Exception e){
            myspotify = new SpotifyData();
            myspotify.safeData();
            new Logger().log("created new spotify file",Logger.INFO,2);
        }
        return myspotify;
    }

    /**
     * get always the sames SpotifyData object
     * @return Spotifydata object
     */
    public static SpotifyData getData(){
        return myspotify;
    }
}
