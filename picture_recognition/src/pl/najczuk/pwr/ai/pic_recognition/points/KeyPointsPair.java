package pl.najczuk.pwr.ai.pic_recognition.points;

/**
 * Created with IntelliJ IDEA.
 * User: Adrian Najczuk
 * Date: 6/1/13
 * Time: 1:14 PM
 */
public class KeyPointsPair {
    KeyPoint keyPoint1, keyPoint2;

    public KeyPointsPair(KeyPoint keyPoint1, KeyPoint keyPoint2) {
        this.keyPoint1 = keyPoint1;
        this.keyPoint2 = keyPoint2;
    }

    public KeyPoint getFirst() {
        return keyPoint1;
    }

    public KeyPoint getSecond() {
        return keyPoint2;
    }

    @Override
    public String toString() {

        return "[("+keyPoint1.x+","+keyPoint1.y+"),("+keyPoint2.x+","+keyPoint2.y+")]";   //To change body of overridden methods use File | Settings | File Templates.
    }
}
