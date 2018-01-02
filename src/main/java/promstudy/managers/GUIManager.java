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
    private boolean totalOption;


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

    public void testData() {
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
            int total = (int) Math.ceil((seq.length - s.sLen)/s.step) + 1;
            float[][][] toPredict = new float[total][s.sLen][4];
            for(int j=0; j<toPredict.length; j++){
                toPredict[j] = Arrays.copyOfRange(seq, j*s.step, j*s.step + s.sLen);
            }
            float[] r = p.predict(toPredict);
            if(!totalOption) {
                for (float f : r) {
                    list.add(new Double(f));
                }
                arrays.add(list);
            }else{
                if(arrays.isEmpty()){
                    for (float f : r) {
                        list.add(new Double(f));
                    }
                    arrays.add(list);
                }else{
                    for(int k=0;k<r.length;k++){
                        arrays.get(0).set(k, arrays.get(0).get(k)+r[k]);
                    }
                }
            }
        }
        Trend trend = new Trend(arrays, names, s.step);
        componentList.add(trend);
        mainWindow.createFrame("Analysis ("+start + ", "+ end + ") " + tIndex++, trend);

        for(int ari=0; ari<arrays.size(); ari++){
            writeToConsole("Array "+ari);
            ArrayList<Double> ar = arrays.get(ari);
            int K = 2;
            int T = ar.size();
            int N = 3;
            double[][] A = new  double[K][K];
            A[0][0]= 0.95;
            A[0][1]= 0.05;
            A[1][1]= 0.01;
            A[1][0]= 0.99;
            double[][] B = new  double[K][N];
            B[0][0]= 0.70;
            B[0][1]= 0.19;
            B[0][2]= 0.01;
            B[1][0]= 0.01;
            B[1][1]= 0.09;
            B[1][2]= 0.90;
            double[] p = new double[2];
            p[0]=0.99;
            p[1]= 0.01;
            int[] observations = new int[ar.size()];
            for(int i = 0; i<ar.size();i++){
                if(ar.get(i)>0.7){
                    observations[i] = 2;
                }else  if(ar.get(i)>0.3){
                    observations[i] = 1;
                }else  {
                    observations[i] = 0;
                }
            }
            //Baum-Welch
            int iterations = 5;
            for(int it = 0; it<iterations; it++) {
                //Viterbi
                double[][] T1 = new double[K][T];
                int[][] T2 = new int[K][T];

                for (int i = 0; i < K; i++) {
                    T1[i][0] = p[i] * B[i][observations[1]];
                }
                for (int i = 1; i < observations.length; i++) {
                    for (int j = 0; j < K; j++) {
                        double k1 = T1[0][i - 1] * A[0][j] * B[j][observations[i]];
                        double k2 = T1[1][i - 1] * A[1][j] * B[j][observations[i]];
                        T1[j][i] = Math.max(k1, k2);
                        if (k2 > k1) {
                            T2[j][i] = 1;
                        }
                    }
                }
                int[] z = new int[T];
                if (T1[1][T - 1] > T1[0][T - 1]) {
                    z[T - 1] = 1;
                }
                int[] x = new int[T];
                x[T - 1] = z[T - 1];
                for (int i = T - 1; i > 0; i--) {
                    z[i - 1] = T2[z[i]][i];
                    x[i - 1] = z[i - 1];
                }
                String st = "";
                for (int i = 0; i < x.length; i++) {
                    if (x[i] == 0) {
                        st += "-";
                    } else {
                        st += "+";
                    }
                }
                writeToConsole(st);

                double[][] a = new double[K][T];
                double[][] b = new double[K][T];
                //forward
                for (int i = 0; i < K; i++) {
                    a[i][0] = p[i] * B[i][observations[0]];
                }
                for (int t = 1; t < observations.length; t++) {
                    for (int i = 0; i < K; i++) {
                        double sum = 0;
                        for (int j = 0; j < K; j++) {
                            sum += a[j][t - 1] * A[j][i];
                        }
                        a[i][t] = B[i][observations[t]] * sum;
                    }
                }
                //backward
                for (int i = 0; i < K; i++) {
                    b[i][T - 1] = 1;
                }
                for (int t = T - 2; t >= 0; t--) {
                    for (int i = 0; i < K; i++) {
                        double sum = 0;
                        for (int j = 0; j < K; j++) {
                            sum += b[j][t + 1] * A[i][j] * B[j][observations[t + 1]];
                        }
                        b[i][t] = sum;
                    }
                }
                //update
                double[][] y = new double[K][T];
                double[][][] e = new double[K][K][T - 1];
                for (int t = 0; t < observations.length; t++) {
                    for (int i = 0; i < K; i++) {
                        double sum = 0;
                        for (int j = 0; j < K; j++) {
                            sum += a[j][t] * b[j][t];
                        }
                        y[i][t] = a[i][t] * b[i][t] / sum;
                    }
                }

                for (int t = 0; t < observations.length - 1; t++) {
                    for (int i = 0; i < K; i++) {
                        for (int j = 0; j < K; j++) {
                            double sum = 0;
                            for (int ii = 0; ii < K; ii++) {
                                for (int jj = 0; jj < K; jj++) {
                                    sum += a[ii][t] * A[ii][jj] * b[jj][t + 1] * B[jj][observations[t + 1]];
                                }
                            }
                            e[i][j][t] = a[i][t] * A[i][j] * b[j][t + 1] * B[j][observations[t + 1]] / sum;
                        }
                    }
                }

                for (int i = 0; i < K; i++) {
                    p[i] = y[i][1];
                }
                for (int i = 0; i < K; i++) {
                    for (int j = 0; j < K; j++) {
                        double sum1 = 0, sum2 = 0;
                        for (int t = 0; t < observations.length - 1; t++) {
                            sum1 += e[i][j][t];
                            sum2 += y[i][t];
                        }
                        A[i][j] = sum1 / sum2;
                    }
                }
                for (int i = 0; i < K; i++) {
                    for (int j = 0; j < N; j++) {
                        double sum1 = 0, sum2 = 0;
                        for (int t = 0; t < observations.length; t++) {
                            if (j == observations[t]) {
                                sum1 += y[i][t];
                            }
                            sum2 += y[i][t];
                        }
                        B[i][j] = sum1 / sum2;
                    }
                }



            }
            writeToConsole("");
        }
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

    public void classify() {
        float[][][] toClassify = io.readData();
        float[] r = p.predict(toClassify);
        writeToConsole("Classification Results:");
        for (int i = 0; i < r.length; i++) {
            writeToConsole("Score -- " + r[i]);
        }
    }

    public void loadModel(){
        String m = io.loadModel();
        if(m!=null) {
            s.sLen = p.loadModel(m);
        }
    }

    public void saveTrends() {
        if (selectedComponent instanceof DataComponent &&  ((DataComponent)selectedComponent).getType().equals("trend")) {
            ArrayList<ArrayList<Double>> arrays = ((Trend)selectedComponent).getArrays();
            StringBuilder sb = new StringBuilder();
            for(int i =0; i<arrays.size(); i++){
                ArrayList<Double> a = arrays.get(i);
                for(int j =0; j<a.size(); j++){
                    sb.append(a.get(j));
                    if(j!=a.size()-1){
                        sb.append(", ");
                    }
                }
                sb.append("\n");
            }
            io.saveCSV(sb.toString());
        }
    }

    public void setTotalOption(boolean totalOption) {
        this.totalOption = totalOption;
    }
}
