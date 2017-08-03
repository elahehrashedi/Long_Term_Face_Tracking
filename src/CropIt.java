
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.Robot;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

public class CropIt
{

    static Globals globals ;
    public CropIt(String folderName, String imageName, Globals globals)
    {

        this.globals = new Globals(globals.getWorkingPath());
        final ImageEasel imageEasel = new ImageEasel(folderName,imageName,globals);

        Icon resetIcon = new ImageIcon(getClass().getResource("/icons/reset.png"));
        JButton reset = new JButton(resetIcon);
        reset.setToolTipText("reset");
        reset.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                imageEasel.showCrop = false;
                imageEasel.repaint();
                String path = globals.getCropPath() + "\\" + folderName + "\\" + imageName ;
                File file = new File(path);
                if(file.delete())
                {
                    System.out.println(file.getName() + " is deleted!");
                }
                else
                {
                    System.out.println("Delete operation is failed.");
                }
            }
        });

        JPanel north = new JPanel();
        north.add(reset);

        String fileName = globals.getFacePath() + "\\" +folderName +"\\"+ imageName;



        BufferedImage img;
        int h=400, w=400;
        try
        {

            img= ImageIO.read(new File (fileName));
            h= img.getHeight()+85 ;
            w = img.getWidth()+30;
        }
        catch(MalformedURLException mue)
        {
            System.err.println("url: " + mue.getMessage());
        }
        catch(IOException ioe)
        {
            System.err.println("read: " + ioe.getMessage());
        }


        JDialog f = new JDialog();
        f.setModal(true);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.getContentPane().add(north, "North");
        f.add(new JScrollPane(imageEasel));
        f.setSize(w,h);

        f.setLocationRelativeTo(null);
        f.setTitle("Selection Pan");
        f.setIconImage(new ImageIcon(getClass().getResource("/icons/wsu.png")).getImage());
        f.setVisible(true);
    }

}

class ImageEasel extends JPanel
{
    BufferedImage image;
    Rectangle clip;
    boolean showCrop;
    Area mask;
    ImageCropper cropper;
    Robot robot;
    static Globals globals;

    public ImageEasel(String folderName, String imageName, Globals globals)
    {
        this.globals = new Globals(globals.getWorkingPath());
        loadImage(folderName, imageName);
        clip = new Rectangle();
        showCrop = false;
        cropper = new ImageCropper(this,folderName,imageName);
        addMouseListener(cropper);
        addMouseMotionListener(cropper);
    }

    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        //scaleFactor = getScaleFactorToFit(new Dimension(tempw,temph ), getSize());
        int x = (w - imageWidth)/2;
        int y = (h - imageHeight)/2;
        g2.drawImage(image, x, y, this);
        if(cropper.dragging)
        {
            g2.setPaint(Color.red);
            g2.draw(clip);
        }
        if(showCrop)
        {
            g2.setPaint(getBackground());
            g2.fill(mask);
        }


    }

    public void setClip(Point p1, Point p2)
    {
        clip.setFrameFromDiagonal(p1, p2);
        repaint();
    }

    public void cropImage(int x, int y, int height, int width,String folderName, String imageName)
    {
        showCrop = true;
        mask = new Area(new Rectangle(0, 0, getWidth(), getHeight()));
        Area clipArea = new Area(clip);

        mask.subtract(clipArea);

        File dir = new File(globals.getCropPath() + "\\" + folderName);
        dir.mkdir();

        BufferedImage SubImgage = image.getSubimage(x, y, width, height);
        File outputfile = new File( dir + "\\" + imageName);
        try {
            ImageIO.write(SubImgage, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        repaint();

    }

    public Dimension getPreferredSize()
    {
        return new Dimension(image.getWidth(), image.getHeight());
    }

    private void loadImage(String folderName, String imageName)
    {
        String fileName = globals.getFacePath()+ "\\" + folderName +"\\"+ imageName ;
        try
        {

            image = ImageIO.read(new File (fileName));
        }
        catch(MalformedURLException mue)
        {
            System.err.println("url: " + mue.getMessage());
        }
        catch(IOException ioe)
        {
            System.err.println("read: " + ioe.getMessage());
        }
    }
}

class ImageCropper extends MouseInputAdapter
{
    ImageEasel imageEasel;
    Point start;
    boolean dragging;
    Point end;
    String folder;
    String picName;

    public ImageCropper(ImageEasel ie, String folderName, String imageName)
    {
        imageEasel = ie;
        dragging = false;
        folder = folderName;
        picName = imageName;

    }

    public void mousePressed(MouseEvent e)
    {
        start = e.getPoint();
        dragging = true;
    }

    public void mouseReleased(MouseEvent e)
    {
        dragging = false;

        int w = imageEasel.getWidth();
        int h = imageEasel.getHeight();
        int imageWidth = imageEasel.image.getWidth();
        int imageHeight = imageEasel.image.getHeight();
        int x = (w - imageWidth)/2;
        int y = (h - imageHeight)/2;

        imageEasel.cropImage(start.x-x,start.y-y, end.y-start.y,end.x-start.x ,folder, picName);


    }

    public void mouseDragged(MouseEvent e)
    {
        imageEasel.setClip(start, e.getPoint());
        end = e.getPoint();


    }
}