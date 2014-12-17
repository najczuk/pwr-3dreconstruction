package pl.najczuk.pwr.ai.pic_recognition.algorithms;

import pl.najczuk.pwr.ai.pic_recognition.points.KeyPointsPair;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Adrian Najczuk
 * Date: 6/2/13
 * Time: 12:31 AM
 */
public interface Algorithm {
    public ArrayList<KeyPointsPair> filterKeyPointPairs();
}
