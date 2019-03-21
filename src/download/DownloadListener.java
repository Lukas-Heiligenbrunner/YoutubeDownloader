package download;

public interface DownloadListener {
    void onDownloadProgressChange(int percent);
    void onDownloadFinished();
    void onDownloadStarted();
    void onDownloadErrored(String message);
    void onGettingApiDataFinished();
}
