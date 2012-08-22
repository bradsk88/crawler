import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;


public class CrawlButton extends JButton {

    /**
     * 
     */
    private static final long serialVersionUID = -4732559064410816367L;

    public CrawlButton(String name, final Interface parent, final WebCrawler crawler, final String filepath) {

        super(name);

        this.addActionListener(new ActionListener() {

            @Override
            public final void actionPerformed(ActionEvent e) {

                WebCrawler.init(filepath);

                final Thread t = new Thread() {

                    @Override
                    public final void run() {

                        parent.setStopFlag(false);
                        try {
                            crawler.start(parent.getAddress().getText(), parent, parent.getUsername().getText(), parent.getPassword().getPassword(), filepath);
                        } catch (TransformerConfigurationException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ParserConfigurationException e) {
                            e.printStackTrace();
                        } catch (TransformerFactoryConfigurationError e) {
                            e.printStackTrace();
                        } catch (TransformerException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        setEnabled(false);

                    }

                };
                t.start();
            }

        });

    }

}
