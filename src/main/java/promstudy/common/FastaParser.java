package promstudy.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FastaParser {

    public static float[][][] parse(File f) throws FileNotFoundException {
        String s = new Scanner(f).useDelimiter("\\Z").next();
        String[] sp = s.split(">.*\n");
        int sLen = sp[1].replaceAll("\\s+", "").length();
        float[][][] result = new float[sp.length - 1][sLen][4];
        //skip first element (empty)
        for (int i = 0; i < result.length; i++) {
            result[i] = encode(sp[i + 1]);
        }
        return result;
    }

    private static float[][] encode(String s) {
        s = s.replaceAll("\\s+", "").toUpperCase();
        float[][] result = new float[s.length()][4];
        for (int i = 0; i < result.length; i++) {
            if (s.charAt(i) == 'A') {
                result[i][0] = 1;
            } else if (s.charAt(i) == 'T') {
                result[i][1] = 1;
            } else if (s.charAt(i) == 'G') {
                result[i][2] = 1;
            } else if (s.charAt(i) == 'C') {
                result[i][3] = 1;
            } else {
                result[i][0] = 1;
            }
        }
        return result;
    }
}
