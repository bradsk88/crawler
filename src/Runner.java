import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


public class Runner {

    public static void main(String[] args) throws IOException {

        try {
            // Set System L&F
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        }
        catch (UnsupportedLookAndFeelException e) {
            // handle exception
        }
        catch (ClassNotFoundException e) {
            // handle exception
        }
        catch (InstantiationException e) {
            // handle exception
        }
        catch (IllegalAccessException e) {
            // handle exception
        }

        //get save folder first
        String filepath = showFileDialog();
        if (filepath == null) {
            System.exit(0);
        }

        Interface frame = new Interface(filepath);
        frame.setBounds(new Rectangle(new Dimension(400,500)));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

    }

    private static String showFileDialog() throws IOException {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select directory for saving booth");
        int returnVal = chooser.showOpenDialog(null);

        String filepath = "";
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            filepath = chooser.getSelectedFile().getCanonicalPath();
            File file = new File(filepath);
            if (!file.exists()) {
                JOptionPane.showMessageDialog(null, "Must create folder first!");
                return showFileDialog();
            }
        } else {

            JOptionPane.showMessageDialog(null, "No directory selected.\nShutting down application.");
            return null;
        }

        return filepath;
    }

}
