package download;

/*
 * A Java class to combine the three classes of The Youtube API, the Youtube to Link API, And the mp3 download Class
 */

import api.Youtube;
import api.YoutubeToLink;
import general.Logger;
import safe.Settings;
import javafx.concurrent.Task;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;

public class DownloadManager {

    private ArrayList<DownloadListener> listeners = new ArrayList<>();

    private Youtube myyoutube = new Youtube();
    //private YoutubeToLinkmp3Music yttl = new YoutubeToLinkmp3Music();
    private YoutubeToLink yttl = new YoutubeToLink();
    private DownloadMusic dld = new DownloadMusic();

    public DownloadManager()  {
    }

    public void startDownloadJob(String songname){

        new Thread(new Task<Boolean>() {
            @Override
            protected Boolean call() {
                Logger.log("searching for "+songname, Logger.INFO,1);

                try{
                    String id = myyoutube.firstResultID(songname);

                    Logger.log("getting direct link",Logger.INFO,2);

                    yttl.getDirectLink(id);
                    String directlink = yttl.getLink();
                    fireApiFinishedEvent();

                    dld.addActionListener(new MusicDownloadListener() {
                        @Override
                        public void onPercentChangeListener(int percent) {
                            fireProgresschangeEvent(percent);
                        }

                        @Override
                        public void onFinishedListener() {
                            Logger.log("finished downloading",Logger.INFO,2);
                            fireFinishedEvents();
                        }

                        @Override
                        public void onDownloadStartListener() {
                            Logger.log("starting downloading",Logger.INFO,2);
                            fireStartEvent();
                        }

                        @Override
                        public void onRetrievingDataListener() {
                            Logger.log("starting retrieving data (Downloader)",Logger.INFO,2);
                        }

                        @Override
                        public void onErrored(String message) {
                            fireErroredEvent(message);
                        }
                    });

                    dld.Download(directlink, Settings.getSettings().getDownloadPath()+"/"+yttl.getName()+".mp3"); //starting the donwload
                }catch (IOException e){
                    Logger.log("cant download --> no internet connection",Logger.ERROR,1);
                    fireErroredEvent("No Internet Connection");
                    e.printStackTrace();
                } catch (ParseException e) {
                    //download isnt available
                    Logger.log("requested video isnt available for download",Logger.ERROR,1);
                    fireErroredEvent("Video Not supported");
                }

                return null;
            }
        }).start();
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




    public void addEventListener(DownloadListener e){
        listeners.add(e);
    }

    private void fireProgresschangeEvent(int percent){
        for (DownloadListener listener:listeners) {
            listener.onDownloadProgressChange(percent);
        }
    }

    private void fireStartEvent(){
        for (DownloadListener listener:listeners) {
            listener.onDownloadStarted();
        }
    }

    private void fireFinishedEvents(){
        for (DownloadListener listener:listeners) {
            listener.onDownloadFinished();
        }
    }

    private void fireErroredEvent(String message){
        for (DownloadListener listener:listeners) {
            listener.onDownloadErrored(message);
        }
    }

    private void fireApiFinishedEvent(){
        for (DownloadListener listener:listeners) {
            listener.onGettingApiDataFinished();
        }
    }

}
