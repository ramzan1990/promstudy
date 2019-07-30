package promstudy.managers;

import java.text.DecimalFormat;

public class temp {
    public static void main(String[] a) {
        //double[] recall = new double[]{0.628 ,  0.884  , 0.700   , 0.908  , 0.647  , 0.691 , 0.845};
        //double[] precision = new double[]{0.722,  0.118  , 0.242   , 0.236  , 0.491  , 0.252 , 0.107};
        //double[] recall = new double[]{0.773 ,  0.948  , 0.889   , 0.868  , 0.764  , 0.775 , 0.810};
        //double[] precision = new double[]{0.775 ,  0.127  , 0.320   , 0.227  , 0.476  , 0.259 , 0.104};
        double[] recall = new double[]{0.755 ,  0.940  , 0.865   , 0.873  , 0.749  , 0.764 , 0.814};
        double[] precision = new double[]{0.769 ,  0.126  , 0.310   , 0.228  , 0.478  , 0.258 , 0.105};
        DecimalFormat df = new DecimalFormat("###.###");
        for(int i = 0; i<recall.length; i++) {
            double f1= 2 * ((precision[i] * recall[i]) / (precision[i] + recall[i]));
            System.out.print(df.format(f1) + " & " );
        }
    }
}
