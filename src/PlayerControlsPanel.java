
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import com.sun.awt.AWTUtilities;
import com.sun.deploy.util.StringUtils;
import com.sun.jna.platform.WindowUtils;
import com.sun.jna.platform.unix.X11;
import com.sun.jna.platform.win32.OaIdl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.binding.LibVlcConst;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.filter.swing.SwingFileFilterFactory;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import uk.co.caprica.vlcj.component.overlay.AbstractJWindowOverlayComponent;

public class PlayerControlsPanel extends JPanel implements TaskListener {

    private static final long serialVersionUID = 1L;

    private static final int SKIP_TIME_MS = 10 * 1000;

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private final EmbeddedMediaPlayerComponent mediaPlayerComponent;
    private final EmbeddedMediaPlayer mediaPlayer;

    Video_Tracking_Thread video_tracking_thread ;
    Globals globals;
    FileofPositions fileofPositions;
    int facenumber = 1 ;
    int filenumber ;
    String filenamecomplete ;

    int tracking_frames = 50 ; //always
    double threshold = 0.75;
    int tpTracking = 50 ;
    int tpNotVerified_face = 60 ;
    int tpNotFace = 100 ;

    // 50 , 0.7 , 3 , 70 , 120
    getFile getfile ;

    RepaintablePanel mediaPanel;

    private JLabel timeLabel;
    private JSlider positionSlider;
    private JLabel chapterLabel;

    private JButton previousChapterButton;
    private JButton rewindButton;
    private JButton stopButton;
    private JButton pauseButton;
    private JButton playButton;
    private JButton fastForwardButton;
    private JButton nextChapterButton;

    private JButton toggleMuteButton;
    private JSlider volumeSlider;

    private JButton captureButton;

    private JButton ejectButton;
    private JButton connectButton;

    private JButton fullScreenButton;

    private JButton subTitlesButton;

    private JFileChooser fileChooser;


    private JTextField thresholdText;
    private JTextField skiptimeText;

    private JButton thresholdButton;
    private JButton skiptimeButton;

    private JLabel TrackProgressLabel;

    private boolean mousePressedPlaying = false;

    String mrl ;
    String videoname ;

    boolean is_video_changed = false ;
    /** starting location of a drag */
    int startX = -1, startY = -1;
    /** current location of a drag */
    int curX = -1, curY = -1;
    /** true if we are in drag */
    boolean inDrag = false;
    /** real pixel size of drag in snapshop*/
    int sx = -1, sy = -1, w = -1, h = -1 , x = -1 , y = -1 ; ;
    double scaleFactor = 1 ;

    private MouseAdapter mouseAdapter1, mouseAdapter2 ;
    private boolean inPauseMode = false ;
    private Image BufferedSnapShot ;

    public PlayerControlsPanel(EmbeddedMediaPlayerComponent mediaPlayerComponent , Globals globals) {
        this.mediaPlayerComponent = mediaPlayerComponent ;
        this.mediaPlayer = mediaPlayerComponent.getMediaPlayer();
        this.globals = new Globals(globals.getWorkingPath());
        fileofPositions = new FileofPositions(globals);
        getfile = new getFile(globals);
        getfile.CreateListOfFiles(globals.getMediaPath());

        mouseAdapter1 = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                startX = p.x;
                startY = p.y;
                inDrag = true;
                //System.err.println("presses");
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                inDrag = false;
                //System.err.println("released");
            }
        };
        mouseAdapter2 = new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point p = e.getPoint();
                curX = p.x;
                curY = p.y;
                if (inDrag) {
                    repaint();
                }
                //System.err.println("dragged");
            }
        };

        setVisible(true);
        createUI();
        executorService.scheduleAtFixedRate(new UpdateRunnable(mediaPlayer), 0L, 1L, TimeUnit.SECONDS);
        video_tracking_thread = new Video_Tracking_Thread();
        video_tracking_thread.addListener(this);
    }


    private void createUI() {
        createControls();
        layoutControls();
        registerListeners();
    }

    private void createControls() {
        timeLabel = new JLabel("hh:mm:ss");

        positionSlider = new JSlider();
        positionSlider.setMinimum(0);
        positionSlider.setMaximum(1000);
        positionSlider.setValue(0);
        positionSlider.setToolTipText("Position");

        chapterLabel = new JLabel("00/00");

        previousChapterButton = new JButton();
        previousChapterButton.setIcon(new ImageIcon(getClass().getResource("/icons/control_start_blue.png")));
        previousChapterButton.setToolTipText("Go to previous chapter");

        rewindButton = new JButton();
        rewindButton.setIcon(new ImageIcon(getClass().getResource("/icons/control_rewind_blue.png")));
        rewindButton.setToolTipText("Skip back");

        stopButton = new JButton();
        stopButton.setIcon(new ImageIcon(getClass().getResource("/icons/control_stop_blue.png")));
        stopButton.setToolTipText("Stop");

        pauseButton = new JButton();
        pauseButton.setIcon(new ImageIcon(getClass().getResource("/icons/control_pause_blue.png")));
        pauseButton.setToolTipText("Play/pause");

        playButton = new JButton();
        playButton.setIcon(new ImageIcon(getClass().getResource("/icons/control_play_blue.png")));
        playButton.setToolTipText("Play");

        fastForwardButton = new JButton();
        fastForwardButton.setIcon(new ImageIcon(getClass().getResource("/icons/control_fastforward_blue.png")));
        fastForwardButton.setToolTipText("Skip forward");

        nextChapterButton = new JButton();
        nextChapterButton.setIcon(new ImageIcon(getClass().getResource("/icons/control_end_blue.png")));
        nextChapterButton.setToolTipText("Go to next chapter");

        toggleMuteButton = new JButton();
        toggleMuteButton.setIcon(new ImageIcon(getClass().getResource("/icons/sound_mute.png")));
        toggleMuteButton.setToolTipText("Toggle Mute");

        volumeSlider = new JSlider();
        volumeSlider.setOrientation(JSlider.HORIZONTAL);
        volumeSlider.setMinimum(LibVlcConst.MIN_VOLUME);
        volumeSlider.setMaximum(LibVlcConst.MAX_VOLUME);
        volumeSlider.setPreferredSize(new Dimension(100, 40));
        volumeSlider.setToolTipText("Change volume");

        captureButton = new JButton();
        captureButton.setIcon(new ImageIcon(getClass().getResource("/icons/camera.png")));
        captureButton.setToolTipText("Take picture");

        ejectButton = new JButton();
        ejectButton.setIcon(new ImageIcon(getClass().getResource("/icons/control_eject_blue.png")));
        ejectButton.setToolTipText("Load/eject media");

        connectButton = new JButton();
        connectButton.setIcon(new ImageIcon(getClass().getResource("/icons/connect.png")));
        connectButton.setToolTipText("Connect to media");

        fileChooser = new JFileChooser(globals.getMediaPath());
        fileChooser.setApproveButtonText("Play");
        fileChooser.addChoosableFileFilter(SwingFileFilterFactory.newVideoFileFilter());
        fileChooser.addChoosableFileFilter(SwingFileFilterFactory.newAudioFileFilter());
        fileChooser.addChoosableFileFilter(SwingFileFilterFactory.newPlayListFileFilter());
        FileFilter defaultFilter = SwingFileFilterFactory.newMediaFileFilter();
        fileChooser.addChoosableFileFilter(defaultFilter);
        fileChooser.setFileFilter(defaultFilter);
        //File workingDirectory = new File(System.getProperty(globals.getMediaPath()));
        //fileChooser.setCurrentDirectory(workingDirectory);
        fullScreenButton = new JButton();
        fullScreenButton.setIcon(new ImageIcon(getClass().getResource("/icons/image.png")));
        fullScreenButton.setToolTipText("Toggle full-screen");

        subTitlesButton = new JButton();
        subTitlesButton.setIcon(new ImageIcon(getClass().getResource("/icons/comment.png")));
        subTitlesButton.setToolTipText("Cycle sub-titles");

        thresholdText = new JTextField(String.valueOf(threshold),3) ; // 0.75 default
        skiptimeText = new JTextField(String.valueOf(tpNotVerified_face),2); // 60 default

        thresholdButton = new JButton("Threshold = "+String.valueOf(threshold)) ;
        thresholdButton.setToolTipText("the similarity threshold between [0,1]");
        skiptimeButton = new JButton ("Skip Frame = "+String.valueOf(tpNotVerified_face)) ;
        skiptimeButton.setToolTipText("Number of frames that the algorithm can skip");
        TrackProgressLabel = new JLabel("You can start tracking a face.");
        //TrackProgressLabel = new JLabel("<<Tracking is under Progress>>");
    }

    private void layoutControls() {
        setBorder(new EmptyBorder(4, 4, 4, 4));

        setLayout(new BorderLayout());

        JPanel positionPanel = new JPanel();
        positionPanel.setLayout(new GridLayout(1, 1));
        positionPanel.add(positionSlider);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout(8, 0));

        topPanel.add(timeLabel, BorderLayout.WEST);
        topPanel.add(positionPanel, BorderLayout.CENTER);
        topPanel.add(chapterLabel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // my new panel to add media player component
        mediaPanel = new RepaintablePanel();
        mediaPanel.setLayout(new GridLayout(1, 1));
        mediaPanel.add(mediaPlayerComponent.getVideoSurface(), BorderLayout.CENTER);
        add(mediaPanel,BorderLayout.CENTER);

        JPanel lowestPanel_pos = new JPanel();
        lowestPanel_pos.setLayout(new GridLayout(2, 1));

        JPanel bottomPanel_pos = new JPanel();
        bottomPanel_pos.setLayout(new FlowLayout());
        bottomPanel_pos.add(previousChapterButton);
        bottomPanel_pos.add(rewindButton);
        bottomPanel_pos.add(stopButton);
        bottomPanel_pos.add(pauseButton);
        bottomPanel_pos.add(playButton);
        bottomPanel_pos.add(fastForwardButton);
        bottomPanel_pos.add(nextChapterButton);
        bottomPanel_pos.add(volumeSlider);
        bottomPanel_pos.add(toggleMuteButton);
        bottomPanel_pos.add(captureButton);
        bottomPanel_pos.add(ejectButton);
        bottomPanel_pos.add(connectButton);
        bottomPanel_pos.add(fullScreenButton);
        bottomPanel_pos.add(subTitlesButton);
        //add(bottomPanel, BorderLayout.SOUTH);

        JPanel bottomPanel2_pos = new JPanel();
        bottomPanel2_pos.setLayout(new FlowLayout());
        bottomPanel2_pos.add(thresholdText);
        bottomPanel2_pos.add(thresholdButton);
        bottomPanel2_pos.add(skiptimeText);
        bottomPanel2_pos.add(skiptimeButton);
        bottomPanel2_pos.add(TrackProgressLabel);

        lowestPanel_pos.add(bottomPanel_pos,BorderLayout.NORTH);
        lowestPanel_pos.add(bottomPanel2_pos,BorderLayout.SOUTH);
        add(lowestPanel_pos, BorderLayout.SOUTH);
    }

    /**
     * Broken out position setting, handles updating mediaPlayer
     */
    private void setSliderBasedPosition() {
        if(!mediaPlayer.isSeekable()) {
            //System.out.println("not seekable");
            //not seekable
            return;
        }
        float positionValue = positionSlider.getValue() / 1000.0f;
        // Avoid end of file freeze-up
        if(positionValue > 0.99f) {
            positionValue = 0.99f;
        }
        mediaPlayer.setPosition(positionValue);
    }

    private void updateUIState() {
        if(!mediaPlayer.isPlaying()) {
            // Resume play or play a few frames then pause to show current position in video
            mediaPlayer.play();
            if(!mousePressedPlaying) {
                try {
                    // Half a second probably gets an iframe
                    Thread.sleep(500);
                }
                catch(InterruptedException e) {
                    // Don't care if unblocked early
                }
                mediaPlayer.pause();
            }
        }
        long time = mediaPlayer.getTime();
        int position = (int)(mediaPlayer.getPosition() * 1000.0f);
        int chapter = mediaPlayer.getChapter();
        int chapterCount = mediaPlayer.getChapterCount();
        updateTime(time);
        updatePosition(position);
        updateChapter(chapter, chapterCount);
    }

    private void skip(int skipTime) {
        // Only skip time if can handle time setting
        if(mediaPlayer.getLength() > 0) {
            mediaPlayer.skip(skipTime);
            updateUIState();
        }
    }

    private void registerListeners() {

        //mediaPlayer.addMediaPlayerEventListener(mediaAdapter);

        positionSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(mediaPlayer.isPlaying()) {
                    mousePressedPlaying = true;
                    mediaPlayer.pause();
                }
                else {
                    mousePressedPlaying = false;
                }
                setSliderBasedPosition();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                setSliderBasedPosition();
                updateUIState();
            }
        });

        previousChapterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //mediaPlayer.previousChapter();
                //fileofPositions.appendAll();
            }
        });

        rewindButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                skip(-SKIP_TIME_MS);
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mediaPlayer.stop();
            }
        });

        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //enable the pause
                if (!inPauseMode) {
                    nextChapterButton.setEnabled(false);
                    inPauseMode = true;
                    mediaPlayer.pause();
                    //pause action
                    BufferedSnapShot = mediaPlayer.getSnapshot();
                    mediaPlayerComponent.getVideoSurface().setVisible(false);
                    mediaPanel.repaint();
                    mediaPanel.addMouseListener(mouseAdapter1);
                    mediaPanel.addMouseMotionListener(mouseAdapter2);
                }
            }

        });

        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //play action
                if (inPauseMode) {
                    nextChapterButton.setEnabled(true);
                    mediaPanel.removeMouseListener(mouseAdapter1);
                    mediaPanel.removeMouseMotionListener(mouseAdapter2);
                    mediaPlayerComponent.getVideoSurface().setVisible(true);
                    inPauseMode = false ;
                    startX = -1 ; startY = -1;
                    inDrag = false;
                }
                //disable the pause
                inPauseMode = false ;
                BufferedSnapShot = null ;
                mediaPlayer.play();
            }
        });

        fastForwardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                skip(SKIP_TIME_MS);
            }
        });

        nextChapterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                mediaPlayer.stop();
                fileofPositions.appendAll();
                //mediaPlayer.nextChapter();
                // add movie to processed folder
                Path destination = FileSystems.getDefault().getPath( new String(globals.getProcessedPath()+"\\"+filenamecomplete ));
                Path source = FileSystems.getDefault().getPath(mrl);
                try {
                    Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
                }catch (IOException error){
                    error.printStackTrace();
                }

                // get next file in folder
                String videonamesmall = getfile.nextfile();
                videoname = videonamesmall;
                if (videonamesmall!=null) {
                    // play next file
                    mrl = new String(globals.getMediaPath() + "\\" + videonamesmall);
                    startPlaying(mrl);
                    is_video_changed = true ;
                }else
                    JOptionPane.showMessageDialog(null, "There is no more video to show !!!");

            }
        });

        toggleMuteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mediaPlayer.mute();
            }
        });

        volumeSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider)e.getSource();
                // if(!source.getValueIsAdjusting()) {
                mediaPlayer.setVolume(source.getValue());
                // }
            }
        });

        captureButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (startX >= 0 || startY >= 0) {

                    String filename = "./VideoPos/" + Paths.get(mrl).getFileName().toString() +".mat" ;
                    File f = new File(filename);
                    //System.out.println("startup mrl + videopos "+posPanel.mrl + " " + filename );
                    if (f.exists()) {
                        // inform the user that the tracking result is available
                        int choice;
                        do {
                            choice = JOptionPane.showOptionDialog(null,
                                    "There is a tracking result already available for this video name.\nDo you want to delete the previous result and create a new one?",
                                    "Warning",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.QUESTION_MESSAGE,
                                    null, new String[]{"Generate NEW results", "Use PREVIOUS results"}, null);
                            // interpret the user's choice
                        }while(choice == JOptionPane.CLOSED_OPTION);
                        if (choice == JOptionPane.YES_OPTION)
                        {
                            captureButton.setEnabled(false); // disable capturing another face untill thread tracking  complete its job
                            TrackProgressLabel.setText("=>Tracking is under Progress.");
                            TrackProgressLabel.setForeground(Color.red);
                            f.delete(); // delete previous file
                            // reset the track panel to zero situation, as if the video has been changed
                            is_video_changed = true ;
                            // Generate NEW results
                            System.out.println("Start Tracking");
                            /** real pixel size of drag in snapshop*/
                            int xPixel = (int) ((sx-x)/scaleFactor);
                            int yPixel = (int) ((sy-y)/scaleFactor) ;
                            int wPixel = (int) (w/scaleFactor);
                            int hPixel = (int) (h/scaleFactor);
                            int framenumber = (int) (mediaPlayer.getTime()*mediaPlayer.getFps()/1000);
                            //mediaPlayer.saveSnapshot();
                            //fileofPositions.appendTo(filenumber,facenumber ,xPixel,yPixel,wPixel,hPixel,framenumber);
                            //video_tracking_thread = new Video_Tracking_Thread();
                            video_tracking_thread.Call_Video_Tracking_Thread(globals,mrl,mediaPlayer.getTime()/1000,xPixel,yPixel,xPixel+wPixel,yPixel+hPixel,
                                    tracking_frames , threshold , tpTracking , tpNotVerified_face , tpNotFace );

                            //video_tracking_thread.addListener();
                            Thread myfork = new Thread(video_tracking_thread);

                            //System.out.println("tracking started");
                            myfork.start(); // start the tracking work from previous collections
                            facenumber_plus();

                        }
                        else {
                            //Use PREVIOUS results
                            //System.out.println("use previou");
                        }

                    }else{ // doesnot exist
                        captureButton.setEnabled(false); // disable capturing another face untill thread tracking  complete its job
                        TrackProgressLabel.setText("=>Tracking is under Progress.");
                        TrackProgressLabel.setForeground(Color.red);
                        // Generate NEW results
                        System.out.println("Start Tracking");
                        /** real pixel size of drag in snapshop*/
                        int xPixel = (int) ((sx-x)/scaleFactor);
                        int yPixel = (int) ((sy-y)/scaleFactor) ;
                        int wPixel = (int) (w/scaleFactor);
                        int hPixel = (int) (h/scaleFactor);
                        int framenumber = (int) (mediaPlayer.getTime()*mediaPlayer.getFps()/1000);
                        //mediaPlayer.saveSnapshot();
                        //fileofPositions.appendTo(filenumber,facenumber ,xPixel,yPixel,wPixel,hPixel,framenumber);
                        //video_tracking_thread = new Video_Tracking_Thread();
                        video_tracking_thread.Call_Video_Tracking_Thread(globals,mrl,mediaPlayer.getTime()/1000,xPixel,yPixel,xPixel+wPixel,yPixel+hPixel,
                                tracking_frames , threshold , tpTracking , tpNotVerified_face , tpNotFace );

                        //video_tracking_thread.addListener();
                        Thread myfork = new Thread(video_tracking_thread);

                        //System.out.println("tracking started");
                        myfork.start(); // start the tracking work from previous collections
                        facenumber_plus();
                    }

                }
            }
        });

        ejectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (inPauseMode) {
                    //play action
                    if (inPauseMode) {
                        nextChapterButton.setEnabled(true);
                        mediaPanel.removeMouseListener(mouseAdapter1);
                        mediaPanel.removeMouseMotionListener(mouseAdapter2);
                        mediaPlayerComponent.getVideoSurface().setVisible(true);
                        inPauseMode = false ;
                        startX = -1 ; startY = -1;
                        inDrag = false;
                    }
                    //disable the pause
                    inPauseMode = false ;
                    BufferedSnapShot = null ;
                    mediaPlayer.play();
                }
                //eject
                mediaPlayer.enableOverlay(false);
                mediaPlayer.pause();


                if(JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(PlayerControlsPanel.this)) {
                    mrl = fileChooser.getSelectedFile().getAbsolutePath();
                    Path temppath = Paths.get(mrl);
                    videoname = temppath.getFileName().toString();
                    mediaPlayer.playMedia(mrl);
                    is_video_changed = true ;
                }

                mediaPlayer.enableOverlay(true);
            }
        });

        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mediaPlayer.enableOverlay(false);
                String mediaUrl = JOptionPane.showInputDialog(PlayerControlsPanel.this, "Enter a media URL", "Connect to media", JOptionPane.QUESTION_MESSAGE);
                if(mediaUrl != null && mediaUrl.length() > 0) {
                    mediaPlayer.playMedia(mediaUrl);
                }
                mediaPlayer.enableOverlay(true);
            }
        });

        fullScreenButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mediaPlayer.toggleFullScreen();
            }
        });

        subTitlesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int spu = mediaPlayer.getSpu();
                if(spu > -1) {
                    spu ++ ;
                    if(spu > mediaPlayer.getSpuCount()) {
                        spu = -1;
                    }
                }
                else {
                    spu = 0;
                }
                mediaPlayer.setSpu(spu);
            }
        });

        thresholdButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                double t = Double.parseDouble(thresholdText.getText());
                if (t> 0 && t <= 1)
                {
                    threshold = t;
                    thresholdButton.setText("Threshold = "+String.valueOf(threshold)) ;

                }
                else
                    threshold = 0.75;
                    thresholdButton.setText("Threshold = "+String.valueOf(threshold)) ;
                //System.out.println("threshold changed to " + threshold );
            }
        });
        skiptimeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int tp = Integer.parseInt(skiptimeText.getText());
                //tpTracking = tp;
                tpNotVerified_face = tp;
                tpNotFace = (int) ((1.5)*tp);
                skiptimeButton.setText("Skip Frame = "+String.valueOf(tpNotVerified_face));
                //System.out.println("tp changed to " + tp );
            }
        });



    }

    @Override
    public void threadComplete(Runnable runner) {
        //System.out.print("work complete");
        JFrame parent = new JFrame();
        if (video_tracking_thread.Is_successful()==1)
            JOptionPane.showMessageDialog(parent, "Tracking is completed");
        else {
            JOptionPane.showMessageDialog(parent, "Tracking was not successful, please choose another frontal face");
        }
        captureButton.setEnabled(true);
        TrackProgressLabel.setText("You can start tracking a face.");
        TrackProgressLabel.setForeground(Color.black);
    }


    private final class UpdateRunnable implements Runnable {

        private final MediaPlayer mediaPlayer;

        private UpdateRunnable(MediaPlayer mediaPlayer) {
            this.mediaPlayer = mediaPlayer;
        }

        @Override
        public void run() {

            final int position = (int)(mediaPlayer.getPosition() * 1000.0f);
            final long time = mediaPlayer.getTime();
            final int chapter = mediaPlayer.getChapter();
            final int chapterCount = mediaPlayer.getChapterCount();

            //System.out.println("time " + time);

            // Updates to user interface components must be executed on the Event
            // Dispatch Thread
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer.isPlaying()) {
                        //System.out.println("invoke later pospanel");
                        updateTime(time);
                        updatePosition(position);
                        updateChapter(chapter, chapterCount);
                    }
                }
            });
        }
    }

    private void updateTime(long millis) {
        String s = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis), TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        timeLabel.setText(s);
    }

    private void updatePosition(int value) {
        // positionProgressBar.setValue(value);
        positionSlider.setValue(value);
        //if (value>985)
            //System.out.println(value);
    }

    private void updateChapter(int chapter, int chapterCount) {
        String s = chapterCount != -1 ? (chapter + 1) + "\\" + chapterCount : "-";
        chapterLabel.setText(s);
        chapterLabel.invalidate();
        validate();
    }

    private void updateVolume(int value) {
        volumeSlider.setValue(value);
    }


    public class RepaintablePanel extends JPanel {

        public void RepaintablePanel(){
            setOpaque(true);
            this.setBackground(Color.BLACK);
        }
        public void paint(Graphics g) {

            //super.paint(g);
            if (BufferedSnapShot!=null) {
               int tempw = BufferedSnapShot.getWidth(this); // this was because matlab code
               int temph = BufferedSnapShot.getHeight(this);
               //scaleFactor = Math.min(1d, getScaleFactorToFit(new Dimension(tempw,temph ), getSize()));
               scaleFactor = getScaleFactorToFit(new Dimension(tempw,temph ), getSize());
               int scaleWidth = (int) Math.round(tempw * scaleFactor);
               int scaleHeight = (int) Math.round(temph * scaleFactor);
               int width = getWidth() - 1;
               int height = getHeight() - 1;
               x = (width - scaleWidth) / 2;
               y = (height - scaleHeight) / 2;
               g.drawImage(BufferedSnapShot, x, y, scaleWidth, scaleHeight, this);
               if (startX >= 0 || startY >= 0) {
                   if (inDrag) {
                       w = curX - startX; h = curY - startY;
                       sx = startX; sy = startY;
                       if (w < 0) { w = -w;sx = curX;}
                       if (h < 0) { h = -h; sy = curY; }
                       g.setColor(Color.yellow);
                       g.drawRect(sx, sy, w, h);

                   }
                   // delete snapshot rectangle
                   else { startX = -1 ; startY = -1; }
               }

           }

        }
    }

    public static double getScaleFactorToFit(Dimension original, Dimension toFit) {
        double dScale = 1d;
        if (original != null && toFit != null) {
            double dScaleWidth = getScaleFactor(original.width, toFit.width);
            double dScaleHeight = getScaleFactor(original.height, toFit.height);
            dScale = Math.min(dScaleHeight, dScaleWidth);
        }
        return dScale;
    }
    public static double getScaleFactor(int iMasterSize, int iTargetSize) {
        double dScale = 1;
        dScale = (double) iTargetSize / (double) iMasterSize;
        return dScale;
    }

    private void reset_facenumber()    {
        facenumber = 1 ;
    }
    private void facenumber_plus(){
        facenumber ++ ;
    }
    private void reset_filenumber(String mrl){
        Pattern pattern = Pattern.compile("\\(\\d+\\)\\.");
        Matcher matcher = pattern.matcher(mrl);
        if (matcher.find()) {
            String temp = matcher.group(0);
            temp = temp.replace("(","");
            temp = temp.replace(")","");
            temp = temp.replace(".","");
            filenumber = Integer.parseInt(temp);
        }
        Path temppath = Paths.get(mrl);
        filenamecomplete = temppath.getFileName().toString();
    }

    public void startPlaying ( String mrl ){
        //System.out.println("start playing in posepanle mrl "+ mrl);
        this.mrl = mrl ;
        Path temppath = Paths.get(mrl);
        videoname = temppath.getFileName().toString();
        reset_facenumber();
        reset_filenumber(mrl);
        mediaPlayer.playMedia(mrl);
        mediaPlayer.setRepeat(true);

    }

    public void Pause (){

        if (!inPauseMode) {
            nextChapterButton.setEnabled(false);
            inPauseMode = true;
            mediaPlayer.pause();
            //pause action
            BufferedSnapShot = mediaPlayer.getSnapshot();
            mediaPlayerComponent.getVideoSurface().setVisible(false);
            mediaPanel.repaint();

            mediaPanel.addMouseListener(mouseAdapter1);
            mediaPanel.addMouseMotionListener(mouseAdapter2);
        }
        //repaint();

    }

    public boolean video_changed (){
        if (is_video_changed){
            is_video_changed = false ;
            return true;
        } else
            return false ;
    }
    public void Play(){

        if (inPauseMode) {
            nextChapterButton.setEnabled(true);
            mediaPanel.removeMouseListener(mouseAdapter1);
            mediaPanel.removeMouseMotionListener(mouseAdapter2);
            mediaPlayerComponent.getVideoSurface().setVisible(true);
            inPauseMode = false ;
            startX = -1 ; startY = -1;
            inDrag = false;
        }
        //disable the pause
        inPauseMode = false ;
        BufferedSnapShot = null ;
        mediaPlayer.play();
        //repaint();

    }

    public  void exit() {

    }

}

