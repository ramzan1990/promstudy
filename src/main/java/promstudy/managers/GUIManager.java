package promstudy.managers;

import promstudy.common.MDouble;
import promstudy.main.PState;
import promstudy.ui.GUI;
import promstudy.ui.PSFrame;
import promstudy.visualization.AccuracyHistogram;
import promstudy.visualization.DataComponent;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class GUIManager {
    private GUI mainWindow;
    private String selectionClassName;
    private ArrayList<DataComponent> componentList;
    private Object selectedComponent;
    public IOManager io;
    private Predictor p;
    private PState s;


    public GUIManager(IOManager io, Predictor p, PState s) {
        this.io = io;
        this.p = p;
        this.s = s;
        componentList = new ArrayList<DataComponent>();
    }

    public void show() {
        mainWindow = new GUI(this);
        mainWindow.setTitle("PromStudy");
        mainWindow.setVisible(true);
    }

    public void wrongInput() {
        JOptionPane.showMessageDialog(null, "Wrong Input! Try again.", "Error", JOptionPane.ERROR_MESSAGE);
    }


    public void reset() {
        componentList = new ArrayList<DataComponent>();
        mainWindow.disposeAllIFrames();
        mainWindow.consoleArea.setText("");
    }


    public void AccuracyHistogram(String title, int index, MDouble decisionThreshold) {
        //AccuracyHistogram ahc = new AccuracyHistogram(decisionThreshold);
        //ahc.setType("accuracyHistogram");
        //componentList.add(ahc);
        // mainWindow.createFrame("Accuracy Histogram (" + title + ")" + index, ahc);
    }

    public void setBins(int n) {
        AccuracyHistogram.setBins(n);
        for (int i = 0; i < componentList.size(); i++) {
            if (componentList.get(i).getType().contains("accuracyHistogram")) {
                AccuracyHistogram ahc = (AccuracyHistogram) componentList.get(i);
                ahc.refresh();
            }
        }
    }

    public void setBins() {
        try {
            setBins(Integer.parseInt(JOptionPane.showInputDialog("Number:")));
        } catch (Exception e) {
        }
    }

    public void help() {
        try {
            File pdfFile = new File("help.pdf");
            if (pdfFile.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(pdfFile);
            } else {
                JOptionPane.showMessageDialog(null, "See file help.pdf for detailed help.");
            }

        } catch (Exception ex) {
        }
    }

    public void pointSizeDec() {
        if (selectedComponent != null) {
            if (DataComponent.class.isAssignableFrom(selectedComponent.getClass())) {
                ((DataComponent) selectedComponent).dotSizeDec();
                ((DataComponent) selectedComponent).repaint();
            }
        }
    }

    public void pointSizeInc() {
        if (selectedComponent != null) {
            if (DataComponent.class.isAssignableFrom(selectedComponent.getClass())) {
                ((DataComponent) selectedComponent).dotSizeInc();
                ((DataComponent) selectedComponent).repaint();
            }
        }
    }

    public void zoomOut() {
        if (selectedComponent != null) {
            if (DataComponent.class.isAssignableFrom(selectedComponent.getClass())) {
                ((DataComponent) selectedComponent).scaleDecrease();
                ((DataComponent) selectedComponent).repaint();
            }
        }
    }

    public void zoomIn() {
        if (selectedComponent != null) {
            if (DataComponent.class.isAssignableFrom(selectedComponent.getClass())) {
                ((DataComponent) selectedComponent).scaleIncrease();
                ((DataComponent) selectedComponent).repaint();
            }
        }
    }


    public void setBarHistogram(boolean b) {
        AccuracyHistogram.setShape(b);
    }

    public void removeFromComponentList(DataComponent c) {
        componentList.remove(c);
    }

    public void setSelectedComponent(Object c) {
        selectedComponent = c;
    }

    public void writeToConsole(String text) {
        mainWindow.writeToConsole(text);
        mainWindow.writeToConsole("\n");
    }

    public void setROCcurve(boolean b) {
        AccuracyHistogram.setROC(b);
    }


    public void updateAccuracyHistograms() {
        for (int i = 0; i < componentList.size(); i++) {
            if (componentList.get(i).getType().equals("accuracyHistogram")) {
                AccuracyHistogram ac = (AccuracyHistogram) componentList.get(i);
                ac.updateSpecificity();
                ac.repaint();
            }
        }
    }

    public void dataSummary() {
        writeToConsole("\n");
    }


    public void takeSnapshot() {
        if (DataComponent.class.isAssignableFrom(selectedComponent.getClass())) {
            io.takeSnapshot((JComponent) selectedComponent);
        }
    }

    public void predict() {
        float[] r = p.predict(s.positive);
        writeToConsole("Promoters");
        for (int i = 0; i < r.length; i++) {
            writeToConsole("Score -- " + r[i]);
        }
        r = p.predict(s.negative);
        writeToConsole("Non-promoters");
        for (int i = 0; i < r.length; i++) {
            writeToConsole("Score -- " + r[i]);
        }
    }
}
