package download;


public interface MusicDownloadListener {

    public void onPercentChangeListener();
    public void onFinishedListener();
    public void onDownloadStartListener();
    public void onRetrievingDataListener();
}
