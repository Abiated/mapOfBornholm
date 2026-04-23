package GUI;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class ScaleBar extends Canvas {

    private static final double barWidth = 100;
    private static final double height = 40.0;

    public ScaleBar() {
        super(200, 100);
    }

    public void update(float zoom) {
        GraphicsContext scale = this.getGraphicsContext2D();
        //Removes previous drawn scaleBar
        scale.clearRect(0, 0, getWidth(), height);

        float degreePrPixel = 1.0f/zoom;     //Amount of degrees of longitude per screen pixel
        int metersPrDegree = 40075000/360;      //Meters per degree at the equator
        //float degreeForDenmark = (float) Math.cos(55); //Corretion for being at danish latitude
        float metersPrPixel = degreePrPixel * metersPrDegree;

        System.out.println("Current zoom: " + zoom);
        System.out.println("Current degree per pixel: " + degreePrPixel);
        System.out.println("Current meter per pixel: " + metersPrPixel);
        System.out.println();
        System.out.println();




        float rawMeters = 100 * metersPrPixel; //Gives meters for a 100 px

        String label = formatDistance(rawMeters);

        double rangedMeters = parseFormatedDistance(label);

        double barWidthPx = rangedMeters / metersPrPixel;

        System.out.println(barWidthPx);

        scale.fillRect(20, 20, barWidthPx, 4);
        scale.fillRect(20, 14, 2, 12);
        scale.fillRect(20 + barWidthPx - 2, 14, 2, 12);
        scale.fillText(label, 20 + barWidthPx / 2 - 16, 12);
    }

    public String formatDistance(float meters) {
           if(meters >= 750) {
               float km = meters / 1000;

               float[] rangeKM = new float[]{0.5F, 1, 2, 5, 10, 20, 50, 100};
               for (float n : rangeKM) {
                   if (km <= n * 1.5) return n + "km";
               }
               return Math.round(km) + "km";
           } else {
               int[] rangeMeters = new int[]{10, 20, 30, 40, 50, 100, 250, 500};
               for (int n : rangeMeters) {
                   if(meters <= n * 1.5) return n + "m";
               }
               return Math.round(meters) + "m";
           }
    }

    public double parseFormatedDistance(String label) {
        if(label.endsWith("km")) {
            label = label.replace("km", "");
            return Double.parseDouble(label) * 1000;
        } else {
            label = label.replace("m", "");
            return Double.parseDouble(label);
        }
    }

}
