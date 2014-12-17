package pl.najczuk.pwr.ai.pic_recognition.picture;

import pl.najczuk.pwr.ai.pic_recognition.points.KeyPoint;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created with IntelliJ IDEA.
 * User: Adrian Najczuk
 * Date: 5/30/13
 * Time: 10:15 PM
 */
public class Pic {
    String keyPointsFile;
    ArrayList<KeyPoint> keyPoints;

    public Pic(String keyPointsFile) {
        this.keyPointsFile = keyPointsFile;
        keyPoints = parseKeyPoints();
    }

//    public static void main(String[] args) {
//        Pic pic = new Pic("/home/adrian/workspace/pwr/ai/picture_recognition/pics/ai_1.png.haraff.sift");
//        for(KeyPoint kp : pic.keyPoints){
//            System.out.print(Arrays.toString(kp.attributes));
//            System.out.println();
//        }
//
//    }


    private ArrayList<KeyPoint> parseKeyPoints() {
        Scanner sc = null;
        try {
            sc = new Scanner(new File(keyPointsFile));
            ArrayList<KeyPoint> result = new ArrayList<KeyPoint>();

            sc.next(); //moves to number of nodes
            int numberOfNodes = sc.nextInt();

            while (sc.hasNext()) {
                int x,y;
                int[] attributes = new int[128];
                x = (int) (sc.nextDouble());
                y = (int) (sc.nextDouble());
                sc.next();
                sc.next();
                sc.next();  // skips elypsis attributes
                for (int i = 0; i < 128; i++) {
                    attributes[i] = sc.nextInt();
                }
                result.add(new KeyPoint(attributes, x, y));
            }


            return result;
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }
}
