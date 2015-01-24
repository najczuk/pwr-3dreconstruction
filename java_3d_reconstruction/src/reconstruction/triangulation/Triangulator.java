package reconstruction.triangulation;

import org.opencv.core.*;
import reconstruction.point.Helpers;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

/**
 * User: Adrian
 * Date: 1/23/2015
 * Time: 01:28
 */
public class Triangulator {

    List<Point3> leftPoints, rightPoints;
    Mat p, p1;


    public Triangulator(MatOfPoint2f matchPoints1, MatOfPoint2f matchPoints2, Mat p, Mat p1) {
        leftPoints = Helpers.matOfPoint2fToPoint3List(matchPoints1);
        rightPoints = Helpers.matOfPoint2fToPoint3List(matchPoints2);
        this.p = p;
        this.p1 = p1;
        simpleTriangulation();

//        lsTriangulation(leftPoints.get(0),p,rightPoints.get(0),p1);
    }

    private void simpleTriangulation(){
        Mat tmp;
        try {
            PrintWriter printWriter = new PrintWriter("images/results/two.out");
            for (int i = 0; i < leftPoints.size(); i++) {
                tmp = lsTriangulation(leftPoints.get(i),p,rightPoints.get(i),p1);
                printWriter.println(tmp.get(0,0)[0]+" "+tmp.get(1,0)[0]+" "+tmp.get(2,0)[0]+ " 0 0 0");
            }
            printWriter.flush();
            printWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }

    private Mat lsTriangulation(Point3 u, Mat p, Point3 u1, Mat p1) {
        double[][] a =
                {{u.x * p.get(2, 0)[0] - p.get(0, 0)[0], u.x * p.get(2, 1)[0] - p.get(0, 1)[0], u.x * p.get(2, 3)[0] - p.get(0, 2)[0]},
                        {u.y * p.get(2, 0)[0] - p.get(1, 0)[0], u.y * p.get(2, 1)[0] - p.get(1, 1)[0], u.y * p.get(2, 2)[0] - p.get(1, 2)[0]},
                        {u1.x * p1.get(2, 0)[0] - p1.get(0, 0)[0], u1.x * p1.get(2, 1)[0] - p1.get(0, 1)[0], u1.x * p1.get(2, 3)[0] - p1.get(0, 2)[0]},
                        {u1.y * p1.get(2, 0)[0] - p1.get(1, 0)[0], u1.y * p1.get(2, 1)[0] - p1.get(1, 1)[0], u1.y * p1.get(2, 2)[0] - p1.get(1, 2)[0]}
                };
        double[] test =
                {u.x * p.get(2, 0)[0] - p.get(0, 0)[0], u.x * p.get(2, 1)[0] - p.get(0, 1)[0], u.x * p.get(2, 3)[0] - p.get(0, 2)[0],
                        u.y * p.get(2, 0)[0] - p.get(1, 0)[0], u.y * p.get(2, 1)[0] - p.get(1, 1)[0], u.y * p.get(2, 2)[0] - p.get(1, 2)[0],
                        u1.x * p1.get(2, 0)[0] - p1.get(0, 0)[0], u1.x * p1.get(2, 1)[0] - p1.get(0, 1)[0], u1.x * p1.get(2, 3)[0] - p1.get(0, 2)[0],
                        u1.y * p1.get(2, 0)[0] - p1.get(1, 0)[0], u1.y * p1.get(2, 1)[0] - p1.get(1, 1)[0], u1.y * p1.get(2, 2)[0] - p1.get(1, 2)[0]
                };

        double[][] b = {
                {-u.x * p.get(2, 3)[0] - p.get(0, 3)[0]},
                {-u.y * p.get(2, 3)[0] - p.get(1, 3)[0]},
                {-u1.x * p1.get(2, 3)[0] - p1.get(0, 3)[0]},
                {-u1.y * p1.get(2, 3)[0] - p1.get(1, 3)[0]}
        };

        Mat aMat = new Mat(4,3,CvType.CV_64F);
        Mat bMat = new Mat(4,1,CvType.CV_64F);
        Mat testMat = new Mat(4,3,CvType.CV_64F);

        aMat.put(0,0,a[0]);
        aMat.put(1,0,a[1]);
        aMat.put(2,0,a[2]);
        aMat.put(3,0,a[3]);

        bMat.put(0,0,b[0]);
        bMat.put(1,0,b[1]);
        bMat.put(2,0,b[2]);
        bMat.put(3,0,b[3]);
//        System.out.println("a matrix");
//        System.out.println(aMat.dump());
//        System.out.println("b matrix");
//        System.out.println(bMat.dump());

        Mat xMat = new Mat();
        Core.solve(aMat,bMat,xMat,Core.DECOMP_SVD);

//        System.out.println("x matrix");
//        System.out.println(xMat.dump());
        return xMat;
    }
}
