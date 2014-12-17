package pl.najczuk.pwr.ai.pic_recognition.ui;


import pl.najczuk.pwr.ai.pic_recognition.points.KeyPointsPair;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;


public class Panel extends JPanel {

    private static final long serialVersionUID = 1L;
    private BufferedImage firstImage;
    private BufferedImage secondImage;
    private BufferedImage finalImage;

    public Panel() {
        setLayout(new BorderLayout());
    }

    public void loadPics(BufferedImage first, BufferedImage second) {
        if (first != null && second != null) {
            this.firstImage = first;
            this.secondImage = second;
            finalImage = new BufferedImage(firstImage.getWidth() * 2, firstImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics g = finalImage.getGraphics();
            g.drawImage(firstImage, 0, 0, null);
            g.drawImage(secondImage, first.getWidth(), 0, null);
        }
    }

    public void drawRaw() {
        finalImage = new BufferedImage(firstImage.getWidth() * 2, firstImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = finalImage.getGraphics();
        g.drawImage(firstImage, 0, 0, null);
        g.drawImage(secondImage, firstImage.getWidth(), 0, null);
    }

    public void drawConnections(ArrayList<KeyPointsPair> pairList) {
        Graphics g = finalImage.getGraphics();
        System.out.println("Filtered pairs: " + pairList.size());
        for (int i = 0; i < pairList.size(); i++) {
            Random r = new Random();
            KeyPointsPair p = pairList.get(i);
            g.setColor(new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255)));
            int x1 = p.getFirst().x;
            int y1 = p.getFirst().y;
            int x2 = p.getSecond().x;
            int y2 = p.getSecond().y;
            g.drawLine(x1, y1, x2 + firstImage.getWidth(), y2);
        }
    }

    public Dimension getDimension() {

        return new Dimension(finalImage.getWidth(), finalImage.getHeight());
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (finalImage != null) {
            g.drawImage(finalImage, 20, 20, null);
        }
    }
}
