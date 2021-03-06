package api.spotify.login;

import api.API;
import general.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import safe.SpotifyData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SpotifyLogin extends API {

    private String ClientId;
    private String ClientSecret;

    private ArrayList<LoginListener> onsuccesslist = new ArrayList<>();

    /**
     * create new SpotifyLogin object
     * @param ClientId Spotify Client id
     * @param ClientSecret Spotify secret id
     */
    public SpotifyLogin(String ClientId, String ClientSecret) {
        this.ClientId = ClientId;
        this.ClientSecret = ClientSecret;
    }


    /**
     *  creates login window and gives the user the opportunity to login on spotify
     */
    public void loginNewAccount() {

        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("SpotifyLogin.fxml"));

            Scene scene = new Scene(fxmlLoader.load(), 1000, 600);
            Stage stage = new Stage();

            stage.initStyle(StageStyle.UTILITY);
            stage.setTitle("please login");
            stage.setScene(scene);

            stage.setOnCloseRequest(event -> fireErrorevent("Login window closed unexpectedly"));

            SpotifyWindowController controller = fxmlLoader.getController();
            controller.addOnSuccessListener(new LoginWindowListener() {
                @Override
                public void onLoginSuccess(String key) {
                    new Thread(() -> {
                        //exchange the key to a valid refresh and access token
                        Map < String, String > mymap = new HashMap<>();
                        mymap.put("grant_type", "authorization_code");
                        mymap.put("code", key);
                        mymap.put("client_id", ClientId);
                        mymap.put("client_secret", ClientSecret);
                        mymap.put("redirect_uri", "https://example.com/callback");  //url must be same as in Spotify popup window...  --> ca. 5h debug time


                        JSONObject request = null;
                        try {
                            request = (JSONObject) requestData("https://accounts.spotify.com/api/token", mymap, true);
                        } catch (IOException | ParseException e1) {
                            e1.printStackTrace();
                            fireErrorevent("IO Error");
                        }
                        SpotifyData myspotifydata = SpotifyData.getData();
                        myspotifydata.setRefreshToken((String) request.get("refresh_token"));
                        myspotifydata.setKey((String) request.get("access_token"));
                        myspotifydata.setExpireSeconds(Calendar.getInstance().getTimeInMillis() + (Long) request.get("expires_in") * 1000);
                        myspotifydata.safeData();

                        Logger.log("successfully logged in!", Logger.INFO);
                        fireSuccessevent();
                    }).start();
                }

                @Override
                public void onLoginError() {
                    fireErrorevent("an login Error occured");
                }
            });

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * fires the Event for successful login
     */
    private void fireSuccessevent() {
        for (LoginListener a : onsuccesslist) {
            a.onLoginSuccess();
        }
    }

    /**
     * fires error event for spotify login
     * @param message error message
     */
    private void fireErrorevent(String message) {
        for (LoginListener a : onsuccesslist) {
            a.onLoginError(message);
        }
    }

    /**
     * adds LoginListener to class for fired events.
     */
    public void addLoginListener(LoginListener a) {
        onsuccesslist.add(a);
    }
}
