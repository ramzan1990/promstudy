package promstudy.main;

import promstudy.common.FastaParser;
import promstudy.managers.GUIManager;
import promstudy.managers.IOManager;
import promstudy.managers.Predictor;
import promstudy.ui.CustomLAF;

import java.io.File;

public class PromStudy {
    public static void main(String[] args) {
        try {
            CustomLAF.change();
        } catch (Exception e) {
        }
        Predictor p = new Predictor();
        PState s = new PState();
        IOManager iom = new IOManager(s);
        GUIManager gm = new GUIManager(iom, p, s);
        gm.show();

        try {
            s.positive = FastaParser.parse(new File("data/small_pos.seq"));
            s.negative = FastaParser.parse(new File("data/small_neg.seq"));
            s.sequences = FastaParser.parse(new File("data/ATTAM.seq"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
