package MainProject;

import com.formdev.flatlaf.FlatDefaultsAddon;
import com.google.gson.internal.LinkedTreeMap;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;

import static java.awt.Color.*;

public class MonitorThread extends Thread{
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
    private final NFT_collection n;
    private Boolean stopRequested=false;
    private Double FP=  0.0;
    //list no good couse too much
    private NFT_Object cheapest;
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
    private Integer Delay;


    private final JScrollBar Y;

    public void RequestStop(){
        this.stopRequested=true;
    }

    public boolean isStopRequested(){
        return stopRequested;
    }

    public MonitorThread(JTabbedPane tabbedPane, NFT_collection n) {
        tabbedPane.add(n.getName(), mainPanel);
        SpinnerModel model = new SpinnerNumberModel( 2, 0.5, 60, 0.1);
        spinner.setModel(model);
        this.n=n;

        Y = scrollPane.getVerticalScrollBar();
        iconJL.setToolTipText("https://magiceden.io/marketplace/"+n.getName());
        iconJL.setIcon(n.getCollectionPic());
        iconJL.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("https://magiceden.io/marketplace/"+n.getName()));
                } catch (IOException | URISyntaxException ex) {
                    System.out.println("Errore nell'apertura del link: https://magiceden.io/marketplace/"+n.getName());
                }
            }
        });

        newCheap.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(cheapest.getMEurl()));
                } catch (IOException | URISyntaxException ex) {
                    System.out.println("Errore nell'apertura del link: "+cheapest.getMEurl());
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
                        System.out.println("Errore nell'apertura del link: "+n.getDiscord());
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
                        System.out.println("Errore nell'apertura del link: "+n.getTwitter());
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
                        System.out.println("Errore nell'apertura del link: "+n.getWebsite());
                    }
            }
            });
        }

    }

    @Override
    public void run() throws UnirestException {


        while(!stopRequested){
            //request a magiceden per richiede i dati della collection e successiva elaborazione della risposta per ottenere il floor price
            JSONObject  obj =Unirest.get("https://api-mainnet.magiceden.io/rpc/getCollectionEscrowStats/"+n.getName()+"?edge_cache=true")
                    .asJson()
                    .getBody()
                    .getObject();

            Map<String, Object> M = obj.toMap();
            M = (Map<String, Object>)M.get("results");//la request ci ritorna una oggetto Linked

            double v24 = Double.parseDouble(M.get("volume24hr").toString()) / Math.pow(10, 9);

            JsonNode j = Unirest.get("https://api-mainnet.magiceden.io/rpc/getListedNFTsByQueryLite?q=%7B%22%24match%22%3A%7B%22collectionSymbol%22%3A%22"
                            + n.getName()
                            + "%22%7D%2C%22%24sort%22%3A%7B%22takerAmount%22%3A1%7D%2C%22%24skip%22%3A0%2C%22%24limit%22%3A2%2C%22status%22%3A%5B%5D%7D")
                    .asJson()
                    .getBody();
            if(j==null)continue;
            obj = j.getObject();

            ArrayList<LinkedTreeMap> arrTmp = (ArrayList) obj.toMap().get("results");
            LinkedTreeMap tmp = arrTmp.get(0);

            cheapest = new NFT_Object(tmp.get("mintAddress").toString(), tmp.get("price").toString(), tmp.get("collectionName").toString());

            if(FP>Double.parseDouble(cheapest.getPrice())){
                newCheap.setForeground(YELLOW);
                newCheap.setText(" New cheap item :"+cheapest.getObjName());

                monitorTA.setForeground(YELLOW);
                monitorTA.append("["+dtf.format(LocalDateTime.now())+"] "+n.getName()+" FloorPrice Changed :"+cheapest.getPrice()+"\n");
            }else{
                newCheap.setForeground(MAGENTA);
                newCheap.setText(" cheapest item :"+cheapest.getObjName());
               // System.out.println(dtf.format(now));
                monitorTA.setForeground(WHITE);
                monitorTA.append("["+dtf.format(LocalDateTime.now())+"] " + n.getName()+" FloorPrice :"+cheapest.getPrice()+"\n");

            }

            FP= Double.valueOf(cheapest.getPrice());

            //stampa del floorprice attuale e del volume nelle ultime 24h

            volume24h.setText("Volume in last 24h :"+ v24);
            this.listedCount.setText("Listed Count: " + M.get("listedCount").toString());

            //set della scrollbar in fondo al range così segue la scritte che vengono aggiunte dalla append
            Y.setValue(Y.getMaximum());

            Delay =   (int)((double)spinner.getValue() *1000);
            System.out.println(Delay);
            //dalay 2sec dalla prossima request
            try {
                sleep(Delay);
            } catch (InterruptedException ex) {
                System.out.println(("Errore interno alla sleep del MOnitorThread della collection"+n.getName()));
            }

        }
        stopRequested=false;
        System.out.println("thread stopped");
    }


}
