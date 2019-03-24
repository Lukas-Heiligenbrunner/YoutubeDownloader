package download;

import general.Logger;
import javafx.concurrent.Task;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class DownloadMusic {

    public DownloadMusic() {

    }

    private Logger logger = new Logger();

    private ArrayList<MusicDownloadListener> listeners = new ArrayList<>();

    private int percent = 0;
    private int loadedbytes = 0;
    private int totallength = 0;
    private String conttype = "";

    private boolean interrupt = false;


    public void Download(String link, String filename) {

        new Thread(new Task<Boolean>() {
            @Override
            protected Boolean call() {
                try {
                    fireRetrievingDataEvent();
                    URLConnection conn = new URL(link).openConnection();
                    conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0"); // setting user agent --> needed on oracle java 8

                    totallength = conn.getContentLength();
                    conttype = conn.getContentType();

                    if (!conttype.equals("audio/mpeg")) {

                        int i = 1;
                        for (i = 1; i <= 10 && !conttype.equals("audio/mpeg"); i++) { //needed because download link sometimes invalid
                            logger.log("invalid Downloadlink --> " + i + "st retry", Logger.WARNING, 2);
                            conn = new URL(link).openConnection();
                            conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0"); // setting user agent --> needed on oracle java 8
                            totallength = conn.getContentLength();
                            conttype = conn.getContentType();
                        }
                        if (i == 10) {
                            // download completelly errored
                            logger.log("Download Error: Downloadlink is invalid", Logger.ERROR, 1);
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
                } catch (FileNotFoundException e) {
                    fireErroredEvent(e.getMessage());
                } catch (MalformedURLException e) {
                    fireErroredEvent(e.getMessage());
                } catch (IOException e) {
                    fireErroredEvent(e.getMessage());
                }

                ;

                return null;
            }

        }).start();
    }

    private void firePercentchangeListener(int percent) {
        for (MusicDownloadListener lis : listeners) {
            lis.onPercentChangeListener(percent);
        }
    }

    private void fireFinishedEvent() {
        for (MusicDownloadListener lis : listeners) {
            lis.onFinishedListener();
        }
    }

    private void fireStartDownloadEvent() {
        for (MusicDownloadListener lis : listeners) {
            lis.onDownloadStartListener();
        }
    }

    private void fireRetrievingDataEvent() {
        for (MusicDownloadListener lis : listeners) {
            lis.onRetrievingDataListener();
        }
    }

    private void fireErroredEvent(String message) {
        for (MusicDownloadListener lis : listeners) {
            lis.onErrored(message);
        }
    }

    public void addActionListener(MusicDownloadListener lis) {
        listeners.add(lis);
    }


    public void interruptdownload() {
        interrupt = true;
    }


    public int getPercent() {
        return percent;
    }

    public int getLoadedbytes() {
        return loadedbytes;
    }

    public int getTotallength() {
        return totallength;
    }

    public String getConttype() {
        return conttype;
    }
}
