package reconstruction.geometry;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.util.Arrays;

/**
 * User: Adrian
 * Date: 1/7/2015
 * Time: 21:17
 */
public class CameraMatrices {
    Mat fundamentalMat, essentialMat,kMat;
    Mat r1,r2,t1,t2;
    Mat p,p1;
    private static int T1 = 1, T2 = -1;

    public CameraMatrices(Mat fundamentalMat) {
        this.fundamentalMat = fundamentalMat;
        this.kMat = generateKMat();
        this.essentialMat = calculateEssentialMatrix();
        calculateCameraMatrices(); //initiates p and p1

    }

    public void calculateCameraMatrices() {
        extractRTfromEssentialMatrix();

        p = generatePMat();
        double[][] p1Arr = {{r1.get(0,0)[0],r1.get(0,1)[0],r1.get(0,2)[0],t2.get(0,0)[0]},
                {r1.get(1,0)[0],r1.get(1,1)[0],r1.get(1,2)[0],t2.get(1,0)[0]},
                {r1.get(2,0)[0],r1.get(2,1)[0],r1.get(2,2)[0],t2.get(2,0)[0]}};
        p1 = matFromArray(p1Arr);
    }

    public void extractRTfromEssentialMatrix() {
//        http://answers.opencv.org/question/30781/svd-on-android-sdk/
        Mat svd_u = new Mat(3, 3, getFundamentalMat().type());
        Mat svd_vt = new Mat(3, 3, getFundamentalMat().type());
        Mat svd_w = new Mat(3, 3, getFundamentalMat().type());

        Core.SVDecomp(getEssentialMat(), svd_w, svd_u, svd_vt, Core.SVD_MODIFY_A);

        Mat w = generateWMat();
        Mat z = generateZMat();
//        multiplyMat(multiplyMat(svd_u,generateWMat()),svd_vt).copyTo(r1);
        r1 = multiplyMat(multiplyMat(svd_u,generateWMat()),svd_vt);
        r2 = multiplyMat(multiplyMat(svd_u, generateZMat()), svd_vt);
        t1 = calculateT(svd_u, T1);
        t2 = calculateT(svd_u, T2);

    }

    private Mat calculateT(Mat svd_u, int tSign) {
        Mat t = new Mat(3, 1, svd_u.type());

        t.put(0, 0, tSign * svd_u.get(0, 2)[0]);
        t.put(1, 0, tSign * svd_u.get(1, 2)[0]);
        t.put(2, 0, tSign * svd_u.get(1, 2)[0]);

        return t;
    }

    public Mat calculateEssentialMatrix() {
        Mat fundamentalMat = getFundamentalMat();

        Mat essentialMat = multiplyMat(multiplyMat(getKMat().t(),getFundamentalMat()), getKMat());
//        Mat_<double> E = K.t() * F * K;

        return essentialMat;
    }

    public Mat multiplyMat(Mat mat1, Mat mat2){
//        System.out.println(mat1.dump());
//        System.out.println(mat2.dump());
        Mat result = Mat.zeros(mat1.rows(), mat2.cols(), mat1.type());
        Core.gemm(mat1,mat2,1,new Mat(),0,result,0);
        return result;
    }

    private Mat matFromArray(double[][] array){
        Mat mat = new Mat(array.length,array[0].length,getFundamentalMat().type());
        for (int i = 0; i < array.length; i++) {
            mat.put(i, 0, array[i]);
        }
        return mat;
    }

    private Mat generateKMat() {
        Mat camMat = new Mat(3, 3, getFundamentalMat().type());
        double camMatrixArray[][] = {{2779.8170435219467, 0., 1631.5}, {0., 2779.8170435219467, 1223.5}, {0., 0., 1.}};
        for (int i = 0; i < camMatrixArray.length; i++) {
            camMat.put(i, 0, camMatrixArray[i]);
        }
        return camMat;
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
        double wMatrixArray[][] = {{0, 1, 0}, {-1, 0, 0}, {0., 0., 0}}; // HZ 9.13
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

        Mat kpMat = multiplyMat(getKMat(),pMat);

        return kpMat;
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
}
