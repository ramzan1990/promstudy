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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
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
    private static String output = "mot_out_";
    private static ArrayList<String> names;
    private static double dt = 0.5;
    private static int count = 1;
    static int w = 500;
    static int h = 100;
    private static boolean ignoreCore = false;
    private static int numSeq = 7000; //7970
    private static int motifLen = 15;

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
                    } else if (option.equals("-core")) {
                        ignoreCore = Integer.parseInt(parameter) == 0;
                    } else if (option.equals("-ns")) {
                        numSeq = Integer.parseInt(parameter);
                    } else if (option.equals("-ml")) {
                        motifLen = Integer.parseInt(parameter);
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
        int tss = 1000;
        ArrayList<float[]> arrays1 = new ArrayList<>();
        ArrayList<float[]> arrays2 = new ArrayList<>();
        System.out.println("Progress: ");
        for (int i = 0; i < numSeq; i++) {
            //float[][] seq = Arrays.copyOfRange(sequences[i], 4800, 5400);
            float[][] seq = sequences[i];
            int total = seq.length - motifLen + 1;
            float[][][] toPredict = new float[total][sLen][4];
            for (int j = 0; j < toPredict.length - 1; j++) {
                float[][] seq2 = cloneArray(seq);
                for (int n = 0; n < motifLen; n++) {
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
            toPredict = new float[1][sLen][4];
            float[][] seq2 = cloneArray(seq);
            for (int n = 0; n < 8; n++) {
                if(n==1 || n==2){
                    continue;
                }
                for (int d = 0; d < seq2[n].length; d++) {
                    seq2[998 + n][d] = 0;
                }
            }
            toPredict[0] = seq2;
            arrays2.add(p.predict(toPredict));
        }
        ArrayList<Sequence> trackNoCore = new ArrayList<>();
        ArrayList<Sequence> trackCore = new ArrayList<>();
        Motif tata = new Motif(15);
        Motif ccat = new Motif(12);
        Motif inr = new Motif(8);
        Motif tct = new Motif(8);
        StringBuilder sb = new StringBuilder();
        for (int ari = 0; ari < arrays1.size(); ari++) {
            float[][] seq = sequences[ari];
            float[] ar1 = arrays1.get(ari);
            float[] ar2 = arrays2.get(ari);
            double maxScore = ar1[ar1.length - 1];
            if(maxScore < 1.0){
                continue;
            }
            for (int c = 0; c < ar1.length - 1; c++) {
                if (maxScore - ar1[c] <= 0) {
                    continue;
                }
                double v = ar1[c] / maxScore;
                if (c > 800 && c < 1200) {
                    trackCore.add(new Sequence(flatten(seq, c, motifLen), v, c));
                } else {
                    trackNoCore.add(new Sequence(flatten(seq, c, motifLen), v, c));
                }
            }
            double[] temp = tatascore(seq, tss);
            if (temp[0] >= -8.16) {
                int p = (int) (tss - temp[1]);
                tata.add(flatten(seq,  p, 15));
                tata.count++;
                tata.pos.add(p);
                tata.effect += ar1[p] / maxScore;
            }

            temp = ccaatscore(seq, tss);
            if (temp[0] >= -4.54) {
                int p = (int) (tss - temp[1]);
                ccat.add(flatten(seq, p, 12));
                ccat.count++;
                ccat.pos.add(p);
                ccat.effect += ar1[p] / maxScore;
            }

            double score = inrscore(seq, tss);
            if(score >= -3.75){
                sb.append("> Sequence\n");
                sb.append(FastaParser.reverse(Arrays.copyOfRange(seq, 998, 998 + 8)));
                sb.append("\n");
                inr.add(flatten(seq, 998, 8));
                inr.count++;
                inr.effect += ar2[0] / maxScore;
            }
            score = tctscore(seq, tss);
            if(score >= 12.84){
                tct.add(flatten(seq, 998, 8));
                tct.count++;
                tct.effect +=  ar2[0] / maxScore;
            }

        }
        System.out.println("Name, Frequency, Position, Effect");
        System.out.println("TATA, " + (double)tata.count/numSeq + ", " + tata.getPosition(tss) + ", " + tata.effect/tata.count);
        System.out.println("CCAT, " + (double)ccat.count/numSeq + ", " + ccat.getPosition(tss) + ", " + ccat.effect/ccat.count);
        System.out.println("Inr, " + (double)inr.count/numSeq + ", " + "-2" + ", " + inr.effect/inr.count);
        System.out.println("TCT, " + (double)tct.count/numSeq + ", " + "-2" + ", " + tct.effect/tct.count);
        saveLogo(tata.fm, "tata");
        saveLogo(ccat.fm, "ccat");
        saveLogo(inr.fm, "inr");
        saveLogo(tct.fm, "tct");

        try (PrintWriter out = new PrintWriter("inr.fasta")) {
            out.print(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        ArrayList<RealMatrix> motifs = KMeans.freqMatrix(trackCore, tss);
        for (int i = 0; i < motifs.size(); i++) {
            saveLogo(motifs.get(i), "core" + i);
        }

        motifs = KMeans.freqMatrix(trackNoCore, tss);
        for (int i = 0; i < motifs.size(); i++) {
            saveLogo(motifs.get(i), "no_core" + i);
        }
        Trend.thick = true;

    }

    private static int[] flatten(float[][] seq, int c, int motifLen) {
        int[] fs = new int[motifLen];
        for (int i = 0; i < motifLen; i++) {
            if (seq[c + i][0] == 1) {
                fs[i] = 1;
            } else if (seq[c + i][1] == 1) {
                fs[i] = 2;
            } else if (seq[c + i][2] == 1) {
                fs[i] = 3;
            } else if (seq[c + i][3] == 1) {
                fs[i] = 4;
            }
        }
        return fs;
    }

    private static void saveLogo(RealMatrix freqMatrix, String name) {
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
            H.setEntry(i, -sum);
        }
        RealVector Rseq = new ArrayRealVector(freqMatrix.getColumnDimension(), 2).subtract(H);
        LogoComponent logo = new LogoComponent(freqMatrix, Rseq, letters);
        try {
            saveComponent(logo, "png", new File(output + name + ".png"));
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

    public static double[] tatascore(float[][] a, int tss) {
        double[][] tata = new double[][]{{-1.02, -1.68, 0, -0.28}, {-3.05, 0, -2.74, -2.06}, {0, -2.28, -4.28, -5.22}, {-4.61, 0, -4.61, -3.49}, {
                0, -2.34, -3.77, -5.17}, {0, -0.52, -4.73, -4.63}, {0, -3.65, -2.65, -4.12}, {0, -0.37, -1.5, -3.74}, {
                -0.01, -1.4, 0, -1.13}, {-0.94, -0.97, 0, -0.05}, {-0.54, -1.4, -0.09, 0}, {-0.48, -0.82, 0, -0.05}, {
                -0.48, -0.66, 0, -0.11}, {-0.74, -0.54, 0, -0.28}, {-0.62, -0.61, 0, -0.4}};
        double maxScore = -1000;
        int maxI = -1000;
        for (int p = 0; p < 14; p++) {
            double[][] seq = new double[15][4];
            for (int i = 0; i < tata.length; i++) {
                for (int j = 0; j < 4; j++) {
                    seq[i][j] = a[tss - 39 + p + i][j];
                }
            }
            double score = 0;
            for (int i = 0; i < tata.length; i++) {
                for (int j = 0; j < 4; j++) {
                    score = score + tata[i][j] * seq[i][j];
                }
            }
            if (score > maxScore) {
                maxScore = score;
                maxI = 39 - p;
            }
        }
        return new double[]{maxScore, maxI};
    }

    public static double[] ccaatscore(float[][] a, int tss) {
        double[][] ccat = {{-0.02, 0, -1.46, -0.01}, {-0.49, -0.01, -0.24, 0}, {-1.19, 0, -1.26, -0.57}, {0, -3.16, -0.4, -3.46}, {
                -0.61, -1.44, 0, -2.45}, {-4.39, -3.99, -4.03, 0}, {-4.4, -4, -4.4, 0}, {0, -4.37, -4.37, -4.37}, {
                0, -1.33, -1.69, -2.45}, {-2.12, 0, -2.26, -4.27}, {-1.32, -2.84, -0.47, 0}, {0, -3.57, -0.81, -2.64}};
        double maxScore = -1000;
        int maxI = -1000;

        for (int p = 0; p < 142; p++) {
            double[][] seq = new double[12][4];
            for (int i = 0; i < ccat.length; i++) {
                for (int j = 0; j < 4; j++) {
                    seq[i][j] = a[tss - 200 + p + i][j];
                }
            }

            double score = 0;
            for (int i = 0; i < ccat.length; i++) {
                for (int j = 0; j < 4; j++) {
                    score = score + ccat[i][j] * seq[i][j];
                }
            }
            if (score > maxScore) {
                maxScore = score;
                maxI = 200 - p;
            }
        }
        return new double[]{maxScore, maxI};
    }

    public static double inrscore(float[][] b, int tss) {
        double[][] inr = new double[][]{{-1.14, 0, -0.75, -1.16}, {-5.26, -5.26, -5.26, 0}, {0, -2.74, -5.21, -5.21}, {-1.51, -0.29, 0, -0.41}, {-0.65, 0, -4.56, -0.45}, {-0.55, -0.36, -0.86, 0}, {-0.91, 0, -0.38, -0.29}, {-0.82, 0, -0.65, -0.18}};
        double[][] a = new double[8][4];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < 4; j++) {
                a[i][j] = b[tss - 2 + i][j];
            }
        }
        double score = 0;
        for (int i = 0; i < inr.length; i++) {
            for (int j = 0; j < 4; j++) {
                score = score + inr[i][j] * a[i][j];
            }
        }
        return score;
    }

    public static double tctscore(float[][] b, int tss) {
        double[][] tct = new double[][]{{0.08, 0.35, 0.30, 0.27}, {0.08, 0.32, 0.17, 0.43}, {0.00, 0.00, 0.00, 11.00}, {0.07, 0.62, 0.08, 0.24}, {0.09, 0.32, 0.16, 0.43}, {0.11, 0.43, 0.15, 0.30}, {0.09, 0.33, 0.22, 0.36}, {0.10, 0.28, 0.24, 0.38}};
        double[][] a = new double[8][4];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < 4; j++) {
                a[i][j] = b[tss - 2 + i][j];
            }
        }
        double score = 0;
        for (int i = 0; i < tct.length; i++) {
            for (int j = 0; j < 4; j++) {
                score = score + tct[i][j] * a[i][j];
            }
        }
        return score;
    }


}

class Motif {
    public ArrayList<Integer> pos;
    public double effect = 0;
    public int count = 0;
    public RealMatrix fm;

    public Motif(int seqLen){
        this.fm = new Array2DRowRealMatrix(4, seqLen);
        this.pos = new ArrayList<>();
    }

    public void add(int[] sequence){
        for (int j = 0; j < fm.getColumnDimension(); j++) {
            if (sequence[j] > 0) {
                fm.addToEntry(sequence[j] - 1, j, 1);
            }
        }
    }

    public String getPosition(int tss) {
        int min = Integer.MAX_VALUE;
        int max = -Integer.MAX_VALUE;
        for(int p : pos){
            if(p>max){
                max = p;
            }
            if(p<min){
                min = p;
            }
        }
        String l = "";
        if(min < tss){
            l = "[" + (min - tss);
        }else{
            l = "[+" + (min - tss + 1);
        }

        String r = "";
        if(max < tss){
            r = (max - tss) + "]";
        }else{
            r = "+" + (max - tss + 1) + "]";
        }
        return l + " : " + r;
    }
}
