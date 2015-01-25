package reconstruction.matcher;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.features2d.*;
import org.opencv.highgui.Highgui;
import org.opencv.utils.Converters;
import org.opencv.video.Video;
import reconstruction.point.Helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * User: Adrian
 * Date: 1/6/2015
 * Time: 00:16
 */
public abstract class Matcher {
    Mat img1, img2;
    //detector, descriptor, matcher
    FeatureDetector detector;
    DescriptorExtractor descriptor;
    DescriptorMatcher matcher;
    //keypoints
    MatOfKeyPoint keyPoints1, keyPoints2;
    MatOfPoint2f matchPoints1, matchPoints2;
    //descriptors
    Mat descriptors1, descriptors2;
    //mateches
    List<MatOfDMatch> matches1, matches2;
    List<DMatch> goodMatches;
    double epipolarDistance = 1.0;
    double ransacConfidence = 0.99;

    public Matcher(Mat img1, Mat img2) {
        this.img1 = img1;
        this.img2 = img2;

    }

    public abstract void match();


    public Mat drawMatchesAndKeyPoints(String path) {
        Mat outImg = new Mat(img1.rows(), img1.cols() * 2, img1.type());
        MatOfDMatch gm = new MatOfDMatch();
        gm.fromList(getGoodMatches());
        Features2d.drawMatches(getImg1(), getKeyPoints1(), img2, getKeyPoints2(), gm, outImg);
        Highgui.imwrite(path, outImg);
        return outImg;
    }
    protected void matchDescriptors() {
        List<MatOfDMatch> matches1 = new ArrayList<MatOfDMatch>(), matches2 = new ArrayList<MatOfDMatch>();

        getMatcher().knnMatch(getDescriptors1(), getDescriptors2(), matches1, 2);
        getMatcher().knnMatch(getDescriptors2(), getDescriptors1(), matches2, 2);

        this.matches1 = matches1;
        this.matches2 = matches2;
//        System.out.println(matches1.size());
    }


    protected List<DMatch> ransacTest(MatOfPoint2f points1, MatOfPoint2f points2, List<DMatch> matches) {
        Mat mask = new Mat();

        Calib3d.findFundamentalMat(points1, points2, Calib3d.RANSAC, 1, ransacConfidence, mask);
        List<DMatch> goodMatches = filterGoodMatches(matches, mask);

        System.out.println("Before ransac: " + matches.size() + " After: " + goodMatches.size());
        return goodMatches;
    }

    public static List<DMatch> filterGoodMatches(List<DMatch> matches, Mat mask) {
        List<DMatch> goodMatches = new ArrayList<DMatch>();
        for (int matchIndex = 0; matchIndex < mask.rows(); matchIndex++) {
            if (mask.get(matchIndex, 0)[0] == 1) {
                goodMatches.add(matches.get(matchIndex));
//                System.out.println(matches.get(matchIndex));
            }
        }

        return goodMatches;
    }

    protected void detect() {
        MatOfKeyPoint keyPoints1 = new MatOfKeyPoint();
        MatOfKeyPoint keyPoints2 = new MatOfKeyPoint();

        detector.detect(getImg1(), keyPoints1);
        detector.detect(getImg2(), keyPoints2);

        this.keyPoints1 = keyPoints1;
        this.keyPoints2 = keyPoints2;

    }
    protected void descript() {
        Mat descriptors1 = new Mat(), descriptors2 = new Mat();

        getDescriptor().compute(getImg1(), getKeyPoints1(), descriptors1);
        getDescriptor().compute(getImg2(), getKeyPoints2(), descriptors2);

        this.descriptors1 = descriptors1;
        this.descriptors2 = descriptors2;
    }


    public Mat getImg1() {
        return img1;
    }

    public Mat getImg2() {
        return img2;
    }

    public FeatureDetector getDetector() {
        return detector;
    }

    public DescriptorExtractor getDescriptor() {
        return descriptor;
    }

    public DescriptorMatcher getMatcher() {
        return matcher;
    }

    public MatOfKeyPoint getKeyPoints1() {
        return keyPoints1;
    }

    public MatOfKeyPoint getKeyPoints2() {
        return keyPoints2;
    }

    public Mat getDescriptors1() {
        return descriptors1;
    }

    public Mat getDescriptors2() {
        return descriptors2;
    }

    public List<MatOfDMatch> getMatches1() {
        return matches1;
    }

    public List<MatOfDMatch> getMatches2() {
        return matches2;
    }

    public List<DMatch> getGoodMatches() {
        return goodMatches;
    }

    public MatOfPoint2f getMatchPoints1() {
        return matchPoints1;
    }

    public MatOfPoint2f getMatchPoints2() {
        return matchPoints2;
    }
}
