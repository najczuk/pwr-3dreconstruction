package reconstruction.matcher;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import reconstruction.point.Helpers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * User: Adrian
 * Date: 1/25/2015
 * Time: 14:59
 */
public class SparseMatcher extends Matcher{

    public SparseMatcher(Mat img1, Mat img2) {
        super(img1,img2);
        this.detector = FeatureDetector.create(FeatureDetector.SURF);
        this.descriptor = DescriptorExtractor.create(DescriptorExtractor.SURF);
        this.matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
    }

    public void match(){
        detect();
        descript();
        matchDescriptors();

        ratioTest(getMatches1());
        ratioTest(getMatches2());
        List<DMatch> symMatches = symTest();
        //FUNDAMENTAL MATRIX TEST AND RECALCULATION
        //points for fundamental matrix
        MatOfPoint2f points1 = new MatOfPoint2f();
        MatOfPoint2f points2 = new MatOfPoint2f();
        Helpers.sortedKeyPointsToMatOfPoint2f(getKeyPoints1(), getKeyPoints2(), symMatches, points1, points2);
        this.goodMatches = ransacTest(points1, points2, symMatches);
        Helpers.sortedKeyPointsToMatOfPoint2f(getKeyPoints1(), getKeyPoints2(), getGoodMatches(), points1, points2);
        this.matchPoints1 = points1;
        this.matchPoints2 = points2;

    }

    private void ratioTest(List<MatOfDMatch> matches) {
        MatOfDMatch match;
        Iterator<MatOfDMatch> it = matches.iterator();
        double ratio;
        while (it.hasNext()) {
            match = it.next();
            if (match.rows() > 1) {
                ratio = Helpers.getDistanceFromKNNMatch(match, 0) / Helpers.getDistanceFromKNNMatch(match, 1);
                if (ratio > 0.65) {
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
            queryIdx1 = Helpers.getQueryIdxFromMatOfDMatch(match1);
            trainIdx1 = Helpers.getTrainIdxFromMatOfDMatch(match1);

            it2 = getMatches2().iterator();
            while (it2.hasNext()) {
//                System.out.println("b");
                match2 = it2.next();
                queryIdx2 = Helpers.getQueryIdxFromMatOfDMatch(match2);
                trainIdx2 = Helpers.getTrainIdxFromMatOfDMatch(match2);

                if (queryIdx1 == trainIdx2 && queryIdx2 == trainIdx1) {
//                    System.out.println(match1.dump());
//                    System.out.println("--");
//                    System.out.println(match2.dump());
                    symMatches.add(new DMatch((int) queryIdx1, (int) trainIdx1, (float) Helpers.getDistanceFromKNNMatch
                            (match1, 0)));
                    break;
                }

            }
        }
        return symMatches;
    }




}
