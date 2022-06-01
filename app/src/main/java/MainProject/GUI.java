package MainProject;

import com.google.gson.internal.LinkedTreeMap;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Map;

import static java.lang.Thread.sleep;

public class GUI extends JFrame {

    private JTabbedPane tabbedPane;
    private JPanel mainPanel;
    private JMenu menu;
    private JPanel statsPage;
    private JLabel imageLabel;
    private JLabel tpsLabel;
    private JLabel solusdtLabel;
    private JMenu edit;
    private JLabel gifLabel;
    private JLabel loadingLabel;
    private JLabel volume24HLabel;
    private JLabel totalVolumeLabel;
    private JPanel sliderPanel;
    private JRadioButton radioButton3;
    private JRadioButton radioButton1;
    private JRadioButton radioButton2;
    private JLabel slideImageLabel;
    private JRadioButton radioButton4;
    private JRadioButton radioButton5;
    private JLabel slideNameLabel;

    ArrayList<NFT_collection> Coll = new ArrayList<>();
    public GUI(){

        super("GUI");
        LoginWindow login = new LoginWindow(this);
        menu_initializer();
        statsPage_initializer();

        setSize(600,400);
        setContentPane(mainPanel);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(false);
    }
    private void menu_initializer(){
        JMenuItem addCollection = new JMenuItem("Add Collection");
        JMenuItem Remove = new JMenuItem("Remove");
        JMenuItem Pause = new JMenuItem("Pause");
        JMenuItem Restart = new JMenuItem("Restart");
        menu.add(addCollection);
        edit.add(Remove);
        edit.add(Pause);
        edit.add(Restart);
        addCollection.addActionListener(e->{
            NFT_collection n = new NFT_collection();
            if(!n.init(JOptionPane.showInputDialog(mainPanel, "Please insert a valid Collection name"))) {
                JOptionPane.showMessageDialog(mainPanel, "invalid Collection Name", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            MonitorThread M=null;
            try {
                M = new MonitorThread(tabbedPane, n);
            }catch (UnirestException ex){
                System.out.println("UnirestException in MonitorThread");
            }
            M.start();
            n.setMonitorTread(M);
            Coll.add(n);
            loadingLabel.setText("Monitoring "+Coll.size()+" Collection");
        });
        Remove.addActionListener(e->{
            if(Coll.size()==0)  return;
            int index = tabbedPane.getSelectedIndex();

            if(index==0) return;
            tabbedPane.remove(index);
            index--;
            MonitorThread tmp = Coll.get(index).getMonitorThread();
            tmp.RequestStop();
            System.out.println(tmp.isAlive());

            Coll.remove(index);
            loadingLabel.setText("Monitoring "+Coll.size()+" Collection");
        });
    }
    private void statsPage_initializer(){
        //sutup ME image
        ImageIcon image = new ImageIcon("me.png");
        imageLabel.setIcon(image);

        ImageIcon gif = new ImageIcon("rotate.gif");
        gifLabel.setIcon(gif);
        slideNameLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(slideNameLabel.getToolTipText()));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                } catch (URISyntaxException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        try{
            Thread T1 = new Thread(()->{
                while (true) {
                    if(Unirest.get("https://api.solanart.io/get_solana_tps").asString().getStatusText().equals("OK")) {
                        JsonNode node = Unirest.get("https://api.solanart.io/get_solana_tps").asJson().getBody();
                        JSONObject obj = node.getObject();
                        LinkedTreeMap M = (LinkedTreeMap) obj.toMap();

                        tpsLabel.setText("TPS: "+M.get("tps").toString());
                    }
                    try {
                        sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            Thread T2 = new Thread(()->{
            while (true) {
                if (Unirest.get("https://api.binance.com/api/v3/avgPrice?symbol=SOLUSDT").asString().getStatusText().equals("OK")) {
                    JsonNode node = Unirest.get("https://api.binance.com/api/v3/avgPrice?symbol=SOLUSDT").asJson().getBody();
                    JSONObject O = node.getObject();
                    Map<String, Object> M = O.toMap();

                    solusdtLabel.setText("SOL/USDT: "+M.get("price").toString().substring(0, 4)+"$");
                }
                try {
                    sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
            Thread T3 = new Thread (() ->{
            while(true){
                if(Unirest.get("https://api-mainnet.magiceden.io/volumes?edge_cache=true").asString().getStatusText().equals("OK")){
                    JsonNode node = Unirest.get("https://api-mainnet.magiceden.io/volumes?edge_cache=true").asJson().getBody();
                    JSONObject O = node.getObject();
                    Map<String, Object> M = O.toMap();
                    volume24HLabel.setText("24H Volume: "+M.get("last24Hrs").toString().substring(0,6)+" SOL");
                    totalVolumeLabel.setText("Total Volume: "+M.get("total").toString().substring(0,4)+"Mln SOL");
                }
                try {
                    sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        });
            Thread T4 = new Thread(() ->{

            //handlin request from magiceden
            JSONObject obj = Unirest.get("https://api-mainnet.magiceden.io/popular_collections?more=true&timeRange=7d&edge_cache=true").asJson().getBody().getObject();
            JSONArray arr = (JSONArray) obj.get("collections");//array containing the most 50 popular collection
            //we will use only the first 5

            ArrayList<Object[]> iconArrayList= new ArrayList<>();
            URL url=null;
            ImageIcon icon;
            Image resizedIcon;
            //retriving imagis for collections and resizing them
            for(int i =0;i<5;i++) {
                obj = (JSONObject) arr.get(i);
                try {
                    url=new URL((String) obj.get("image"));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                icon = new ImageIcon(url);
                resizedIcon = icon.getImage().getScaledInstance(200, 200,Image.SCALE_DEFAULT);
                iconArrayList.add(new Object[]{ (String)obj.get("name"),resizedIcon , "https://magiceden.io/marketplace/"+obj.get("symbol")});

            }
            while(true){
                for(int i=0;i<5;i++){
                        radioButton1.setSelected(i == 0);
                        radioButton2.setSelected(i == 1);
                        radioButton3.setSelected(i == 2);
                        radioButton4.setSelected(i == 3);
                        radioButton5.setSelected(i == 4);
                        Object[] s = iconArrayList.get(i);
                        ImageIcon tmp = new ImageIcon((Image)s[1]);
                        slideImageLabel.setIcon(tmp);
                        slideNameLabel.setText((String) s[0]);
                        slideNameLabel.setToolTipText((String) s[2]);
                        try {
                            sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }


                }
            }
        });

            T1.start();
            T2.start();
            T3.start();
            T4.start();
        }catch(java.lang.NullPointerException e){
            System.out.println("unirest exception from a thread in stats page");
        }
    }
}
