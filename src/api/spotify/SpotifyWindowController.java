package api.spotify;

import javafx.application.Platform;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class SpotifyWindowController {
    public WebView myView;

    private ArrayList<ActionListener> onsucceedlist = new ArrayList<>();

    private static final String clientID = "5b7f34f605214a52b6ce4d7b5a9e135c";

    public SpotifyWindowController() {
        Platform.runLater(() -> {
            try {
                myView.getEngine().load("https://accounts.spotify.com/en/authorize?client_id="+clientID+"&response_type=code&redirect_uri="+ URLEncoder.encode("https://example.com/callback","UTF-8") +"&scope=user-read-private%20user-read-email&state=34fFs29kd09");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            myView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
                String result = myView.getEngine().getLocation();
                if (result.contains("https://example.com/callback")){
                    result=result.substring(result.indexOf("code=")+5,result.lastIndexOf("&state="));
                    fireOnSuccessEvent(result);
                    ((Stage) myView.getScene().getWindow()).close();
                }
            });
        });
    }

    private void fireOnSuccessEvent(String result){
        for (ActionListener e:onsucceedlist) {
            e.actionPerformed(new ActionEvent(this,42,result)); //return request code...
        }
    }

    public void addOnSuccessListener(ActionListener e){
        onsucceedlist.add(e);
    }


}
