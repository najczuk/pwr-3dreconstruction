package reconstruction;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import reconstruction.matcher.Matcher;

/**
 * User: Adrian
 * Date: 1/6/2015
 * Time: 00:51
 */
public class Main {
    static {
        System.loadLibrary("opencv_java2410");
    }

    public static void main(String[] args) {
        Mat img1 = Highgui.imread("images\\SylvainJpg\\S02.jpg", Highgui.CV_LOAD_IMAGE_GRAYSCALE);
        Mat img2 = Highgui.imread("images\\SylvainJpg\\S03.jpg", Highgui.CV_LOAD_IMAGE_GRAYSCALE);

        Matcher matcher = new Matcher(img1, img2);
//        matcher.printMatches();
    }
}
