package pl.najczuk.pwr.ai.pic_recognition.points;

/**
 * Created with IntelliJ IDEA.
 * User: Adrian Najczuk
 * Date: 5/30/13
 * Time: 10:13 PM
 */
public class KeyPoint {
    public int[] attributes;
    public int x,y;
    private KeyPoint neighbour;

    public KeyPoint(int[] attributes, int x, int y) {
        this.attributes = attributes;
        this.x = x;
        this.y = y;
    }

    public KeyPoint getNeighbour() {
        return neighbour;
    }

    public void setNeighbour(KeyPoint neighbour) {
        this.neighbour = neighbour;
    }
}
