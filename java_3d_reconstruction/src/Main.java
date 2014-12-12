import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        System.loadLibrary("opencv_java2410");
        Mat m  = Mat.eye(3, 3, CvType.CV_8UC1);
//        System.out.println("m = " + m.dump());
        File file1 = new File("D:\\workspace\\pwr\\java_3d_reconstruction\\images\\SylvainJpg\\S02.jpg");
        File file2 = new File("D:\\workspace\\pwr\\java_3d_reconstruction\\images\\SylvainJpg\\S03.jpg");
        Mat mat1 = Highgui.imread(file1.getAbsolutePath());
        Mat mat2 = Highgui.imread(file2.getAbsolutePath());
//        Calib3d.findFundamentalMat(mat1,mat2,Calib3d.RANSAC,1)

    }
}
