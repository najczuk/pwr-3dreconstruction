package reconstruction.matcher;

import org.opencv.core.*;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.utils.Converters;
import org.opencv.video.Video;
import reconstruction.point.PointHelpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * User: Adrian
 * Date: 1/15/2015
 * Time: 19:48
 */
public class OFMatcher {
    Mat img1, img2;
    MatOfKeyPoint leftKeypoints, rightKeypoints;
    double RATIO = 0.7;

    public OFMatcher(Mat img1, Mat img2) {
        this.img1 = img1;
        this.img2 = img2;
        detectKeypoints(img1, img2);
    }

    private void detectKeypoints(Mat img1, Mat img2) {
        this.leftKeypoints = new MatOfKeyPoint();
        this.rightKeypoints = new MatOfKeyPoint();
        FeatureDetector detector = FeatureDetector.create(FeatureDetector.FAST);
        detector.detect(img1, leftKeypoints);
        detector.detect(img2, rightKeypoints);
    }

    public void match() {


        MatOfPoint2f leftPointsMat = PointHelpers.keyPointsToMatOfPoint2f(leftKeypoints);
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
        Mat rightPointsFeatures = PointHelpers.keyPointsToMatOfPoint2f(rightKeypoints).reshape(1, rightKeypoints.rows());

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
                if (ratio <= RATIO) {
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

}
