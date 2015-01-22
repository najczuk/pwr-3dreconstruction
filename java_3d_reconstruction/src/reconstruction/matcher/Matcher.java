package reconstruction.matcher;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.features2d.*;
import org.opencv.highgui.Highgui;
import org.opencv.utils.Converters;
import org.opencv.video.Video;
import reconstruction.point.PointHelpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * User: Adrian
 * Date: 1/6/2015
 * Time: 00:16
 */
public class Matcher {
    Mat img1, img2;
    //detector, descriptor, matcher
    FeatureDetector detector;
    DescriptorExtractor descriptor;
    DescriptorMatcher matcher;
    //keypoints
    MatOfKeyPoint keyPoints1, keyPoints2;
    MatOfPoint2f matchPoints1,matchPoints2;
    //descriptors
    Mat descriptors1, descriptors2;
    //mateches
    List<MatOfDMatch> matches1, matches2;
    List<DMatch> goodMatches;
    double ratio = 0.65;
    double epipolarDistance = 1.0;
    double ransacConfidence = 0.98;

    public Matcher(Mat img1, Mat img2) {
        this.img1 = img1;
        this.img2 = img2;
        this.detector = FeatureDetector.create(FeatureDetector.SURF);
        this.descriptor = DescriptorExtractor.create(DescriptorExtractor.SURF);
        this.matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);

        detect();
        descript();
        matchDescriptors();

        //clear matches
        ratioTest(getMatches1());
        ratioTest(getMatches2());
        List<DMatch> symMatches = symTest();
        //FUNDAMENTAL MATRIX TEST AND RECALCULATION
        //points for fundamental matrix
        MatOfPoint2f points1 = new MatOfPoint2f();
        MatOfPoint2f points2 = new MatOfPoint2f();
        PointHelpers.sortedKeyPointsToMatOfPoint2f(getKeyPoints1(), getKeyPoints2(), symMatches, points1, points2);
        this.goodMatches = ransacTest(points1, points2, symMatches);
        PointHelpers.sortedKeyPointsToMatOfPoint2f(getKeyPoints1(), getKeyPoints2(), getGoodMatches(), points1, points2);
        this.matchPoints1 = points1;
        this.matchPoints2 = points2;

    }

    public Mat drawMatchesAndKeyPoints(String path) {
        Mat outImg = new Mat(img1.rows(), img1.cols() * 2, img1.type());
        MatOfDMatch gm = new MatOfDMatch();
        gm.fromList(getGoodMatches());
        Features2d.drawMatches(getImg1(), getKeyPoints1(), img2, getKeyPoints2(), gm, outImg);
        Highgui.imwrite(path, outImg);
        return outImg;
    }

    private void detect() {
        MatOfKeyPoint keyPoints1 = new MatOfKeyPoint();
        MatOfKeyPoint keyPoints2 = new MatOfKeyPoint();

        getDetector().detect(getImg1(), keyPoints1);
        getDetector().detect(getImg2(), keyPoints2);

        this.keyPoints1 = keyPoints1;
        this.keyPoints2 = keyPoints2;

    }

    private void descript() {
        Mat descriptors1 = new Mat(), descriptors2 = new Mat();

        getDescriptor().compute(getImg1(), getKeyPoints1(), descriptors1);
        getDescriptor().compute(getImg2(), getKeyPoints2(), descriptors2);

        this.descriptors1 = descriptors1;
        this.descriptors2 = descriptors2;
    }

    private void matchDescriptors() {
        List<MatOfDMatch> matches1 = new ArrayList<MatOfDMatch>(), matches2 = new ArrayList<MatOfDMatch>();

        getMatcher().knnMatch(getDescriptors1(), getDescriptors2(), matches1, 2);
        getMatcher().knnMatch(getDescriptors2(), getDescriptors1(), matches2, 2);

        this.matches1 = matches1;
        this.matches2 = matches2;
        System.out.println(matches1.size());
    }

    private void ratioTest(List<MatOfDMatch> matches) {
        MatOfDMatch match;
        Iterator<MatOfDMatch> it = matches.iterator();
        double ratio;
        while (it.hasNext()) {
            match = it.next();
            if (match.rows() > 1) {
                ratio = getDistanceFromKNNMatch(match, 0) / getDistanceFromKNNMatch(match, 1);
                if (ratio > this.ratio) {
                    it.remove();
                }
            } else {
                it.remove();
            }
        }

    }

    private List<DMatch> symTest() {
        MatOfDMatch match1, match2;
        Iterator<MatOfDMatch> it2;
        double queryIdx1, trainIdx1, queryIdx2, trainIdx2;
        List<DMatch> symMatches = new ArrayList<DMatch>();
        Iterator<MatOfDMatch> it1 = getMatches1().iterator();
        while (it1.hasNext()) {
            match1 = it1.next();
            queryIdx1 = PointHelpers.getQueryIdxFromMatOfDMatch(match1);
            trainIdx1 = PointHelpers.getTrainIdxFromMatOfDMatch(match1);

            it2 = getMatches2().iterator();
            while (it2.hasNext()) {
//                System.out.println("b");
                match2 = it2.next();
                queryIdx2 = PointHelpers.getQueryIdxFromMatOfDMatch(match2);
                trainIdx2 = PointHelpers.getTrainIdxFromMatOfDMatch(match2);

                if (queryIdx1 == trainIdx2 && queryIdx2 == trainIdx1) {
                    System.out.println(match1.dump());
                    System.out.println("--");
                    System.out.println(match2.dump());
                    symMatches.add(new DMatch((int) queryIdx1, (int) trainIdx1, (float) getDistanceFromKNNMatch(match1, 0)));
                    break;
                }

            }
        }
        return symMatches;
    }

    private List<DMatch> ransacTest(MatOfPoint2f points1, MatOfPoint2f points2, List<DMatch> matches) {
        Mat mask = new Mat();

        Calib3d.findFundamentalMat(points1, points2, Calib3d.RANSAC, epipolarDistance, ransacConfidence, mask);
        List<DMatch> goodMatches = filterGoodMatches(matches, mask);
        return goodMatches;
    }

    private List<DMatch> filterGoodMatches(List<DMatch> matches, Mat mask) {
        List<DMatch> goodMatches = new ArrayList<DMatch>();
        for (int matchIndex = 0; matchIndex < mask.rows(); matchIndex++) {
            if (mask.get(matchIndex, 0)[0] == 1) {
                goodMatches.add(matches.get(matchIndex));
                System.out.println(matches.get(matchIndex));
            }
        }
        return goodMatches;
    }

    public void printMatches() {
        System.out.println("dd");
        MatOfDMatch matOfDMatch;
        for (int i = 0; i < getMatches2().size(); i++) {

            matOfDMatch = getMatches2().get(i);

            System.out.println(i + " " + matOfDMatch.dump());
            System.out.println(PointHelpers.getQueryIdxFromMatOfDMatch(matOfDMatch) + " " + PointHelpers.getTrainIdxFromMatOfDMatch(matOfDMatch));
            System.out.println(getDistanceFromKNNMatch(matOfDMatch, 0) + " " + getDistanceFromKNNMatch(matOfDMatch, 1)
                    + " " + getDistanceFromKNNMatch(matOfDMatch, 0) / getDistanceFromKNNMatch(matOfDMatch, 1));
        }
        System.out.println(getMatches1().size());
    }

    private double getDistanceFromKNNMatch(MatOfDMatch matOfDMatch, int neighbourIndex) {
        return matOfDMatch.get(neighbourIndex, 0)[3];
    }

    public void ofMatch() {


        MatOfPoint2f leftPointsMat = PointHelpers.keyPointsToMatOfPoint2f(keyPoints1);
        MatOfPoint2f rightPointsMat = new MatOfPoint2f();

        //calculate leftPoints movement
        MatOfByte status = new MatOfByte();
        MatOfFloat err = new MatOfFloat();
        Video.calcOpticalFlowPyrLK(img1, img2, leftPointsMat, rightPointsMat, status, err);

        List<Point> leftPoints = new ArrayList<Point>();
        List<Point> rightPoints = new ArrayList<Point>();
        Converters.Mat_to_vector_Point2f(leftPointsMat, leftPoints);
        Converters.Mat_to_vector_Point2f(rightPointsMat, rightPoints);

        System.out.println(Arrays.deepToString(leftPoints.toArray()));

        //filter high error points and keep original index
        List<Point> rightPointsToFind = new ArrayList<Point>();
        List<Integer> rightPointsToFindBackIndex = new ArrayList<Integer>();
        for (int i = 0; i < status.rows(); i++) {
            if (status.get(i, 0)[0] == 1 && err.get(i, 0)[0] < 12) {
                rightPointsToFindBackIndex.add(i);
                rightPointsToFind.add(rightPoints.get(i));
            }
        }

        //match rightPoints found by OF to its features
        Mat rightPointsToFindMat = Converters.vector_Point2f_to_Mat(rightPointsToFind).reshape(1, rightPointsToFind.size());
        Mat rightPointsFeatures = PointHelpers.keyPointsToMatOfPoint2f(keyPoints2).reshape(1, keyPoints2.rows());

//        System.out.println(rightPointsToFindMat.dump());
//        System.out.println(rightPointsFeatures.dump());

        DescriptorMatcher bfMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
        List<MatOfDMatch> bfMatches = new ArrayList<MatOfDMatch>();
        bfMatcher.radiusMatch(rightPointsToFindMat, rightPointsFeatures, bfMatches, 2.0f);

        for (MatOfDMatch match : bfMatches) {
            System.out.println(match.dump());
        }
        List<DMatch> goodMatchesWithOriginalQueryIdx = ofRatioTest(bfMatches, rightPointsToFindBackIndex);



    }

    private List<DMatch> ofRatioTest(List<MatOfDMatch> matches, List<Integer> originalIndexes) {
        List<DMatch> goodMatchesWithOriginalIndexes = new ArrayList<DMatch>();
        MatOfDMatch match;
        Iterator<MatOfDMatch> it = matches.iterator();
        double ratio;

        int queryIdx, trainIdx, oldQueryIdx;
        while (it.hasNext()) {
            match = it.next();
            if (match.rows() == 1) {
                queryIdx = (int) PointHelpers.getQueryIdxFromMatOfDMatch(match);
                oldQueryIdx = originalIndexes.get(queryIdx);
                trainIdx = (int) PointHelpers.getTrainIdxFromMatOfDMatch(match);
                System.out.println("here");

                goodMatchesWithOriginalIndexes.add(new DMatch(
                        oldQueryIdx,
                        trainIdx,
                        (float) PointHelpers.distanceFromKNNMatch(match, 0)
                ));
            } else if (match.rows() > 1) {
                ratio = PointHelpers.distanceFromKNNMatch(match, 0) / PointHelpers.distanceFromKNNMatch(match, 1);
                if (ratio <= ratio) {
                    queryIdx = (int) PointHelpers.getQueryIdxFromMatOfDMatch(match);
                    oldQueryIdx = originalIndexes.get(queryIdx);
                    trainIdx = (int) PointHelpers.getTrainIdxFromMatOfDMatch(match);

                    goodMatchesWithOriginalIndexes.add(new DMatch(
                            oldQueryIdx,
                            trainIdx,
                            (float) PointHelpers.distanceFromKNNMatch(match, 0)
                    ));
                }
            }
        }

        return goodMatchesWithOriginalIndexes;

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
