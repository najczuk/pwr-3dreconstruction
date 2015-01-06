package reconstruction.point;

import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.KeyPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Adrian
 * Date: 1/6/2015
 * Time: 00:17
 */
public class PointHelpers {


    public static void convertKeyPointsToMatOfPoint2f(MatOfKeyPoint srcPoints, MatOfKeyPoint dstPoints,
                                                      List<DMatch> matches, MatOfPoint2f srcFilteredMat, MatOfPoint2f dstFilteredMat) {
        KeyPoint[] srcArray = srcPoints.toArray();
        KeyPoint[] dstArray = dstPoints.toArray();
        ArrayList<Point> srcFilteredPoints = new ArrayList<Point>();
        ArrayList<Point> dstFilteredPoints = new ArrayList<Point>();

        for (DMatch match : matches) {
            srcFilteredPoints.add(srcArray[match.queryIdx].pt);
            dstFilteredPoints.add(dstArray[match.trainIdx].pt);
        }

        srcFilteredMat.fromList(srcFilteredPoints);
        dstFilteredMat.fromList(dstFilteredPoints);

    }

    public static double getQueryIdxFromMatOfDMatch(MatOfDMatch matOfDMatch) {
        return matOfDMatch.get(0, 0)[0];
    }

    public static double getTrainIdxFromMatOfDMatch(MatOfDMatch matOfDMatch) {
        return matOfDMatch.get(0, 0)[1];
    }
}
