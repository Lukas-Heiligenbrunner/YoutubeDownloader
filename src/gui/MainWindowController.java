package gui;

import api.YoutubeToLinkmp3Music;
import api.spotify.Playlist;
import api.spotify.Song;
import api.spotify.Spotify;
import api.spotify.UserProfileData;

import download.DownloadListener;
import download.DownloadManager;

import general.Logger;
import general.ProxySettings;

import org.json.simple.parser.ParseException;
import safe.Settings;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class MainWindowController {

    public TabPane rootTabPane;

    //Single Download Elements
    public ProgressBar progressbar;
    public TextField searchfield;
    public Label filenamelabel;
    public Label statusbottomlabel;

    //Multiple Download elements
    public ListView tableMultipleLinks;
    public Label listsearchInfoLabel;
    public ProgressBar listProgressbar;
    public Label liststatuslabel;


    //Settings elements
    public CheckBox proxenabledcheckbox;
    public TextField proxhostfield;
    public TextField proxportfield;
    public TextField userfield;
    public PasswordField passfield;

    public Label settingPathLabel;


    public Label spotifyInfoLabel;
    public ProgressBar SpotifyProgressbar;
    public Label Spotifystatuslabel;
    public Label accountInfoLabel;
    public Button loginbtn;
    public Label versioninfolabel;
    public ListView playlistsListView;


    private Logger logger = new Logger();
    private DownloadManager singleDownloadManager;

    private Settings settings = Settings.getSettings();

    public MainWindowController() {
        Platform.runLater(() -> rootTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.getId().equals("spotifysearch")) {
                //in spotify tab --> load infos
                logger.log("tab changed to spotify search", Logger.INFO, 2);
                new Thread(new Task<Boolean>() {
                    @Override
                    protected Boolean call() {
                        Spotify myspotify = new Spotify();
                        if (myspotify.isLoggedIn()) {
                            UserProfileData user = myspotify.getUserProfile();
                            Platform.runLater(() -> {
                                loginbtn.setText("Logout");
                                accountInfoLabel.setText("Logged in user: \nE-Mail: " + user.email + "\nName: " + user.name + "\nCountry: " + user.country + "\nAccount Type: " + user.product);
                            });

                            //TODO load playlists of user

                            ArrayList<Playlist> playlists = myspotify.getPlaylists();
                            for (Playlist play : playlists) {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        playlistsListView.getItems().add(new Label(play.name));
                                    }
                                });
                            }

                        } else {
                            Platform.runLater(() -> {
                                accountInfoLabel.setText("Not logged in yet!!!");
                                loginbtn.setText("Login");
                            });
                        }

                        return null;
                    }
                }).start();
            } else if (newValue.getId().equals("settings")) {
                logger.log("tab changed to settings", Logger.INFO, 2);
                Platform.runLater(() -> {
                    proxenabledcheckbox.setSelected(settings.isProxyEnabled());
                    userfield.setText(settings.getProxyUser());
                    passfield.setText(settings.getProxyPass());
                    proxportfield.setText(settings.getProxyPort());
                    proxhostfield.setText(settings.getProxyHost());
                    versioninfolabel.setText("Version: " + Main.version);

                    settingPathLabel.setText(settings.getDownloadPath());
                });

            } else if (newValue.getId().equals("multiplesearch")) {
                logger.log("tab changed to multiple search", Logger.INFO, 2);
            } else if (newValue.getId().equals("basicsearch")) {
                logger.log("tab changed to basic search", Logger.INFO, 2);
            }
        }));


        if (settings.isProxyEnabled()) { //set proxy if enabled
            ProxySettings.setProxy(settings.getProxyUser(), settings.getProxyPass(), settings.getProxyHost(), settings.getProxyPort());
        }


    }

    public void searchbtn() {
        DownloadSingle();
    }

    public void cancelbutton() {
        new Thread(new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {

                try {
                    new YoutubeToLinkmp3Music().getDirectLink("L4sjxRhAJHA");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                return null;
            }
        }).start();


        logger.log("stopping download...", Logger.WARNING, 1);
        //singleDownloadManager.interruptDownload();
    }

    public void clickbtnDownloadList() {
        Platform.runLater(() -> tableMultipleLinks.getItems().add(new TextField("Songname")));
    }

    public void startDldBtnList() {
        DownloadMultiple();
    }

    private void DownloadMultiple() {
        ArrayList<String> downloadlinks = new ArrayList<>();
        for (int i = 0; i < tableMultipleLinks.getItems().size(); i++) {
            downloadlinks.add(((TextField) tableMultipleLinks.getItems().get(i)).getText());
        }
        DownloadMultipleRec(0, downloadlinks);
    }

    private void DownloadMultipleRec(int num, ArrayList<String> downloadlinks) {

        new Thread(new Task<Boolean>() {
            @Override
            protected Boolean call() {
                Platform.runLater(() -> {
                    listProgressbar.setProgress(-1.0);
                    liststatuslabel.setText("retrieving necessary  data!");
                });

                DownloadManager mydownload = new DownloadManager();

                mydownload.addEventListener(new DownloadListener() {
                    @Override
                    public void onDownloadProgressChange(int percent) {
                        Platform.runLater(() -> {
                            liststatuslabel.setText("downloading");
                            listProgressbar.setProgress(percent / 100.0);
                            DecimalFormat format = new DecimalFormat();
                            format.setMaximumFractionDigits(2);

                            listsearchInfoLabel.setText("Filename: " + mydownload.getFilename() + "\nprogress:  " + percent + "%\nLoaded: " + format.format((float) mydownload.getLoadedBytes() / (1024 * 1024)) + "MB/" + format.format((float) mydownload.getTotalBytes() / (1024 * 1024)) + "MB");
                        });
                    }

                    @Override
                    public void onDownloadFinished() {
                        Platform.runLater(() -> {
                            liststatuslabel.setText("finished downloding -- ready to download new");
                            listProgressbar.setProgress(0.0);
                            if (downloadlinks.size() > num + 1) {
                                DownloadMultipleRec(num + 1, downloadlinks);
                            }
                        });
                    }

                    @Override
                    public void onDownloadStarted() {
                        Platform.runLater(() -> liststatuslabel.setText("starting the download"));
                    }

                    @Override
                    public void onDownloadErrored(String message) {
                        //TODO!!!
                    }

                    @Override
                    public void onGettingApiDataFinished() {
                        //TODO!!!
                    }
                });

                mydownload.startDownloadJob(downloadlinks.get(num));

                return null;
            }
        }).start();

    }

    private void DownloadSingle() {
        singleDownloadManager = new DownloadManager();
        Platform.runLater(() -> {
            progressbar.setProgress(-1.0);
            statusbottomlabel.setText("retrieving necessary data!");
        });

        singleDownloadManager.addEventListener(new DownloadListener() {
            @Override
            public void onDownloadProgressChange(int percent) {
                Platform.runLater(() -> {
                    statusbottomlabel.setText("downloading");
                    progressbar.setProgress(percent / 100.0);
                    DecimalFormat format = new DecimalFormat();
                    format.setMaximumFractionDigits(2);

                    filenamelabel.setText("Filename: " + singleDownloadManager.getFilename() + "\nprogress: " + percent + "%\nLoaded: " + format.format((float) singleDownloadManager.getLoadedBytes() / (1024 * 1024)) + "MB/" + format.format((float) singleDownloadManager.getTotalBytes() / (1024 * 1024)) + "MB");
                });
            }

            @Override
            public void onDownloadFinished() {
                Platform.runLater(() -> {
                    statusbottomlabel.setText("finished downloding -- ready to download new");
                    progressbar.setProgress(0.0);
                });
            }

            @Override
            public void onDownloadStarted() {
                Platform.runLater(() -> statusbottomlabel.setText("starting the download"));
            }

            @Override
            public void onDownloadErrored(String message) {
                Platform.runLater(() -> {
                    statusbottomlabel.setText("An Error occured ");
                    progressbar.setProgress(0.0);
                });
            }

            @Override
            public void onGettingApiDataFinished() {

            }
        });

        singleDownloadManager.startDownloadJob(searchfield.getText());
    }

    public void SettingSafeBtnClick() {
        settings.setProxyEnabled(proxenabledcheckbox.isSelected());
        settings.setProxyUser(userfield.getText());
        settings.setProxyPass(passfield.getText());
        settings.setProxyHost(proxhostfield.getText());
        settings.setProxyPort(proxportfield.getText());

        settings.safeSettings();


    }

    public void selectDownloadPathBtn() {
        DirectoryChooser mychooser = new DirectoryChooser();
        mychooser.setTitle("select downloadpath");
        mychooser.setInitialDirectory(new File(settings.getDownloadPath())); //setting default windows to Download path
        File mydir = mychooser.showDialog(rootTabPane.getScene().getWindow()); //show dialog
        if (mydir != null) {
            logger.log("setting Download path to: " + mydir.getPath(), Logger.INFO, 2);
            Platform.runLater(() -> settingPathLabel.setText(mydir.getPath()));
            settings.setDownloadPath(mydir.getPath());
        } else {
            logger.log("nothing selected", Logger.WARNING, 2);
        }
    }

    public void startSpotifyDownloadBtn() {
        Spotify myspotify = new Spotify();

        Platform.runLater(() -> {
            spotifyInfoLabel.setText("retrieving necessary data!");
            SpotifyProgressbar.setProgress(-1.0);
        });

        if (myspotify.isLoggedIn()) {
            ArrayList<Song> songs = myspotify.getSongsList();
            downloadSpotifyListRec(0, songs);
        } else {
            logger.log("Not logged in", Logger.ERROR, 1);
        }
    }

    private void downloadSpotifyListRec(int num, ArrayList<Song> songlist) {
        new Thread(new Task<Boolean>() {
            @Override
            protected Boolean call() {
                Platform.runLater(() -> {
                    SpotifyProgressbar.setProgress(-1.0);
                    Spotifystatuslabel.setText("retrieving necessary  data!");
                });

                DownloadManager mydownload = new DownloadManager();

                mydownload.addEventListener(new DownloadListener() {
                    @Override
                    public void onDownloadProgressChange(int percent) {
                        Platform.runLater(() -> {
                            Spotifystatuslabel.setText("downloading");
                            SpotifyProgressbar.setProgress(percent / 100.0);
                            DecimalFormat format = new DecimalFormat();
                            format.setMaximumFractionDigits(2);

                            spotifyInfoLabel.setText("Filename: " + mydownload.getFilename() + "\nProgress:  " + percent + "%\nLoaded: " + format.format((float) mydownload.getLoadedBytes() / (1024 * 1024)) + "MB/" + format.format((float) mydownload.getTotalBytes() / (1024 * 1024)) + "MB");
                        });
                    }

                    @Override
                    public void onDownloadFinished() {
                        Platform.runLater(() -> {
                            Spotifystatuslabel.setText("finished downloding -- ready to download new");
                            SpotifyProgressbar.setProgress(0.0);
                            if (songlist.size() > num + 1) {
                                downloadSpotifyListRec(num + 1, songlist);
                            }
                        });
                    }

                    @Override
                    public void onDownloadStarted() {
                        Platform.runLater(() -> Spotifystatuslabel.setText("starting the download"));
                    }

                    @Override
                    public void onDownloadErrored(String message) {
                        //TODO!!!
                    }

                    @Override
                    public void onGettingApiDataFinished() {
                        //TODO!!!
                    }
                });

                mydownload.startDownloadJob(songlist.get(num).songname + " " + songlist.get(num).artistname);
                return null;
            }
        }).start();
    }

    public void newSpotifyBtnListener() {
        Spotify myspotify = new Spotify();
        if (myspotify.isLoggedIn()) {
            //logout
            myspotify.logout();
            Platform.runLater(() -> {
                accountInfoLabel.setText("Not logged in yet!!!");
                loginbtn.setText("Login");
            });
        } else {
            myspotify.addLoginSuccessListener(e -> {
                UserProfileData user = myspotify.getUserProfile();
                Platform.runLater(() -> {
                    loginbtn.setText("Logout");
                    accountInfoLabel.setText("Logged in user: \nE-Mail: " + user.email + "\nName: " + user.name + "\nCountry: " + user.country + "\nAccount Type: " + user.product);
                });
            });
            myspotify.loginNewAccount();
        }
    }

    public void updatebtnlistener() {

        //checking version
        String version = "";
        try {
            URL versionurl = new URL("http://lukisvpnserver.ddns.net/HowToSteelMusic/version.info");
            BufferedReader myreader = new BufferedReader(new InputStreamReader(versionurl.openStream()));
            version = myreader.readLine();
            myreader.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("current version: " + Main.version + " -- version on server: " + version);

        if (!Main.version.equals(version)) {
            //update
            try {
                final File currentJar = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";


                if (currentJar.delete()) {
                    System.out.println("File deleted successfully");
                    URL url = new URL("http://lukisvpnserver.ddns.net/HowToSteelMusic/HowToSteelMusic.jar");
                    URLConnection conn = url.openConnection();
                    int totallength = conn.getContentLength();
                    InputStream is = conn.getInputStream();

                    OutputStream outstream = new FileOutputStream(currentJar);
                    byte[] buffer = new byte[4096];
                    int len;
                    int percentold = 42;
                    int percent = 0;
                    int loadedbytes = 0;

                    while ((len = is.read(buffer)) > 0) {
                        outstream.write(buffer, 0, len);
                        loadedbytes += len;
                        percent = (loadedbytes * 100) / totallength;
                        if (percent != percentold) {
                            System.out.println(percent);
                            percentold = percent;
                        }
                    }
                    outstream.close();

                    /* Build command: java -jar application.jar */
                    final ArrayList<String> command = new ArrayList<String>();
                    command.add(javaBin);
                    command.add("-jar");
                    command.add(currentJar.getPath());

                    final ProcessBuilder builder = new ProcessBuilder(command);
                    builder.start();
                    System.exit(0);


                } else {
                    System.out.println("Failed to delete the file");
                }
                System.out.println(currentJar.getPath());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
