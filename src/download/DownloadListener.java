package download;

public interface DownloadListener {
    public void onDownloadProgressChange();
    public void onDownloadFinished();
    public void onDownloadStarted();
    public void onDownloadErrored(String message);
    public void onGettingApiDataFinished();
}
