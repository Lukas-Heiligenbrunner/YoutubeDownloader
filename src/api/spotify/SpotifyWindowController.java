package api.spotify;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.Event;
import javafx.event.EventHandler;
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

            myView.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
                @Override
                public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
                    //System.out.println("changed...");
                    String result = myView.getEngine().getLocation();
                    if (result.contains("https://example.com/callback")){
                        //System.out.println("successful");
                        //System.out.println(result);
                        result=result.substring(result.indexOf("code=")+5,result.lastIndexOf("&state="));
                        fireOnSuccessEvent(result);
                        ((Stage) myView.getScene().getWindow()).close();
                    }
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
