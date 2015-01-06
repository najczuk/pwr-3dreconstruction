import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import reconstruction.geometry.EpipolarGeometry;

public class Main {
    static {
        System.loadLibrary("opencv_java2410");
    }
    public static void main(String[] args) {
        System.loadLibrary("opencv_java2410");


        Mat img1 = Highgui.imread("images\\butelki\\1.jpg", Highgui.CV_LOAD_IMAGE_GRAYSCALE);
        Mat img2 = Highgui.imread("images\\butelki\\3.jpg", Highgui.CV_LOAD_IMAGE_GRAYSCALE);

        KeyPairsDetector keyPairsDetector = new KeyPairsDetector(img1,img2);
        EpipolarGeometry epipolarGeometry = new EpipolarGeometry(keyPairsDetector.srcSortedGoodPoints,
                keyPairsDetector.dstSortedGoodPoints);

        Mat matchedImg = keyPairsDetector.drawMatchesAndKeyPoints();
        epipolarGeometry.drawEpiLines(matchedImg);

        Highgui.imwrite("images\\SylvainJpg\\S00c.jpg", matchedImg);

    }
}
