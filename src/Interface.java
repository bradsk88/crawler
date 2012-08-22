import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;


public class Interface extends JFrame {

    /**
     * 
     */
    private static final long serialVersionUID = 6520895674755624910L;
    private final ArrayList<FailedImage> failedImages;
    private boolean stop = false;
    private final JTextField address;
    private final JTextField username;
    private final JPasswordField password;
    private JTextPane feed;
    private JTextPane fail;
    final JProgressBar progbar;
    final TryAgainButton tabutton;
    final JButton stopbutton;
    final CrawlButton button;

    public Interface(String filepath) {

        super("Dailybooth downloader");

        setIconImage(Toolkit.getDefaultToolkit()
                .getImage("icon.png"));
        showHelpMsg();

        JPanel mainpanel = new JPanel(new BorderLayout());
        this.add(mainpanel);
        final JScrollPane pane = buildFeedArea();
        final JScrollPane failpane = buildFailureArea();
        address = new JTextField();

        JPanel outputpanel = new JPanel(new GridLayout(0, 1));
        outputpanel.add(pane);
        outputpanel.add(failpane);
        mainpanel.add(outputpanel, BorderLayout.CENTER);

        JPanel loginpnl = new JPanel();
        JLabel uLabel = new JLabel("username");
        username = new JTextField();
        username.setColumns(10);
        JLabel pLabel = new JLabel("password");
        password = new JPasswordField();
        password.setColumns(10);
        loginpnl.add(uLabel);
        loginpnl.add(username);
        loginpnl.add(pLabel);
        loginpnl.add(password);

        JPanel addresspnl = new JPanel();
        JLabel addLabel = new JLabel("Address of newest booth");
        addresspnl.add(addLabel);
        address.setColumns(20);
        addresspnl.add(address);

        JPanel topPanel = new JPanel(new GridLayout(0, 1));
        topPanel.add(loginpnl);
        topPanel.add(addresspnl);
        mainpanel.add(topPanel, BorderLayout.NORTH);

        progbar = new JProgressBar();
        progbar.setOrientation(JProgressBar.VERTICAL);
        mainpanel.add(progbar, BorderLayout.WEST);

        failedImages = new ArrayList<>();
        WebCrawler crawler = new WebCrawler();

        button = new CrawlButton("Start Crawling", this, crawler, filepath);

        tabutton = new TryAgainButton("Try Downloading Images", this, crawler);
        tabutton.setEnabled(false);
        stopbutton = new JButton("Stop");
        stopbutton.addActionListener(new ActionListener() {

            @Override
            public final void actionPerformed(ActionEvent e) {

                stop = true;
                System.out.println(stop);

            }

        });
        stopbutton.setEnabled(false);

        JButton aboutBtn = new JButton("?");
        aboutBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                showHelpMsg();

            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(button);
        buttonPanel.add(tabutton);
        buttonPanel.add(stopbutton);
        buttonPanel.add(aboutBtn);

        mainpanel.add(buttonPanel, BorderLayout.SOUTH);

    }

    private JScrollPane buildFailureArea() {
        fail = new JTextPane();
        final JScrollPane failpane = new JScrollPane(fail,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        Style failstyle = fail.addStyle("err", null);
        StyleConstants.setForeground(failstyle, Color.red);
        StyleConstants.setItalic(failstyle, true);

        Style failstyle2 = fail.addStyle("div", null);
        StyleConstants.setForeground(failstyle2, Color.black);
        StyleConstants.setBold(failstyle2, true);

        StyledDocument styledDocument2 = fail.getStyledDocument();
        try {
            styledDocument2.insertString(styledDocument2.getLength(),
                    "Failed Downloads:\n",
                    styledDocument2.getStyle("err"));
        } catch (BadLocationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return failpane;
    }

    private JScrollPane buildFeedArea() {
        feed = new JTextPane();
        JScrollPane pane = new JScrollPane(feed,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        Style style = feed.addStyle("err", null);
        StyleConstants.setForeground(style, Color.red);
        StyleConstants.setItalic(style, true);

        Style stylen = feed.addStyle("norm", null);
        StyleConstants.setForeground(stylen, Color.black);

        Style stylem = feed.addStyle("img", null);
        StyleConstants.setForeground(stylem, Color.blue);
        return pane;
    }

    public void setStopFlag(boolean b) {

        stop = b;

    }

    public JTextField getAddress() {
        // TODO Auto-generated method stub
        return address;
    }

    public void putErrorMsg(String string) {

        StyledDocument styledDocument = feed.getStyledDocument();
        try {
            styledDocument.insertString(styledDocument.getLength(),
                    string,
                    styledDocument.getStyle("err"));
        } catch (BadLocationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        feed.select(styledDocument.getLength(), styledDocument.getLength());

    }

    public void putInfoMsg(String string) {

        StyledDocument styledDocument = feed.getStyledDocument();
        try {
            styledDocument.insertString(styledDocument.getLength(),
                    string,
                    styledDocument.getStyle("norm"));
        } catch (BadLocationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        feed.select(styledDocument.getLength(), styledDocument.getLength());

    }

    public void putSpecialMsg(String string) {

        StyledDocument styledDocument = feed.getStyledDocument();
        try {
            styledDocument.insertString(styledDocument.getLength(),
                    string,
                    styledDocument.getStyle("img"));
        } catch (BadLocationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        feed.select(styledDocument.getLength(), styledDocument.getLength());

    }

    public void putFailMsg(String string) {
        StyledDocument styledDocument = fail.getStyledDocument();
        try {
            styledDocument.insertString(styledDocument.getLength(),
                    string,
                    styledDocument.getStyle("err"));
        } catch (BadLocationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        fail.select(styledDocument.getLength(), styledDocument.getLength());

    }


    public void putFailDividerMsg(String string) {
        StyledDocument styledDocument = fail.getStyledDocument();
        try {
            styledDocument.insertString(styledDocument.getLength(),
                    string,
                    styledDocument.getStyle("div"));
        } catch (BadLocationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        fail.select(styledDocument.getLength(), styledDocument.getLength());

    }

    public void setProgBarValue(int i) {

        this.progbar.setValue(i);

    }

    public JTextField getUsername() {

        return username;

    }

    public JPasswordField getPassword() {

        return password;

    }

    public ArrayList<FailedImage> getFailedImages() {

        return failedImages;

    }


    public void clearFailedImages() {

        failedImages.clear();

    }

    public boolean getStopFlag() {
        return stop;
    }

    public void enableTryAgain() {

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public final void run() {

                tabutton.setEnabled(true);

            }

        });


    }

    public void disableTryAgain() {

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public final void run() {

                tabutton.setEnabled(false);

            }

        });

    }

    public void enableStopBut() {

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public final void run() {

                stopbutton.setEnabled(true);

            }

        });



    }

    public void disableStoplBut() {

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public final void run() {

                stopbutton.setEnabled(false);

            }

        });

    }

    public void enableCrawlBut() {

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public final void run() {

                button.setEnabled(true);

            }

        });



    }

    public void disableCrawlBut() {

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public final void run() {

                button.setEnabled(false);

            }

        });

    }

    public void setProgBarIndeterminate(boolean b) {

        progbar.setIndeterminate(b);

    }

    private void showHelpMsg() {

        JOptionPane.showMessageDialog(null, "" +
                "This program starts crawling at the address you enter.  It\n" +
                "then downloads all of the booth-relevant text from the page\n" +
                "as well as the main booth image and any image comments.  \n\n" +
                "" +
                "  After downloading the booth, it proceeds to the PREVIOUS\n" +
                "booth until it reaches your first.\n\n  " +
                "" +
                "  The Dailybooth servers seem to get \"weaker\" as you get\n" +
                "into older pictures, so expect the program to slow down\n" +
                "considerably for those.\n\n" +
                "" +
                "  Once the program has finished a complete sweep of your\n" +
                "Dailybooth account it will start retrying any failed image\n" +
                "downloads.  It will make 10 attempts, slowing down to allow\n" +
                "time for the DB servers to wake up.  If at the end of the\n" +
                "entire process there are still images that wont download, copy\n" +
                "them from the output window and save them somewhere on your\n" +
                "computer.  Although, I've noticed that every booth seems to have\n" +
                "about 2 images that simply \"don't work\".\n\n" +
                "" +
                "  You can leave the program running and it will automatically\n" +
                "do all it can to download you entire account to your local\n" +
                "harddrive.\n\n" +
                "" +
                "  This program does not send any information out.  The reason\n" +
                "you need to enter a password is so that this program is\n" +
                "compatible with private accounts.", "About This Program", JOptionPane.INFORMATION_MESSAGE);

    }

}
