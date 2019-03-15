package api.spotify;

import api.API;
import general.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import safe.SpotifyData;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.*;

public class Spotify extends API {
    private static final String ClientId="5b7f34f605214a52b6ce4d7b5a9e135c";
    private static final String ClientSecret="6d451d7334ea4c60b1329a6396bcc07c";

    private ArrayList<ActionListener> onLoginSuccessList = new ArrayList<>();

    private SpotifyData data = SpotifyData.getData();

    private Logger logger = new Logger();

    public Spotify() {
    }

    public ArrayList<Song> getSongsList(){
        ArrayList<Song> songs = new ArrayList<>();

        ArrayList<Playlist> playlists = getPlaylists();
        for (Playlist play:playlists) {
            if (play.name.equals("meineliada")){
                try {
                    songs = getSongNames(play);
                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                }
            }
        }

//        System.out.println(songs.size());
//        for (Song song:songs) {
//            System.out.println(song.songname+"  "+song.artistname);
//        }

        return songs;
    }

    public ArrayList<Playlist> getPlaylists(){
        checkKeyValidity();

        ArrayList<Playlist> playlists = new ArrayList<>();

        JSONObject result=null;
        try {
            Map<String,String> head = new HashMap<>();
            head.put("Authorization","Bearer "+data.getKey());

            result = (JSONObject) requestData("https://api.spotify.com/v1/me/playlists",head);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        JSONArray playlistarr = (JSONArray)result.get("items");

        for (Object obj:playlistarr) {
            JSONObject myobj = (JSONObject)obj;

            Playlist myplaylist = new Playlist();
            myplaylist.name = (String)myobj.get("name");
            myplaylist.id = (String) myobj.get("id");
            myplaylist.tracknumber = Math.toIntExact((long)((JSONObject)myobj.get("tracks")).get("total"));

            playlists.add(myplaylist);
        }


        return playlists;
    }

    public ArrayList<Song> getSongNames(Playlist list) throws IOException, ParseException {
        checkKeyValidity();

        ArrayList<Song> songs = new ArrayList<>();
        int songnumber = list.tracknumber;
        int offset = 0;

        for (int i = songnumber;i>0;i=i-100){

            Map<String,String> get = new HashMap<>();
            Map<String,String> head = new HashMap<>();
            head.put("Authorization","Bearer "+SpotifyData.getData().getKey());


            get.put("offset",String.valueOf(offset));

            JSONObject result = (JSONObject) requestData("https://api.spotify.com/v1/playlists/"+list.id+"/tracks",get,head,false);
            JSONArray songarr = (JSONArray) result.get("items");

            for (Object obj:songarr) {
                JSONObject songobj = (JSONObject)obj;

                Song mysong = new Song();
                mysong.songname = (String)((JSONObject)songobj.get("track")).get("name");
                JSONArray artists = (JSONArray)((JSONObject)songobj.get("track")).get("artists");
                for (Object o:artists) {
                    mysong.artistname+=((JSONObject)o).get("name")+" ";
                }
                songs.add(mysong);
            }
            offset+=100;
        }
        return songs;
    }

    public void loginNewAccount(){
        SpotifyLogin login = new SpotifyLogin(ClientId,ClientSecret);
        login.addOnLoggedInListener(e -> {
            for (ActionListener a:onLoginSuccessList) {
                a.actionPerformed(new ActionEvent(this,e.getID(),e.getActionCommand()));
            }
        });
        login.loginNewAccount();
    }

    public void addLoginSuccessListener(ActionListener a){
        onLoginSuccessList.add(a);
    }

    public UserProfileData getUserProfile(){
        checkKeyValidity();

        UserProfileData user = null;

        Map<String,String> header = new HashMap<>();
        header.put("Authorization","Bearer "+SpotifyData.getData().getKey());
        try {
            JSONObject userdata = (JSONObject) requestData("https://api.spotify.com/v1/me",header);
            user = new UserProfileData();
            user.country = (String) userdata.get("country");
            user.email = (String) userdata.get("email");
            user.id = (String) userdata.get("id");
            user.name = (String) userdata.get("display_name");
            user.product = (String) userdata.get("product");
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return user;
    }

    private void refreshToken(){
        Map<String,String> mymap = new HashMap<>();
        mymap.put("grant_type","refresh_token");
        mymap.put("refresh_token",data.getRefreshToken());

        Map<String, String> head = new HashMap<>();
        head.put("Authorization","Basic "+ Base64.getEncoder().encodeToString((ClientId+":"+ClientSecret).getBytes()));

        try {
            JSONObject refreshed = (JSONObject)requestData("https://accounts.spotify.com/api/token",mymap,head,true);
            data.setKey((String)refreshed.get("access_token"));
            data.setExpireSeconds(Calendar.getInstance().getTimeInMillis()+(long)refreshed.get("expires_in")*1000);
            data.safeData();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private void checkKeyValidity(){
        if(data.getExpireSeconds() > Calendar.getInstance().getTimeInMillis()){
            //valid
            logger.log("key is valid until: "+(data.getExpireSeconds()-Calendar.getInstance().getTimeInMillis()),Logger.INFO);
        }else {
            //invalid
            logger.log("key is invalid, refreshing",Logger.INFO);
            if (!data.getRefreshToken().equals("")){
                refreshToken();
            }

        }
    }

    public boolean isLoggedIn(){
        return !data.getRefreshToken().equals(""); //return false if reqfresh token is ""
    }

    public void logout(){
        data.setKey("");
        data.setExpireSeconds(0);
        data.setRefreshToken("");
        data.safeData();
    }
}
