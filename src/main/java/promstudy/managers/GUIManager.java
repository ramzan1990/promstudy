package promstudy.managers;

import promstudy.common.ClassAndValue;
import promstudy.main.PState;
import promstudy.ui.GUI;
import promstudy.visualization.AccuracyHistogram;
import promstudy.visualization.DataComponent;
import promstudy.visualization.Trend;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class GUIManager {
    private GUI mainWindow;
    private String selectionClassName;
    private ArrayList<DataComponent> componentList;
    private int aIndex, tIndex;
    private Object selectedComponent;
    public IOManager io;
    private Predictor p;
    private PState s;


    public GUIManager(IOManager io, Predictor p, PState s) {
        this.io = io;
        this.p = p;
        this.s = s;
        aIndex = 1;
        tIndex = 1;
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


    public void accuracyHistogram() {
        ClassAndValue[] cav = new ClassAndValue[s.positive.length + s.negative.length];
        int i =0;
        float[] r = p.predict(s.positive);
        for(float f:r){
            cav[i++] = new ClassAndValue(1, f);
        }
        r = p.predict(s.negative);
        for(float f:r){
            cav[i++] = new ClassAndValue(0, f);
        }
        AccuracyHistogram ahc = new AccuracyHistogram(cav, s.decisionThreshold, this);
        componentList.add(ahc);
        mainWindow.createFrame("Accuracy Histogram " + aIndex++, ahc);
    }

    public void chooseBins(int n) {
        AccuracyHistogram.setBins(n);
        for (int i = 0; i < componentList.size(); i++) {
            if (componentList.get(i).getType().contains("accuracyHistogram")) {
                AccuracyHistogram ahc = (AccuracyHistogram) componentList.get(i);
                ahc.refresh();
            }
        }
    }

    public void chooseBins() {
        try {
            chooseBins(Integer.parseInt(JOptionPane.showInputDialog("Number:")));
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

    public void analyse() {
        String inp = JOptionPane.showInputDialog("Select range from 1 to " + s.sequences.length+" (start, end): ");
        int start = Integer.parseInt(inp.split(",")[0]) - 1;
        int end = Integer.parseInt(inp.split(",")[1]);
        ArrayList<ArrayList<Double>> arrays = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();
        for(int i = start; i<end; i++){
            ArrayList<Double> list = new ArrayList<>();
            names.add(""+(i+1));
            float[][] seq = s.sequences[i];
            int total = (int) Math.floor((seq.length - s.sLen)/s.step);
            float[][][] toPredict = new float[total][s.sLen][4];
            for(int j=0; j<toPredict.length; j++){
                toPredict[j] = Arrays.copyOfRange(seq, j*s.step, j*s.step + s.sLen);
            }
            float[] r = p.predict(toPredict);
            for(float f: r){
                list.add(new Double(f));
            }
            arrays.add(list);
        }
        Trend t = new Trend(arrays, names);
        componentList.add(t);
        mainWindow.createFrame("Analysis ("+start + ", "+ end + ") " + tIndex++, t);
    }

    public void chooseStep() {
        try{
            s.step = Integer.parseInt(JOptionPane.showInputDialog("Choose window step size: "));
        }catch (Exception e){

        }
    }

    public void setTheme(boolean white) {
        if(white) {
            DataComponent.setWhiteTheme();
        }else{
            DataComponent.setBlackTheme();
        }
        for (Component c:componentList) {
            c.repaint();
        }
    }

    public void ROCCurve(boolean roc) {
        AccuracyHistogram.setROC(roc);
        for (DataComponent c:componentList) {
            if (c.getType().equals("accuracyHistogram")) {
                c.repaint();
            }
        }
    }

}
