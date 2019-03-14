package api.spotify;

import api.API;
import general.Logger;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import safe.SpotifyData;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SpotifyLogin extends API {

    private String ClientId = "";
    private String ClientSecret = "";

    private ArrayList<ActionListener> onsuccesslist = new ArrayList<>();

    public SpotifyLogin(String ClientId,String ClientSecret) {
        this.ClientId=ClientId;
        this.ClientSecret=ClientSecret;
    }

    public void loginNewAccount(){

        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("SpotifyLogin.fxml"));

            Scene scene = new Scene(fxmlLoader.load(), 1000, 600);
            Stage stage = new Stage();

            stage.initStyle(StageStyle.UTILITY);
            stage.setTitle("dings");
            stage.setScene(scene);

            SpotifyWindowController controller = fxmlLoader.getController();
            controller.addOnSuccessListener(e -> {
                String code = e.getActionCommand();
                //System.out.println("success "+code);
                //TODO request api code with gotten code

                new Thread(new Task<Boolean>() {
                    @Override
                    protected Boolean call() {
                        System.out.println("in thread...");
                        Map<String,String> mymap = new HashMap<>();
                        mymap.put("grant_type","authorization_code");
                        mymap.put("code",code);
                        mymap.put("client_id",ClientId);
                        mymap.put("client_secret",ClientSecret);
                        mymap.put("redirect_uri","https://example.com/callback");  //url must be same as in Spotify popup window...  --> ca. 5h debug time


                        JSONObject request = null;
                        try {
                            request = (JSONObject ) requestData("https://accounts.spotify.com/api/token",mymap,true);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }
                        SpotifyData myspotifydata = SpotifyData.getData();
                        myspotifydata.setRefreshToken((String)request.get("refresh_token"));
                        myspotifydata.setKey((String)request.get("access_token"));
                        myspotifydata.setExpireSeconds(Calendar.getInstance().getTimeInMillis()+(Long)request.get("expires_in")*1000);
                        myspotifydata.safeData();

                        new Logger().log("successfully logged in!", Logger.INFO);
                        fireOnloggedinevent();

                        return null;
                    }
                }).start();
            });

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fireOnloggedinevent(){
        for (ActionListener a:onsuccesslist) {
            a.actionPerformed(new ActionEvent(this,42,"successfully logged in"));
        }
    }

    public void addOnLoggedInListener(ActionListener a){
        onsuccesslist.add(a);
    }
}
