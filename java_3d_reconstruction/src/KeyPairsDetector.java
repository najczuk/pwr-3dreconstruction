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
                "\\SylvainJpg\\S00.jpg", Highgui.CV_LOAD_IMAGE_GRAYSCALE);
        Mat img2 = Highgui.imread("D:\\workspace\\pwr\\pwr-3dreconstruction\\java_3d_reconstruction\\images" +
                "\\SylvainJpg\\S01.jpg", Highgui.CV_LOAD_IMAGE_GRAYSCALE);

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
        FeatureDetector surfDetector = FeatureDetector.create(FeatureDetector.SURF);
        surfDetector.detect(getImg1(), keyPoints1);
        surfDetector.detect(getImg2(), keyPoints2);

        Mat descriptors1 = new Mat(), descriptors2 = new Mat();
        DescriptorExtractor surfExtractor = DescriptorExtractor.create(DescriptorExtractor.SURF);
        surfExtractor.compute(getImg1(), keyPoints1, descriptors1);
        surfExtractor.compute(getImg2(), keyPoints2, descriptors2);

        DescriptorMatcher descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
        ArrayList<MatOfDMatch> matches = new ArrayList<MatOfDMatch>();
        descriptorMatcher.knnMatch(descriptors1, descriptors2, matches, 1);

        MatOfPoint2f srcPoints = new MatOfPoint2f();
        MatOfPoint2f dstPoints = new MatOfPoint2f();
        convertUnsortedKeyPointsIntoPoint2f(keyPoints1, keyPoints2, matches, srcPoints, dstPoints);

        Mat mask = new Mat();
        Calib3d.findHomography(srcPoints,dstPoints,Calib3d.RANSAC,3,mask);



//        Mat fundamentalMat = Calib3d.findFundamentalMat(points1, points2, Calib3d.RANSAC, 1.0, 0.98, mask);

        List<MatOfDMatch> goodMatches = new ArrayList<MatOfDMatch>();
        getInliers(keyPoints1, keyPoints2, mask, matches, goodMatches);

//        convertSortedKeyPointsIntoPoint2f(filteredPoints1, filteredPoints2, points1, points2);
//        MatOfPoint2f goodPoints1 = new MatOfPoint2f();
//        MatOfPoint2f goodPoints2 = new MatOfPoint2f();
//        convertUnsortedKeyPointsIntoPoint2f(keyPoints1, keyPoints2, goodPoints1, goodPoints2);
//        Mat fundamentalMat = Calib3d.findFundamentalMat(points1, points2, Calib3d.FM_8POINT, 0.0, 0.0);

        Mat outImg = new Mat(img1.rows(), img1.cols() * 2, img1.type());
        Features2d.drawMatches2(img1, keyPoints1, img2, keyPoints2, goodMatches, outImg);
        Highgui.imwrite("D:\\workspace\\pwr\\pwr-3dreconstruction\\java_3d_reconstruction\\images" +
                "\\SylvainJpg\\S00a.jpg", outImg);


    }

    private void getInliers(MatOfKeyPoint keyPoints1, MatOfKeyPoint keyPoints2, Mat mask, List<MatOfDMatch>
            matches, List<MatOfDMatch> goodMatches) {


        for (int row = 0; row < mask.rows(); row++) {
            if (mask.get(row, 0)[0] == 1.0) {
                System.out.println(matches.get(row).get(0, 0)[0]);
                System.out.println(matches.get(row).get(0, 0)[1]);
                System.out.println(matches.get(row).dump());
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

        for(MatOfDMatch match:matches){
            pointsList1.add(kplist1.get((int)(match.get(0,0)[0])).pt);
            pointsList2.add(kplist2.get((int)(match.get(0,0)[1])).pt);
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
