package promstudy.visualization;

import org.imgscalr.Scalr;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class PairMapComp extends DataComponent {

    private int maxCount;
    private double max, min;
    private double[][] array;
    private double wStep;
    private int offset;
    public static boolean thick = false;
    private static Map<String, Color> customColors;

    public PairMapComp(double[][] array) {
        super("trend");
        this.array = array;

        max = -Double.MAX_VALUE;
        min = Double.MAX_VALUE;
        for(int i = 0; i<array.length; i++){
            for(int j =0; j < array[i].length; j++){
                double d = Math.abs(array[i][j] - 1);
                if(d > max){
                    max = d;
                }
                if(d < min){
                    min = d;
                }
            }
        }


        customColors = new HashMap<>();
        customColors.put("A", Color.decode("#228B22"));
        customColors.put("T",Color.decode("#e62200")  );
        customColors.put("G", Color.ORANGE);
        customColors.put("C",Color.decode("#00008B"));
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        margin = 20;
        Graphics2D g2d = (Graphics2D) g;
        height = getHeight();
        width = getWidth();
        g2d.setRenderingHints(renderHints);
        int fs = 20;
        Font origFont = new Font("Arial", Font.PLAIN, fs);
        g2d.setFont(origFont);

        AffineTransform affineTransform = new AffineTransform();
        affineTransform.rotate(Math.toRadians(45), 0, 0);
        Font rotatedFont = origFont.deriveFont(affineTransform);


        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(textColor);
        step = 30;

        for (int i = 0; i < array.length; i++) {
            for(int j =0; j < array[i].length; j++) {
                if(array[i][j] == 0){
                    continue;
                }
                if (array[i][j] > 1) {
                    g2d.setColor(colors[0]);
                    g2d.setColor(new Color(g2d.getColor().getRed(), g2d.getColor().getGreen(), g2d.getColor().getBlue(), (int) Math.floor(255 * ( Math.abs(array[i][j] - 1) / max))));
                } else {
                    g2d.setColor(colors[1]);
                    g2d.setColor(new Color(g2d.getColor().getRed(), g2d.getColor().getGreen(), g2d.getColor().getBlue(), (int) Math.floor(255 * (Math.abs(1 - array[i][j]) / max))));
                }

                g2d.fillRect(step * j, step * i, step, step);
                g2d.setFont(new Font("Arial", Font.PLAIN, 9));
                g2d.setColor(Color.BLACK);
                g2d.drawString((i+1)+"", step * j + 10, step * i + 10);
                g2d.drawString((j+1)+"", step * j + 10, step * i + 20);
            }

            //String result = String.format("%.4f", array[i]);
            //g2d.drawString(result, step + (i/4) * step, height - (5*step - step*(i%4)));
            //g2d.setFont(origFont);
        }
    }

    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }
        BufferedImage bimage = new BufferedImage(img.getWidth(null),
                img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();
        return bimage;
    }
}
