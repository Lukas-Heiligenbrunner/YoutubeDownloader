package download;

/*
 * A Java class to combine the three classes of The Youtube API, the Youtube to Link API, And the mp3 download Class
 */

import api.Youtube;
import api.YoutubeToLink;
import general.Logger;
import javafx.event.Event;
import javafx.event.EventHandler;
import safe.Settings;
import javafx.concurrent.Task;
import org.json.simple.parser.ParseException;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EventListener;

public class DownloadManager {

    private Logger logger = new Logger();

    private ArrayList<ActionListener> onfinished = new ArrayList<>();
    private ArrayList<ActionListener> onstart = new ArrayList<>();
    private ArrayList<ActionListener> onprogresschange = new ArrayList<>();
    private ArrayList<ActionListener> onapidatafinished = new ArrayList<>();
    private ArrayList<ActionListener> onErrored = new ArrayList<>();

    private Youtube myyoutube = new Youtube();
    private YoutubeToLink yttl = new YoutubeToLink();
    private DownloadMusic dld = new DownloadMusic();

    public DownloadManager()  {
    }

    public void startDownloadJob(String songname){

        new Thread(new Task<Boolean>() {
            @Override
            protected Boolean call() {
                logger.log("searching for "+songname, Logger.INFO,1);

                try{
                    String id = myyoutube.firstResultID(songname);

                    logger.log("getting direct link",Logger.INFO,2);

                    yttl.getDirectLink(id);
                    String directlink = yttl.getLink();
                    fireGettingAPIDataEvent();


                    dld.onPercentChangeListener(e ->fireProgressChangeEvent());

                    dld.onFinishedListener(e -> {
                        logger.log("finished downloading",Logger.INFO,2);
                        fireFinishedEvent();
                    });

                    dld.onDownloadStartListener(e -> {
                        logger.log("starting downloading",Logger.INFO,2);
                        fireOnStartEvent();
                    });

                    dld.onRetrievingDataListener(e -> logger.log("starting retrieving data (Downloader)",Logger.INFO,2));

                    dld.Download(directlink, Settings.getSettings().getDownloadPath()+"/"+yttl.getName()+".mp3"); //starting the donwload
                }catch (IOException e){
                    logger.log("cant download --> no internet connection",Logger.ERROR,1);
                    fireErrorEvent("No Internet Connection");
                    e.printStackTrace();
                } catch (ParseException e) {
                    //download isnt available
                    logger.log("requested video isnt available for download",Logger.ERROR,1);
                    fireErrorEvent("Video Not supported");
                }

                return null;
            }
        }).start();
    }

    private void fireFinishedEvent(){
        for (ActionListener a:onfinished) {
            a.actionPerformed(new ActionEvent(this,42,"finished event"));
        }
    }
    public void onFinishedListener(ActionListener a){
        onfinished.add(a);
    }


    private void fireOnStartEvent(){
        for (ActionListener a:onstart) {
            a.actionPerformed(new ActionEvent(this,42,"start event"));
        }
    }
    public void onDownloadStartListener(ActionListener a){
        onstart.add(a);
    }


    private void fireGettingAPIDataEvent(){
        for (ActionListener a:onapidatafinished) {
            a.actionPerformed(new ActionEvent(this,42,"finished getting data from apis"));
        }
    }
    public void onGettingAPIDataFinishedListener(ActionListener a){
        onapidatafinished.add(a);
    }


    private void fireProgressChangeEvent(){
        for (ActionListener a:onprogresschange) {
            a.actionPerformed(new ActionEvent(this,42,"Download progress changed"));
        }
    }
    public void onDownloadProgressChangeListener(ActionListener a){
        onprogresschange.add(a);
    }

    private void fireErrorEvent(String message){
        for (ActionListener a:onErrored) {
            a.actionPerformed(new ActionEvent(this,42,message));
        }
    }
    public void onErrorListener(ActionListener a){
        onErrored.add(a);
    }


    public double getDownloadProgress(){
        return dld.getPercent()/100.0;
    }


    public void interruptDownload()
    {
        dld.interruptdownload();
    }

    public String getFilename(){
        return yttl.getName();
    }

    public int getLoadedBytes(){
        return dld.getLoadedbytes();
    }

    public int getTotalBytes(){
        return dld.getTotallength();
    }

    public void addEventDings(EventListener listener){

    }

    public void addEventHandler(EventHandler listener){

    }

}
