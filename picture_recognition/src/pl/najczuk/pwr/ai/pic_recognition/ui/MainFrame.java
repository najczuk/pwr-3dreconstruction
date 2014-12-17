package pl.najczuk.pwr.ai.pic_recognition.ui;

import pl.najczuk.pwr.ai.pic_recognition.algorithms.Algorithm;
import pl.najczuk.pwr.ai.pic_recognition.algorithms.NeighboursConsistensyAlgorithm;
import pl.najczuk.pwr.ai.pic_recognition.algorithms.RansacAlgorithm;
import pl.najczuk.pwr.ai.pic_recognition.picture.Pic;
import pl.najczuk.pwr.ai.pic_recognition.picture.PicResearcher;
import pl.najczuk.pwr.ai.pic_recognition.points.KeyPointsPair;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainFrame extends JFrame implements ActionListener {
    JLabel txt_1, txt_2, txt_3, txt_4;
    private Panel picPanel;
    private JPanel menuPanel;
    private JPanel buttonsPanel;
    private JButton pic1Button, pic2Button, processButton, drawFilteredPairs, drawKeyPairs;
    private JComboBox<String> algorithmCombo;
    private JTextField commonNeighbours;
    private JTextField distanceTolerance;
    private JTextField ransacIterations;
    private JTextField ransacTolerance;
    private BufferedImage firstImage, secondImage;
    private Algorithm alg;
    private ArrayList<KeyPointsPair> filteredPairs, rawKeyPairs;
    private Pic pic1, pic2;

    public MainFrame(int width, int lenght) {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Picture recognition");
        setSize(new Dimension(width, lenght));
        Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenDim.width - width) / 2, (screenDim.height - lenght) / 2);
        setResizable(true);
        setLayout(new BorderLayout());
        processButton = new JButton("Process");
        commonNeighbours = new JTextField(5);
        txt_1 = new JLabel("Common neighbours");
        distanceTolerance = new JTextField("", 5);
        txt_2 = new JLabel("Consistency tolerance");
        ransacIterations = new JTextField("", 5);
        txt_3 = new JLabel("No. of iterations");
        ransacTolerance = new JTextField("", 5);
        txt_4 = new JLabel("Ransac tolerance");
        commonNeighbours.setText("10");
        distanceTolerance.setText("30");
        ransacIterations.setText("300000");
        ransacTolerance.setText("6u0");
        pic1Button = new JButton("Load Picture 1");
        pic2Button = new JButton("Load Picture 2");
        drawKeyPairs = new JButton("Draw key pairs");
        drawFilteredPairs = new JButton("Draw filtered");
        String[] list = {"Ransac", "Neighbours consistency"};
        algorithmCombo = new JComboBox<String>(list);
        picPanel = new Panel();
        menuPanel = new JPanel();
        buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(3, 1));
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new GridLayout(2, 1));
        leftPanel.add(menuPanel);
        leftPanel.add(buttonsPanel);
        menuPanel.setPreferredSize(new Dimension(140, 400));

        menuPanel.add(pic1Button);
        menuPanel.add(pic2Button);

        menuPanel.add(algorithmCombo);
        menuPanel.setLayout(new GridLayout(15, 1));
        menuPanel.add(txt_1);
        menuPanel.add(commonNeighbours);
        menuPanel.add(txt_2);
        menuPanel.add(distanceTolerance);
        menuPanel.add(txt_3);
        menuPanel.add(ransacIterations);
        menuPanel.add(txt_4);
        menuPanel.add(ransacTolerance);
        buttonsPanel.add(processButton);
        buttonsPanel.add(drawKeyPairs);
        buttonsPanel.add(drawFilteredPairs);
        picPanel.setPreferredSize(new Dimension(1000, 800));
        JScrollPane scroll = new JScrollPane(picPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setPreferredSize(new Dimension(1500, 800));
        this.add(scroll, BorderLayout.CENTER);
        pack();

        this.add(leftPanel, BorderLayout.WEST);
//        this.add(menuPanel, BorderLayout.WEST);
        pic1Button.addActionListener(this);
        pic2Button.addActionListener(this);
        algorithmCombo.addActionListener(this);
        processButton.addActionListener(this);
        drawKeyPairs.addActionListener(this);
        drawFilteredPairs.addActionListener(this);
        setVisible(true);
        hideComponents();
    }

    public void actionPerformed(ActionEvent e) {
        JFileChooser fc = new JFileChooser("/home/adrian/workspace/pwr/ai/pics");
        fc.setFileFilter(new FileNameExtensionFilter("PNG picture", new String[]{"png"}));
        if (e.getSource() == algorithmCombo) {
            hideComponents();
        }
        if (e.getSource() == pic1Button) {
            fc.showOpenDialog(this);
            firstImage = loadImage(fc.getSelectedFile());
            pic1 = new Pic(fc.getSelectedFile().toString() + ".haraff.sift");
        }
        if (e.getSource() == pic2Button) {
            fc.showOpenDialog(this);
            secondImage = loadImage(fc.getSelectedFile());
            pic2 = new Pic(fc.getSelectedFile().toString() + ".haraff.sift");

            if (pic1 != null && pic2 != null) {
                PicResearcher picResearcher = new PicResearcher(pic1, pic2);
                rawKeyPairs = picResearcher.findPairedKeys();

            }
        }


        if (e.getSource() == processButton) {

            int usedAlg = algorithmCombo.getSelectedIndex();
            int commonNeighboursCount = Integer.parseInt(commonNeighbours.getText());
            int neighboursDistanceTolerance = Integer.parseInt(distanceTolerance.getText());
            int ransacIterations = Integer.parseInt(this.ransacIterations.getText());
            double maxError = Double.parseDouble(ransacTolerance.getText());
            picPanel.loadPics(firstImage, secondImage);
            picPanel.setPreferredSize(picPanel.getDimension());
            long startTime = System.currentTimeMillis();

            if (usedAlg != 0) {
                this.alg = new NeighboursConsistensyAlgorithm(rawKeyPairs, neighboursDistanceTolerance, commonNeighboursCount);
                filteredPairs = this.alg.filterKeyPointPairs();
            } else {
                this.alg = new RansacAlgorithm(rawKeyPairs, ransacIterations, maxError);
                filteredPairs = this.alg.filterKeyPointPairs();
            }
            long estimatedTime = System.currentTimeMillis() - startTime;
            System.out.println("Execution time: " + estimatedTime + " ms");
            repaint();
        }

        if (e.getSource() == drawKeyPairs) {
            picPanel.drawRaw();
            picPanel.drawConnections(rawKeyPairs);
            repaint();
        }

        if (e.getSource() == drawFilteredPairs) {
            picPanel.drawRaw();
            picPanel.drawConnections(filteredPairs);
            repaint();
        }
    }

    public void hideComponents() {
        if (algorithmCombo.getSelectedIndex() == 0) {
            menuPanel.remove(commonNeighbours);
            menuPanel.remove(distanceTolerance);
            menuPanel.remove(txt_1);
            menuPanel.remove(txt_2);


            menuPanel.add(txt_3);
            menuPanel.add(ransacIterations);
            menuPanel.add(txt_4);
            menuPanel.add(ransacTolerance);

        } else {
            menuPanel.add(txt_1);
            menuPanel.add(commonNeighbours);
            menuPanel.add(txt_2);
            menuPanel.add(distanceTolerance);

            menuPanel.remove(ransacIterations);
            menuPanel.remove(ransacTolerance);
            menuPanel.remove(txt_4);
            menuPanel.remove(txt_3);
        }
        repaint();
    }

    private BufferedImage loadImage(File file) {
        try {
            return ImageIO.read(file);
        } catch (IOException ex) {
            System.out.println("Wystąpił błąd podczas ładowania obrazu rastrowego");
            ex.printStackTrace();
        }
        System.out.println("Blad");
        return null;
    }
}
