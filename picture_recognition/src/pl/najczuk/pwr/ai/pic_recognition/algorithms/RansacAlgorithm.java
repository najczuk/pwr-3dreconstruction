package pl.najczuk.pwr.ai.pic_recognition.algorithms;

import Jama.Matrix;
import pl.najczuk.pwr.ai.pic_recognition.points.KeyPointsPair;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: Adrian Najczuk
 * Date: 6/2/13
 * Time: 2:02 PM
 */
public class RansacAlgorithm implements Algorithm {

    ArrayList<KeyPointsPair> keyPointsPairs;
    int ransacIterations;
    double ransacTolerance;

    public RansacAlgorithm(ArrayList<KeyPointsPair> keyPointsPairs, int ransacIterations, double ransacTolerance) {
        this.keyPointsPairs = keyPointsPairs;
        this.ransacIterations = ransacIterations;
        this.ransacTolerance = ransacTolerance;
    }

    @Override
    public ArrayList<KeyPointsPair> filterKeyPointPairs() {
        ArrayList<KeyPointsPair> listaWynikow = new ArrayList<KeyPointsPair>();
        Matrix najlepszyModel = new Matrix(3, 3);
        int najlepszyWynik = 0, wynik = 0;
        double error = 0;
        Random r = new Random();
        for (int i = 0; i < ransacIterations; i++) {
            Matrix trans = null;
            do {
                    trans = findBestAffiniteTransformation(keyPointsPairs.get(r.nextInt(keyPointsPairs.size())),
                            keyPointsPairs.get(r.nextInt(keyPointsPairs.size())),
                            keyPointsPairs.get(r.nextInt(keyPointsPairs.size()))
                    );
            }
            while (trans == null);
            wynik = 0;
            for (int j = 0; j < keyPointsPairs.size(); j++) {
                error = calculateError(trans, keyPointsPairs.get(j));
                if (error < ransacTolerance) {
                    wynik++;
                }
            }
            if (wynik > najlepszyWynik) {
                najlepszyWynik = wynik;
                najlepszyModel = trans;
            }
        }
        for (int j = 0; j < keyPointsPairs.size(); j++) {
            error = calculateError(najlepszyModel, keyPointsPairs.get(j));
            if (error < ransacTolerance) {
                listaWynikow.add(keyPointsPairs.get(j));
            }
        }
        return listaWynikow;
    }

    private Matrix findBestAffiniteTransformation(KeyPointsPair p1, KeyPointsPair p2, KeyPointsPair p3) {
        double[][] array = {{p1.getFirst().x, p2.getFirst().x, p3.getFirst().x, 0., 0., 0.},
                {p1.getFirst().y, p2.getFirst().y, p3.getFirst().y, 0., 0., 0.},
                {1., 1., 1., 0., 0., 0.},
                {0., 0., 0., p1.getFirst().x, p2.getFirst().x, p3.getFirst().x},
                {0., 0., 0., p1.getFirst().y, p2.getFirst().y, p3.getFirst().y},
                {0., 0., 0., 1., 1., 1.}};
        Matrix m = new Matrix(array);
        double[][] array2 = {{p1.getSecond().x,
                p2.getSecond().x,
                p3.getSecond().x,
                p1.getSecond().y,
                p2.getSecond().y,
                p3.getSecond().y}};

        Matrix n = new Matrix(array2);
        double[][] pr = m.getArray();

        try {
            m = m.inverse();
            pr = m.getArray();
        } catch (Exception ex) {
            return null;
        }
        Matrix result = n.times(m);
        double[][] resultArray = {{result.get(0, 0), result.get(0, 1), result.get(0, 2)},
                {result.get(0, 3), result.get(0, 5), result.get(0, 5)},
                {0., 0., 1.}};
        result = new Matrix(resultArray);
        return result;
    }

    private double calculateError(Matrix m, KeyPointsPair p) {
        Matrix k = new Matrix(new double[]{p.getFirst().x, p.getFirst().y, 1.}, 1);
        Matrix g = k.times(m);
        double x = g.get(0, 0);
        double y = g.get(0, 1);
        return Math.sqrt((p.getSecond().x - x) * (p.getSecond().x - x) + (p.getSecond().y - y) * (p.getSecond().y - y));
    }
}
