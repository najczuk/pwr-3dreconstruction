package pl.najczuk.pwr.ai.pic_recognition.picture;

import pl.najczuk.pwr.ai.pic_recognition.algorithms.NeighboursConsistensyAlgorithm;
import pl.najczuk.pwr.ai.pic_recognition.points.KeyPoint;
import pl.najczuk.pwr.ai.pic_recognition.points.KeyPointsPair;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: Adrian Najczuk
 * Date: 5/30/13
 * Time: 11:49 PM
 */
public class PicResearcher {
    Pic pic1, pic2;
    ArrayList<KeyPointsPair> keyPointPairs;

    public PicResearcher(Pic pic1, Pic pic2) {
        this.pic1 = pic1;
        this.pic2 = pic2;
        keyPointPairs = findPairedKeys();
    }

    public static void main(String[] args) {
        Pic pic1 = new Pic("/home/adrian/workspace/pwr/ai/picture_recognition/pics/ai_1.png.haraff.sift");
        Pic pic2 = new Pic("/home/adrian/workspace/pwr/ai/picture_recognition/pics/ai_2.png.haraff.sift");
        PicResearcher picResearcher = new PicResearcher(pic1, pic2);
        ArrayList<KeyPointsPair> keyPointsPair = picResearcher.keyPointPairs;
//        for(KeyPointsPair kpp : keyPointsPair){
//            System.out.print(kpp);
//            System.out.println();
//        }
        NeighboursConsistensyAlgorithm algorithm = new NeighboursConsistensyAlgorithm(keyPointsPair,30,10);
        algorithm.filterKeyPointPairs();

    }

    public ArrayList<KeyPointsPair> findPairedKeys() {
        ArrayList<KeyPointsPair> keyPointsPairs = new ArrayList<KeyPointsPair>();
        int firstPicKeyPointsNearestNeighboursIndexes[] = findNearestNeighboursIndexes(pic1.keyPoints, pic2.keyPoints);
        int secondPicKeyPointsNearestNeighboursIndexes[] = findNearestNeighboursIndexes(pic2.keyPoints, pic1.keyPoints);

        System.out.println(Arrays.toString(firstPicKeyPointsNearestNeighboursIndexes));
        System.out.println(Arrays.toString(secondPicKeyPointsNearestNeighboursIndexes));

        for (int i = 0; i < pic1.keyPoints.size(); i++) {
            if (i == secondPicKeyPointsNearestNeighboursIndexes[firstPicKeyPointsNearestNeighboursIndexes[i]]) {
                keyPointsPairs.add(new KeyPointsPair(pic1.keyPoints.get(i), pic2.keyPoints.get(firstPicKeyPointsNearestNeighboursIndexes[i])));
            }
        }
        System.out.println(keyPointsPairs.size());
//                for(KeyPointsPair kpp : keyPointsPairs){
//            System.out.print(kpp);
//            System.out.println();
//        }
        return keyPointsPairs;
    }

    private int[] findNearestNeighboursIndexes(ArrayList<KeyPoint> picKeyPoints, ArrayList<KeyPoint> othersPicKeyPoints) {
        int neighboursIndexes[] = new int[picKeyPoints.size()];
        for (int i = 0; i < picKeyPoints.size(); i++) {
            findNearestNeigbourIndex(picKeyPoints, othersPicKeyPoints, neighboursIndexes, i);
        }
//        System.out.println(Arrays.toString(neighboursIndexes));
        return neighboursIndexes;
    }

    private void findNearestNeigbourIndex(ArrayList<KeyPoint> picKeyPoints, ArrayList<KeyPoint> othersPicKeyPoints, int[] neighboursIndexes, int currentPointIndex) {
        double minimum = Double.MAX_VALUE;
        int neighbourIndex = 0;
        double distance;
        KeyPoint currentPicPoint = picKeyPoints.get(currentPointIndex);

        for (int i = 0; i < othersPicKeyPoints.size(); i++) {
            distance = 0;
            KeyPoint currentOthersPicPoint = othersPicKeyPoints.get(i);

            for (int j = 0; j < 128; j++) {
                distance += (currentOthersPicPoint.attributes[j] - currentPicPoint.attributes[j]) *
                        (currentOthersPicPoint.attributes[j] - currentPicPoint.attributes[j]);
                if (distance > minimum) {
                    break;
                }
            }
            if (distance < minimum) {
                minimum = distance;
                neighbourIndex = i;
            }

        }
        neighboursIndexes[currentPointIndex] = neighbourIndex;
    }


}
