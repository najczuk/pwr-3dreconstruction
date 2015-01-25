package reconstruction.triangulation;

import org.opencv.core.*;
import reconstruction.geometry.CameraMatrices;
import reconstruction.point.Helpers;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * User: Adrian
 * Date: 1/23/2015
 * Time: 01:28
 */
public class Triangulator {

    List<Point3> leftPoints, rightPoints;
    Mat p, p1,k;


    public Triangulator(MatOfPoint2f matchPoints1, MatOfPoint2f matchPoints2, Mat p, Mat p1,Mat k) {
        leftPoints = Helpers.matOfPoint2fToPoint3List(matchPoints1);
        rightPoints = Helpers.matOfPoint2fToPoint3List(matchPoints2);
        this.p = p;
        this.p1 = p1;
        this.k = k;
        triangulatePoints(p,p1,k);
//        simpleTriangulation();
//        iterativeLLSTriangulation(leftPoints.get(0), p, rightPoints.get(0), p1);
//        lsTriangulation(leftPoints.get(0),p,rightPoints.get(0),p1);
    }

    private void simpleTriangulation() {
        Mat tmp;
        try {
            PrintWriter printWriter = new PrintWriter("images/results/two.out");
            for (int i = 0; i < leftPoints.size(); i++) {
                tmp = lsTriangulation(leftPoints.get(i), p, rightPoints.get(i), p1);
                printWriter.println(tmp.get(0, 0)[0] + " " + tmp.get(1, 0)[0] + " " + tmp.get(2, 0)[0] + " 0 0 0");
            }
            printWriter.flush();
            printWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }

    private void triangulatePoints(Mat p, Mat p1, Mat k) {
        double[][] p1HArr = {
                {p1.get(0, 0)[0], p1.get(0, 1)[0], p1.get(0, 2)[0], p1.get(0, 3)[0]},
                {p1.get(1, 0)[0], p1.get(1, 1)[0], p1.get(1, 2)[0], p1.get(1, 3)[0]},
                {p1.get(2, 0)[0], p1.get(2, 1)[0], p1.get(2, 2)[0], p1.get(2, 3)[0]},
                {0, 0, 0, 1}
        };

        Mat kInv = k.inv();

        Mat kP1 = CameraMatrices.multiplyMat(k, p1);
        for (int i = 0; i < leftPoints.size(); i++) {
            Point3 u = leftPoints.get(i);
            double[][] uArr = {{u.x} ,{u.y}, {u.z}};
            Mat um = CameraMatrices.multiplyMat(kInv,Helpers.matFromArray(uArr));

            Point3 u1 = rightPoints.get(i);
            double[][] u1Arr = {{u1.x} ,{u1.y}, {u1.z}};
            Mat u1m = CameraMatrices.multiplyMat(kInv,Helpers.matFromArray(u1Arr));

            Mat x = iterativeLLSTriangulation(u,p,u1,p1);

        }


    }


    //"Triangulation", Hartley, R.I. and Sturm, P., Computer vision and image understanding, 1997
    private Mat iterativeLLSTriangulation(Point3 u, Mat p, Point3 u1, Mat p1) {
        double wi = 1, wi1 = 1;
        Mat x = new Mat(4, 1, CvType.CV_64FC1);
        for (int i = 0; i < 10; i++) {
            Mat xH = lsTriangulation(u, p, u1, p1);
            x.put(0, 0, xH.get(0, 0));
            x.put(1, 0, xH.get(1, 0));
            x.put(2, 0, xH.get(2, 0));
            x.put(3, 0, 1.);

            //recalculate weights
            double p2x = CameraMatrices.multiplyMat(p.row(2), x).get(0, 0)[0];
            double p2x1 = CameraMatrices.multiplyMat(p1.row(2), x).get(0, 0)[0];

            //braking point
            wi = p2x;
            wi1 = p2x1;

            //reweight equations and solve
            double[][] a = {{(u.x * p.get(2, 0)[0] - p.get(0, 0)[0]) / wi, (u.x * p.get(2, 1)[0] - p.get(0, 1)[0]) / wi,
                    (u.x * p.get(2, 2)[0] - p.get(0, 2)[0]) / wi},
                    {(u.y * p.get(2, 0)[0] - p.get(1, 0)[0]) / wi, (u.y * p.get(2, 1)[0] - p.get(1, 1)[0]) / wi,
                            (u.y * p.get(2, 2)[0] - p.get(1, 2)[0]) / wi},
                    {(u1.x * p1.get(2, 0)[0] - p1.get(0, 0)[0]) / wi1, (u1.x * p1.get(2, 1)[0] - p1.get(0, 1)[0]) / wi1,
                            (u1.x * p1.get(2, 2)[0] - p1.get(0, 2)[0]) / wi1},
                    {(u1.y * p1.get(2, 0)[0] - p1.get(1, 0)[0]) / wi1, (u1.y * p1.get(2, 1)[0] - p1.get(1, 1)[0]) / wi1,
                            (u1.y * p1.get(2, 2)[0] - p1.get(1, 2)[0]) / wi1},};
            double[][] b = {{-(u.x * p.get(2, 3)[0] - p.get(0, 3)[0]) / wi},
                    {-(u.y * p.get(2, 3)[0] - p.get(1, 3)[0]) / wi},
                    {-(u1.x * p1.get(2, 3)[0] - p1.get(0, 3)[0]) / wi1},
                    {-(u1.y * p1.get(2, 3)[0] - p1.get(1, 3)[0]) / wi1}};

            Mat aMat = Helpers.matFromArray(a);
            Mat bMat = Helpers.matFromArray(b);
//            System.out.println(aMat.dump());
//            System.out.println(bMat.dump());
            Core.solve(aMat, bMat, xH, Core.DECOMP_SVD);
            x.put(0, 0, xH.get(0, 0));
            x.put(1, 0, xH.get(1, 0));
            x.put(2, 0, xH.get(2, 0));
            x.put(3, 0, 1.);
        }

        System.out.println(x.get(0,0)[0]*10000+" "+x.get(1,0)[0]*10000+" "+x.get(2,0)[0]*10000+" 0 0 0");


        return x; //TODO

    }

    private Mat lsTriangulation(Point3 u, Mat p, Point3 u1, Mat p1) {
        double[][] a =
                {{u.x * p.get(2, 0)[0] - p.get(0, 0)[0], u.x * p.get(2, 1)[0] - p.get(0, 1)[0], u.x * p.get(2, 3)[0] - p.get(0, 2)[0]},
                        {u.y * p.get(2, 0)[0] - p.get(1, 0)[0], u.y * p.get(2, 1)[0] - p.get(1, 1)[0], u.y * p.get(2, 2)[0] - p.get(1, 2)[0]},
                        {u1.x * p1.get(2, 0)[0] - p1.get(0, 0)[0], u1.x * p1.get(2, 1)[0] - p1.get(0, 1)[0], u1.x * p1.get(2, 3)[0] - p1.get(0, 2)[0]},
                        {u1.y * p1.get(2, 0)[0] - p1.get(1, 0)[0], u1.y * p1.get(2, 1)[0] - p1.get(1, 1)[0], u1.y * p1.get(2, 2)[0] - p1.get(1, 2)[0]}
                };

        double[][] b = {
                {-u.x * p.get(2, 3)[0] - p.get(0, 3)[0]},
                {-u.y * p.get(2, 3)[0] - p.get(1, 3)[0]},
                {-u1.x * p1.get(2, 3)[0] - p1.get(0, 3)[0]},
                {-u1.y * p1.get(2, 3)[0] - p1.get(1, 3)[0]}
        };

        Mat aMat = new Mat(4, 3, CvType.CV_64F);
        Mat bMat = new Mat(4, 1, CvType.CV_64F);

        aMat.put(0, 0, a[0]);
        aMat.put(1, 0, a[1]);
        aMat.put(2, 0, a[2]);
        aMat.put(3, 0, a[3]);

        bMat.put(0, 0, b[0]);
        bMat.put(1, 0, b[1]);
        bMat.put(2, 0, b[2]);
        bMat.put(3, 0, b[3]);

        Mat xMat = new Mat();
        Core.solve(aMat, bMat, xMat, Core.DECOMP_SVD);
        return xMat;
    }
}
