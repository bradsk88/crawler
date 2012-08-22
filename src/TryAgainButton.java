import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;


public class TryAgainButton extends JButton {

    /**
     * 
     */
    private static final long serialVersionUID = -4732559064410816367L;

    public TryAgainButton(String name, final Interface interf, final WebCrawler crawler) {

        super(name);

        this.addActionListener(new ActionListener() {

            @Override
            public final void actionPerformed(ActionEvent e) {

                final Thread t = new Thread() {

                    @Override
                    public final void run() {

                        interf.disableTryAgain();
                        interf.enableStopBut();
                        int attempts = 0;
                        while (attempts < 10 && interf.getFailedImages().size() > 0) {

                            if (interf.getStopFlag()) {

                                crawler.printErrMsg(interf, "Received stop command.\nImages remaining to download: " + interf.getFailedImages().size());

                            } else {
                                attempts++;
                                ArrayList<FailedImage> lastFailed = new ArrayList<>(interf.getFailedImages());
                                interf.clearFailedImages();
                                crawler.printFailDividerMsg(interf, "Download attempt " + attempts + "----");
                                for ( int i = 0; i < lastFailed.size(); i++) {

                                    try {
                                        crawler.saveImage(lastFailed.get(i), interf);
                                    } catch (IOException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }

                                }

                            }

                        }

                        if (attempts < 10) {


                            crawler.printSpecialMsg(interf, "Downloaded all images successfully");

                        } else {

                            interf.enableTryAgain();

                        }
                    }

                };
                t.start();
            }

        });

    }

}
