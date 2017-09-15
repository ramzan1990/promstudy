package promstudy.main;

import promstudy.managers.GUIManager;
import promstudy.managers.IOManager;
import promstudy.managers.Predictor;

public class PromStudy {
    public static void main(String[] args) {
        Predictor p = new Predictor();
        PState s = new PState();
        IOManager iom = new IOManager(s);
        GUIManager gm = new GUIManager(iom, p, s);
        gm.show();
    }
}
