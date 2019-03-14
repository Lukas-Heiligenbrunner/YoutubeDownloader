package api.spotify;

import api.API;
import general.Logger;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
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

    private static final String meineliadaid="60nhT85rlEoKhccSs8kx2e";

    private ArrayList<ActionListener> onLoginSuccessList = new ArrayList<>();

    private SpotifyData data = SpotifyData.getData();
    Logger logger = new Logger();

    public Spotify() {
    }

    public ArrayList<String> getSongsList(){
        ArrayList<String> songs = new ArrayList<>();

        int songnumber = 0;
        JSONArray playlists = (JSONArray)((JSONObject)getPlaylists()).get("items");
        for (Object list:playlists) {
            if(((String)((JSONObject)list).get("name")).equals("meineliada")){
                songnumber=Math.toIntExact((long)((JSONObject)((JSONObject)list).get("tracks")).get("total"));
                System.out.println(songnumber);
            }

        }

        int offset=0;
        for (int i = songnumber;i>0;i--){
            JSONArray myplaylist  = (JSONArray) ((JSONObject)getSongNames(meineliadaid,offset)).get("items");
            System.out.println(myplaylist.size());
            for (Object o:myplaylist) {
                String name = (String)  ((JSONObject)((JSONObject)o).get("track")).get("name");
                //System.out.println(name);
                songs.add(name);
            }
            i=i-100;
            offset+=100;
        }
        return songs;
    }

    public Object getPlaylists(){
        checkKeyValidity();

        Object result=null;
        try {
            Map<String,String> head = new HashMap<>();
            head.put("Authorization","Bearer "+data.getKey());

            result = requestData("https://api.spotify.com/v1/me/playlists",head);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Object getSongNames(String playlistid,int offset){
        checkKeyValidity();

        Object result=null;
        try {
            Map<String,String> get = new HashMap<>();
            Map<String,String> head = new HashMap<>();
            head.put("Authorization","Bearer "+SpotifyData.getData().getKey());


            get.put("offset",String.valueOf(offset));

            result = requestData("https://api.spotify.com/v1/playlists/"+playlistid+"/tracks",get,head,false);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
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
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return user;
    }

    public void refreshToken(){
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
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void checkKeyValidity(){
        if(data.getExpireSeconds() > Calendar.getInstance().getTimeInMillis()){
            //valid
            System.out.println("key is valid until: "+(data.getExpireSeconds()-Calendar.getInstance().getTimeInMillis()));
        }else {
            //invalid
            System.out.println("key is invalid");
            if (data.getRefreshToken().equals("")){
                System.out.println("not logged in yet");
            }else {
                refreshToken();
            }

        }
    }

    public boolean isLoggedIn(){
        if (data.getRefreshToken().equals("")){
            return false;
        }else {
            return true;
        }
    }

    public void logout(){
        data.setKey("");
        data.setExpireSeconds(0);
        data.setRefreshToken("");
        data.safeData();
    }
}
