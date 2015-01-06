package reconstruction;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import reconstruction.geometry.EpipolarGeometry;
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
        Mat img1 = Highgui.imread("images/SylvainJpg/S01.jpg", Highgui.CV_LOAD_IMAGE_GRAYSCALE);
        Mat img2 = Highgui.imread("images/SylvainJpg/S02.jpg", Highgui.CV_LOAD_IMAGE_GRAYSCALE);

        Matcher matcher = new Matcher(img1, img2);
        Mat outImg = matcher.drawMatchesAndKeyPoints("images\\SylvainJpg\\out.jpg");
        EpipolarGeometry geometry = new EpipolarGeometry(matcher.getMatchPoints1(),matcher.getMatchPoints2());
        outImg = geometry.drawEpiLines(outImg);
        Highgui.imwrite("images\\SylvainJpg\\out1.jpg", outImg);


        Mat im1 = geometry.drawEpipolarLinesOnImage(matcher.getImg1(),0);
        Mat im2 = geometry.drawEpipolarLinesOnImage(matcher.getImg2(),1);


//        matcher.printMatches();
    }
}
