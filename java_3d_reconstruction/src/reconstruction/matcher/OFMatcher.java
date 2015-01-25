package reconstruction.matcher;

import org.opencv.core.*;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.utils.Converters;
import org.opencv.video.Video;
import reconstruction.point.Helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * User: Adrian
 * Date: 1/15/2015
 * Time: 19:48
 */
public class OFMatcher extends Matcher{

    public OFMatcher(Mat img1, Mat img2) {
        super(img1, img2);
    }

    public void match() {

        this.detector = FeatureDetector.create(FeatureDetector.FAST);
        detect();

        MatOfPoint2f leftPointsMat = Helpers.keyPointsToMatOfPoint2f(keyPoints1);
        MatOfPoint2f rightPointsMat = new MatOfPoint2f();

        //calculate leftPoints movement
        MatOfByte status = new MatOfByte();
        MatOfFloat err = new MatOfFloat();
        Video.calcOpticalFlowPyrLK(img1, img2, leftPointsMat, rightPointsMat, status, err);

        List<Point> leftPoints = new ArrayList<Point>();
        List<Point> rightPoints = new ArrayList<Point>();
        Converters.Mat_to_vector_Point2f(leftPointsMat, leftPoints);
        Converters.Mat_to_vector_Point2f(rightPointsMat, rightPoints);

//        System.out.println(Arrays.deepToString(leftPoints.toArray()));

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
        Mat rightPointsFeatures = Helpers.keyPointsToMatOfPoint2f(keyPoints2).reshape(1, keyPoints2.rows());

//        System.out.println(rightPointsToFindMat.dump());
//        System.out.println(rightPointsFeatures.dump());

        DescriptorMatcher bfMatcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
        List<MatOfDMatch> bfMatches = new ArrayList<MatOfDMatch>();
        bfMatcher.radiusMatch(rightPointsToFindMat, rightPointsFeatures, bfMatches, 2.0f);

        List<DMatch> goodMatchesWithOriginalQueryIdx = ofRatioTest(bfMatches, rightPointsToFindBackIndex);
        this.goodMatches = goodMatchesWithOriginalQueryIdx;
        MatOfPoint2f points1 = new MatOfPoint2f();
        MatOfPoint2f points2 = new MatOfPoint2f();


        Helpers.sortedKeyPointsToMatOfPoint2f(getKeyPoints1(), getKeyPoints2(), getGoodMatches(), points1, points2);
        this.matchPoints1 = points1;
        this.matchPoints2 = points2;

        this.goodMatches = ransacTest(points1, points2, this.goodMatches);
        Helpers.sortedKeyPointsToMatOfPoint2f(getKeyPoints1(), getKeyPoints2(), getGoodMatches(), points1, points2);
        this.matchPoints1 = points1;
        this.matchPoints2 = points2;


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
                queryIdx = (int) Helpers.getQueryIdxFromMatOfDMatch(match);
                oldQueryIdx = originalIndexes.get(queryIdx);
                trainIdx = (int) Helpers.getTrainIdxFromMatOfDMatch(match);

                goodMatchesWithOriginalIndexes.add(new DMatch(
                        oldQueryIdx,
                        trainIdx,
                        (float) Helpers.distanceFromKNNMatch(match, 0)
                ));
            } else if (match.rows() > 1) {
                ratio = Helpers.distanceFromKNNMatch(match, 0) / Helpers.distanceFromKNNMatch(match, 1);
                if (ratio < 0.7) {
                    queryIdx = (int) Helpers.getQueryIdxFromMatOfDMatch(match);
                    oldQueryIdx = originalIndexes.get(queryIdx);
                    trainIdx = (int) Helpers.getTrainIdxFromMatOfDMatch(match);

                    goodMatchesWithOriginalIndexes.add(new DMatch(
                            oldQueryIdx,
                            trainIdx,
                            (float) Helpers.distanceFromKNNMatch(match, 0)
                    ));
                }
            }
        }
        System.out.println("Before ratio-test: " + matches.size() + " After: " + goodMatchesWithOriginalIndexes.size());
        return goodMatchesWithOriginalIndexes;

    }
}
