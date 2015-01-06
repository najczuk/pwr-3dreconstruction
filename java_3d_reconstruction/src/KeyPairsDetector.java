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

    MatOfKeyPoint srcKeyPoints, dstKeyPoints;
    MatOfPoint2f srcSortedGoodPoints,dstSortedGoodPoints;
    List<MatOfDMatch> matches;

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

        this.srcKeyPoints = keyPoints1;
        this.dstKeyPoints = keyPoints2;

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
        this.matches = goodMatches;
        MatOfPoint2f srcGoodSortedPoints = new MatOfPoint2f();
        MatOfPoint2f dstGoodSortedPoints = new MatOfPoint2f();
        extractAndSortGoodMatchPoints(keyPoints1, keyPoints2, goodMatches, srcGoodSortedPoints, dstGoodSortedPoints);
        this.srcSortedGoodPoints = srcGoodSortedPoints;
        this.dstSortedGoodPoints = dstGoodSortedPoints;


        //// draw key points and good matches
        Mat outImg = new Mat(img1.rows(), img1.cols() * 2, img1.type());
        Features2d.drawMatches2(img1, keyPoints1, img2, keyPoints2, goodMatches, outImg);


        Highgui.imwrite("images" +
                "\\SylvainJpg\\S00b.jpg", outImg);


    }

    public Mat drawMatchesAndKeyPoints(){
        Mat outImg = new Mat(img1.rows(), img1.cols() * 2, img1.type());
        Features2d.drawMatches2(img1, this.srcKeyPoints, img2, this.dstKeyPoints, this.matches, outImg);

        return outImg;

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

    private void computeDescriptors(MatOfKeyPoint keyPoints1, MatOfKeyPoint keyPoints2, Mat descriptors1, Mat descriptors2) {
        DescriptorExtractor surfExtractor = DescriptorExtractor.create(DescriptorExtractor.SIFT);
        surfExtractor.compute(getImg1(), keyPoints1, descriptors1);
        surfExtractor.compute(getImg2(), keyPoints2, descriptors2);
    }

    private void detectKeyPoints(MatOfKeyPoint keyPoints1, MatOfKeyPoint keyPoints2) {
        FeatureDetector surfDetector = FeatureDetector.create(FeatureDetector.SIFT);
        surfDetector.detect(getImg1(), keyPoints1);
        surfDetector.detect(getImg2(), keyPoints2);
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
}
