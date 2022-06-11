package MainProject;


import kong.unirest.Unirest;
import kong.unirest.UnirestException;


import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


import static java.awt.Color.*;

public class MonitorThread extends Thread {
    private JTextArea monitorTA;
    private JLabel volume24h;
    private JLabel listedCount;
    private JScrollPane scrollPane;
    private JPanel mainPanel;
    private JLabel newCheap;
    private JLabel iconJL;
    private JLabel dsIcon;
    private JLabel twIcon;
    private JLabel wbIcon;
    private JSpinner spinner;
    private NFT_collection n;
    private Boolean stopRequested = false;
    private Boolean pauseRequested = false;
    private Double FP = 0.0;

    private NFT_Object cheapest;
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
    private Integer Delay;


    private JScrollBar Y;

    public MonitorThread(){}

    public void MonitorThreadInit(JTabbedPane tabbedPane, NFT_collection n) {
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
                    System.out.println("Errore nell'apertura del link: https://magiceden.io/marketplace/" + n.getName());
                }
            }
        });

        newCheap.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(cheapest.getMEurl()));
                } catch (IOException | URISyntaxException ex) {
                    System.out.println("Errore nell'apertura del link: " + cheapest.getMEurl());
                }
            }
        });

        if (!n.getDiscord().isBlank()) {
            dsIcon.setToolTipText(n.getDiscord());
            dsIcon.setIcon(new ImageIcon("ds.png"));
            dsIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        Desktop.getDesktop().browse(new URI(n.getDiscord()));
                    } catch (IOException | URISyntaxException ex) {
                        System.out.println("Errore nell'apertura del link: " + n.getDiscord());
                    }
                }
            });
        }
        if (!n.getTwitter().isBlank()) {
            twIcon.setToolTipText(n.getTwitter());
            twIcon.setIcon(new ImageIcon("tw.png"));
            twIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        Desktop.getDesktop().browse(new URI(n.getTwitter()));
                    } catch (IOException | URISyntaxException ex) {
                        System.out.println("Errore nell'apertura del link: " + n.getTwitter());
                    }
                }
            });
        }
        if (!n.getWebsite().isBlank()) {
            wbIcon.setToolTipText(n.getWebsite());
            wbIcon.setIcon(new ImageIcon("wb.png"));
            wbIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        Desktop.getDesktop().browse(new URI(n.getWebsite()));
                    } catch (IOException | URISyntaxException ex) {
                        System.out.println("Errore nell'apertura del link: " + n.getWebsite());
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
        while (isStopRequested()) ;
        this.start();
    }

    public void RequestPause() {
        pauseRequested = true;
    }

    public void RequestRestart() {
        pauseRequested = false;
    }

    @Override
    public void run() {
        while (!stopRequested) {
            try {
                while (pauseRequested) {
                    //System.out.println("il thread "+ this.getName() + " è in pausa");
                    sleep(2000);
                }

                String[] s = JSONParser.parseFromString(Unirest.get("https://api-mainnet.magiceden.io/rpc/getCollectionEscrowStats/" +
                                n.getName() +
                                "?edge_cache=true")
                        .asString()
                        .getBody(), new String[]{"volume24hr", "listedCount"});

                Integer v24 = (int) (Double.parseDouble(s[0]) / Math.pow(10, 9));


                String[] s2 = JSONParser.parseFromString(Unirest.get("https://api-mainnet.magiceden.io/rpc/getListedNFTsByQueryLite?q=%7B%22%24match%22%3A%7B%22collectionSymbol%22%3A%22"
                                + n.getName()
                                + "%22%7D%2C%22%24sort%22%3A%7B%22takerAmount%22%3A1%7D%2C%22%24skip%22%3A0%2C%22%24limit%22%3A20%2C%22status%22%3A%5B%5D%7D")
                        .asString()
                        .getBody(), new String[]{"mintAddress", "price", "collectionName"});


                cheapest = new NFT_Object(s2[0], s2[1], s2[2]);

                if (FP > Double.parseDouble(cheapest.getPrice())) {
                    newCheap.setForeground(YELLOW);
                    newCheap.setText(" New cheap item :" + cheapest.getObjName());

                    monitorTA.setForeground(YELLOW);
                    monitorTA.append("[" + dtf.format(LocalDateTime.now()) + "] " + n.getName() + " FloorPrice Changed :" + cheapest.getPrice() + "\n");
                } else {
                    newCheap.setForeground(MAGENTA);
                    newCheap.setText(" cheapest item :" + cheapest.getObjName());
                    // System.out.println(dtf.format(now));
                    monitorTA.setForeground(WHITE);
                    monitorTA.append("[" + dtf.format(LocalDateTime.now()) + "] " + n.getName() + " FloorPrice :" + cheapest.getPrice() + "\n");

                }

                FP = Double.valueOf(cheapest.getPrice());

                //stampa del floorprice attuale e del volume nelle ultime 24h

                volume24h.setText("Volume in last 24h :" + String.valueOf(v24) + " SOL");
                this.listedCount.setText("Listed Count: " + s[1]);

                //set della scrollbar in fondo al range così segue la scritte che vengono aggiunte dalla append
                Y.setValue(Y.getMaximum());

                Delay = (int) ((double) spinner.getValue() * 1000);

                sleep(Delay);


            } catch (UnirestException | NullPointerException | InterruptedException ex) {
                System.out.println(ex.getMessage());
                System.out.println("Probably something wrong on ME end about " + this.getName() + " on collection " + n.getName());
                try {
                    sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        stopRequested = false;
        System.out.println("thread stopped");
    }
}