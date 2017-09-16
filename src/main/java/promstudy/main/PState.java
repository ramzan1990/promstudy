package promstudy.main;

import promstudy.common.MDouble;

public class PState {
    public float[][][] positive, negative;
    public float[][][] sequences;
    public MDouble decisionThreshold;
    public int step;
    public int sLen = 251;

    public PState(){
        decisionThreshold = new MDouble(0.5);
        step = 10;
    }
}
