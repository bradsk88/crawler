import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

// A minimal Web Crawler written in Java
// Usage: From command line
//     java WebCrawler <URL> [N]
//  where URL is the url to start the crawl, and N (optional)
//  is the maximum number of pages to download.


public class WebCrawler {

    private static boolean initialized = false;

    private static int biggestBoothNo;

    public static void init(String filepath)
    {

        File f = new File(filepath + "/images");
        if (! f.exists()) {
            f.mkdir();
        }

        File f2 = new File(filepath + "/commentpics");
        if (! f2.exists()) {
            f2.mkdir();
        }

        File f3 = new File(filepath + "/xml");
        if (! f3.exists()) {
            f3.mkdir();
        }

        initialized = true;

    }

    public void start(String pageurl, final Interface interf, final String username, char[] password, final String filepath) throws TransformerConfigurationException, IOException, ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException, InterruptedException {

        if (username.equals("") || password.equals("")) {

            printErrMsg(interf, "Login error.  Check your username and password.\n");
            interf.enableCrawlBut();
            return;

        }

        //first try a normal crawl
        try {
            Connection.Response res = Jsoup.connect("http://dailybooth.com/auth")
                    .data("username", username, "password", String.copyValueOf(password))
                    .method(Method.POST)
                    .execute();
            @SuppressWarnings("unused")
            Document doc = res.parse();

            Map<String, String> cookies = res.cookies();
            interf.enableStopBut();
            crawlPage(pageurl, filepath, true, 0, interf, cookies);

            interf.enableStopBut();
            interf.setProgBarIndeterminate(true);
            int attempts = 0;
            while (attempts < 10 && interf.getFailedImages().size() > 0) {

                if (interf.getStopFlag() == true) {

                    printErrMsg(interf, "Received stop command.\nImages remaining to download: " + interf.getFailedImages().size());

                } else {
                    attempts++;
                    ArrayList<FailedImage> lastFailed = new ArrayList<>(interf.getFailedImages());
                    interf.clearFailedImages();
                    printFailDividerMsg(interf, "Download attempt " + attempts + "----");
                    for ( int i = 0; i < lastFailed.size(); i++) {

                        try {
                            saveImage(lastFailed.get(i), interf);
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }
                }

            }

            if (attempts < 10) {


                printSpecialMsg(interf, "Downloaded all images successfully");
                interf.setProgBarValue(0);
                interf.setProgBarIndeterminate(false);
                interf.disableTryAgain();
                interf.disableStoplBut();

            } else {

                interf.enableTryAgain();
                interf.setProgBarValue(0);
                interf.disableStoplBut();

            }

            Desktop desktop = null;
            // Before more Desktop API is used, first check
            // whether the API is supported by this particular
            // virtual machine (VM) on this particular host.
            if (Desktop.isDesktopSupported()) {
                desktop = Desktop.getDesktop();
            }
            try {
                desktop.open(new File(filepath));
            } catch (IOException e) {
            }

        }catch (java.net.SocketTimeoutException ex) {


            for (int i = 10; i > 0; i -= 5) {

                final int j = i;
                printErrMsg(interf, "Server timed out.  Pausing " + j + "\n");
                Thread.sleep(5000);

            }

            //try again
            start(pageurl, interf, username, password, filepath);

        }catch(IllegalArgumentException exx) {

            printErrMsg(interf, "URL was invalid.\n");

        }catch(Exception e) {

            e.printStackTrace();
            printErrMsg(interf, "Unidentifiable error.  Try re-entering your login info.\n");
            interf.enableCrawlBut();

        }

    }

    private void crawlPage(final String pageurl, String filepath, boolean veryFirst, final int tries, final Interface interf, Map<String, String> cookies)
            throws IOException, ParserConfigurationException, TransformerConfigurationException,
            TransformerFactoryConfigurationError, TransformerException, InterruptedException {

        if (interf.getStopFlag() == false) {

            if (initialized) {

                try {

                    String back_url_string = crawlOnePage(pageurl, filepath, veryFirst, tries, interf, cookies);

                    if (back_url_string == null) {

                        printInfoMsg(interf, "Finished!\n");
                        interf.setProgBarValue(0);
                        interf.enableTryAgain();

                    } else {

                        final String pageurl2 = "http://www.dailybooth.com" + back_url_string;
                        printInfoMsg(interf, "Going to " + pageurl2 + "\n");
                        crawlPage(pageurl2, filepath, false, 0, interf, cookies);

                    }

                } catch (java.net.SocketTimeoutException ex) {


                    for (int i = 10; i > 0; i -= 5) {

                        final int j = i;
                        printErrMsg(interf, "Server timed out.  Pausing " + j + "\n");
                        Thread.sleep(5000);

                    }

                    //try again
                    crawlPage(pageurl, filepath, veryFirst, 0, interf, cookies);

                }

            } else {

                throw new RuntimeException("File system not initialized");

            }
        } else {

            printErrMsg(interf, "Received stop instruction. \n" +
                    "Next page to crawl was: " + pageurl + "\n");
            interf.enableTryAgain();
            interf.enableCrawlBut();

        }

    }

    private String crawlOnePage(final String pageurl, String filepath, boolean veryFirst, final int tries, final Interface interf, Map<String, String> cookies)
            throws TransformerFactoryConfigurationError, IOException, ParserConfigurationException, TransformerException {

        Document doc = Jsoup.connect(pageurl)
                .cookies(cookies)
                .get();

        String htmltop = "<html>\n\t<head>\n\t\t<title>\n\t\t\t" + doc.title() + "\t\t</title>\n\t</head>\n\t<body><center>";

        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        org.w3c.dom.Document outDoc = docBuilder.newDocument();

        Elements numberElem = doc.getElementsByAttributeValueContaining("data-type", "picture");
        final String pagenumber = numberElem.get(0).attr("data-id");
        String number = pagenumber;
        org.w3c.dom.Element root = outDoc.createElement("booth");
        outDoc.appendChild(root);

        BufferedWriter out = new BufferedWriter(new FileWriter(filepath + "/" + pagenumber + ".html"));
        out.write(htmltop);

        org.w3c.dom.Element xnum = outDoc.createElement("pagenumber");
        xnum.appendChild(outDoc.createTextNode(pagenumber));
        root.appendChild(xnum);

        org.w3c.dom.Element xboothnum = outDoc.createElement("boothnumber");
        Element element = doc.getElementsByClass("recent_pictures").get(0);
        Elements elementsByClazz = element.getElementsByTag("li");

        String boothno = "UNKNOWN";

        for (Element element2 : elementsByClazz) {

            Element element3 = element2.getElementsByClass("right").get(0);
            Element element4 = element3.getElementsByTag("h3").get(0);
            Elements elementsByTag = element4.getElementsByTag("a");
            Elements correctLink = elementsByTag.get(0).getElementsByAttributeValueContaining("href", pagenumber);
            if (correctLink.size() == 1) {
                boothno = correctLink.text().substring(9);
            }

        }

        xboothnum.appendChild(outDoc.createTextNode(boothno));
        root.appendChild(xboothnum);

        out.append(boothno);

        if (veryFirst) {

            biggestBoothNo = Integer.parseInt(boothno);

        }

        final int barVal = (int) ((Double.parseDouble(boothno)/biggestBoothNo) * 100);

        final String crawlstatus = "Crawling booth #" + boothno + ":" + pagenumber + "\n";
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public final void run() {


                interf.setProgBarValue(barVal);
                interf.putInfoMsg(crawlstatus);

            }

        });

        Elements back = doc.getElementsByClass("arrow_back");

        String back_url_string = null;

        out.append("<div style = 'width: 640px;'>");

        if (back.size() == 1) {

            org.w3c.dom.Element backurl = outDoc.createElement("back_url");
            back_url_string = back.get(0).attr("href");
            backurl.appendChild(outDoc.createTextNode(back_url_string));
            root.appendChild(backurl);

            out.append("<br/><div style = 'float:left;'><a href = '" + back_url_string.split("/")[2] + ".html'>Prev</a></div>");
            out.newLine();

        }

        Elements forward = doc.getElementsByClass("arrow_forward");

        if (forward.size() == 1) {

            org.w3c.dom.Element forw_url = outDoc.createElement("forw_url");
            String forw_url_string = forward.get(0).attr("href");
            forw_url.appendChild(outDoc.createTextNode(forw_url_string));
            root.appendChild(forw_url);
            out.append("<br/><div style = 'float:right;'><a href = '" + forw_url_string.split("/")[2] + ".html'>Next</a></div>");
            out.newLine();

        }

        try {

            Elements image = doc.getElementsByAttributeValueContaining("src", "/pictures/large/");
            saveImage(new FailedImage(image.get(0).attr("src"), filepath + "/images/" + number + ".jpg"), interf);
            out.append("<br/><div style = 'width: 100%;'><img src = 'images/" + number + ".jpg'></div>");
            out.newLine();

            Element blurb = doc.getElementById("blurb");
            org.w3c.dom.Element xblurb = outDoc.createElement("blurb");
            xblurb.appendChild(outDoc.createTextNode(blurb.text()));
            root.appendChild(xblurb);

            out.append("<div align = 'left'><br/>" + blurb.text() + "</div>");
            out.newLine();

            org.w3c.dom.Element xcomments = outDoc.createElement("comments");

            Elements comments = doc.getElementsByAttribute("data-comment_id");
            for (Element comment : comments) {

                out.append("<div style = 'width:100%' align = left><hr/>");
                out.newLine();

                org.w3c.dom.Element xxComment = outDoc.createElement("comment");

                Elements elementsByClass = comment.getElementsByClass("action");
                Element element5 = elementsByClass.get(0);
                Elements elementsByTag2 = element5.getElementsByTag("a");
                Element element6 = elementsByTag2.get(0);
                String author = element6.text();
                org.w3c.dom.Element xAuthor = outDoc.createElement("author");
                xAuthor.appendChild(outDoc.createTextNode(author));
                xxComment.appendChild(xAuthor);
                out.append("<b>" + author + " commented<br/></b>");
                out.newLine();

                String commentNumber = comment.getElementsByClass("comment_number").get(0).getElementsByTag("a").get(0).attr("name").substring(1);
                org.w3c.dom.Element xNum = outDoc.createElement("number");
                xNum.appendChild(outDoc.createTextNode(commentNumber));
                xxComment.appendChild(xNum);

                Elements commentPic = comment.getElementsByAttributeValueContaining("src", "comments/medium");
                if (commentPic.size() == 1) {

                    org.w3c.dom.Element xHasPic = outDoc.createElement("haspic");
                    xHasPic.appendChild(outDoc.createTextNode("true"));
                    xxComment.appendChild(xHasPic);

                    saveImage(new FailedImage(commentPic.get(0).attr("src"), filepath + "/commentpics/" + commentNumber + ".jpg"), interf);
                    out.append("<img src= 'commentpics/" + commentNumber + ".jpg'/><br/>");
                    out.newLine();

                }


                String commentBody = "failed to parse";
                if ( (comment.getElementsByClass("comment_picture").size() == 1) || (comment.getElementsByClass("comment picture").size() == 1)) {

                    commentBody = comment.getElementsByTag("p").get(1).html();

                } else {
                    Elements ps = comment.getElementsByTag("p");
                    for (Element e : ps) {
                        if (e.hasClass("comment")) {
                            commentBody = e.html();
                        }
                    }

                }
                org.w3c.dom.Element xBody = outDoc.createElement("body");
                xBody.appendChild(outDoc.createTextNode(commentBody));
                xxComment.appendChild(xBody);
                out.append(commentBody);

                xcomments.appendChild(xxComment);
                out.append("</div>");

            }

            root.appendChild(xcomments);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            final File file = new File(filepath + "/xml/" + number + ".xml");

            out.append("</center></body></html>");
            out.close();

            printInfoMsg(interf, "Written to " + file.getAbsolutePath() + "\n");
            Result output = new StreamResult(file);
            Source input = new DOMSource(outDoc);

            transformer.transform(input, output);


        } catch (IndexOutOfBoundsException e) {

            printInfoMsg(interf, "Failed to parse " + pagenumber + "\n");

            if (tries < 5) {

                System.out.println("parse tries = " + tries);

                printInfoMsg(interf, "Trying again\n");

                try {
                    crawlOnePage(pageurl, filepath, false, tries + 1, interf, cookies);
                } catch (
                        IOException
                        | ParserConfigurationException
                        | TransformerFactoryConfigurationError
                        | TransformerException eg) {
                    // TODO Auto-generated catch block
                    eg.printStackTrace();
                }


            } else {

                printErrMsg(interf, "Could not parse. Aborting\n");
                printFailMsg(interf, pageurl + "\n");

            }

        }

        return back_url_string;

    }

    public void saveImage(FailedImage image, final Interface interf) throws IOException {

        printSpecialMsg(interf, "Found image.  Attempting to download.\n");
        saveImage(image, 0, interf);

    }

    public void saveImage(FailedImage image, int attempt,
            Interface interf) throws IOException {

        if (interf.getStopFlag() == false) {

            try {
                URL url = new URL(image.getImageUrl());
                InputStream is = url.openStream();
                OutputStream os = new FileOutputStream(image.getDestinationFile());

                byte[] b = new byte[2048];
                int length;

                while ((length = is.read(b)) != -1) {
                    os.write(b, 0, length);
                }

                is.close();
                os.close();
            } catch (IOException e) {
                if (attempt < 3 ) {

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    saveImage(image, attempt + 1, interf);

                } else {

                    printErrMsg(interf, "Could not download. Aborting\n");
                    printFailMsg(interf, image.getImageUrl() + "\n");
                    interf.getFailedImages().add(image);

                }
            }
        }
    }

    public final void printInfoMsg(final Interface interf, final String string) {

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public final void run() {

                interf.putInfoMsg(string);

            }

        });

    }

    public final void printErrMsg(final Interface interf, final String string) {

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public final void run() {

                interf.putErrorMsg(string);

            }

        });

    }

    public final void printFailMsg(final Interface interf, final String string) {

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public final void run() {

                interf.putFailMsg(string);

            }

        });

    }
    public void printFailDividerMsg(final Interface interf, final String string) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public final void run() {

                interf.putFailDividerMsg(string);

            }

        });

    }

    public final void printSpecialMsg(final Interface interf, final String string) {

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public final void run() {

                interf.putSpecialMsg(string);

            }

        });

    }


}
