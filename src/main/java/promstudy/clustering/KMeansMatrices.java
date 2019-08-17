package promstudy.clustering;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class KMeansMatrices {

    public static int k = 10;
    public static int itNum = 100;
    public static int scale = 1;
    public static int maxSize = 5000000;
    private static int seqLen = 15;

    public static ArrayList<RealMatrix> freqMatrix(ArrayList<RealMatrix> input) {
        RealMatrix s[] = new RealMatrix[input.size()];
        for (int i = 0; i < s.length; i++) {
            s[i] = input.get(i);
        }
        RealMatrix[] means = new RealMatrix[k];
        ArrayList<Integer> ki = new ArrayList<>();

        Random r = new Random(9025);
        while (ki.size() != k) {
            int rn = r.nextInt(s.length);
            if (!ki.contains(rn)) {
                ki.add(rn);
            }
        }

        ArrayList<RealMatrix>[] clusters = new ArrayList[k];
        for (int i = 0; i < k; i++) {
            means[i] = s[ki.get(i)];
            clusters[i] = new ArrayList<RealMatrix>();
        }

        for (int i = 0; i < itNum; i++) {
            for (int j = 0; j < k; j++) {
                clusters[j].clear();
            }
            for (int j = 0; j < s.length; j++) {
                int c = closest(s[j], means);
                clusters[c].add(s[j]);
            }
            for (int j = 0; j < k; j++) {
                means[j] = mean(clusters[j]);
            }
        }


        for (int i = 0; i < k; i++) {
            System.out.println("Cluster (" + (i + 1) + ") size " + clusters[i].size());
        }
        ArrayList<RealMatrix> fms = new ArrayList<>();
        for (int c = 0; c < k; c++) {
            RealMatrix fm = new Array2DRowRealMatrix(input.get(0).getRowDimension(), input.get(0).getColumnDimension());
            for (int i = 0; i < clusters[c].size() && i < maxSize; i++) {
                RealMatrix rm = clusters[c].get(i);
                fm = fm.add(rm);
            }
            fm = fm.scalarMultiply(1.0/clusters[c].size());
            fms.add(fm);
        }
        return fms;
    }

    private static RealMatrix mean(ArrayList<RealMatrix> cluster) {
        RealMatrix mean = cluster.get(0);
        for(int i = 1; i < cluster.size(); i++){
            mean.add(cluster.get(i));
        }
        mean.scalarMultiply(1.0/cluster.size());
        return mean;
    }

    private static int closest(RealMatrix m, RealMatrix[] means) {
        double bestScore = Double.MAX_VALUE;
        int index = -1;
        for (int i = 0; i < means.length; i++) {
            double score = score(means[i], m);
            if (score < bestScore) {
                bestScore = score;
                index = i;
            }
        }
        return index;
    }

    private static double score(RealMatrix mean, RealMatrix m) {
        double norm = m.subtract(mean).getFrobeniusNorm();

        return norm;
    }

    public static int findPopular(int[] a) {

        if (a == null || a.length == 0)
            return 0;

        Arrays.sort(a);

        int previous = a[0];
        int popular = a[0];
        int count = 1;
        int maxCount = 1;

        for (int i = 1; i < a.length; i++) {
            if (a[i] == previous)
                count++;
            else {
                if (count > maxCount) {
                    popular = a[i - 1];
                    maxCount = count;
                }
                previous = a[i];
                count = 1;
            }
        }

        return count > maxCount ? a[a.length - 1] : popular;

    }

}
