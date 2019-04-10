package download;

/*
 * A Java class to combine the three classes of The Youtube API, the Youtube to Link API, And the mp3 download Class
 */

import api.Youtube;
import api.YoutubeToLink;
import general.Logger;
import org.json.simple.parser.ParseException;
import safe.Settings;

import java.io.IOException;
import java.util.ArrayList;

public class DownloadManager {

    private ArrayList<DownloadListener> listeners = new ArrayList<>();

    private Youtube myyoutube = new Youtube();
    //private YoutubeToLinkmp3Music yttl = new YoutubeToLinkmp3Music();
    private YoutubeToLink yttl = new YoutubeToLink();
    private DownloadMusic dld = new DownloadMusic();

    public DownloadManager() {
    }

    /**
     * start a new download job
     *
     * @param songname name of song to download
     */
    public void startDownloadJob(String songname) {
        new Thread(() -> {
            Logger.log("searching for " + songname, Logger.INFO, 1);

            try {
                String id = myyoutube.firstResultID(songname);

                Logger.log("getting direct link", Logger.INFO, 2);

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
                        Logger.log("finished downloading", Logger.INFO, 2);
                        fireFinishedEvents();
                    }

                    @Override
                    public void onDownloadStartListener() {
                        Logger.log("starting downloading", Logger.INFO, 2);
                        fireStartEvent();
                    }

                    @Override
                    public void onRetrievingDataListener() {
                        Logger.log("starting retrieving data (Downloader)", Logger.INFO, 2);
                    }

                    @Override
                    public void onErrored(String message) {
                        fireErroredEvent(message);
                    }
                });

                dld.Download(directlink, Settings.getSettings().getDownloadPath() + "/" + yttl.getName() + ".mp3"); //starting the donwload
            } catch (IOException e) {
                Logger.log("cant download --> no internet connection", Logger.ERROR, 1);
                fireErroredEvent("No Internet Connection");
                e.printStackTrace();
            } catch (ParseException e) {
                //download isnt available
                Logger.log("requested video isnt available for download", Logger.ERROR, 1);
                fireErroredEvent("Video Not supported");
            }
        }).start();
    }

    /**
     * get download progress
     *
     * @return download progress
     */
    public double getDownloadProgress() {
        return dld.getPercent() / 100.0;
    }

    /**
     * interrupt current download job
     */
    public void interruptDownload() {
        dld.interruptdownload();
    }

    /**
     * get filename of current download
     *
     * @return string with filename
     */
    public String getFilename() {
        return yttl.getName();
    }

    /**
     * get already downloaded bytes
     *
     * @return downloaded bytes
     */
    public int getLoadedBytes() {
        return dld.getLoadedbytes();
    }

    /**
     * get whole size of file
     *
     * @return total size of file in bytes
     */
    public int getTotalBytes() {
        return dld.getTotallength();
    }

    /**
     * add new DownloadManager Event Listneer
     *
     * @param listener a new instance of the DownloadListener
     */
    public void addEventListener(DownloadListener listener) {
        listeners.add(listener);
    }

    private void fireProgresschangeEvent(int percent) {
        for (DownloadListener listener : listeners) {
            listener.onDownloadProgressChange(percent);
        }
    }

    private void fireStartEvent() {
        for (DownloadListener listener : listeners) {
            listener.onDownloadStarted();
        }
    }

    private void fireFinishedEvents() {
        for (DownloadListener listener : listeners) {
            listener.onDownloadFinished();
        }
    }

    private void fireErroredEvent(String message) {
        for (DownloadListener listener : listeners) {
            listener.onDownloadErrored(message);
        }
    }

    private void fireApiFinishedEvent() {
        for (DownloadListener listener : listeners) {
            listener.onGettingApiDataFinished();
        }
    }

}
