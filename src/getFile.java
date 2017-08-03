import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.StandardCopyOption.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Elaheh on 2/1/2016.
 */
public class getFile {

    int indexfolder = 0; // index of listOfFolders
    int indexfile = 0 ; // index of listOfFiles
    static Globals globals ;
    public List<String> listOfFolders = new ArrayList<>();
    public List<String> listOfFiles = new ArrayList<>();

    public getFile(Globals globals)    {

        this.globals = new Globals(globals.getWorkingPath());
    }

   public void CreateListOfFolders( String root ) {
        //  public  void GetListOfFolders() {
        this.globals = new Globals(globals.getWorkingPath());
        //List<String> folderName = new ArrayList<String>();
        //String temp = globals.getFacePath();
        File folder = new File(root);
        if (folder.exists()) {
            File[] listOfFiles = folder.listFiles();
            for (int k = 0; k < listOfFiles.length; k++) {
                //folderName.add(k, listOfFiles[k].getName());
                listOfFolders.add(k, listOfFiles[k].getName());
            }
        }
    }

    public String getFolderName(int _index)
    {
        String str ="" ;
        if (_index >=0 && _index < listOfFolders.size())
         str=  listOfFolders.get(_index);
        return str;
    }

    //get all names of all files in folderName
    public void CreateListOfFiles(String root)
    {
        //List<String> fileName = new ArrayList<String>();
        //String temp = globals.getFacePath()+"\\"+ folderName;
        File folder = new File(root);
        if(folder.exists()) {
            File[] lstF = folder.listFiles();

            for (int i = 0; i < lstF.length; i++) {
                if (lstF[i].isFile()) {
                    listOfFiles.add(i, lstF[i].getName());

                } else if (lstF[i].isDirectory()) {
                    System.out.println("Directory " + lstF[i].getName());
                }
            }

        }

    }

    public int getSizeFiles (){
        return listOfFiles.size();
    }

    public String getFileName (int _index){
        if (_index >=0 && _index < listOfFiles.size())
            return listOfFiles.get(_index);
        else return null;
    }

    public String next() // list of folders
    {
        indexfolder++;
        return getFolderName(indexfolder);


    }
    public String prev()
    {
        indexfolder--;
        return getFolderName(indexfolder);
    }

    public String nextfile() // list of folders
    {
        indexfile ++ ;
        return getFileName(indexfile);


    }
    public String prevfile()
    {
        indexfile--;
        return getFileName(indexfile);
    }

    public boolean isEmptyFiles (){
         return listOfFiles.isEmpty();
    }

   /* public boolean checkValidFile(String folderName)
    {
        return GetFileName(folderName).isEmpty();
    }*/

    public void moveFolder() throws IOException {
        for (int i = 0 ; i < indexfolder ; i++)
        {
            Path source = FileSystems.getDefault().getPath(globals.getFacePath() +'\\' + listOfFolders.get(i));
            Path destination = FileSystems.getDefault().getPath(globals.getProcessedPath()+'\\' + listOfFolders.get(i));
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);


        }
    }

}
