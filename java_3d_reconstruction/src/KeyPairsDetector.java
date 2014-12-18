import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.features2d.*;
import org.opencv.highgui.Highgui;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Adrian
 * Date: 12/16/2014
 * Time: 21:05
 */
public class KeyPairsDetector {
    static {
        System.loadLibrary("opencv_java2410");
    }

    public static void main(String[] args) {
        Mat img1 = Highgui.imread("D:\\workspace\\pwr\\pwr-3dreconstruction\\java_3d_reconstruction\\images" +
                "\\SylvainJpg\\S02.jpg", Highgui.CV_LOAD_IMAGE_GRAYSCALE);
        Mat img2 = Highgui.imread("D:\\workspace\\pwr\\pwr-3dreconstruction\\java_3d_reconstruction\\images" +
                "\\SylvainJpg\\S03.jpg", Highgui.CV_LOAD_IMAGE_GRAYSCALE);

        KeyPairsDetector keyPairsDetector = new KeyPairsDetector(img1, img2);

    }

    private Mat img1, img2;

    public KeyPairsDetector(Mat img1, Mat img2) {
        this.img1 = img1;
        this.img2 = img2;
        matchImages();
    }

    public Mat getImg1() {
        return img1;
    }

    public Mat getImg2() {
        return img2;
    }

    private void matchImages() {
        MatOfKeyPoint keyPoints1 = new MatOfKeyPoint();
        MatOfKeyPoint keyPoints2 = new MatOfKeyPoint();
        detectKeypoints(keyPoints1, keyPoints2);

        Mat descriptors1 = new Mat(), descriptors2 = new Mat();
        computeDescriptors(keyPoints1, keyPoints2, descriptors1, descriptors2);

        DescriptorMatcher descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
        ArrayList<MatOfDMatch> matches = new ArrayList<MatOfDMatch>();
        descriptorMatcher.knnMatch(descriptors1, descriptors2, matches, 1);

        MatOfPoint2f srcPoints = new MatOfPoint2f();
        MatOfPoint2f dstPoints = new MatOfPoint2f();
        convertUnsortedKeyPointsIntoPoint2f(keyPoints1, keyPoints2, matches, srcPoints, dstPoints);

        Mat mask = new Mat();
        Calib3d.findHomography(srcPoints, dstPoints, Calib3d.RANSAC, 5, mask);


        List<MatOfDMatch> goodMatches = new ArrayList<MatOfDMatch>();
        getInliers(mask, matches, goodMatches);

        Mat outImg = new Mat(img1.rows(), img1.cols() * 2, img1.type());
        Features2d.drawMatches2(img1, keyPoints1, img2, keyPoints2, goodMatches, outImg);
//        Highgui.imwrite("D:\\workspace\\pwr\\pwr-3dreconstruction\\java_3d_reconstruction\\images" +
//                "\\SylvainJpg\\S00a.jpg", outImg);
        Mat fundamentalMat = computeFundamentalMatrix(srcPoints, dstPoints);


        Mat epiLinesOn2 = new Mat(), epiLinesOn1 = new Mat();
        computeEpiLines(srcPoints, dstPoints, fundamentalMat, epiLinesOn2, epiLinesOn1);
        drawEpiLines(outImg, epiLinesOn1, epiLinesOn2);

        Highgui.imwrite("D:\\workspace\\pwr\\pwr-3dreconstruction\\java_3d_reconstruction\\images" +
                "\\SylvainJpg\\S00a.jpg", outImg);

    }

    private void computeEpiLines(MatOfPoint2f srcPoints, MatOfPoint2f dstPoints, Mat fundamentalMat, Mat epiLinesOn2, Mat epiLinesOn1) {
        Calib3d.computeCorrespondEpilines(srcPoints, 2, fundamentalMat, epiLinesOn2);
        Calib3d.computeCorrespondEpilines(dstPoints, 2, fundamentalMat, epiLinesOn1);
    }

    private void drawEpiLines(Mat outImg, Mat epiLinesOn1, Mat epiLinesOn2) {
//        cv::line(image_out,
//                cv::Point(0,-(*it)[2]/(*it)[1]),
//        cv::Point(image2.cols,-((*it)[2]+   (*it)[0]*image2.cols)/(*it)[1]),
//        cv::Scalar(255,255,255));
//        Mat outImg = new Mat(img1.rows(),img1.cols()*2,img1.type());
        int epiLinesCount = epiLinesOn1.rows();
        System.out.println(epiLinesOn1.dump());

        double a, b, c;


        for (int line = 0; line < epiLinesCount; line++) {
            a = epiLinesOn1.get(line, 0)[0];
            b = epiLinesOn1.get(line, 0)[1];
            c = epiLinesOn1.get(line, 0)[2];
            Point p1 = new Point(0, -c / b);
            Point p2 = new Point(img1.cols(), -(c + a * img1.cols()) / b);
            Scalar color = new Scalar(255, 255, 255);
            Core.line(outImg, p1, p2, color);

        }

        for (int line = 0; line < epiLinesCount; line++) {
            a = epiLinesOn2.get(line, 0)[0];
            b = epiLinesOn2.get(line, 0)[1];
            c = epiLinesOn2.get(line, 0)[2];
            Point p1 = new Point(img1.cols(), -c / b);
            Point p2 = new Point(img1.cols()*2, -(c + a * img1.cols()*2) / b);
            Scalar color = new Scalar(255, 255, 255);
            Core.line(outImg, p1, p2, color);

        }

    }

    private void computeDescriptors(MatOfKeyPoint keyPoints1, MatOfKeyPoint keyPoints2, Mat descriptors1, Mat descriptors2) {
        DescriptorExtractor surfExtractor = DescriptorExtractor.create(DescriptorExtractor.SURF);
        surfExtractor.compute(getImg1(), keyPoints1, descriptors1);
        surfExtractor.compute(getImg2(), keyPoints2, descriptors2);
    }

    private void detectKeypoints(MatOfKeyPoint keyPoints1, MatOfKeyPoint keyPoints2) {
        FeatureDetector surfDetector = FeatureDetector.create(FeatureDetector.SURF);
        surfDetector.detect(getImg1(), keyPoints1);
        surfDetector.detect(getImg2(), keyPoints2);
    }

    private Mat computeFundamentalMatrix(MatOfPoint2f srcPoints, MatOfPoint2f dstPoints) {
        return Calib3d.findFundamentalMat(srcPoints, dstPoints, Calib3d.FM_8POINT, 0.0, 0.0);
    }

    private void getInliers(Mat mask, List<MatOfDMatch>
            matches, List<MatOfDMatch> goodMatches) {


        for (int row = 0; row < mask.rows(); row++) {
            if (mask.get(row, 0)[0] == 1.0) {
//                System.out.println(matches.get(row).get(0, 0)[0]);
//                System.out.println(matches.get(row).get(0, 0)[1]);
//                System.out.println(matches.get(row).dump());
                goodMatches.add(matches.get(row));
            }
        }
    }

    private void convertUnsortedKeyPointsIntoPoint2f(MatOfKeyPoint keyPoints1, MatOfKeyPoint keyPoints2,
                                                     List<MatOfDMatch> matches, MatOfPoint2f points1,
                                                     MatOfPoint2f points2) {


        List<KeyPoint> kplist1 = keyPoints1.toList();
        List<KeyPoint> kplist2 = keyPoints2.toList();

        ArrayList<Point> pointsList1 = new ArrayList<Point>();
        ArrayList<Point> pointsList2 = new ArrayList<Point>();

        for (MatOfDMatch match : matches) {
            pointsList1.add(kplist1.get((int) (match.get(0, 0)[0])).pt);
            pointsList2.add(kplist2.get((int) (match.get(0, 0)[1])).pt);
        }


        points1.fromList(pointsList1);
        points2.fromList(pointsList2);

    }

    private void convertSortedKeyPointsIntoPoint2f(MatOfKeyPoint keyPoints1, MatOfKeyPoint keyPoints2, MatOfPoint2f
            points1, MatOfPoint2f points2) {


        List<KeyPoint> kp1list = keyPoints1.toList();
        List<KeyPoint> kp2list = keyPoints2.toList();

        ArrayList<Point> pointsList1 = new ArrayList<Point>();
        ArrayList<Point> pointsList2 = new ArrayList<Point>();


        for (int i = 0; i < kp1list.size(); i++) {
            pointsList1.add(kp1list.get(i).pt);
            pointsList2.add(kp2list.get(i).pt);
        }
        points1.fromList(pointsList1);
        points2.fromList(pointsList2);

    }
}
