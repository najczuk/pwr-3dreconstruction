package reconstruction.point;

import org.opencv.core.*;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.KeyPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: Adrian
 * Date: 1/6/2015
 * Time: 00:17
 */
public class Helpers {


    public static void sortedKeyPointsToMatOfPoint2f(MatOfKeyPoint srcPoints, MatOfKeyPoint dstPoints,
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

    public static MatOfPoint2f keyPointsToMatOfPoint2f(MatOfKeyPoint keyPoints){


        List<Point> pointsList = new ArrayList<Point>();
        KeyPoint[] kpArr = keyPoints.toArray();
        for (int i = 0; i < kpArr.length; i++) {
           pointsList.add(kpArr[i].pt);
        }
        MatOfPoint2f points = new MatOfPoint2f();
        points.fromList(pointsList);

        return points;
    }

    public static List<Point> keyPointsToPointList(MatOfKeyPoint keyPoints){


        List<Point> pointsList = new ArrayList<Point>();
        KeyPoint[] kpArr = keyPoints.toArray();
        for (int i = 0; i < kpArr.length; i++) {
            pointsList.add(kpArr[i].pt);
        }

        return pointsList;
    }

    public static List<Point> matOfPoint2fToPointList(MatOfPoint2f matOfPoint2f){
        Point[] pointArr = matOfPoint2f.toArray();
        List<Point> pointList = new ArrayList<Point>(Arrays.asList(pointArr));
        return pointList;
    }
    public static List<Point3> matOfPoint2fToPoint3List(MatOfPoint2f matOfPoint2f){
        Point[] pointArr = matOfPoint2f.toArray();
        List<Point3> point3List = new ArrayList<Point3>();
        for (int i = 0; i < pointArr.length; i++) {
            Point clone = pointArr[i].clone();
            point3List.add(new Point3(clone.x,clone.y,1));
        }
        return point3List;
    }
    public static MatOfPoint2f pointsListToMatOfPoint2f(List<Point> points){
        MatOfPoint2f matOfPoint2f = new MatOfPoint2f();
        matOfPoint2f.fromList(points);
        return matOfPoint2f;
    }
    public static double distanceFromKNNMatch(MatOfDMatch matOfDMatch, int neighbourIndex) {
        return matOfDMatch.get(neighbourIndex, 0)[3];
    }

    public static double getQueryIdxFromMatOfDMatch(MatOfDMatch matOfDMatch) {
        return matOfDMatch.get(0, 0)[0];
    }

    public static double getTrainIdxFromMatOfDMatch(MatOfDMatch matOfDMatch) {
        return matOfDMatch.get(0, 0)[1];
    }

    public static Mat matFromArray(double[][] array){
        Mat mat = new Mat(array.length,array[0].length,CvType.CV_64F);
        for (int i = 0; i < array.length; i++) {

            mat.put(i, 0, array[i]);
        }
        return mat;
    }

    public static Mat multiplyMat(Mat mat1, Mat mat2){
        Mat result = Mat.zeros(mat1.rows(), mat2.cols(), mat1.type());
        Core.gemm(mat1,mat2,1,new Mat(),0,result,0);
        return result;
    }

}
