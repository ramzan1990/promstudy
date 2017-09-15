package promstudy.managers;

import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

public class Predictor {
    Session s;

    public Predictor(){
        try {
            SavedModelBundle smb = SavedModelBundle.load("/home/ramzan/Dropbox/PromStudy/new_model_xx", "serve");
            s = smb.session();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public float[] predict(float[][][] sequences){
        Tensor in1 = Tensor.create(sequences);
        Tensor out = s.runner().feed("input_java", in1).fetch("output_java").run().get(0);
        float[][] temp = out.copyTo(new float[sequences.length][2]);
        float[] result = new float[sequences.length];
        for(int i =0;i<result.length;i++){
            result[i] = (temp[i][0] - temp[i][1] + 1)/2;
        }
        return result;
    }
}
