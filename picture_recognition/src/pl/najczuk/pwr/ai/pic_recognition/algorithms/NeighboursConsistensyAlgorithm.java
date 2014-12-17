package pl.najczuk.pwr.ai.pic_recognition.algorithms;

import pl.najczuk.pwr.ai.pic_recognition.points.KeyPoint;
import pl.najczuk.pwr.ai.pic_recognition.points.KeyPointsPair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created with IntelliJ IDEA.
 * User: Adrian Najczuk
 * Date: 6/1/13
 * Time: 3:53 PM
 */
public class NeighboursConsistensyAlgorithm implements Algorithm {
    ArrayList<KeyPointsPair> keyPointsPairs;
    double tolerancy;
    int commonNeighboursLimit;

    public NeighboursConsistensyAlgorithm(ArrayList<KeyPointsPair> keyPointsPairs, double tolerancy, int commonNeighboursLimit) {
        this.keyPointsPairs = keyPointsPairs;
        this.tolerancy = tolerancy;
        this.commonNeighboursLimit = commonNeighboursLimit;
    }

    public ArrayList<KeyPointsPair> filterKeyPointPairs() {
        ArrayList<KeyPointsPair> filteredKeyPointPairs = new ArrayList<KeyPointsPair>();
        tolerancy *= 0.01;
        for (int i = 0; i < keyPointsPairs.size(); i++) {
            double res = commonNeighboursCount(keyPointsPairs.get(i));
            if (res >= tolerancy) {
                filteredKeyPointPairs.add(keyPointsPairs.get(i));
            }
        }
        System.out.println(filteredKeyPointPairs.size());

        return filteredKeyPointPairs;
    }

    private double commonNeighboursCount(KeyPointsPair keyPointsPair) {
        ArrayList<KeyPointsPair> commonNeighbours = new ArrayList<KeyPointsPair>();

        ArrayList<NeighbourKeyPoints> firstPointNeighbours = new ArrayList<NeighbourKeyPoints>();
        ArrayList<NeighbourKeyPoints> secondPointNeighbours = new ArrayList<NeighbourKeyPoints>();

        KeyPoint firstKeyPoint = keyPointsPair.getFirst();
        KeyPoint secondKeyPoint = keyPointsPair.getSecond();
        KeyPoint currentFirstKeyPoint, currentSecondKeyPoint;
        for (int i = 0; i < keyPointsPairs.size(); i++) {
            KeyPointsPair currentKeyPointsPair = keyPointsPairs.get(i);
            currentFirstKeyPoint = currentKeyPointsPair.getFirst();
            currentSecondKeyPoint = currentKeyPointsPair.getSecond();

            NeighbourKeyPoints firstKeyPointNeighbour = new NeighbourKeyPoints(firstKeyPoint,currentFirstKeyPoint,currentKeyPointsPair);
            NeighbourKeyPoints secondKeyPointNeighbour = new NeighbourKeyPoints(secondKeyPoint,currentSecondKeyPoint,currentKeyPointsPair);

            if (firstKeyPointNeighbour.distance != 0) {
                firstPointNeighbours.add(firstKeyPointNeighbour);
            }
            if (secondKeyPointNeighbour.distance != 0) {
                secondPointNeighbours.add(secondKeyPointNeighbour);
            }
        }


        Collections.sort(firstPointNeighbours, new NeighboursKeyPointsComparator());
        Collections.sort(secondPointNeighbours, new NeighboursKeyPointsComparator());

        for (int i = 0; i < commonNeighboursLimit; i++) {
            KeyPointsPair firstSortedKeyPointPair =  firstPointNeighbours.get(i).keyPointsPair;
            for (int j = 0; j < commonNeighboursLimit; j++) {
                if (firstSortedKeyPointPair==(secondPointNeighbours.get(j).keyPointsPair)) {
                    commonNeighbours.add(firstSortedKeyPointPair);
                    break;
                }
            }
        }
        if (commonNeighbours.size() > 0) {
            return ((double) commonNeighbours.size() / (double)commonNeighboursLimit);
        }
        return 0;  //To change body of created methods use File | Settings | File Templates.
    }

    class NeighbourKeyPoints {
        KeyPoint keyPoint, neighbour;
        KeyPointsPair keyPointsPair;
        double distance;

        NeighbourKeyPoints(KeyPoint keyPoint, KeyPoint neighbour, KeyPointsPair keyPointsPair) {
            this.keyPointsPair = keyPointsPair;
            this.neighbour = neighbour;
            this.keyPoint = keyPoint;
            this.distance = calculateDistance(keyPoint, neighbour);
        }

        private double calculateDistance(KeyPoint keyPoint, KeyPoint neighbour) {
            double distance =
                    Math.sqrt((keyPoint.x - neighbour.x) * (keyPoint.x - neighbour.x) + (keyPoint.y - neighbour.y) * (keyPoint.y - neighbour.y));
//            System.out.println(distance);
            return distance;
        }

    }

    class NeighboursKeyPointsComparator implements Comparator<NeighbourKeyPoints> {

        public int compare(NeighbourKeyPoints o1, NeighbourKeyPoints o2) {
            return (o1.distance > o2.distance ? 1 : (o1.distance == o2.distance ? 0 : -1));
        }
    }
}
