package MainProject;

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

    public GUI() {

        super("GUI");
        LoginWindow login = new LoginWindow(this);
        menu_initializer();
        statsPage_initializer();

        setSize(600, 400);
        setContentPane(mainPanel);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(false);
    }

    private void menu_initializer() {
        JMenuItem addCollection = new JMenuItem("Add Collection");
        JMenuItem Remove = new JMenuItem("Remove");
        JMenuItem Pause = new JMenuItem("Pause");
        JMenuItem Restart = new JMenuItem("Restart");
        menu.add(addCollection);
        edit.add(Remove);
        edit.add(Pause);
        edit.add(Restart);
        addCollection.addActionListener(e -> {
            NFT_collection n = new NFT_collection();
            if (!n.init(JOptionPane.showInputDialog(mainPanel, "Please insert a valid Collection name"))) {
                JOptionPane.showMessageDialog(mainPanel, "invalid Collection Name", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            MonitorThread M = new MonitorThread(tabbedPane, n);
            M.start();
            n.setMonitorTread(M);
            Coll.add(n);
            loadingLabel.setText("Monitoring " + Coll.size() + " Collection");
        });
        Remove.addActionListener(e -> {
            if(Coll.size()==0)  return;
            int index = tabbedPane.getSelectedIndex();

            if (index == 0) return;
            tabbedPane.remove(index);
            index--;
            MonitorThread tmp = Coll.get(index).getMonitorThread();
            tmp.RequestStop();

            Coll.remove(index);
            loadingLabel.setText("Monitoring " + Coll.size() + " Collection");
        });
        Pause.addActionListener(e -> {
            if(Coll.size()==0)  return;
            int index = tabbedPane.getSelectedIndex();
            if (index == 0) return;
            index--;
            MonitorThread tmp = Coll.get(index).getMonitorThread();
            tmp.RequestPause();
        });
        Restart.addActionListener(e -> {
            if(Coll.size()==0)  return;
            int index = tabbedPane.getSelectedIndex();
            if (index == 0) return;
            index--;
            MonitorThread tmp = Coll.get(index).getMonitorThread();
            tmp.RequestRestart();
        });
    }
    private void tps() {
        Thread T = new Thread(() -> {
            try {
                while (true) {
                    if (Unirest.get("https://api.solanart.io/get_solana_tps").asString().getStatusText().equals("OK")) {
                        String tps = JSONParser.parseFromString(Unirest.get("https://api.solanart.io/get_solana_tps").asString().getBody(), "tps");
                        tpsLabel.setText("TPS: " + tps);
                    }
                    try {
                        sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (NullPointerException | UnirestException e) {
                System.out.println("Something went wrong with solanart api");
                try {
                    sleep(10000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                tps();
                return;
            }

        });
        T.start();
    }

    private void volumes() {
        Thread T = new Thread(() -> {
            try{
                while (true) {
                    if (Unirest.get("https://api-mainnet.magiceden.io/volumes?edge_cache=true").asString().getStatusText().equals("OK")) {
                        String[] s = {"total", "last24Hrs"};
                        s = JSONParser.parseFromString(Unirest.get("https://api-mainnet.magiceden.io/volumes?edge_cache=true").asString().getBody(), s);
                        volume24HLabel.setText("24H Volume: " + (int)Double.parseDouble(s[1]) + " SOL");
                        totalVolumeLabel.setText("Total Volume: " + (int)Double.parseDouble(s[0]) + " Mln SOL");
                    }
                    sleep(60000);
                }
            }catch ( UnirestException | NullPointerException | InterruptedException e){
                System.out.println("Exception from retriving ME volumes");
                volumes();
                return;
            }
        });
        T.start();
    }

    private void imageSlider() {
        Thread T = new Thread(() -> {
            ArrayList<Object[]> iconArrayList = new ArrayList<>();
            try {
                URL url = null;
                ImageIcon icon;
                Image resizedIcon;
                String[] s = {"image", "name", "symbol"};
                String[][] result = JSONParser.parseFromString(Unirest.get("https://api-mainnet.magiceden.io/popular_collections?more=true&timeRange=7d&edge_cache=true")
                        .asString()
                        .getBody(), s, 5);
                for(String[] str : result){
                    url = new URL(str[0]);
                    icon = new ImageIcon(url);
                    resizedIcon = icon.getImage().getScaledInstance(200, 200, Image.SCALE_DEFAULT);
                    iconArrayList.add(new Object[]{str[1], resizedIcon, "https://magiceden.io/marketplace/" + str[2]});
                }
            } catch (NullPointerException | UnirestException | MalformedURLException e) {
                imageSlider();
                return;
            }

            while (true) {
                for (int i = 0; i < 5; i++) {
                    radioButton1.setSelected(i == 0);
                    radioButton2.setSelected(i == 1);
                    radioButton3.setSelected(i == 2);
                    radioButton4.setSelected(i == 3);
                    radioButton5.setSelected(i == 4);
                    Object[] s = iconArrayList.get(i);
                    ImageIcon tmp = new ImageIcon((Image) s[1]);
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
        T.start();
    }

    private void exchange() {
        Thread T = new Thread(() -> {
            try{
            while (true) {
                if (Unirest.get("https://api.binance.com/api/v3/avgPrice?symbol=SOLUSDT").asString().getStatusText().equals("OK")) {

                String s = JSONParser.parseFromString(Unirest.get("https://api.binance.com/api/v3/avgPrice?symbol=SOLUSDT").asString().getBody(), "price");
                    solusdtLabel.setText("SOL/USDT: " + s.substring(0 ,4) + "$");
                }
                sleep(10000);
            }
            }catch (NullPointerException | UnirestException | InterruptedException e){
                System.out.println("Exception from "+ this.getName() +" regarding exchange class");

                exchange();
                return;
            }

        });

        T.start();
    }

    private void statsPage_initializer() {
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
        imageSlider();
        tps();
        volumes();
        exchange();

    }
}
