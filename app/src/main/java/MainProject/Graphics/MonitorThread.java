package MainProject.Graphics;

import MainProject.Utils.*;
import MainProject.NFTClasses.*;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static java.awt.Color.*;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

/*
 *  Class that manage the monitoring of a specific collection of nft,
 *  with a representation of that on a new panel added on the JTabbedPanel of the GUI.
 *  That new Panel will show the main information of the collection:
 *      -Image of the collection(linked to the market page)
 *      -Cheapest item of that collection(liked to it)
 *      -Floor Price monitor with custom delay
 *      -Volume in the last 24h and number of nft listed
 *      -Link to Twitter profile page, Discord channel and if its available also to the Web site
 *  Other than that we can stop and restart the monitor through the menu bar,
 *  and also add some price trigger so the application will automatically let us know when the floor price hit the
 *  desired price through a windows notification
 */

public class MonitorThread extends Thread {
    private JTextArea monitorTA;
    private JLabel volume24h;
    private JLabel listedCount;
    private JScrollPane scrollPane;
    private JPanel mainPanel;
    private JLabel newCheapLabel;
    private JLabel iconJL;
    private JLabel dsIcon;
    private JLabel twIcon;
    private JLabel wbIcon;
    private JSpinner spinner;
    private NFT_collection n;
    private Boolean stopRequested = false;
    private Boolean pauseRequested = false;
    private Double FP = 0.0;
    private final List<String[]> triggerList = new ArrayList<String[]>();
    private NFT_Object cheapest;
    private Integer Delay;
    private JScrollBar Y;


    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");

    public MonitorThread() {

    }

    public MonitorThread(JTabbedPane tabbedPane, NFT_collection n) {
        this.setName(n.getName() + "_thread");
        tabbedPane.add(n.getName(), mainPanel);
        SpinnerModel model = new SpinnerNumberModel(2, 0.5, 60, 0.1);
        spinner.setModel(model);
        this.n = n;

        Y = scrollPane.getVerticalScrollBar();
        iconJL.setToolTipText("https://magiceden.io/marketplace/" + n.getName());
        iconJL.setIcon(n.getCollectionPic());
        iconJL.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("https://magiceden.io/marketplace/" + n.getName()));
                } catch (IOException | URISyntaxException ex) {
                    System.out.println("[" + dtf.format(LocalDateTime.now()) + "] "+
                            "Errore nell'apertura del link: https://magiceden.io/marketplace/" + n.getName());
                }
            }
        });
        newCheapLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(cheapest.getMEurl()));
                } catch (IOException | URISyntaxException ex) {
                    System.out.println("[" + dtf.format(LocalDateTime.now()) + "] "+"Errore nell'apertura del link: " + cheapest.getMEurl());
                }
            }
        });

        if (!n.getDiscord().equals("null")) {
            dsIcon.setToolTipText(n.getDiscord());
            dsIcon.setIcon(new ImageIcon("ds.png"));
            dsIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        Desktop.getDesktop().browse(new URI(n.getDiscord()));
                    } catch (IOException | URISyntaxException ex) {

                        System.out.println("[" + dtf.format(LocalDateTime.now()) + "] "+"Errore nell'apertura del link: " + n.getDiscord());
                    }
                }
            });
        }
        if (!n.getTwitter().equals("null")) {
            twIcon.setToolTipText(n.getTwitter());
            twIcon.setIcon(new ImageIcon("tw.png"));
            twIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        Desktop.getDesktop().browse(new URI(n.getTwitter()));
                    } catch (IOException | URISyntaxException ex) {
                        System.out.println("[" + dtf.format(LocalDateTime.now()) + "] "+"Errore nell'apertura del link: " + n.getTwitter());
                    }
                }
            });
        }
        if (!n.getWebsite().equals("null")) {
            wbIcon.setToolTipText(n.getWebsite());
            wbIcon.setIcon(new ImageIcon("wb.png"));
            wbIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        Desktop.getDesktop().browse(new URI(n.getWebsite()));
                    } catch (IOException | URISyntaxException ex) {
                        System.out.println("[" + dtf.format(LocalDateTime.now()) + "] "+"Errore nell'apertura del link: " + n.getWebsite());
                    }
                }
            });
        }
    }

    public void RequestStop() {
        this.stopRequested = true;
    }

    public boolean isStopRequested() {
        return stopRequested;
    }

    public void restartRequested() {
        while (isStopRequested())
            ;
        this.start();
    }

    public void RequestPause() {
        pauseRequested = true;
    }

    public void RequestRestart() {
        pauseRequested = false;
    }

    public List<String[]> getTriggerList() {
        return triggerList;
    }

    public void setTriggerList(Double value, String str2) {
        if (value <= 0) {
            showMessageDialog(mainPanel, "invalid Trigger Value", "Error", ERROR_MESSAGE);
            return;
        }
        String str1 = String.valueOf(value);
        String[] in = {str1, str2};
        for (String[] str : triggerList) {
            if (str[0].equals(str1) && str[1].equals(str2))
                return;
        }
        triggerList.add(in);
    }

    @Override
    public void run() {
        Thread T = new Thread(() -> {
            // stampa del floorprice attuale e del volume nelle ultime 24h
            while (!stopRequested) {
                try {
                    String[] s = JSONParser.parseFromString(Unirest
                            .get("https://api-mainnet.magiceden.io/rpc/getCollectionEscrowStats/" +
                                    n.getName() +
                                    "?edge_cache=true")
                            .asString()
                            .getBody(), new String[]{"volume24hr", "listedCount"});
                    Integer v24 = (int) (Double.parseDouble(s[0]) / Math.pow(10, 9));
                    this.volume24h.setText("Volume in last 24h :" + v24 + " SOL");
                    this.listedCount.setText("Listed Count: " + s[1]);
                    sleep(60000);
                }catch (UnirestException | NullPointerException | InterruptedException e) {
                    System.out.println("Probably some error on me end about "+ this.getName()+" thread of "+n.getName()+" monitor");
                }

            }

        }, "volume");
        T.start();

        while (!stopRequested) {
            try {
                while (pauseRequested) {
                    // System.out.println("il thread "+ this.getName() + " è in pausa");
                    sleep(2000);
                }

                String[] response = JSONParser.parseFromString(Unirest.get(
                                "https://api-mainnet.magiceden.io/rpc/getListedNFTsByQueryLite?q=%7B%22%24match%22%3A%7B%22collectionSymbol%22%3A%22"
                                        + n.getName()
                                        + "%22%7D%2C%22%24sort%22%3A%7B%22takerAmount%22%3A1%7D%2C%22%24skip%22%3A0%2C%22%24limit%22%3A20%2C%22status%22%3A%5B%5D%7D")
                        .asString()
                        .getBody(), new String[]{"mintAddress", "price", "collectionName"});

                cheapest = new NFT_Object(response[0], response[1], response[2]);

                if (FP > Double.parseDouble(cheapest.getPrice())) {
                    newCheapLabel.setForeground(YELLOW);
                    newCheapLabel.setText(" New cheap item : " + cheapest.getObjName());

                    monitorTA.setForeground(YELLOW);
                    monitorTA.append("[" + dtf.format(LocalDateTime.now()) + "] " + n.getName()
                            + " FloorPrice Changed :" + cheapest.getPrice() + "\n");
                } else {
                    newCheapLabel.setForeground(MAGENTA);
                    newCheapLabel.setText(" cheapest item :" + cheapest.getObjName());

                    monitorTA.setForeground(WHITE);
                    monitorTA.append("[" + dtf.format(LocalDateTime.now()) + "] " + n.getName() + " FloorPrice :"
                            + cheapest.getPrice() + "\n");
                }

                FP = Double.valueOf(cheapest.getPrice());

                // controllo se il nuovo FP triggera qualche trigger
                for (Iterator<String[]> it = triggerList.iterator(); it.hasNext(); ) {
                    String[] str = it.next();
                    if (str[1].equals(GUI.DECREASE)) {
                        if (FP <= Double.parseDouble(str[0])) {
                            // showMessageDialog(mainPanel, "The collection "+n.getName()+" reached the
                            // desidered floorprice of "+ str[0]);
                            WindowsNotification.sendNotification(
                                    "The collection " + n.getName() + " reached the desidered floorprice of " + str[0]);
                            triggerList.remove(str);
                        }
                    } else {
                        if (FP >= Double.parseDouble(str[0])) {
                            // showMessageDialog(mainPanel, "The collection "+n.getName()+" reached the
                            // desidered floorprice of "+ str[0]);
                            WindowsNotification.sendNotification(
                                    "The collection " + n.getName() + " reached the desidered floorprice of " + str[0]);
                            triggerList.remove(str);
                        }
                    }
                }

                // set della scrollbar in fondo al range così segue la scritte che vengono
                // aggiunte dalla append

                Y.setValue(Y.getMaximum());
                System.out.println(String.valueOf( Double.parseDouble(spinner.getValue().toString()) * 1000));
                Delay = (int) ((double) spinner.getValue() * 1000);
                sleep(Delay);

            } catch (UnirestException | NullPointerException | InterruptedException
                     | ConcurrentModificationException ex) {
                System.out.println(ex.getStackTrace());
                System.out.println("[" + dtf.format(LocalDateTime.now()) + "] "+
                        "Probably something wrong on ME end about " + this.getName() + " on collection " + n.getName());
                try {
                    sleep(5000);
                } catch (InterruptedException e) {
                }
            }
        }
        stopRequested = false;
        System.out.println("[" + dtf.format(LocalDateTime.now()) + "] "+"thread stopped");
    }

    {
        // GUI initializer generated by IntelliJ IDEA GUI Designer
        // >>> IMPORTANT!! <<<
        // DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(6, 3, new Insets(0, 0, 0, 0), -1, -1));
        newCheapLabel = new JLabel();
        newCheapLabel.setEnabled(true);
        Font newCheapFont = this.$$$getFont$$$(null, -1, 14, newCheapLabel.getFont());
        if (newCheapFont != null)
            newCheapLabel.setFont(newCheapFont);
        newCheapLabel.setForeground(new Color(-3276545));
        newCheapLabel.setText("Here will be dropped the link of new cheap item for this collection");
        mainPanel.add(newCheapLabel, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        volume24h = new JLabel();
        volume24h.setForeground(new Color(-3276545));
        volume24h.setText("Volume in last 24h:--");
        mainPanel.add(volume24h,
                new GridConstraints(0, 0, 2, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(205, 13), null, 0, false));
        listedCount = new JLabel();
        listedCount.setForeground(new Color(-3276545));
        listedCount.setText("Listed Count:--");
        mainPanel.add(listedCount,
                new GridConstraints(0, 1, 2, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(206, 13), null, 0, false));
        iconJL = new JLabel();
        iconJL.setText("");
        mainPanel.add(iconJL,
                new GridConstraints(2, 0, 3, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                        false));
        twIcon = new JLabel();
        twIcon.setHorizontalAlignment(2);
        twIcon.setHorizontalTextPosition(0);
        twIcon.setText("");
        mainPanel.add(twIcon, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        scrollPane = new JScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(31);
        mainPanel.add(scrollPane,
                new GridConstraints(2, 1, 4, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null,
                        new Dimension(206, 17), null, 0, false));
        monitorTA = new JTextArea();
        monitorTA.setEditable(false);
        scrollPane.setViewportView(monitorTA);
        dsIcon = new JLabel();
        dsIcon.setHorizontalAlignment(2);
        dsIcon.setHorizontalTextPosition(10);
        dsIcon.setText("");
        mainPanel.add(dsIcon,
                new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                        false));
        wbIcon = new JLabel();
        wbIcon.setHorizontalAlignment(2);
        wbIcon.setHorizontalTextPosition(11);
        wbIcon.setText("");
        mainPanel.add(wbIcon,
                new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_SOUTH, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                        false));
        spinner = new JSpinner();
        spinner.setAutoscrolls(false);
        spinner.setEnabled(true);
        spinner.setOpaque(false);
        mainPanel.add(spinner,
                new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(30, 20), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Adjust Delay");
        mainPanel.add(label1, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null)
            return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(),
                size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize())
                : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}