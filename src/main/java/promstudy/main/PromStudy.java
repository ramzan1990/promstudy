package promstudy.main;

import promstudy.common.FastaParser;
import promstudy.managers.GUIManager;
import promstudy.managers.IOManager;
import promstudy.managers.Predictor;
import promstudy.ui.CustomLAF;

import java.io.File;

public class PromStudy {
    public static void main(String[] args) {
        Predictor p = new Predictor();
        PState s = new PState();
        IOManager iom = new IOManager(s);
        File toPred = null;
        if (args.length > 0) {
            try {
                for (int i = 0; i < args.length / 2; i++) {
                    String option = args[2 * i];
                    String parameter = args[2 * i + 1];
                    if (option.equals("-pred")) {
                        toPred = new File(parameter);
                    } else if (option.equals("-mod")) {
                         p = new Predictor(parameter);
                    }  else {
                        System.err.println("Unknown option: " + option);
                        System.err.println("Available Options: ");
                        System.err.println("-pred: file to predict");
                        System.err.println("-mod: model file");
                        return;
                    }
                }
                if (toPred!=null) {
                    float[][][] toClassify = FastaParser.parse(toPred);
                    float[] r = p.predict(toClassify);
                    System.out.println("Classification Results:");
                    for (int i = 0; i < r.length; i++) {
                        System.out.println("Score -- " + r[i]);
                    }
                }
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        try {
            CustomLAF.change();
        } catch (Exception e) {
        }

        GUIManager gm = new GUIManager(iom, p, s);
        gm.show();

        try {
            s.positive = FastaParser.parse(new File("data/small_pos.seq"));
            s.negative = FastaParser.parse(new File("data/small_neg.seq"));
            s.sequences = FastaParser.parse(new File("data/ATTAS.seq"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
