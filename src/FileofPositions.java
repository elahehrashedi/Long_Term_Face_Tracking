import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.*;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Elaheh on 1/28/2016.
 */
public class FileofPositions {

    boolean isOLD ;
    String positionsFileUrl ;
    static Globals globals ;
    private final Lock lock = new ReentrantLock();
    LinkedList<int[]> waitinglist;
    int MAX_BUFFER_SIZE = 100;

    public FileofPositions(Globals globals) {
        this.globals = new Globals(globals.getWorkingPath());
        positionsFileUrl = globals.getInputFiles()+"\\videopositions.txt";
        waitinglist = new LinkedList<>();

    }

    protected void finalize( ) throws Throwable {
        appendAll();
        super.finalize();
    }

    public void appendTo (int fileNum , int faceNum, int startx , int starty, int width, int height, int frameNum){

        int[] outline = new int [] {fileNum, faceNum,startx,starty, width, height, frameNum};
        waitinglist.add (outline);
        if (waitinglist.size()==MAX_BUFFER_SIZE)
            appendAll();
    }

    public void appendAll (){
        lock.lock();
        PrintWriter out = null;
        try{
            lock.lock();
            out = new PrintWriter(new BufferedWriter(new FileWriter(positionsFileUrl, true)));
            for (int i=0; i< waitinglist.size();i++) {
                out.println(waitinglist.get(i)[0] + " " + waitinglist.get(i)[1] + " " + waitinglist.get(i)[2] + " " + waitinglist.get(i)[3] + " " + waitinglist.get(i)[4] + " " + waitinglist.get(i)[5] + " " + waitinglist.get(i)[6]);
            }
            waitinglist.clear();
        }catch (IOException e) {
            e.printStackTrace();
        }finally{
            lock.unlock();
            if(out != null) {
                out.close();
            }
        }
    }

    public int [] deleteLine (){
        int[] outline = new int [7];
        byte [] line = new byte [100] ;
        byte[] temp ;
        int lenghtofline = 0;
        RandomAccessFile f = null;
        try {
            lock.lock();
            f = new RandomAccessFile(positionsFileUrl, "rw");

            long length = f.length() - 1;
            if (length <= 0) return null;
            byte b = 10 ;
            do {
                length -= 1;
                f.seek(length);
                b = f.readByte();
                line [lenghtofline]= b  ;
                lenghtofline ++;
            } while(b != 10 && length > 0);
            if (length == 0) {
                f.setLength(length);
                f.close();
            } else {
                f.setLength(length + 1);
                f.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
        temp = new byte[lenghtofline];
        for (int i=0; i<lenghtofline; i++)
            temp[i] = line [lenghtofline-i-1];

        try {
            String string = new String(temp,"UTF-8");
            string = string.replace("\n","");
            string = string.replace("\r","");
            String[] parts = string.split(" ");
            for (int i=0; i<7 ; i++)
                outline[i]= Integer.parseInt(parts[i]);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return outline;
    }



    public static void main(String[] args) {

        Globals globals = new Globals("C:\\Users\\Elaheh\\Dropbox\\05_winter2016\\ver1");
        FileofPositions fileofPositions = new FileofPositions(globals);
        FileofPositions fileofPositions2 = new FileofPositions(globals);

        int[] outline = new int [7];
        outline = fileofPositions2.deleteLine();
        fileofPositions.appendTo(1,10,12,200,45,100,3987);
        fileofPositions.appendTo(2,10,12,200,45,100,3987);
        fileofPositions.appendTo(3,10,12,200,45,100,3987);

        outline = fileofPositions2.deleteLine();
        outline = fileofPositions2.deleteLine();

        fileofPositions.appendTo(4,10,12,200,45,100,3987);
        fileofPositions.appendAll();

        fileofPositions2.deleteLine();
        fileofPositions2.deleteLine();
        fileofPositions2.deleteLine();
        fileofPositions.appendTo(5,10,12,200,45,100,3987);
        fileofPositions.appendTo(6,10,12,200,45,100,3987);
        fileofPositions.appendTo(7,10,12,200,45,100,3987);
        fileofPositions.appendAll();

        fileofPositions2.deleteLine();
        fileofPositions2.deleteLine();
        fileofPositions2.deleteLine();



    }
}
