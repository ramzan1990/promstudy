package promstudy.managers;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import promstudy.clustering.KMeans;
import promstudy.clustering.Sequence;
import promstudy.common.FastaParser;
import promstudy.dataVisualisation.LogoComponent;
import promstudy.visualization.SalMapComp;
import promstudy.visualization.Trend;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by umarovr on 3/22/18.
 */
public class MotifMap {
    private static Predictor p;
    private static float[][][] sequences;
    private static int step = 1;
    private static int sLen = 2001;
    private static int sd = 10;
    private static int minDist = 1000;
    private static String output = "mot_out";
    private static ArrayList<String> names;
    private static double dt = 0.5;
    private static int count = 1;
    static int w = 500;
    static int h = 100;
    private static boolean ignoreCore = false;
    private static int numSeq = 4000;

    public static void main(String[] args) {
        File toPred = null;
        if (args.length > 0) {
            try {
                for (int i = 0; i < args.length / 2; i++) {
                    String option = args[2 * i];
                    String parameter = args[2 * i + 1];
                    if (option.equals("-set")) {
                        toPred = new File(parameter);
                    } else if (option.equals("-mod")) {
                        p = new Predictor(parameter);
                    } else if (option.equals("-out")) {
                        output = parameter;
                    }else if (option.equals("-core")) {
                        ignoreCore = Integer.parseInt(parameter) == 0;
                    }else if (option.equals("-ns")) {
                        numSeq = Integer.parseInt(parameter);
                    } else {
                        System.err.println("Unknown option: " + option);
                        System.err.println("Available Options: ");
                        System.err.println("-set: file with long sequences");
                        System.err.println("-mod: location of trained model");
                        System.err.println("-out: output");
                        return;
                    }
                }
                Object[] o = FastaParser.parse(toPred, 100);
                sequences = (float[][][]) o[0];
                names = (ArrayList<String>) o[1];
                analyseMotifs();
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static void analyseMotifs() {
        int motifLen = 15;
        ArrayList<float[]> arrays1 = new ArrayList<>();
        System.out.println("Progress: ");
        for (int i = 0; i < numSeq; i++) {
            //float[][] seq = Arrays.copyOfRange(sequences[i], 4800, 5400);
            float[][] seq = sequences[i];
            int total = seq.length - motifLen + 1;
            float[][][] toPredict = new float[total][sLen][4];
            for (int j = 0; j < toPredict.length - 1; j++) {
                float[][] seq2 = cloneArray(seq);
                for(int n = 0; n< motifLen; n++) {
                    for (int d = 0; d < seq2[n].length; d++) {
                        seq2[j + n][d] = 0;
                    }
                }
                toPredict[j] = seq2;
            }
            toPredict[toPredict.length - 1] = seq;
            arrays1.add(p.predict(toPredict));
            if ((i + 1) % 10 == 0) {
                System.out.print((i + 1) + " ");
            }
        }
        ArrayList<Sequence> trackNoCore = new ArrayList<>();
        ArrayList<Sequence> trackCore =  new ArrayList<>();
        for (int ari = 0; ari < arrays1.size(); ari++) {
            float[][] seq = sequences[ari];
            float[] ar1 = arrays1.get(ari);
            double maxScore = ar1[ar1.length - 1];
            for (int c = 0; c < ar1.length - 1; c++) {
                if (c > 800 && c < 1200) {
                    trackCore.add(new Sequence(flatten(seq, c, motifLen), (maxScore - ar1[c])));
                }else {
                    trackNoCore.add(new Sequence(flatten(seq, c, motifLen), (maxScore - ar1[c])));
                }
            }
        }

        ArrayList<RealMatrix> motifs = KMeans.freqMatrix(trackCore);
        for(int i = 0; i < motifs.size(); i++) {
            saveLogo(motifs.get(i), "core" + i);
        }

        motifs = KMeans.freqMatrix(trackNoCore);
        for(int i = 0; i < motifs.size(); i++) {
            saveLogo(motifs.get(i), "no_core" + i);
        }
        Trend.thick = true;

    }

    private static int[] flatten(float[][] seq, int c, int motifLen) {
        int[] fs = new int[motifLen];
        for(int i =0; i< motifLen; i++){
            if(seq[c + i][0] == 1){
                fs[i] = 1;
            }else if(seq[c + i][1] == 1){
                fs[i] = 2;
            }else if(seq[c + i][2] == 1){
                fs[i] = 3;
            }else if(seq[c + i][3] == 1){
                fs[i] = 4;
            }
        }
        return fs;
    }

    private static void saveLogo(RealMatrix freqMatrix, String name){
        ArrayList<String> letters = new ArrayList<>();
        letters.add("A");
        letters.add("T");
        letters.add("G");
        letters.add("C");
        //letters.add("N");
        for (int i = 0; i < freqMatrix.getColumnDimension(); i++) {
            double csum = 0;
            for (int j = 0; j < freqMatrix.getRowDimension(); j++) {
                csum += freqMatrix.getEntry(j, i);
            }
            for (int j = 0; j < freqMatrix.getRowDimension(); j++) {
                freqMatrix.setEntry(j, i, (double) freqMatrix.getEntry(j, i) / csum);
            }
        }
        RealVector H = new ArrayRealVector(freqMatrix.getColumnDimension());
        //double e = (1 / Math.log(2)) * ((letters.size() - 1) / (2 * line));
        for (int i = 0; i < freqMatrix.getColumnDimension(); i++) {
            double sum = 0;
            for (int j = 0; j < letters.size(); j++) {
                if (freqMatrix.getEntry(j, i) != 0) {
                    sum += freqMatrix.getEntry(j, i) * (Math.log(freqMatrix.getEntry(j, i)) / Math.log(2));
                }
            }
            H.setEntry(i, -sum );
        }
        RealVector Rseq = new ArrayRealVector(freqMatrix.getColumnDimension(), 2).subtract(H);
        LogoComponent logo = new LogoComponent(freqMatrix, Rseq, letters);
        try {
            saveComponent(logo, "png", new File(output  + name + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    static void saveComponent(Object c, String format, File outputfile) throws IOException {
        BufferedImage myImage;
        JComponent jc = (JComponent) c;
        if (jc instanceof LogoComponent) {
            ((LogoComponent) jc).setQuality(1);
        }
        jc.setSize(jc.getPreferredSize().width, jc.getPreferredSize().height);
        jc.repaint();

        myImage = new BufferedImage(jc.getWidth(), jc.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = myImage.createGraphics();
        jc.paint(g);
        try {
            ImageIO.write(myImage, format, outputfile);

        } catch (Exception e) {
            e.printStackTrace();
        }
        jc.setSize(jc.getWidth(), jc.getHeight());

        if (jc instanceof LogoComponent) {
            ((LogoComponent) jc).setQuality(0);
        }
    }


    public static float[][] cloneArray(float[][] src) {
        int length = src.length;
        float[][] target = new float[length][src[0].length];
        for (int i = 0; i < length; i++) {
            System.arraycopy(src[i], 0, target[i], 0, src[i].length);
        }
        return target;
    }
}
