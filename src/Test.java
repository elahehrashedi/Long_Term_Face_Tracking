
import java.awt.*;
import java.io.IOException;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.swing.*;
//import org.opencv.core.Core;


public class Test{
    //static {System.loadLibrary(Core.NATIVE_LIBRARY_NAME);}
    public static void main(String []args) throws IOException, Exception
    {
        // display the showOptionDialog
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
            System.out.println("yes");
        }
        else {
            System.out.println("no");
        }

    }
}