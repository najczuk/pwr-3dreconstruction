package reconstruction.geometry;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.highgui.Highgui;

/**
 * User: Adrian
 * Date: 12/30/2014
 * Time: 14:56
 */
public class EpipolarGeometry {
    static {
        System.loadLibrary("opencv_java2410");
    }

    /*
    * Mat fundamentalMat = computeFundamentalMatrix(srcFilteredMat, dstFilteredMat);
    *
    *
    * */


    MatOfPoint2f srcPoints, dstPoints;
    Mat fundamentalMatrix, essentialMatrix, epilinesSrc, epilinesDst;

    public EpipolarGeometry(MatOfPoint2f srcPoints, MatOfPoint2f dstPoints) {
        this.srcPoints = srcPoints;
        this.dstPoints = dstPoints;
        computeFundamentalMatrix();
        computeEpiLines();

    }


    private void computeFundamentalMatrix() {
        Mat outliers = new Mat();
        fundamentalMatrix = Calib3d.findFundamentalMat(srcPoints, dstPoints, Calib3d.FM_8POINT, 0, 0);
    }

    private void computeEpiLines() {
        epilinesSrc = new Mat();
        epilinesDst = new Mat();

        Calib3d.computeCorrespondEpilines(srcPoints, 1, fundamentalMatrix, epilinesDst);
        Calib3d.computeCorrespondEpilines(dstPoints, 2, fundamentalMatrix, epilinesSrc);
    }

    public Mat drawEpiLines(Mat outImg) {

        int epiLinesCount = epilinesSrc.rows();

        double a, b, c;

        for (int line = 0; line < epiLinesCount; line++) {
            a = epilinesSrc.get(line, 0)[0];
            b = epilinesSrc.get(line, 0)[1];
            c = epilinesSrc.get(line, 0)[2];

            int x0 = 0;
            int y0 = (int) (-(c + a * x0) / b);
            int x1 = outImg.cols() / 2;
            int y1 = (int) (-(c + a * x1) / b);

            Point p1 = new Point(x0, y0);
            Point p2 = new Point(x1, y1);
            Scalar color = new Scalar(255, 255, 255);
            Core.line(outImg, p1, p2, color);

        }

        for (int line = 0; line < epiLinesCount; line++) {
            a = epilinesDst.get(line, 0)[0];
            b = epilinesDst.get(line, 0)[1];
            c = epilinesDst.get(line, 0)[2];

            int x0 = 0;
            int y0 = (int) (-(c + a * x0) / b);
            int x1 = outImg.cols()/2;
            int y1 = (int) (-(c + a * x1) / b);

            Point p1 = new Point(x0+outImg.cols()/2, y0);
            Point p2 = new Point(x1+outImg.cols()/2, y1);
            Scalar color = new Scalar(255, 255, 255);
            Core.line(outImg, p1, p2, color);

        }
        return outImg;
    }
//    public Mat drawEpipolarLines(Mat img1, Mat img2){
//        Mat im1 = drawEpipolarLinesOnImage(img1);
//        Mat im2 = drawEpipolarLinesOnImage(img2);
//        Mat out = combineImages(img1,img2);
//        return out;
//    }

    public Mat drawEpipolarLinesOnImage(Mat outImg, int whichImgFlag) {


        Mat epilines = whichImgFlag ==0 ? epilinesSrc:epilinesDst;
        int epiLinesCount = epilines.rows();

        double a, b, c;

        for (int line = 0; line < epiLinesCount; line++) {
            a = epilines.get(line, 0)[0];
            b = epilines.get(line, 0)[1];
            c = epilines.get(line, 0)[2];

            int x0 = 0;
            int y0 = (int) (-(c + a * x0) / b);
            int x1 = outImg.cols();
            int y1 = (int) (-(c + a * x1) / b);

            Point p1 = new Point(x0, y0);
            Point p2 = new Point(x1, y1);
            Scalar color = new Scalar(255, 255, 255);
            Core.line(outImg, p1, p2, color);

        }
        return outImg;
    }

    private Mat combineImages(Mat img_object, Mat img_scene) {
        Mat combine = new Mat(img_object.rows(), img_object.cols() * 2, img_object.type());


        int size = (int) img_object.total() * img_object.channels();
        float[] buff = new float[size];
//        img_object.get(0, 0, buff);

        for (int col = 0; col < combine.cols(); col++) {
            if (col < img_object.cols()) {
                for (int row = 0; row < combine.rows(); row++) {
//                    img_object.get(row, col, buff);
//                    combine.put(col, row, buff);
                }
            } else {
                for (int row = 0; row < combine.rows(); row++) {
                    img_object.get(row, col, buff);
                    combine.put(col, row, buff);
                }
            }
        }
        return combine;
    }
}
