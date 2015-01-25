package reconstruction;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import reconstruction.geometry.EpipolarGeometry;
import reconstruction.geometry.CameraMatrices;
import reconstruction.matcher.Matcher;
import reconstruction.triangulation.Triangulator;

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
        Mat img1 = Highgui.imread("images/sfinks/20150105_221355.jpg", Highgui.CV_LOAD_IMAGE_GRAYSCALE);
        Mat img2 = Highgui.imread("images/sfinks/20150105_221400.jpg", Highgui.CV_LOAD_IMAGE_GRAYSCALE);

        Matcher matcher = new Matcher(img1, img2);
        Mat outImg = matcher.drawMatchesAndKeyPoints("images\\SylvainJpg\\out.jpg");
        EpipolarGeometry geometry = new EpipolarGeometry(matcher.getMatchPoints1(),matcher.getMatchPoints2());
        outImg = geometry.drawEpiLines(outImg);
        Highgui.imwrite("images\\SylvainJpg\\out1.jpg", outImg);

//        DRAW SEPARATE IMGS
//        Mat im1 = geometry.drawEpipolarLinesOnImage(matcher.getImg1(),0);
//        Mat im2 = geometry.drawEpipolarLinesOnImage(matcher.getImg2(),1);

        CameraMatrices calculator = new CameraMatrices(geometry.getFundamentalMatrix());

        System.out.println("fundamental:" + calculator.getFundamentalMat().dump());
        System.out.println("k mat:" + calculator.getKMat().dump());
        System.out.println("essential:" + calculator.getEssentialMat().dump());

        Triangulator triangulator = new Triangulator(matcher.getMatchPoints1(),matcher.getMatchPoints2(),calculator
                .getP(),calculator.getP1(),calculator.getKMat());

//        calculator.calculateCameraMatrices();

//        MULTI TEST
//        Mat result = calculator.multiplyMat(calculator.generateWMat(),calculator.generateZMat());
//        System.out.println(result.dump());


//        matcher.printMatches();
    }
}
