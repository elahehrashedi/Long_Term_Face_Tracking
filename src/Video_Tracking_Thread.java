import VideoTrackingLib22.VideoTrackClass;
import com.mathworks.toolbox.javabuilder.MWCellArray;
import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;


public class Video_Tracking_Thread   implements Runnable {

        double int_result = 0 ; // zero not successful
        Globals globals;
        FileofPositions fileofPositions;
        //TrackerClass trackerClass;
        VideoTrackClass trackerClass2 ;
        boolean windows_open = true ;
        String vide_name;
        double start_time ;// start tracking time
        double x1_trackingbox , y1_trackingbox , x2_trackingbox , y2_trackingbox ;
        double tracking_frames, similarity_threshold , tp_tracking , tp_not_verified_face , tp_not_face ;


        private java.util.List<TaskListener> listeners = Collections.synchronizedList( new ArrayList<TaskListener>() );
        /**
        * Adds a listener to this object.
        * @param listener Adds a new listener to this object.
        */
        public void addListener( TaskListener listener ){
            listeners.add(listener);
        }
        /**
         * Removes a particular listener from this object, or does nothing if the listener
         * is not registered.
         * @param listener The listener to remove.
         */
        public void removeListener( TaskListener listener ){
            listeners.remove(listener);
        }
        /**
         * Notifies all listeners that the thread has completed.
         */
        private final void notifyListeners() {
            synchronized ( listeners ){
                for (TaskListener listener : listeners) {
                    listener.threadComplete(this);
                }
            }
        }

        public Video_Tracking_Thread (){

        }
        public void Call_Video_Tracking_Thread(Globals globals , String video_name , double start_time , double x1 , double y1 , double x2 , double y2 ,
                                     double tracking_frames, double similarity_threshold , double tp_tracking ,
                                     double tp_not_verified_face , double tp_not_face ){

            this.globals = new Globals(globals.getWorkingPath());
            fileofPositions = new FileofPositions(globals);
            this.vide_name = video_name ;
            x1_trackingbox = x1;
            y1_trackingbox = y1 ;
            x2_trackingbox = x2 ;
            y2_trackingbox = y2 ;
            this.start_time = start_time ;
            this.tracking_frames = tracking_frames; this.similarity_threshold= similarity_threshold ; this.tp_tracking = tp_tracking;
            this.tp_not_verified_face = tp_not_verified_face ; this.tp_not_face = tp_not_face;
            int_result = 0 ; // zero not successful
            try {
                //trackerClass = new TrackerClass();
                trackerClass2 = new VideoTrackClass();

            } catch (MWException e) {
                e.printStackTrace();
            }
        }

    public void stop (){
        windows_open = false; // close the thread
    }
    public void run( ) {

        try {
            try {
                int_result = 0;
                //System.out.println("start calling matlab tracking function x1=" + x1_trackingbox + " y1=" + y1_trackingbox + " x2=" + x2_trackingbox + " y2=" + y2_trackingbox);
                //trackerClass.Tracker(video_path, face_path, (double) line[2], (double) line[3], (double) line[4], (double) line[5], (double) line[6], (double) 0);
                //videoName, start_time, px1, py1, px2, py2,...
                //tracking_frames, similarity_threshold ,tp_tracking,...
                //tp_not_verified_face, tp_not_face
                Object[] result = trackerClass2.VTWVerification((int) 1, vide_name, (double) start_time,
                        (double) y1_trackingbox, (double) x1_trackingbox, (double) y2_trackingbox, (double) x2_trackingbox,
                        (double) tracking_frames, (double) similarity_threshold, (double) tp_tracking,
                        (double) tp_not_verified_face, (double) tp_not_face, globals.getFaceretrievalpath());
                //, globals.getConvnetpath() ,
                //, globals.getSvmpath() , globals.getPrjpath());
                int_result = ((MWNumericArray) result[0]).getDouble(1);
                ;
                System.out.println("tracking is done " + int_result);
            } catch (MWException e) {
                e.printStackTrace();
            }                 //this comment should be delete
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }finally {
            notifyListeners();
        }
    }

    public int Is_successful (){
        return (int) int_result; // zero not successful
    }
    public static void main(String[] args) throws InterruptedException {
        /*Globals globals = new Globals(new String("C:\\Users\\Elaheh\\Dropbox\\05_winter2016\\ver1"));
        TrackingThread test = new TrackingThread(globals );
        (new Thread(test)).start(); // start test as a thread
        Thread.sleep(200);
        test.stop();*/
    }

}
