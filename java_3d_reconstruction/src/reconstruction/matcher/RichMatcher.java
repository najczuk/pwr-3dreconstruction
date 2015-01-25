package reconstruction.matcher;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.features2d.*;
import org.opencv.highgui.Highgui;
import reconstruction.point.Helpers;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Adrian
 * Date: 1/25/2015
 * Time: 14:59
 */
public class RichMatcher extends Matcher {
    DescriptorExtractor extractor;

    static {
        System.loadLibrary("opencv_java2410");
    }
    public static void main(String[] args){
        Mat img1 = Highgui.imread("images/sf2/20150125_131251.jpg", Highgui.CV_LOAD_IMAGE_GRAYSCALE);
        Mat img2 = Highgui.imread("images/sf2/20150125_131259.jpg", Highgui.CV_LOAD_IMAGE_GRAYSCALE);

        Matcher matcher = new RichMatcher(img1, img2);
        matcher.match();
    }

    public RichMatcher(Mat img1, Mat img2) {
        super(img1, img2);
        this.detector = FeatureDetector.create(FeatureDetector.PYRAMID_FAST);
        this.descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        this.matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
        detect();
        descript();
        matchDescriptors();
    }

    @Override
    public void match() {
        List<DMatch> goodMatches = new ArrayList<DMatch>();
        List<DMatch> veryGoodMatches = new ArrayList<DMatch>();


        List<Integer> existingTrainIdx = new ArrayList<Integer>();
//        System.out.println(matches1.size());
        for (int i = 0; i < matches1.size(); i++) {
//            System.out.println(matches1.get(i).dump());

           DMatch match =  matches1.get(i).toArray()[0];
            int trainIdx = match.trainIdx;
            int queryIdx = match.queryIdx;
            goodMatches.add(match);
        }
        MatOfPoint2f points1 = new MatOfPoint2f();
        MatOfPoint2f points2 = new MatOfPoint2f();
        Helpers.sortedKeyPointsToMatOfPoint2f(getKeyPoints1(), getKeyPoints2(), goodMatches, points1, points2);
        this.goodMatches = ransacTest(points1, points2, goodMatches);
        Helpers.sortedKeyPointsToMatOfPoint2f(getKeyPoints1(), getKeyPoints2(), this.goodMatches, points1, points2);
        this.matchPoints1 = points1;
        this.matchPoints2 = points2;



    }

}
