package promstudy.managers;

import promstudy.common.FastaParser;
import promstudy.main.PState;
import promstudy.main.PromStudy;
import promstudy.visualization.DataComponent;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;


public class IOManager {
    private File fcDir;
    PState s;

    public IOManager(PState s) {
        this.s = s;
        fcDir = new File(System.getProperty("user.dir"));
    }


    public float[][][] readData() {
        try {
            FileDialog fd = new FileDialog((Frame) null, "Open file for classification", FileDialog.LOAD);
            fd.setVisible(true);
            if (fd.getFiles().length > 0) {
                return FastaParser.parse(fd.getFiles()[0]);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Cannot read the file!", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    public void loadData() {
        try {
            FileDialog fd = new FileDialog((Frame) null, "Open file with Promoters", FileDialog.LOAD);
            fd.setVisible(true);
            if (fd.getFiles().length > 0) {
                s.positive = FastaParser.parse(fd.getFiles()[0]);
            }
            fd = new FileDialog((Frame) null, "Open file with Non-promoters", FileDialog.LOAD);
            fd.setVisible(true);
            if (fd.getFiles().length > 0) {
                s.negative = FastaParser.parse(fd.getFiles()[0]);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Cannot read the file!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadLData() {
        try {
            FileDialog fd = new FileDialog((Frame) null, "Open file", FileDialog.LOAD);
            fd.setVisible(true);
            if (fd.getFiles().length > 0) {
                s.sequences = FastaParser.parse(fd.getFiles()[0]);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Cannot read the file!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void takeSnapshot(JComponent selectedComponent) {
        try {
            FileDialog fd = new FileDialog((Frame) null, "Save Snapshot", FileDialog.SAVE);
            fd.setVisible(true);
            fd.setFilenameFilter(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    if (name.toLowerCase().endsWith(".png")) {

                        return true;

                    }

                    return false;
                }
            });
            if (fd.getFiles().length > 0) {
                File f = fd.getFiles()[0];
                String name = f.getAbsolutePath();
                if (!name.toLowerCase().endsWith(".png")) {
                    name = name + ".png";
                }
                File o = new File(name);
                saveComponent(selectedComponent, "png", o);
            }

        } catch (Exception e) {
        }
    }

    private void saveComponent(Object c, String format, File outputfile) throws IOException {
        BufferedImage myImage = null;
        if (DataComponent.class.isAssignableFrom(c.getClass())) {
            JComponent jc = (JComponent) c;
            jc.repaint();
            myImage = new BufferedImage(jc.getWidth(), jc.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = myImage.createGraphics();
            jc.paint(g);
            try {
                ImageIO.write(myImage, format, outputfile);
            } catch (Exception e) {
            }
        }
    }


    public String loadModel() {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);

            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                return chooser.getSelectedFile().toString();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Cannot read the file!", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    public void saveCSV(String s) {
        FileDialog fd = new FileDialog((Frame) null, "Save", FileDialog.SAVE);
        fd.setVisible(true);
        if (fd.getFiles().length > 0) {
            File f = fd.getFiles()[0];
            try {
                Files.write(Paths.get(f.getAbsolutePath()), s.getBytes(), StandardOpenOption.CREATE);
            }catch(Exception e){

            }
        }
    }
}
