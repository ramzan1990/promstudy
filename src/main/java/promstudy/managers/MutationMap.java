package promstudy.managers;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import promstudy.clustering.KMeansMatrices;
import promstudy.common.FastaParser;
import promstudy.visualization.SalMapComp;
import promstudy.visualization.Trend;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by umarovr on 3/22/18.
 */
public class MutationMap {
    private static Predictor p;
    private static float[][][] sequences;
    private static int step = 1;
    private static int sLen = 2001;
    private static int sd = 10;
    private static int minDist = 1000;
    private static String output = "output";
    private static ArrayList<String> names;
    private static double dt = 0.5;
    private static int count = 1;
    static int w = 40 * sLen;
    static int h = 6 * 40 + 100;
    private static boolean ignoreCore = false;
    private static int numSeq = 1000;

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
                //analyseSal();
                analyseMut();
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static void analyseSal() {
        ArrayList<float[]> arrays1 = new ArrayList<>();
        System.out.println("Progress: ");
        for (int i = 0; i < numSeq; i++) {
            //float[][] seq = Arrays.copyOfRange(sequences[i], 4800, 5400);
            float[][] seq = sequences[i];
            int total = 2001 + 1;
            float[][][] toPredict = new float[total][sLen][4];
            for (int j = 0; j < toPredict.length - 1; j++) {
                float[][] seq2 = cloneArray(seq);
                float[] tmp = seq2[j].clone();
                for (int d = 0; d < tmp.length; d++) {
                    if (tmp[d] == 1) {
                        tmp[d] += 0.1;
                    }
                }
                seq2[j] = tmp;
                toPredict[j] = seq2;
            }
            toPredict[toPredict.length - 1] = seq;
            arrays1.add(p.predict(toPredict));
            if ((i + 1) % 10 == 0) {
                System.out.print((i + 1) + " ");
            }
        }
        double[] trackNoCore = new double[4 * (arrays1.get(0).length - 1) * step];
        double[] trackCore = new double[4 * (arrays1.get(0).length - 1) * step];
        for (int ari = 0; ari < arrays1.size(); ari++) {
            float[][] seq = sequences[ari];
            float[] ar1 = arrays1.get(ari);
            double maxScore = ar1[ar1.length - 1];
            for (int c = 0; c < ar1.length - 1; c++) {
                int n = 0;
                if (seq[c][0] == 1) {
                    n = 0;
                } else if (seq[c][1] == 1) {
                    n = 1;
                } else if (seq[c][2] == 1) {
                    n = 2;
                } else if (seq[c][3] == 1) {
                    n = 3;
                }
                int p = c * 4 + n;
                if (c > 800 && c < 1200) {
                    trackCore[p] = (trackCore[p] + (ar1[c] - maxScore));
                } else {
                    trackNoCore[p] = (trackNoCore[p] + (ar1[c] - maxScore));
                }
            }
        }

        Trend.thick = true;
        try {
            saveComponents(new SalMapComp[]{new SalMapComp(trackNoCore)}, "png", new File(output + "_sal_no_core.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            saveComponents(new SalMapComp[]{new SalMapComp(trackCore)}, "png", new File(output + "_sal_core.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void analyseMut() {
        ArrayList<float[]> arrays1 = new ArrayList<>();
        System.out.println("Progress: ");
        for (int i = 0; i < numSeq; i++) {
            float[][] seq = sequences[i];
            int total = 4 * sLen + 1;
            float[][][] toPredict = new float[total][sLen][4];
            for (int j = 0; j < toPredict.length - 1; j++) {
                float[][] seq2 = cloneArray(seq);
                float[] tmp = new float[]{0, 0, 0, 0};
                tmp[j % 4] = 1;
                seq2[j / 4] = tmp;
                toPredict[j] = seq2;
            }
            toPredict[toPredict.length - 1] = seq;
            arrays1.add(p.predict(toPredict));
            if ((i + 1) % 10 == 0) {
                System.out.print((i + 1) + " ");
            }
        }


        ArrayList<RealMatrix> resultsCore = new ArrayList<>();
        ArrayList<RealMatrix> resultsNoCore = new ArrayList<>();
        for (int ari = 0; ari < arrays1.size(); ari++) {
            float[] ar1 = arrays1.get(ari);
            double maxScore = ar1[ar1.length - 1];
            double[] trackNoCore = new double[(arrays1.get(0).length - 1) * step];
            double[] trackCore = new double[(arrays1.get(0).length - 1) * step];
            for (int c = 0; c < ar1.length - 1; c++) {
                if (c > 4 * 950 && c < 4 * 1051) {
                    trackCore[c] = (trackCore[c] + (ar1[c] - maxScore));
                    ;
                } else {
                    trackNoCore[c] = (trackNoCore[c] + (ar1[c] - maxScore));
                }
            }
            resultsCore.add(new Array2DRowRealMatrix(trackCore));
            resultsNoCore.add(new Array2DRowRealMatrix(trackNoCore));
        }

        ArrayList<RealMatrix> clusters = KMeansMatrices.freqMatrix(resultsCore);

        Trend.thick = true;
       /* try {
            saveComponents(new SalMapComp[]{new SalMapComp(trackNoCore)}, "png", new File(output + "_mut_no_core.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        for (int i = 0; i < clusters.size(); i++) {
            try {
                saveComponents(new SalMapComp[]{new SalMapComp(clusters.get(i).getColumn(0))}, "png", new File(output + "_mut_core_" + i + ".png"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void saveComponents(JComponent c[], String format, File outputfile) throws IOException {

        int cols = (int) Math.round(Math.sqrt((4.0 / 3.0) * count));
        int rows = (int) Math.ceil((double) count / cols);
        int width = cols * w;
        int height = rows * h;
        BufferedImage myImage = null;
        BufferedImage result = new BufferedImage(
                width, height, //work these out
                BufferedImage.TYPE_INT_RGB);
        Graphics gg = result.getGraphics();

        ArrayList<BufferedImage> bis = new ArrayList<>();
        for (JComponent jc : c) {
            jc.setSize(new Dimension(w, h));
            jc.repaint();
            myImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = myImage.createGraphics();
            jc.paint(g);
            bis.add(myImage);
        }

        int x = 0;
        int y = 0;
        for (BufferedImage bi : bis) {
            gg.drawImage(bi, x, y, null);
            x += w;
            if (x >= result.getWidth()) {
                x = 0;
                y += h;
            }
        }


        ImageIO.write(result, format, outputfile);

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
