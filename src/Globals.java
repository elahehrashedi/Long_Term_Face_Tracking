/**
 * Created by Elaheh on 1/31/2016.
 */

import com.jmatio.io.*;
import com.jmatio.types.*;
import uk.co.caprica.vlcj.binding.LibC;

import java.io.File;
import java.io.IOException;

public class Globals {
    private String WORKING_PATH;
    private String MEDIA_PATH ;
    private String FACE_PATH;
    private String CROP_RESULT;
    private String INPUT_FILES;
    private String VLC_LIB;
    private String JNA_LIB ;
    private String PROCESSED;
    private String convnetpath;
    private String faceretrievalpath;
    private String svmpath;
    private String prjpath;


    public Globals (String working_path){
        WORKING_PATH = working_path ;
        MEDIA_PATH = working_path+"\\media";
        FACE_PATH = working_path + "\\faceData";
        CROP_RESULT = working_path + "\\results";
        INPUT_FILES = working_path + "\\inputFiles";
        PROCESSED = working_path + "\\processed";
        VLC_LIB = "C:\\Program Files\\VideoLAN\\VLC" ;
        JNA_LIB = "C:\\Program Files\\VideoLAN\\VLC" ;
        // on mac
        // /Applications/VLC.app/Contents/MacOS/lib
        convnetpath = "C:\\Demo\\matconvnet-1.0-beta18\\matconvnet-1.0-beta18" ;
        faceretrievalpath = "C:\\Demo" ;
        svmpath = "C:\\Demo\\libsvm-master";
        prjpath = "C:\\Demo\\matconvnet-1.0-beta18\\matconvnet-1.0-beta18\\Prj_Demo_Ver.1";
        /*try {
            String programFolder = new File(AudioCataloger.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getAbsolutePath()+"/";
            LibC.INSTANCE.setenv("VLC_PLUGIN_PATH", programFolder + "bin/VLC.app/Contents/MacOS/plugins", 1);
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }*/
    }

    public Globals (Globals globals){
        this.WORKING_PATH = globals.WORKING_PATH ;
        this.MEDIA_PATH = globals.MEDIA_PATH;
        this.FACE_PATH = globals.FACE_PATH;
        this.CROP_RESULT = globals.CROP_RESULT;
        this.INPUT_FILES = globals.INPUT_FILES;
        this.VLC_LIB = globals.VLC_LIB;
        this.JNA_LIB = globals.JNA_LIB;
        this.PROCESSED = globals.PROCESSED;
        this.convnetpath = globals.convnetpath;
        this.faceretrievalpath = globals.faceretrievalpath;
        this.svmpath = globals.svmpath;
        this.prjpath = globals.prjpath;
    }

    public String getWorkingPath (){
        return WORKING_PATH;
    }
    public String getMediaPath () {
        return MEDIA_PATH;
    }
    public String getFacePath (){
        return FACE_PATH;
    }
    public String getCropPath (){
        return CROP_RESULT;
    }
    public String getInputFiles () { return INPUT_FILES; }
    public String getVlcLib (){ return VLC_LIB;}
    public String getJnaLib(){
        return JNA_LIB;
    }
    public String getProcessedPath(){ return PROCESSED; }
    public String getConvnetpath(){
        return convnetpath;
    }
    public String getFaceretrievalpath(){
        return faceretrievalpath ;
    }
    public String getSvmpath(){
        return svmpath;
    }
    public String getPrjpath(){
        return prjpath;
    }

    public static void main(String[] args) {
        String w = "C:\\Users\\Elaheh\\Dropbox\\05_winter2016\\ver1";
        Globals g = new Globals(w);
        System.out.print(g.getWorkingPath());

    }

}
