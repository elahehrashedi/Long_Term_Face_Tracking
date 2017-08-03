import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLDouble;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;
import uk.co.caprica.vlcj.binding.LibVlcConst;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.bytedeco.javacv.FrameGrabber;
import uk.co.caprica.vlcj.player.MediaPlayer;

public class TrackPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int SKIP_TIME_MS = 10 * 1000;

    private final ScheduledExecutorService executorService2 = Executors.newSingleThreadScheduledExecutor();

    private FFmpegFrameGrabber frameGrabber ;
    Java2DFrameConverter converter;
    Frame frame_buf ;

    static RectDraw newrect;

    Globals globals;

    RepaintablePanel mediaPanel; // panel of video surface only

    private JLabel timeLabel;
    private JLabel timeLabel2;

    private JSlider positionSlider;
    private JLabel chapterLabel;
    private JLabel chapterLabel2;

    private JButton rewindButton;
    private JButton pauseButton;
    private JButton playButton;
    private JButton fastForwardButton;
    private JButton toggleMuteButton;
    private JSlider volumeSlider;

    private JLabel TrackingLabel;

    private static double[][] mlArrayDouble ;

    String mrl ;
    static int numberofFrames ;
    static int currentFrame = 0 ;
    static double frameRate = 1 ;
    static long duration = 0 ;
    static int mlArrayDouble_itr = 0 ; // iterator of mlArrayDouble
    static int mlArrayDouble_lenght = 0 ; // iterator of mlArrayDouble
    private boolean mousePressedPlaying = false;

    /** starting location of a drag */
    int startX = -1, startY = 100;
    /** current location of a drag */
    int StartW = 100, StartH = 100;

    double scaleFactor = 1 ;
    private Image BufferedSnapShot ;
    private Image SnippingAjaxLoad ;
    private boolean inPauseMode = true ;
    public static boolean is_track_box_available = false;

    ShowFrameThread videorunner ;
    Thread videorunnerthread ;

    boolean continue_showing = true ;

    public TrackPanel( Globals globals) {

        this.globals = new Globals(globals.getWorkingPath());
        // in the beginning there is nothing in tracking panel
        // only a spinning circle
        SnippingAjaxLoad = new ImageIcon (getClass().getResource("/icons/Ajax_loader.gif")).getImage();
        setVisible(true);
        createUI();
        converter = new Java2DFrameConverter();
        videorunner = new ShowFrameThread();
        videorunnerthread = new Thread(videorunner);
        videorunnerthread.start(); // start the thread to repaint the panel
        //executorService2.scheduleAtFixedRate(new UpdateRunnable2(frameGrabber), 0L, 1L, TimeUnit.SECONDS); //ella delete 7:52 pm 04/21
        //executorService2.scheduleAtFixedRate(new UpdateRunnable2(), 0L, 1L, TimeUnit.SECONDS); //ella delete 7:52 pm 04/21
    }

    private void createUI() {
        createControls();
        layoutControls();
        registerListeners();
    }

    // when mouse drag the slider
    private void setSliderBasedPosition() {
        if (frameGrabber==null)
            return;
        float positionValue = positionSlider.getValue() / 1000.0f;
        if(positionValue > 0.99f) {
            positionValue = 0.99f;
        }
        int distance = currentFrame ;
        //System.out.println( positionValue + " "+ frameGrabber.getFrameRate() + " " + frameGrabber.getLengthInTime() / 1000000 );
        currentFrame = (int) ((positionValue * (double)frameGrabber.getFrameRate() * (double) frameGrabber.getLengthInTime()) /  1000000.0f );
        //System.out.println("new slider current frame " + currentFrame) ;
        distance = currentFrame - distance ;
        if (distance<0){ // to move the iterator backward
            mlArrayDouble_itr += distance ;
            if (mlArrayDouble_itr<0)
                mlArrayDouble_itr = 0;
        }

        try {
            frameGrabber.setFrameNumber(currentFrame);
            //update position
        } catch (FrameGrabber.Exception e) {
            System.out.println("set frame exception");
            e.printStackTrace();
        }
        System.out.println("new current frame "+currentFrame +" / frame grabber current frame " + frameGrabber.getFrameNumber());
    }

    private void updateUIState() {
        if (inPauseMode){
            Play();
            if(!mousePressedPlaying) {
                try {
                    // Half a second probably gets an iframe
                    Thread.sleep(500);
                }
                catch(InterruptedException e) {
                    // Don't care if unblocked early
                }
                Pause();
            }
        }
        final long time = frameGrabber.getTimestamp() / 1000 ;//(long) (currentFrame / frameRate);
        final double temp =  (double)frameGrabber.getFrameNumber() / (double)frameGrabber.getLengthInFrames() ;
        //System.out.println("temp " + temp + " " +  frameGrabber.getTimestamp() + " " + frameGrabber.getLengthInTime() ) ;
        //System.out.println("temp " + temp + " " +  frameGrabber.getFrameNumber() + " " + frameGrabber.getLengthInFrames() ) ;
        final int position = (int)( temp * 1000); // time/duration
        updateTime(time);
        updatePosition(position);


    }

    private void skip(int skipTime) {
        // Only skip time if can handle time setting
        /*if(mediaPlayer.getLength() > 0) {
            mediaPlayer.skip(skipTime);
            updateUIState();
        }*/
    }

    private void registerListeners() {

        positionSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {

                if (!inPauseMode) {
                    mousePressedPlaying = true;
                    Pause();
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


        rewindButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                skip(-SKIP_TIME_MS);
            }
        });


        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //enable the pause
                //System.out.println("pause");
                Pause(); // our function
            }

        });

        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //play action
                //System.out.println("play");
                Play();
            }
        });

        fastForwardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                skip(SKIP_TIME_MS);
            }
        });


        toggleMuteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //mediaPlayer.mute();
            }
        });

        volumeSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider)e.getSource();
                //mediaPlayer.setVolume(source.getValue());
            }
        });

    }

    private void createControls() {

        timeLabel = new JLabel("hh:mm:ss");
        timeLabel2 = new JLabel("hh:mm:ss");
        timeLabel2.setForeground(Color.lightGray);

        positionSlider = new JSlider();
        positionSlider.setMinimum(0);
        positionSlider.setMaximum(1000);
        positionSlider.setValue(0);
        positionSlider.setToolTipText("Position");

        chapterLabel = new JLabel("00/00");
        chapterLabel2 = new JLabel("00/00");
        chapterLabel2.setForeground(Color.lightGray);

        rewindButton = new JButton();
        rewindButton.setIcon(new ImageIcon(getClass().getResource("/icons/control_rewind_blue.png")));
        rewindButton.setToolTipText("Skip back");

        pauseButton = new JButton();
        pauseButton.setIcon(new ImageIcon(getClass().getResource("/icons/control_pause_blue.png")));
        pauseButton.setToolTipText("Play/pause");

        playButton = new JButton();
        playButton.setIcon(new ImageIcon(getClass().getResource("/icons/control_play_blue.png")));
        playButton.setToolTipText("Play");

        fastForwardButton = new JButton();
        fastForwardButton.setIcon(new ImageIcon(getClass().getResource("/icons/control_fastforward_blue.png")));
        fastForwardButton.setToolTipText("Skip forward");


        toggleMuteButton = new JButton();
        toggleMuteButton.setIcon(new ImageIcon(getClass().getResource("/icons/sound_mute.png")));
        toggleMuteButton.setToolTipText("Toggle Mute");

        volumeSlider = new JSlider();
        volumeSlider.setOrientation(JSlider.HORIZONTAL);
        volumeSlider.setMinimum(LibVlcConst.MIN_VOLUME);
        volumeSlider.setMaximum(LibVlcConst.MAX_VOLUME);
        volumeSlider.setPreferredSize(new Dimension(100, 40));
        volumeSlider.setToolTipText("Change volume");

        newrect= new RectDraw();

        TrackingLabel = new JLabel("Copyright by Wayne State University © 2016");
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

        ////////////////////////////////////////////
        JPanel rectpanel = new JPanel();
        rectpanel.setLayout(new GridLayout(1, 1));
        rectpanel.add(newrect);
        JPanel timerectpanel = new JPanel();
        timerectpanel.setLayout(new BorderLayout(15, 0));
        timerectpanel.add(timeLabel2, BorderLayout.WEST);
        timerectpanel.add(rectpanel, BorderLayout.CENTER);
        timerectpanel.add(chapterLabel2, BorderLayout.EAST);

        JPanel bothtoppanel = new JPanel();
        bothtoppanel.setLayout(new GridLayout(2, 1));

        bothtoppanel.add(topPanel,BorderLayout.NORTH);
        bothtoppanel.add(timerectpanel,BorderLayout.SOUTH);
        add(bothtoppanel, BorderLayout.NORTH);

        // my new panel to add media player component
        mediaPanel = new RepaintablePanel();
        mediaPanel.setLayout(new GridLayout(1, 1));
        //mediaPanel.add(mediaPlayerComponent.getVideoSurface(), BorderLayout.CENTER); // ella deleted
        //mediaPlayerComponent.getVideoSurface().setVisible(false); // ella deleted
        //mediaPanel.add (SnippingAjaxLoad,BorderLayout.CENTER);//ella deleted
        add(mediaPanel,BorderLayout.CENTER);

        JPanel lowestPanel_tr = new JPanel();
        lowestPanel_tr.setLayout(new GridLayout(2, 1));

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());
        bottomPanel.add(rewindButton);
        bottomPanel.add(pauseButton);
        bottomPanel.add(playButton);
        bottomPanel.add(fastForwardButton);
        bottomPanel.add(volumeSlider);
        bottomPanel.add(toggleMuteButton);

        JPanel bottomPanel2_tr = new JPanel();
        bottomPanel2_tr.setLayout(new FlowLayout());
        bottomPanel2_tr.add(TrackingLabel);

        lowestPanel_tr.add(bottomPanel,BorderLayout.NORTH);
        lowestPanel_tr.add(bottomPanel2_tr,BorderLayout.SOUTH);
        add(lowestPanel_tr, BorderLayout.SOUTH);
        ////////////////////////////
        //disable all bottoms
        rewindButton.setEnabled(false);
        pauseButton.setEnabled(true);
        playButton.setEnabled(true);
        fastForwardButton.setEnabled(false);
        volumeSlider.setEnabled(false);
        toggleMuteButton.setEnabled(false);
    }

    // if the boxes are available we will start
    public void startPlaying ( String mrl ){
        //System.out.println("start playing");

        if (frameGrabber!=null) {
            try {
                frameGrabber.stop();
            } catch (FrameGrabber.Exception e) {
                e.printStackTrace();
            }
            frameGrabber = null;
        }

        BufferedSnapShot = null ;
        if (mlArrayDouble!=null){
            //System.out.println("array not null");
            mlArrayDouble = null ;
            mlArrayDouble_itr = 0 ;
            mlArrayDouble_lenght = 0 ;
            //newrect.repaint();
            //newrect.paint(newrect.getGraphics());
            //mediaPanel.paint(mediaPanel.getGraphics());
            newrect.paintComponent(newrect.getGraphics());
        }
        updateTime(0);
        updatePosition(0);

        //mediaPanel.repaint();
        if (!is_track_box_available ) {
            this.mrl = mrl;
            //enable all bottoms
            rewindButton.setEnabled(false);
            pauseButton.setEnabled(true);
            playButton.setEnabled(true);
            fastForwardButton.setEnabled(false);
            volumeSlider.setEnabled(false);
            toggleMuteButton.setEnabled(false);

            //System.getProperty("java.library.path");
            frameGrabber = new FFmpegFrameGrabber(mrl);
            try {
                frameGrabber.start();
            } catch (FrameGrabber.Exception e) {
                e.printStackTrace();
            }
            numberofFrames = frameGrabber.getLengthInFrames() ;
            frameRate = frameGrabber.getFrameRate();
            currentFrame = 0 ;
            duration = frameGrabber.getLengthInTime()/1000000 ;
            //System.out.println("number of frames after grabbing "+numberofFrames);
            loadBoxes(); // load boxes
            inPauseMode = false ;
            //videorunnerthread.start(); // start the thread to repaint the panel
            executorService2.scheduleAtFixedRate(new UpdateRunnable2(frameGrabber), 0L, 1L, TimeUnit.SECONDS);
        }
    }

    // if boxes are available we will load them
    public  void loadBoxes (){
        //System.out.println("start loading boxes");
        MatFileReader matfilereader = null;
        try {
            Path temppath = Paths.get(mrl);
            String positionFileName = "./VideoPos/" + temppath.getFileName().toString() + ".mat";
            //System.out.println ("looking for position files " + positionFileName);
            matfilereader = new MatFileReader(positionFileName);
            mlArrayDouble = ((MLDouble) matfilereader.getMLArray("positionMat")).getArray();
            mlArrayDouble_lenght = mlArrayDouble.length;
            //System.out.println ("read mat file");
            is_track_box_available = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // to be changed
    private void Show_one_Frame (){
        int i;
        try {
            //System.out.println("play media");
            //converter.convert(frameGrabber.grab());
            frame_buf = frameGrabber.grabImage();
            // Convert frame to an buffered image so it can be processed and saved
            //Image img = (new BufferToImage((VideoFormat) buf.getFormat()).createImage(buf));
            BufferedSnapShot = converter.convert(frame_buf);

            if (BufferedSnapShot!=null) {
                currentFrame++;

                while (mlArrayDouble_itr < mlArrayDouble.length && mlArrayDouble[mlArrayDouble_itr][0] < currentFrame) {
                    mlArrayDouble_itr++;
                }
                if (mlArrayDouble_itr < mlArrayDouble.length && mlArrayDouble[mlArrayDouble_itr][0] == currentFrame) {
                    startX = (int) mlArrayDouble[mlArrayDouble_itr][1];
                    startY = (int) mlArrayDouble[mlArrayDouble_itr][2];
                    StartH = (int) mlArrayDouble[mlArrayDouble_itr][3];
                    StartW = (int) mlArrayDouble[mlArrayDouble_itr][4];
                    //System.out.println(startX +" "+startY+" "+StartH+" "+StartW);
                }
                try {
                    Thread.sleep(10);  //milliseconds
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                mediaPanel.paint(mediaPanel.getGraphics());
                if (currentFrame > numberofFrames) { //
                    continue_showing = false;
                    //inPauseMode = true ;
                }
            }else { // if we reach to the end of tracking
                BufferedSnapShot = null ;
                updateTime(0);
                updatePosition(0);
                frameGrabber.setTimestamp(0); // start from zero again
                currentFrame = 0 ;
                inPauseMode = false ;
                mlArrayDouble_itr = 0 ;
            }

        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
    }

    // for button pause
    public void Pause (){
        if (is_track_box_available) {
            if (!inPauseMode) {
                inPauseMode = true;
            }
        }

    }

    //for button play
    public void Play() {
        if (is_track_box_available) {
            if (inPauseMode) {
                inPauseMode = false;
            }
        }

    }

    private final class ShowFrameThread implements Runnable {
        private ShowFrameThread() {
        }
        public void run() {
            while (continue_showing) { //stop thread later
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!inPauseMode) {
                    //System.out.println("showing frame in thread");
                    Show_one_Frame();
                } else {
                    // if inPauseMode sleep for longer time
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }

    }


    private void updateVolume(int value) {
        volumeSlider.setValue(value);
    }

    // for media panel of video surface
    public class RepaintablePanel extends JPanel {

        public void RepaintablePanel(){
            setOpaque(true);
            this.setBackground(Color.BLACK);
        }
        public void paint(Graphics g) {
            //super.paint(g); // showing this line makes a problem
            if (is_track_box_available) {
                if (BufferedSnapShot!=null) {
                    int tempw = BufferedSnapShot.getWidth(this); // this was because matlab code
                    int temph = BufferedSnapShot.getHeight(this);
                    scaleFactor = getScaleFactorToFit(new Dimension(tempw, temph), getSize());
                    int scaleWidth = (int) Math.round(tempw * scaleFactor);
                    int scaleHeight = (int) Math.round(temph * scaleFactor);
                    int width = getWidth() - 1;
                    int height = getHeight() - 1;
                    int x = (width - scaleWidth) / 2;
                    int y = (height - scaleHeight) / 2;
                    g.drawImage(BufferedSnapShot, x, y, scaleWidth, scaleHeight, this); //bring back later
                    timeLabel.setForeground(Color.BLACK);
                    chapterLabel.setForeground(Color.black);
                    timeLabel2.setForeground(Color.lightGray);
                    chapterLabel2.setForeground(Color.lightGray);
                    if (startX >= 0 ) { // if there is a box to show
                        g.setColor(Color.yellow);
                        int boxx = y + (int) Math.round(startX * scaleFactor);
                        int boxy = x + (int) Math.round(startY * scaleFactor);
                        int boxw = (int) Math.round(StartW * scaleFactor);
                        int boxh = (int) Math.round(StartH * scaleFactor);
                        //g.drawRect(startX, startY, StartW, StartH);
                        g.drawRect(boxy, boxx, boxh, boxw);
                        timeLabel.setForeground(Color.RED);
                        timeLabel2.setForeground(Color.PINK);
                        chapterLabel.setForeground(Color.RED);
                        chapterLabel2.setForeground(Color.PINK);
                        //System.out.println("draw rect "+boxx+" "+boxy+" "+boxw+" "+boxh);
                        startX = -1 ; // to avoid showing the same box again
                    }
                }
                else{
                    super.paint(g);
                    //System.out.println("Buffer snap shot is null");
                }
            }
            else{ // if no tracking available is  empty
                //System.out.println("painting the snipping waiting circle");
                super.paint(g);
                int iWidth2 = SnippingAjaxLoad.getWidth(this)/2 ;
                int iHeight2 = SnippingAjaxLoad.getHeight(this)/2 ;
                int x = this.getParent().getWidth()/2 - iWidth2;
                int y = this.getParent().getHeight()/2 - iHeight2;
                g.drawImage(SnippingAjaxLoad,x,y,this);

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

    private  static class RectDraw extends JPanel {
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            //System.out.println("rectdraw");
            Dimension dim = getSize();
            int w = dim.width ;
            double position;
            int counter = 0;
            if (numberofFrames!=0 && dim.width!=0 && mlArrayDouble!=null ) {
                position = (double) dim.width / numberofFrames;
                Graphics2D g2 = (Graphics2D) g;
                for (double i = 0, j = 0; i < w; i += position, j++) {
                    //System.out.println("draw small box " + i + " " + position);
                    if (counter < mlArrayDouble.length && mlArrayDouble[counter][0] == j) {
                        g2.setColor(Color.CYAN);
                        g2.draw(new Rectangle2D.Double(i, 1, position, 10));
                        //g2.draw(new Rectangle2D.Double(i, dim.height/2 - 5, position, 10));
                        counter++;
                    } else {
                        g2.setColor(Color.LIGHT_GRAY);
                        g2.draw(new Rectangle2D.Double(i, 1, position, 10));
                        //g2.draw(new Rectangle2D.Double(i, dim.height/2 -5, position, 10));
                    }
                }
            }else { // in the beginning the rectangles are not available
                    g.setColor(Color.DARK_GRAY);
                    g.drawRect(0, 1, dim.width-1 , 10);
                    //g.drawRect(0, dim.height/2 - 5, dim.width-1 , 10);
            }
        }
    }

    public static void main(String[] args) {

    }


    private final class UpdateRunnable2 implements Runnable {
        private final FFmpegFrameGrabber frameGrabber;
        private UpdateRunnable2(FFmpegFrameGrabber frameGrabber) {
            this.frameGrabber = frameGrabber;
        }

        @Override
        public void run() {
            //if (frameGrabber!=null){
                //System.out.println("run slider1 " + duration);
                final long time = frameGrabber.getTimestamp()/1000 ;//milisecond
                //final double temp =  frameGrabber.getTimestamp() / frameGrabber.getLengthInTime() ;
                final double temp =  (double)frameGrabber.getFrameNumber() / (double)frameGrabber.getLengthInFrames() ;
                //System.out.println("temp " + temp + " " +  frameGrabber.getTimestamp() + " " + frameGrabber.getLengthInTime() ) ;
                //System.out.println("temp " + temp + " " +  frameGrabber.getFrameNumber() + " " + frameGrabber.getLengthInFrames() ) ;
                final int position = (int)( temp * 1000); // time/duration
                //System.out.println("time " + time + " pose " + position + " temp " + temp );
                // Updates to user interface components must be executed on the Event
                // Dispatch Thread
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        //System.out.println("invoke later slider2");
                        if(!inPauseMode) { // while playing
                            updateTime(time);
                            updatePosition(position);
                        }
                    }
                });
            //}

        }
    }

    private void updateTime(long millis) {
        String s = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis), TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        timeLabel.setText(s);
        timeLabel2.setText(s);
    }

    private void updatePosition(int value) {
        // positionProgressBar.setValue(value);
        positionSlider.setValue(value);
    }

    public  void exit() {
        if (frameGrabber!=null){
            try {
                frameGrabber.stop();
            } catch (FrameGrabber.Exception e) {
                e.printStackTrace();
            }
            frameGrabber=null;
        }
    }
}
