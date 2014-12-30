import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.features2d.*;
import org.opencv.highgui.Highgui;

import java.util.ArrayList;
import java.util.Arrays;
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

    MatOfKeyPoint srcKeyPoints,dstKeyPoints;

    public static void main(String[] args) {
        Mat img1 = Highgui.imread("D:\\workspace\\pwr\\pwr-3dreconstruction\\java_3d_reconstruction\\images" +
                "\\SylvainJpg\\S01.jpg", Highgui.CV_LOAD_IMAGE_GRAYSCALE);
        Mat img2 = Highgui.imread("D:\\workspace\\pwr\\pwr-3dreconstruction\\java_3d_reconstruction\\images" +
                "\\SylvainJpg\\S02.jpg", Highgui.CV_LOAD_IMAGE_GRAYSCALE);

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
        detectKeyPoints(keyPoints1, keyPoints2);

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


        List<MatOfDMatch> goodMatches = getInliers(mask, matches);
        MatOfPoint2f srcFilteredMat = new MatOfPoint2f();
        MatOfPoint2f dstFilteredMat = new MatOfPoint2f();
        extractAndSortGoodMatchPoints(keyPoints1, keyPoints2, goodMatches, srcFilteredMat, dstFilteredMat);


        //// draw key points and good matches
        Mat outImg = new Mat(img1.rows(), img1.cols() * 2, img1.type());
        Features2d.drawMatches2(img1, keyPoints1, img2, keyPoints2, goodMatches, outImg);
//        Highgui.imwrite("D:\\workspace\\pwr\\pwr-3dreconstruction\\java_3d_reconstruction\\images" +
//                "\\SylvainJpg\\S00a.jpg", outImg);
        Mat fundamentalMat = computeFundamentalMatrix(srcFilteredMat, dstFilteredMat);


        //// drawing epilines
        Mat epiLinesOn2 = new Mat(), epiLinesOn1 = new Mat();

        computeEpiLines(dstFilteredMat, srcFilteredMat, fundamentalMat, epiLinesOn2, epiLinesOn1);
        drawEpiLines(outImg, epiLinesOn1, epiLinesOn2);
        ////

        Highgui.imwrite("D:\\workspace\\pwr\\pwr-3dreconstruction\\java_3d_reconstruction\\images" +
                "\\SylvainJpg\\S00b.jpg", outImg);


//        Mat camMat =   getCamMat();

    }

    private void extractAndSortGoodMatchPoints(MatOfKeyPoint srcPoints, MatOfKeyPoint dstPoints, List<MatOfDMatch> goodMatches,
                                               MatOfPoint2f srcFilteredMat, MatOfPoint2f dstFilteredMat) {
        KeyPoint[] srcArray = srcPoints.toArray();
        KeyPoint[] dstArray = dstPoints.toArray();

        List srcFilteredPoints = new ArrayList();
        List dstFilteredPoints = new ArrayList();

        int i = 0;
        for (MatOfDMatch match : goodMatches) {
            int rows = match.rows();
            int cols = match.cols();

            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
//                    match.dump()
                    System.out.println(match.dump());
                    int queryIdx = (int) match.get(row, col)[0];
                    int trainIdx = (int) match.get(row, col)[1];
//                    srcArray[queryIdx].pt
                    srcFilteredPoints.add(srcArray[queryIdx].pt);
                    dstFilteredPoints.add(dstArray[trainIdx].pt);

                }
            }
            i++;
        }
        srcFilteredMat.fromList(srcFilteredPoints);
        dstFilteredMat.fromList(dstFilteredPoints);

    }

    private Mat getCamMat() {
        Mat camMat = new Mat();
        double camMatrixArray[][] = {{2779.8170435219467, 0., 1631.5}, {0., 2779.8170435219467, 1223.5}, {0., 0., 1.}};
        for (int i = 0; i < camMatrixArray.length; i++) {
            camMat.put(i, 0, camMatrixArray[i]);
        }
        return camMat;
    }

    private void computeEpiLines(MatOfPoint2f srcPoints, MatOfPoint2f dstPoints, Mat fundamentalMat, Mat epiLinesOn2, Mat epiLinesOn1) {
        Calib3d.computeCorrespondEpilines(srcPoints, 0, fundamentalMat, epiLinesOn2);
        Calib3d.computeCorrespondEpilines(dstPoints, 0, fundamentalMat, epiLinesOn1);
    }

    private void drawEpiLines(Mat outImg, Mat epiLinesOn1, Mat epiLinesOn2) {

        int epiLinesCount = epiLinesOn1.rows();

        double a, b, c;
//        val a = lines.get(i, 0, 0)
//        val b = lines.get(i, 0, 1)
//        val c = lines.get(i, 0, 2)
//        val x0 = 0
//        val y0 = math.round(-(c + a * x0) / b).toInt
//        val x1 = image.width
//        val y1 = math.round(-(c + a * x1) / b).toInt

        for (int line = 0; line < epiLinesCount; line++) {
            a = epiLinesOn1.get(line, 0)[0];
            b = epiLinesOn1.get(line, 0)[1];
            c = epiLinesOn1.get(line, 0)[2];

            int x0 = 0;
            int y0 = (int) (-(c+a*x0) / b);
            int x1 = img1.cols();
            int y1 = (int) (-(c+a*x1) / b);

            Point p1 = new Point(x0, y0);
            Point p2 = new Point(x1,y1);
            Scalar color = new Scalar(255, 255, 255);
            Core.line(outImg, p1, p2, color);

        }

        for (int line = 0; line < epiLinesCount; line++) {
            a = epiLinesOn2.get(line, 0)[0];
            b = epiLinesOn2.get(line, 0)[1];
            c = epiLinesOn2.get(line, 0)[2];

            int x0 = img1.cols();
            int y0 = (int) (-(c+a*x0) / b);
            int x1 = img1.cols()*2;
            int y1 = (int) (-(c+a*x1) / b);

            Point p1 = new Point(x0, y0);
            Point p2 = new Point(x1,y1);
            Scalar color = new Scalar(255, 255, 255);
            Core.line(outImg, p1, p2, color);

        }



    }

    private void computeDescriptors(MatOfKeyPoint keyPoints1, MatOfKeyPoint keyPoints2, Mat descriptors1, Mat descriptors2) {
        DescriptorExtractor surfExtractor = DescriptorExtractor.create(DescriptorExtractor.SURF);
        surfExtractor.compute(getImg1(), keyPoints1, descriptors1);
        surfExtractor.compute(getImg2(), keyPoints2, descriptors2);
    }

    private void detectKeyPoints(MatOfKeyPoint keyPoints1, MatOfKeyPoint keyPoints2) {
        FeatureDetector surfDetector = FeatureDetector.create(FeatureDetector.SURF);
        surfDetector.detect(getImg1(), keyPoints1);
        surfDetector.detect(getImg2(), keyPoints2);
    }

    private Mat computeFundamentalMatrix(MatOfPoint2f srcPoints, MatOfPoint2f dstPoints) {
        return Calib3d.findFundamentalMat(srcPoints, dstPoints, Calib3d.FM_8POINT, 0.0, 0.0);
    }

    private List<MatOfDMatch> getInliers(Mat mask, List<MatOfDMatch>
            matches) {

        List<MatOfDMatch> goodMatches = new ArrayList<MatOfDMatch>();

        for (int row = 0; row < mask.rows(); row++) {
            if (mask.get(row, 0)[0] == 1.0) {
                goodMatches.add(matches.get(row));
            }
        }
        return goodMatches;
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
