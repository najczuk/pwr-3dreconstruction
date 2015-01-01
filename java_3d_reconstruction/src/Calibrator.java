import org.opencv.core.Mat;

/**
 * User: Adrian
 * Date: 12/30/2014
 * Time: 10:49
 */
public class Calibrator {
    private Mat getCamMat() {
        Mat camMat = new Mat();
        double camMatrixArray[][] = {{2779.8170435219467, 0., 1631.5}, {0., 2779.8170435219467, 1223.5}, {0., 0., 1.}};
        for (int i = 0; i < camMatrixArray.length; i++) {
            camMat.put(i, 0, camMatrixArray[i]);
        }
        return camMat;
    }

}
