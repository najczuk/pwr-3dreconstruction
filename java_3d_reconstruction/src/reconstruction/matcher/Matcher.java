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
import reconstruction.point.PointHelpers;

import java.util.ArrayList;
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
    //descriptors
    Mat descriptors1, descriptors2;
    //mateches
    List<MatOfDMatch> matches1, matches2;
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
        List<DMatch> goodMatches = symTest();
        //FUNDAMENTAL MATRIX TEST AND RECALCULATION
        //points for fundamental matrix
        MatOfPoint2f points1 = new MatOfPoint2f();
        MatOfPoint2f points2 = new MatOfPoint2f();
        PointHelpers.convertKeyPointsToMatOfPoint2f(getKeyPoints1(),getKeyPoints2(),goodMatches,points1,points2);


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

        getMatcher().knnMatch(getDescriptors1(),getDescriptors2(), matches1, 2);
        getMatcher().knnMatch(getDescriptors2(),getDescriptors1(), matches2, 2);

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
                if(match.rows()>1){
                    ratio = getDistanceFromKNNMatch(match,0)/getDistanceFromKNNMatch(match,1);
                    if(ratio>this.ratio){
                        it.remove();
                    }
                } else{
                    it.remove();
                }
        }

    }
    private List<DMatch> symTest(){
        MatOfDMatch match1,match2;
        Iterator<MatOfDMatch> it2;
        double queryIdx1,trainIdx1,queryIdx2,trainIdx2;
        List<DMatch> symMatches = new ArrayList<DMatch>();
        Iterator<MatOfDMatch> it1 = getMatches1().iterator();
        while (it1.hasNext()){
            match1 = it1.next();
            queryIdx1 = PointHelpers.getQueryIdxFromMatOfDMatch(match1);
            trainIdx1 = PointHelpers.getTrainIdxFromMatOfDMatch(match1);

            it2 = getMatches2().iterator();
            while (it2.hasNext()){
//                System.out.println("b");
                match2=it2.next();
                queryIdx2 = PointHelpers.getQueryIdxFromMatOfDMatch(match2);
                trainIdx2 = PointHelpers.getTrainIdxFromMatOfDMatch(match2);

                if(queryIdx1==trainIdx2 && queryIdx2==trainIdx1){
                    System.out.println(match1.dump());
                    System.out.println("--");
                    System.out.println(match2.dump());
                    symMatches.add(new DMatch((int)queryIdx1,(int)trainIdx1,(float)getDistanceFromKNNMatch(match1,0)));
                    break;
                }

            }
        }
        return symMatches;
    }

    private void ransacTest(MatOfPoint2f points1,MatOfPoint2f points2){
        Mat mask = new Mat();

        Mat fundamentalMat = Calib3d.findFundamentalMat(points1,points2,Calib3d.RANSAC,epipolarDistance,
                ransacConfidence,mask);
        
    }

    public void printMatches() {
        System.out.println("dd");
        MatOfDMatch matOfDMatch;
        for (int i = 0; i < getMatches2().size(); i++) {

            matOfDMatch = getMatches2().get(i);

            System.out.println(i + " " + matOfDMatch.dump());
            System.out.println(PointHelpers.getQueryIdxFromMatOfDMatch(matOfDMatch) + " " + PointHelpers.getTrainIdxFromMatOfDMatch(matOfDMatch));
            System.out.println(getDistanceFromKNNMatch(matOfDMatch,0) + " " + getDistanceFromKNNMatch(matOfDMatch,1)
                    + " " + getDistanceFromKNNMatch(matOfDMatch,0)/getDistanceFromKNNMatch(matOfDMatch,1));
        }
        System.out.println(getMatches1().size());
    }

    private double getDistanceFromKNNMatch(MatOfDMatch matOfDMatch, int neighbourIndex){
        return matOfDMatch.get(neighbourIndex,0)[3];
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


}
