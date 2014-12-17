package pl.najczuk.pwr.ai.pic_recognition;

import pl.najczuk.pwr.ai.pic_recognition.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e){
            System.out.println("Unable to load System Look and Feel");
        }

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {

                @SuppressWarnings("unused")
                MainFrame mainFrame = new MainFrame(1500, 1000);
            }
        });

    }
}
