package reconstruction.geometry;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import reconstruction.point.Helpers;

import java.util.Arrays;

/**
 * User: Adrian
 * Date: 1/7/2015
 * Time: 21:17
 */
public class CameraMatrices {
    Mat fundamentalMat, essentialMat, kMat, distortionCoefficentsMat;
    Mat r1, r2, t1, t2;
    Mat p, p1;
    Mat img1, img2;
    private static int T1 = 1, T2 = -1;

    public CameraMatrices(Mat fundamentalMat, Mat img1, Mat img2) {
        this.img1 = img1;
        this.img2 = img2;
        this.fundamentalMat = fundamentalMat;
        this.kMat = generateKMat();
        this.distortionCoefficentsMat = generateDistortionCoefficents();
        this.essentialMat = calculateEssentialMatrix();
        calculateCameraMatrices(); //initiates p and p1
    }

    public void calculateCameraMatrices() {
        extractRTfromEssentialMatrix();

        p = generatePMat();
        double[][] p1Arr = {{r1.get(0, 0)[0], r1.get(0, 1)[0], r1.get(0, 2)[0], t1.get(0, 0)[0]},
                {r1.get(1, 0)[0], r1.get(1, 1)[0], r1.get(1, 2)[0], t1.get(1, 0)[0]},
                {r1.get(2, 0)[0], r1.get(2, 1)[0], r1.get(2, 2)[0], t1.get(2, 0)[0]}};

        double[][] p2Arr = {{r1.get(0, 0)[0], r1.get(0, 1)[0], r1.get(0, 2)[0], t2.get(0, 0)[0]},
                {r1.get(1, 0)[0], r1.get(1, 1)[0], r1.get(1, 2)[0], t2.get(1, 0)[0]},
                {r1.get(2, 0)[0], r1.get(2, 1)[0], r1.get(2, 2)[0], t2.get(2, 0)[0]}};

        double[][] p3Arr = {{r2.get(0, 0)[0], r2.get(0, 1)[0], r2.get(0, 2)[0], t1.get(0, 0)[0]},
                {r2.get(1, 0)[0], r2.get(1, 1)[0], r2.get(1, 2)[0], t1.get(1, 0)[0]},
                {r2.get(2, 0)[0], r2.get(2, 1)[0], r2.get(2, 2)[0], t1.get(2, 0)[0]}};

        double[][] p4Arr = {{r2.get(0, 0)[0], r2.get(0, 1)[0], r2.get(0, 2)[0], t2.get(0, 0)[0]},
                {r2.get(1, 0)[0], r2.get(1, 1)[0], r2.get(1, 2)[0], t2.get(1, 0)[0]},
                {r2.get(2, 0)[0], r2.get(2, 1)[0], r2.get(2, 2)[0], t2.get(2, 0)[0]}};
        p1 = matFromArray(p4Arr);
        Mat p11 = matFromArray(p1Arr);
        Mat p12 = matFromArray(p2Arr);
        Mat p13 = matFromArray(p3Arr);
        Mat p14 = matFromArray(p4Arr);
        System.out.println("p1:\n" + p11.dump());
        System.out.println("p2\n:" + p12.dump());
        System.out.println("p3:\n" + p13.dump());
        System.out.println("p4:\n" + p14.dump());
        System.out.println("p:\n" + p.dump());
    }

    public void extractRTfromEssentialMatrix() {
//        http://answers.opencv.org/question/30781/svd-on-android-sdk/
        Mat svd_u = new Mat();
        Mat svd_vt = new Mat();
        Mat svd_w = new Mat();

        Core.SVDecomp(getEssentialMat(), svd_w, svd_u, svd_vt, Core.SVD_MODIFY_A);

        System.out.println("U:\n"+svd_u.dump());
        System.out.println("Vt:\n"+svd_vt.dump());
        System.out.println("W:\n"+svd_w.dump());


        Mat w = generateWMat();
        Mat z = generateZMat();
        r1 = multiplyMat(multiplyMat(svd_u, w), svd_vt);
        r2 = multiplyMat(multiplyMat(svd_u, z), svd_vt);
        t1 = calculateT(svd_u, T1);
        t2 = calculateT(svd_u, T2);
        System.out.println("r1:\n" + r1.dump());
        System.out.println("r2:\n" + r2.dump());
        System.out.println("t1:\n" + t1.dump());
        System.out.println("t2:\n" + t2.dump());

    }

    private Mat calculateT(Mat svd_u, int tSign) {
        Mat t = new Mat(3, 1, svd_u.type());

        t.put(0, 0, tSign * svd_u.get(0, 2)[0]);
        t.put(1, 0, tSign * svd_u.get(1, 2)[0]);
        t.put(2, 0, tSign * svd_u.get(2, 2)[0]);

        return t;
    }

    public Mat calculateEssentialMatrix() {
        Mat fundamentalMat = getFundamentalMat();

        Mat essentialMat = multiplyMat(multiplyMat(getKMat().t(), getFundamentalMat()), getKMat());
//        Mat_<double> E = K.t() * F * K;

        return essentialMat;
    }

    public static Mat multiplyMat(Mat mat1, Mat mat2) {
        Mat result = Mat.zeros(mat1.rows(), mat2.cols(), mat1.type());
        Core.gemm(mat1, mat2, 1, new Mat(), 0, result, 0);
        return result;
    }

    private Mat matFromArray(double[][] array) {
        Mat mat = new Mat(array.length, array[0].length, getFundamentalMat().type());
        for (int i = 0; i < array.length; i++) {
            mat.put(i, 0, array[i]);
        }
        return mat;
    }

    private Mat generateKMat() {
        Mat camMat = new Mat(3, 3, getFundamentalMat().type());
        double max = Math.max(img1.cols(), img2.rows());
        double camMatrixArray[][] = {{max, 0., img1.cols() / 2}, {0., max, img1.rows() / 2}, {0., 0., 1.}};
        for (int i = 0; i < camMatrixArray.length; i++) {
            camMat.put(i, 0, camMatrixArray[i]);
        }
        return camMat;
    }

    private Mat generateDistortionCoefficents() {
        Mat distortionCoefficents = new Mat(1, 6, getFundamentalMat().type());
        double arr[][] = {{6.6639995658161630e-002}, {6.2179022466885614e-001}, {0}, {0},
                {-3.0949152483289066e+000}};
        for (int i = 0; i < arr.length; i++) {
            distortionCoefficents.put(i, 0, arr[i]);
        }
        return distortionCoefficents;
    }

    public Mat generateWMat() {
        Mat wMat = new Mat(3, 3, getFundamentalMat().type());
        double wMatrixArray[][] = {{0, -1, 0}, {1, 0, 0}, {0., 0., 1.}}; // HZ 9.13
        for (int i = 0; i < wMatrixArray.length; i++) {
            wMat.put(i, 0, wMatrixArray[i]);
        }
        return wMat;
    }

    public Mat generateZMat() {
        Mat zMat = new Mat(3, 3, getFundamentalMat().type());
        double wMatrixArray[][] = {{0, 1, 0}, {-1, 0, 0}, {0., 0., 1.}}; // HZ 9.13
        for (int i = 0; i < wMatrixArray.length; i++) {
            zMat.put(i, 0, wMatrixArray[i]);
        }
        return zMat;
    }

    private Mat generatePMat() {
        Mat pMat = new Mat(3, 4, getFundamentalMat().type());
        double wMatrixArray[][] = {{1, 0, 0, 0}, {0, 1, 0, 0}, {0., 0., 1, 0}}; // HZ 9.13
        for (int i = 0; i < wMatrixArray.length; i++) {
            pMat.put(i, 0, wMatrixArray[i]);
        }
        return pMat;
    }

    public Mat getP() {
        return p;
    }

    public Mat getP1() {
        return p1;
    }

    public Mat getFundamentalMat() {
        return fundamentalMat;
    }

    public Mat getEssentialMat() {
        return essentialMat;
    }

    public Mat getKMat() {
        return kMat;
    }

    public Mat getDistortionCoefficentsMat() {
        return distortionCoefficentsMat;
    }
}
