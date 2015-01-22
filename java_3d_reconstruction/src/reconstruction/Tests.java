package reconstruction;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import reconstruction.matcher.OFMatcher;

/**
 * User: Adrian
 * Date: 1/15/2015
 * Time: 20:58
 */
public class Tests {
    static {
        System.loadLibrary("opencv_java2410");
    }

    public static void main(String[] args){
        Mat img1 = Highgui.imread("images/SylvainJpg/S02.jpg", Highgui.CV_LOAD_IMAGE_GRAYSCALE);
        Mat img2 = Highgui.imread("images/SylvainJpg/S03.jpg", Highgui.CV_LOAD_IMAGE_GRAYSCALE);

        OFMatcher matcher = new OFMatcher(img1,img2);
        matcher.match();
    }
}
