package gui;

import api.spotify.Playlist;
import api.spotify.Song;
import api.spotify.Spotify;
import api.spotify.UserProfileData;
import api.spotify.login.LoginListener;

import download.DownloadListener;
import download.DownloadManager;
import general.Logger;
import general.ProxySettings;
import safe.Settings;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;

import java.io.*;
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


    //Spotify Elements
    public Label spotifyInfoLabel;
    public ProgressBar SpotifyProgressbar;
    public Label Spotifystatuslabel;
    public Label accountInfoLabel;
    public Button loginbtn;
    public Label versioninfolabel;
    public ListView playlistsListView;


    //class member definitions
    private DownloadManager singleDownloadManager;
    private Settings settings = Settings.getSettings();
    private Spotify myspotify = new Spotify();
    private ArrayList<Playlist> playlists = new ArrayList<>();

    private Boolean interruptspotifyDownload = false;

    /**
     * Controller constructor loaded on GUI start
     */
    public MainWindowController() {
        Platform.runLater(() -> rootTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue.getId()) {
                case "spotifysearch":
                    //in spotify tab --> load infos
                    Logger.log("tab changed to spotify search", Logger.INFO, 2);
                    new Thread(() -> {
                        if (myspotify.isLoggedIn()) {
                            UserProfileData user = myspotify.getUserProfile();
                            Platform.runLater(() -> {
                                loginbtn.setText("Logout");
                                accountInfoLabel.setText("Logged in user: \nE-Mail: " + user.email + "\nName: " + user.name + "\nCountry: " + user.country + "\nAccount Type: " + user.product);
                            });

                            playlists = myspotify.getPlaylists();
                            Platform.runLater(() -> {
                                playlistsListView.getItems().clear();
                                for (Playlist play : playlists) {
                                    playlistsListView.getItems().add(new Label(play.name));
                                }
                                playlistsListView.getSelectionModel().selectFirst(); //select first as default
                            });
                        } else {
                            Platform.runLater(() -> {
                                accountInfoLabel.setText("Not logged in yet!!!");
                                loginbtn.setText("Login");
                            });
                        }
                    }).start();
                    break;
                case "settings":
                    Logger.log("tab changed to settings", Logger.INFO, 2);
                    Platform.runLater(() -> {
                        proxenabledcheckbox.setSelected(settings.isProxyEnabled());
                        userfield.setText(settings.getProxyUser());
                        passfield.setText(settings.getProxyPass());
                        proxportfield.setText(settings.getProxyPort());
                        proxhostfield.setText(settings.getProxyHost());
                        versioninfolabel.setText("Version: " + Main.version);

                        settingPathLabel.setText(settings.getDownloadPath());
                    });

                    break;
                case "multiplesearch":
                    Logger.log("tab changed to multiple search", Logger.INFO, 2);
                    break;
                case "basicsearch":
                    Logger.log("tab changed to basic search", Logger.INFO, 2);
                    break;
            }
        }));


        if (settings.isProxyEnabled()) { //set proxy if enabled
            ProxySettings.setProxy(settings.getProxyUser(), settings.getProxyPass(), settings.getProxyHost(), settings.getProxyPort());
        }


    }

    //------------------[ Button Action Listener ]--------------------//

    /**
     * click event of normal search button
     */
    public void searchbtn() {
        DownloadSingle();
    }

    /**
     * click event of cancel button
     */
    public void cancelbutton() {
        Logger.log("stopping download...", Logger.WARNING, 1);
        singleDownloadManager.interruptDownload();
    }

    /**
     * click event to add new song name in list
     */
    public void clickbtnDownloadList() {
        Platform.runLater(() -> tableMultipleLinks.getItems().add(new TextField("Songname")));
    }

    /**
     * click event of Download button of a list
     */
    public void startDldBtnList() {
        ArrayList<String> downloadlinks = new ArrayList<>();
        for (int i = 0; i < tableMultipleLinks.getItems().size(); i++) {
            downloadlinks.add(((TextField) tableMultipleLinks.getItems().get(i)).getText());
        }
        DownloadMultipleRec(0, downloadlinks);
    }

    /**
     * recursive method to download one song after the other
     * @param num current number in arraylist to download
     * @param downloadlinks arraylist of songnames to download
     */
    private void DownloadMultipleRec(int num, ArrayList<String> downloadlinks) {
        new Thread(() -> {
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
                    Logger.log(message, Logger.ERROR, 1);
                }

                @Override
                public void onGettingApiDataFinished() {

                }
            });

            mydownload.startDownloadJob(downloadlinks.get(num));
        }).start();
    }

    /**
     * download a single song
     */
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

    /**
     * click event of save button on the settings page
     */
    public void SettingSafeBtnClick() {
        settings.setProxyEnabled(proxenabledcheckbox.isSelected());
        settings.setProxyUser(userfield.getText());
        settings.setProxyPass(passfield.getText());
        settings.setProxyHost(proxhostfield.getText());
        settings.setProxyPort(proxportfield.getText());

        settings.safeSettings();

        if (settings.isProxyEnabled()){
            ProxySettings.setProxy(settings.getProxyUser(), settings.getProxyPass(), settings.getProxyHost(), settings.getProxyPort()); //set new proxy settings after safing
        }else {
            ProxySettings.disbleProxy();
        }

    }

    /**
     * click event of the button on the settings page to set the download path
     */
    public void selectDownloadPathBtn() {
        DirectoryChooser mychooser = new DirectoryChooser();
        mychooser.setTitle("select downloadpath");
        mychooser.setInitialDirectory(new File(settings.getDownloadPath())); //setting default windows to Download path
        File mydir = mychooser.showDialog(rootTabPane.getScene().getWindow()); //show dialog
        if (mydir != null) {
            Logger.log("setting Download path to: " + mydir.getPath(), Logger.INFO, 2);
            Platform.runLater(() -> settingPathLabel.setText(mydir.getPath()));
            settings.setDownloadPath(mydir.getPath());
        } else {
            Logger.log("nothing selected", Logger.WARNING, 2);
        }
    }

    /**
     * click event of the button on the spotify page to start the download of the spotify playlist
     */
    public void startSpotifyDownloadBtn() {
        interruptspotifyDownload = false;
        Platform.runLater(() -> {
            spotifyInfoLabel.setText("retrieving necessary data!");
            SpotifyProgressbar.setProgress(-1.0);
        });

        if (myspotify.isLoggedIn()) {
            ArrayList<Song> songs = myspotify.getSongList(playlists.get(playlistsListView.getSelectionModel().getSelectedIndex()));
            downloadSpotifyListRec(0, songs);
        } else {
            Logger.log("Not logged in", Logger.ERROR, 1);
        }
    }

    /**
     * recursive method to download one song after the other from spotify playlist
     * @param num current number in arraylist to download
     * @param songlist arraylist of Song objects to download
     */
    private void downloadSpotifyListRec(int num, ArrayList<Song> songlist) {
        new Thread(() -> {
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
                            if (!interruptspotifyDownload) {
                                downloadSpotifyListRec(num + 1, songlist);
                            }
                        }
                    });
                }

                @Override
                public void onDownloadStarted() {
                    Platform.runLater(() -> Spotifystatuslabel.setText("starting the download"));
                }

                @Override
                public void onDownloadErrored(String message) {
                    Logger.log(message, Logger.ERROR, 1);
                    Platform.runLater(() -> {
                        Spotifystatuslabel.setText("errored downloading:\n\n Error:" + message);
                        SpotifyProgressbar.setProgress(0.0);
                    });
                }

                @Override
                public void onGettingApiDataFinished() {

                }
            });

            mydownload.startDownloadJob(songlist.get(num).songname + " " + songlist.get(num).artistname);
        }).start();
    }

    /**
     * click event of button to select a new spotify user
     */
    public void newSpotifyBtnListener() {
        if (myspotify.isLoggedIn()) {
            //logout
            myspotify.logout();
            Platform.runLater(() -> {
                accountInfoLabel.setText("Not logged in yet!!!");
                loginbtn.setText("Login");
                playlistsListView.getItems().clear();
            });
        } else {
            myspotify.addLoginListener(new LoginListener() {
                @Override
                public void onLoginSuccess() {
                    UserProfileData user = myspotify.getUserProfile();
                    Platform.runLater(() -> {
                        loginbtn.setText("Logout");
                        accountInfoLabel.setText("Logged in user: \nE-Mail: " + user.email + "\nName: " + user.name + "\nCountry: " + user.country + "\nAccount Type: " + user.product);
                    });

                    //get playlists of logged in user
                    playlists = myspotify.getPlaylists();
                    Platform.runLater(() -> {
                        for (Playlist play : playlists) {
                            playlistsListView.getItems().add(new Label(play.name));
                        }
                        playlistsListView.getSelectionModel().selectFirst(); //select first as default
                    });
                }

                @Override
                public void onLoginError(String message) {
                    Logger.log(message, Logger.ERROR, 1);
                    Platform.runLater(() -> accountInfoLabel.setText("Login Error occured."));
                }
            });
            myspotify.loginNewAccount();
        }
    }

    /**
     * click event of button to update the software
     */
    public void updatebtnlistener() {
        //checking version
        String version = "";
        try {
            URL versionurl = new URL("http://lukisvpnserver.ddns.net/HowToSteelMusic/version.info");
            BufferedReader myreader = new BufferedReader(new InputStreamReader(versionurl.openStream()));
            version = myreader.readLine();
            myreader.close();
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
                    int percent;
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

                    final ArrayList<String> command = new ArrayList<>();
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
            } catch (URISyntaxException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * click event to stop the current  (spotify) download
     */
    public void spotifybtnStop() {
        interruptspotifyDownload = true;
        //TODO full interrupt also current download
    }
}
