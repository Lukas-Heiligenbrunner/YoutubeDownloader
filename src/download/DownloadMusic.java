package download;

import general.Logger;
import javafx.concurrent.Task;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class DownloadMusic{

    public DownloadMusic() {

    }

    Logger logger = new Logger();

    private ArrayList<ActionListener> actionlist = new ArrayList<>();
    private ArrayList<ActionListener> finishedlistener = new ArrayList<>();
    private ArrayList<ActionListener> startDownloadlistener = new ArrayList<>();
    private ArrayList<ActionListener> retrievingDatalistener = new ArrayList<>();

    private int percent = 0;
    private int loadedbytes = 0;
    private int totallength = 0;
    private String conttype = "";

    private boolean interrupt = false;


    public void Download(String link,String filename) {

        new Thread(new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                fireRetrievingDataEvent();
                URLConnection conn = new URL(link).openConnection();
                conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0"); // setting user agent --> needed on oracle java 8

                totallength = conn.getContentLength();
                conttype = conn.getContentType();

                if (!conttype.equals("audio/mpeg")){

                    int i=1;
                    for (i = 1;i<=10 && !conttype.equals("audio/mpeg");i++){ //needed because download link sometimes invalid
                        logger.log("invalid Downloadlink --> "+i+"st retry",Logger.WARNING,2);
                        conn = new URL(link).openConnection();
                        conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0"); // setting user agent --> needed on oracle java 8
                        totallength = conn.getContentLength();
                        conttype = conn.getContentType();
                    }
                    if (i == 10){
                        // download completelly errored
                        logger.log("Download Error: Downloadlink is invalid",Logger.ERROR,1);
                    }

                }

                InputStream is = conn.getInputStream();

                //TODO check if output path is writeable
                // and user input of path
                // check speed
                OutputStream outstream = new FileOutputStream(new File(filename));
                byte[] buffer = new byte[4096];
                int len;
                int percentold = 42;
                fireStartDownloadEvent();
                while ((len = is.read(buffer)) > 0 && !interrupt) {
                    outstream.write(buffer, 0, len);
                    loadedbytes+=len;
                    percent = (loadedbytes*100)/totallength;
                    if (percent != percentold)
                    {
                        firelisteners();
                        percentold = percent;
                    }
                }
                outstream.close();
                fireFinishedEvent();

                return null;
            }
        }).start();
    }

    private void firelisteners(){
        for (ActionListener lis:actionlist) {
            lis.actionPerformed(new ActionEvent(this, 0,"myaction"));
        }
    }

    private void fireFinishedEvent(){
        for (ActionListener lis:finishedlistener) {
            lis.actionPerformed(new ActionEvent(this, 0,"finished listener"));
        }
    }

    private void fireStartDownloadEvent(){
        for (ActionListener lis:startDownloadlistener) {
            lis.actionPerformed(new ActionEvent(this, 0,"start download listener"));
        }
    }

    private void fireRetrievingDataEvent(){
        for (ActionListener lis:retrievingDatalistener) {
            lis.actionPerformed(new ActionEvent(this, 0,"retrieving data listener"));
        }
    }



    public void onPercentChangeListener(ActionListener listener){
        actionlist.add(listener);
    }

    public void onFinishedListener(ActionListener listener){
        finishedlistener.add(listener);
    }

    public void onDownloadStartListener(ActionListener listener){
        startDownloadlistener.add(listener);
    }

    public void onRetrievingDataListener(ActionListener listener){
        retrievingDatalistener.add(listener);
    }


    public void interruptdownload(){
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
