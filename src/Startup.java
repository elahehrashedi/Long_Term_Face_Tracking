import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.sun.glass.events.KeyEvent;
import com.sun.jna.NativeLibrary;
import com.sun.jna.platform.win32.WinBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CanvasVideoSurface;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Startup extends JFrame {

    private JTabbedPane tabs;
    PlayerControlsPanel posPanel;
    private ShowImages imgPanel;
    private TrackPanel trackPanel;
    private Globals globals;
    //TrackingThread trackingThread;
    private final EmbeddedMediaPlayerComponent mediaPlayerComponent;

    //private final EmbeddedMediaPlayer mediaPlayer;
    //private MediaPlayerFactory mediaPlayerFactory;
    //CanvasVideoSurface videoSurface;
    //Canvas canvas;

    private static boolean is_track_box_available = false;
    private String mrl ;

    public static void main(String[] args) {
        //args[1]= new String("C:\\Users\\Elaheh\\Dropbox\\05_winter2016\\ver1");
        //Globals globals = new Globals(args[0]);
        Globals globals = new Globals("C:\\ver1.9");
        NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), globals.getVlcLib());
        System.setProperty("jna.library.path", globals.getJnaLib());


        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                new Startup(globals).start(); // start the GUI thread
                //(new Thread(new TrackingThread(globals))).start(); // start tracking thread
            }
        });
    }

    public Startup( Globals globals ) {


        this.globals = new Globals(globals.getWorkingPath());
        //TrackingThread test = new TrackingThread(globals);
        //Thread myfork = new Thread(test);
        //myfork.start(); // start the tracking work from previous collections

        setLocation(100, 100);
        setSize(1050, 600);
        setTitle("WSU Face");
        //setContentPane(mediaPlayerComponent);
        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        //close the window
        WindowListener exitListener = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showOptionDialog(
                        null, "Are You Sure to Close Application?",
                        "Exit Confirmation", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, null, null);
                if (confirm == 0) {
                    posPanel.exit ();
                    trackPanel.exit () ;
                    try {
                        imgPanel.moveFile();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    /*try {
                        myfork.join();
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }*/
                    System.exit(0);
                }
            }
        };
        addWindowListener(exitListener);



        //setIconImage(new ImageIcon(getClass().getClassLoader().getResource("icons/wsu.png")));
        setIconImage(new ImageIcon(getClass().getResource("/icons/wsu.png")).getImage());

        //posPanel = new JPanel();
        //posPanel.setLayout(new BorderLayout());
        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();

        //posPanel.add(mediaPlayerComponent.getVideoSurface(), BorderLayout.CENTER);
        //posPanel.setVisible(true);

        imgPanel = new ShowImages(globals);

        posPanel = new PlayerControlsPanel(mediaPlayerComponent,globals);
        //posPanel.add(mediaPlayerComponent.getVideoSurface(), BorderLayout.CENTER);
        //posPanel.setVisible(true);

        trackPanel = new TrackPanel(globals);

        tabs = new JTabbedPane();
        //ImageIcon icon = createImageIcon("images/middle.gif");
        tabs.addTab("Position selection",null ,posPanel,"Select the start position of track here");
        tabs.setMnemonicAt(0, KeyEvent.VK_1);
        //tabs.addTab("Image selection",null ,imgPanel,"Does nothing");
        //tabs.setMnemonicAt(1, KeyEvent.VK_2);
        tabs.addTab("Tracking Results",null ,trackPanel,"Watch the tracking result here");
        tabs.setMnemonicAt(1, KeyEvent.VK_3);
        add(tabs, BorderLayout.CENTER);

        // move between tabs
        tabs.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                int s = tabs.getSelectedIndex();
                if (s==0){ // video tab
                    // video start again
                    //posPanel.repaint();
                    posPanel.Play();
                    if (is_track_box_available)
                        trackPanel.Pause ();

                }
                else if (s==1){ // track panel
                    //video panel pause
                    // here is the video has been changed in the middle
                    // then we assume that the tracking box is not available
                    posPanel.Pause();
                    boolean is_video_changed = posPanel.video_changed() ;

                    if (is_video_changed){
                        is_track_box_available = false ;
                        trackPanel.is_track_box_available = false ;
                    }
                    // if tracking boxes are not available
                    if (!is_track_box_available) {
                        // if file is available
                        String filename = "./VideoPos/" + Paths.get(posPanel.mrl).getFileName().toString() +".mat" ;
                        File f = new File(filename);
                        //System.out.println("startup mrl + videopos "+posPanel.mrl + " " + filename );
                        if (f.exists()) {
                            is_track_box_available = true ;
                            trackPanel.startPlaying(posPanel.mrl); // read tracking boxes
                        }

                    }
                    else{
                        trackPanel.Play (); // play tracking panel
                    }
                }

            }
        });

        //pack();
        setVisible(true);


    }

    private void start() {
        //mrl = globals.getMediaPath()+"\\(5).avi";
        getFile getfile = new getFile(globals);
        getfile.CreateListOfFiles(globals.getMediaPath());
        String mrl = new String(globals.getMediaPath()+"\\"+getfile.getFileName(0));
        //mediaPlayerComponent.getMediaPlayer().playMedia(mrl);
        //mediaPlayer.playMedia(mrl);*/

        //System.out.println("first mrl "+ mrl) ;
        posPanel.startPlaying(mrl);
    }

}