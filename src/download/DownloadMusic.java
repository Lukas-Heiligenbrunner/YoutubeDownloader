package download;

import general.Logger;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class DownloadMusic {

    public DownloadMusic() {

    }

    private ArrayList<MusicDownloadListener> listeners = new ArrayList<>();

    private int percent = 0;
    private int loadedbytes = 0;
    private int totallength = 0;
    private String conttype = "";

    private boolean interrupt = false;

    /**
     * Download new song from the internet
     * @param link url to file
     * @param filename filename where to download to
     */
    public void Download(String link, String filename) {
        new Thread(() -> {
            try {
                fireRetrievingDataEvent();
                URLConnection conn = new URL(link).openConnection();
                conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0"); // setting user agent --> needed on oracle java 8

                totallength = conn.getContentLength();
                conttype = conn.getContentType();

                if (!conttype.equals("audio/mpeg")) {

                    int i;
                    for (i = 1; i <= 10 && !conttype.equals("audio/mpeg"); i++) { //needed because download link sometimes invalid
                        Logger.log("invalid Downloadlink --> " + i + "st retry", Logger.WARNING, 2);
                        conn = new URL(link).openConnection();
                        conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0"); // setting user agent --> needed on oracle java 8
                        totallength = conn.getContentLength();
                        conttype = conn.getContentType();
                    }
                    if (i == 10) {
                        // download completelly errored
                        Logger.log("Download Error: Downloadlink is invalid", Logger.ERROR, 1);
                    }

                }

                InputStream is = conn.getInputStream();

                OutputStream outstream = new FileOutputStream(new File(filename));
                byte[] buffer = new byte[4096];
                int len;
                int percentold = 42;
                fireStartDownloadEvent();
                while ((len = is.read(buffer)) > 0 && !interrupt) {
                    outstream.write(buffer, 0, len);
                    loadedbytes += len;
                    percent = (loadedbytes * 100) / totallength;
                    if (percent != percentold) {
                        firePercentchangeListener(percent);
                        percentold = percent;
                    }
                }
                outstream.close();
                fireFinishedEvent();

                System.out.println("finished successful");
            } catch (IOException e) {
                fireErroredEvent(e.getMessage());
            }
        }).start();
    }

    /**
     * fire percent change event
     * @param percent amount of loaded percent
     */
    private void firePercentchangeListener(int percent) {
        for (MusicDownloadListener lis : listeners) {
            lis.onPercentChangeListener(percent);
        }
    }

    /**
     *  fire finished downloading event
     */
    private void fireFinishedEvent() {
        for (MusicDownloadListener lis : listeners) {
            lis.onFinishedListener();
        }
    }

    /**
     * fires start event
     */
    private void fireStartDownloadEvent() {
        for (MusicDownloadListener lis : listeners) {
            lis.onDownloadStartListener();
        }
    }

    /**
     * fire start to retrieve data event
     */
    private void fireRetrievingDataEvent() {
        for (MusicDownloadListener lis : listeners) {
            lis.onRetrievingDataListener();
        }
    }

    /**
     * fire error event
     * @param message
     */
    private void fireErroredEvent(String message) {
        for (MusicDownloadListener lis : listeners) {
            lis.onErrored(message);
        }
    }

    /**
     * add new Downloadlistener
     * @param lis the MusicDownloadListener
     */
    public void addActionListener(MusicDownloadListener lis) {
        listeners.add(lis);
    }

    /**
     * interrupt the current processing download
     */
    public void interruptdownload() {
        interrupt = true;
    }

    /**
     * get the percentage of the downloaded song
     * @return percentage
     */
    public int getPercent() {
        return percent;
    }

    /**
     * get the amount of loaded byets
     * @return loaded bytes
     */
    public int getLoadedbytes() {
        return loadedbytes;
    }

    /**
     * get the total size of the song
     * @return total size in bytes
     */
    public int getTotallength() {
        return totallength;
    }

    /**
     * get the contentype of the file
     * @return content type as string
     */
    public String getConttype() {
        return conttype;
    }
}
