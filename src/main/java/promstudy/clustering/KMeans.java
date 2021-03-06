package promstudy.clustering;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class KMeans {

    public static int k = 10;
    public static int itNum = 200;
    public static int scale = 1;
    public static int maxSize = 5000000;
    private static int seqLen = 15;

    public static ArrayList<RealMatrix> freqMatrix(ArrayList<Sequence> input, int tss) {
        seqLen = input.get(0).seq.length;
        Collections.sort(input);
        //Collections.reverse(input);
        System.out.println(input.size());
        input.subList((int) (0.05 * input.size()), input.size()).clear();
        System.out.println(input.size());
        Sequence s[] = new Sequence[input.size()];
        for (int i = 0; i < s.length; i++) {
            s[i] = input.get(i);
        }
        Sequence[] means = new Sequence[k];
        ArrayList<Integer> ki = new ArrayList<>();

        int rn = 0;
        ki.add(rn++);
        while (ki.size() != k) {
            double best = 0;
            for (int c : ki) {
                double score = score(s[c].seq, s[rn].seq);
                if (score > best) {
                    best = score;
                }
            }
            if (best < seqLen / 2 + 1) {
                ki.add(rn);
            }
            rn++;
            if (rn >= s.length) {
                k = ki.size();
                break;
            }
        }
        ArrayList<Sequence>[] clusters = new ArrayList[k];
        for (int i = 0; i < k; i++) {
            means[i] = s[ki.get(i)];
            clusters[i] = new ArrayList<Sequence>();
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
        /// Math.log(1.5)
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < clusters[i].size(); j++) {
                clusters[i].get(j).sim = score(means[i].seq, clusters[i].get(j).seq) * (1.0 / (Math.log(Math.abs(means[i].p - clusters[i].get(j).p) + 1) + 1));
            }
            Collections.sort(clusters[i], (o1, o2) -> o1.sim.compareTo(o2.sim));
            Collections.reverse(clusters[i]);
            clusters[i].subList((int) (0.5 * clusters[i].size()), clusters[i].size()).clear();

            means[i] = mean(clusters[i]);

            for (int j = clusters[i].size() - 1; j >=0; j--) {
                if(Math.abs(means[i].p - clusters[i].get(j).p) > 100){
                    clusters[i].remove(j);
                }
                if(j%50==0){
                    means[i] = mean(clusters[i]);
                }
            }

            for (int j = 0; j < clusters[i].size(); j++) {
                clusters[i].get(j).sim = score(means[i].seq, clusters[i].get(j).seq) * (1.0 / (Math.log(Math.abs(means[i].p - clusters[i].get(j).p) + 1) + 1));
            }
            Collections.sort(clusters[i], (o1, o2) -> o1.sim.compareTo(o2.sim));
            Collections.reverse(clusters[i]);
            clusters[i].subList((int) (0.5 * clusters[i].size()), clusters[i].size()).clear();

            double score = 0;
            for (int j = 0; j < clusters[i].size(); j++) {
                score += clusters[i].get(j).v;
            }
            //score/= clusters[i].size();
            System.out.println("Cluster (" + (i + 1) + ") size " + clusters[i].size() + " effect " + score / clusters[i].size() + " location " + getPosition(tss, clusters[i]));
        }
        // bestCluster = Integer.parseInt(JOptionPane.showInputDialog("123"));
        ArrayList<RealMatrix> fms = new ArrayList<>();
        for (int c = 0; c < k; c++) {
            RealMatrix fm = new Array2DRowRealMatrix(4, seqLen);
            for (int i = 0; i < clusters[c].size() && i < maxSize; i++) {
                Sequence sequence = clusters[c].get(i);
                for (int j = 0; j < seqLen; j++) {
                    if (sequence.seq[j] > 0) {
                        fm.addToEntry(sequence.seq[j] - 1, j, 1); //Math.pow(sequence.v, scale)
                    }
                }
            }
            fms.add(fm);
        }
        return fms;
    }

    private static Sequence mean(ArrayList<Sequence> cluster) {
        int count[][] = new int[seqLen][cluster.size()];
        double p = 0;
        for (int i = 0; i < cluster.size(); i++) {
            p += cluster.get(i).p;
            for (int j = 0; j < seqLen; j++) {
                count[j][i] = cluster.get(i).seq[j];
            }
        }
        int mean[] = new int[seqLen];
        for (int i = 0; i < seqLen; i++) {
            mean[i] = findPopular(count[i]);
        }
        return new Sequence(mean, 0, (int) (p / cluster.size()));
    }

    private static int closest(Sequence is, Sequence[] means) {
        double bestScore = -1;
        int index = -1;
        for (int i = 0; i < means.length; i++) {
            double score = score(means[i].seq, is.seq) * (1.0 / (Math.log(Math.abs(means[i].p - is.p) + 1) / Math.log(1.5) + 1));
            if (score > bestScore) {
                bestScore = score;
                index = i;
            }
        }
        return index;
    }

    private static int score(int[] mean, int[] is) {
        int count = 0;
        for (int i = 0; i < mean.length; i++) {
            if (mean[i] == is[i] && mean[i] != 0) {
                count++;
            }
        }
        return count;
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

    public static String getPosition(int tss, ArrayList<Sequence> cluster) {
        int min = Integer.MAX_VALUE;
        int max = -Integer.MAX_VALUE;
        for (Sequence p : cluster) {
            if (p.p > max) {
                max = p.p;
            }
            if (p.p < min) {
                min = p.p;
            }
        }
        String l = "";
        if (min < tss) {
            l = "[" + (min - tss);
        } else {
            l = "[+" + (min - tss + 1);
        }

        String r = "";
        if (max < tss) {
            r = (max - tss) + "]";
        } else {
            r = "+" + (max - tss + 1) + "]";
        }
        return l + " : " + r;
    }
}
