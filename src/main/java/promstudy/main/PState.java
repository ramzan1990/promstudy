package promstudy.main;

import promstudy.common.MDouble;

public class PState {
    public float[][][] positive, negative;
    public MDouble decisionThreshold;

    public PState(){
        decisionThreshold = new MDouble(0.5);
    }
}
