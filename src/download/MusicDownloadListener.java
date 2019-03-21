package download;


public interface MusicDownloadListener {

    void onPercentChangeListener(int percent);
    void onFinishedListener();
    void onDownloadStartListener();
    void onRetrievingDataListener();
    void onErrored(String message);
}
