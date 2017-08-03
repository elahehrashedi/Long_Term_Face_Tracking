import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.io.File;
import java.io.IOException;


/**
 * @version 1.0 1/20/2016
 */
public class ShowImages extends JPanel{

    private  Map<String, ImageIcon> imageMap;
    static  int indexOfFolders = 1;
    int indexOfItem;
    //List<String> strs;
    DefaultListModel model;
    String imgName;
    static Globals globals;

    //List<String> faceFolders = new ArrayList<>();
    getFile faceDataFiles;

    public ShowImages(Globals globals) {

        this.globals = new Globals(globals.getWorkingPath());

        faceDataFiles = new getFile(globals);
        faceDataFiles.indexfolder = 0;
        faceDataFiles.CreateListOfFolders(globals.getFacePath());
        faceDataFiles.CreateListOfFiles(new String(globals.getFacePath()+"\\"+faceDataFiles.getFolderName(faceDataFiles.indexfolder)));

        model = new DefaultListModel();

        for (int i = 0; i < faceDataFiles.getSizeFiles(); i++) {
            model.addElement(faceDataFiles.getFileName(i));
        }

        imageMap = createImageMap();
        JList list = new JList(model);

        JPanel PositionPanel = new JPanel(new GridLayout(1, 1));

        JPanel ListPanel = new JPanel();


        list.setCellRenderer(new ListRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setBorder(new EmptyBorder(0,4, 0, 0));


        list.setLayoutOrientation(JList.HORIZONTAL_WRAP);

        list.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                int index = list.locationToIndex(e.getPoint());
                indexOfItem = index+1;
                JList list = (JList)e.getSource();
                if (e.getClickCount() == 2) {

                    if(indexOfItem != 0) {
                        new CropIt(faceDataFiles.getFolderName(faceDataFiles.indexfolder), list.getSelectedValue().toString(),globals);
                    }
                    else
                        JOptionPane.showMessageDialog(null, "Please select an image to show");
                }


            }
        });

        JScrollPane sp = new JScrollPane(list);
        sp.setHorizontalScrollBarPolicy(sp.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(sp.VERTICAL_SCROLLBAR_ALWAYS);
        sp.setPreferredSize(new Dimension(1000, 400));

        ListPanel.add(sp);


        Icon showIcon = new ImageIcon(getClass().getResource("/icons/show.png"));
        JButton showButton = new JButton(showIcon);
        showButton.setToolTipText("Show Image");
        showButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                if(indexOfItem != 0)
                    new CropIt(faceDataFiles.getFolderName(faceDataFiles.indexfolder), list.getSelectedValue().toString(),globals);
                else
                    JOptionPane.showMessageDialog(null, "Please select an image to show");
            }
        });

        Icon previousIcon = new ImageIcon(getClass().getResource("/icons/previous.png"));
        JButton previousButton = new JButton(previousIcon);
        previousButton.setToolTipText("Go to previous list");
        previousButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {


                String datafiles = faceDataFiles.prev();
                if (!datafiles.isEmpty())
                        //faceDataFiles.GetFileName(datafiles);
                        faceDataFiles.CreateListOfFiles(new String(globals.getFacePath()+"\\"+datafiles));
                else
                    JOptionPane.showMessageDialog(null, "There is no more file to show !!!");

                if(!faceDataFiles.isEmptyFiles()) {
                    model.clear();

                    for (int i = 0; i < faceDataFiles.getSizeFiles(); i++) {
                        //model.addElement(strs.get(i));
                        model.addElement(faceDataFiles.getFileName(i));
                    }

                    imageMap = createImageMap();
                    list.repaint();
                }
                else
                {
                    JOptionPane.showMessageDialog(null, "There is no more file to show !!!");
                }

            }
        });

        Icon nextIcon = new ImageIcon(getClass().getResource("/icons/next.png"));
        JButton nextButton = new JButton(nextIcon);
        nextButton.setToolTipText("Go to next list");
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {


                String datafiles = faceDataFiles.next();

                if (!datafiles.isEmpty())
                   // strs = faceDataFiles.GetFileName(datafiles);
                    faceDataFiles.CreateListOfFiles(new String(globals.getFacePath()+"\\"+datafiles));

                else
                    JOptionPane.showMessageDialog(null, "There is no more file to show !!!");
                if(!faceDataFiles.isEmptyFiles()) {
                    model.clear();

                    for (int i = 0; i < faceDataFiles.getSizeFiles(); i++) {
                        model.addElement(faceDataFiles.getFileName(i));
                    }

                    imageMap = createImageMap();
                    list.repaint();
                }
                else
                {
                    JOptionPane.showMessageDialog(null, "There is no more file to show !!!");
                }
            }
        });

        JPanel panel = new JPanel();

        panel.setLayout(new FlowLayout());
        panel.add(showButton,BorderLayout.WEST);
        panel.add(previousButton,BorderLayout.CENTER);
        panel.add(nextButton,BorderLayout.EAST);


        setBorder(new EmptyBorder(4, 4, 4, 4));
        setLayout(new BorderLayout());

        add(panel,BorderLayout.NORTH);
        PositionPanel.add(ListPanel,BorderLayout.SOUTH);
        add(PositionPanel);

    }

    public void moveFile() throws IOException {
        faceDataFiles.moveFolder();
    }


    private Map<String, ImageIcon> createImageMap() {
        Map<String, ImageIcon> map = new HashMap<>();

        try {
            for(String s: faceDataFiles.listOfFiles) {

                String temp = globals.getFacePath()+"\\"+faceDataFiles.getFolderName(faceDataFiles.indexfolder) +"\\"+s ; // fceData path
                ImageIcon icon = new ImageIcon(temp);
                Image img = icon.getImage();
                map.put(s, new ImageIcon(img.getScaledInstance(150, 150,  Image.SCALE_SMOOTH)));

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return map;
    }

    public class ListRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(
                JList list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {

            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            label.setIcon(imageMap.get((String) value));
            label.setForeground(Color.white);

            label.setVerticalTextPosition(JLabel.BOTTOM);

            return label;

        }
    }

}


