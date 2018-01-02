package promstudy.managers;

import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Shape;
import org.tensorflow.Tensor;

public class Predictor {
    Session s;

    public Predictor(){
        try {
            SavedModelBundle smb = SavedModelBundle.load("test_model_new", "serve");
            s = smb.session();
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public Predictor(String parameter) {
        try {
            SavedModelBundle smb = SavedModelBundle.load(parameter, "serve");
            s = smb.session();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public float[] predict(float[][][] sequences){
        Tensor in1 = Tensor.create(sequences);
        Tensor out = s.runner().feed("input_java", in1).fetch("output_java").run().get(0);
        float[][] temp = (float[][])out.copyTo(new float[sequences.length][2]);
        float[] result = new float[sequences.length];
        for(int i =0;i<result.length;i++){
            result[i] = (temp[i][0] - temp[i][1] + 1)/2;
        }
        return result;
    }

    public int loadModel(String parameter){
        try {
            SavedModelBundle smb = SavedModelBundle.load(parameter, "serve");
            s = smb.session();
            return (int) smb.graph().operation("input_java").output(0).shape().size(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
