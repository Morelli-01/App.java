package MainProject.Graphics;

import MainProject.Utils.JSONParser;
import MainProject.NFTClasses.NFT_collection;
import MainProject.database.dbConn;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import kong.unirest.GetRequest;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.lang.Thread.sleep;
import static javax.swing.JOptionPane.*;

/*
 *  Class that define the main panel of the GUI,
 *  after a Succefull Login the GUI will be showed,
 *  with a basic Menu Bar containign the essentials button to manipulate the tool.
 *  Other then that the GUI will start with an Statistics page rapresenting
 *  some of the most important information about the market:
 *      -TPS(transaction per second): basically a status of the solana network, if it goe to zero it means that the block production of the network has halted
 *      -SOL/USDT: the value of the cryptocurency SOLANA in dollars
 *      -24H Volume and TotalVolume: these two are the rapresentation of how much worth the NFT martket on solana
 *  Other than that there is also an image slider that shows the most popular collection in the last 7 days
 */
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
    protected static final String[] choseOption = {"Decrease", "Increase"};
    protected static final String DECREASE = choseOption[0];
    protected static final String INCREASE = choseOption[1];
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
    private List<NFT_collection> Coll = new ArrayList<>();

    public GUI() {

        super("GUI");

        dbConn C = new dbConn();

        EventQueue.invokeLater(() -> new LoginWindow(this, C.getCredentials()));

        menu_initializer();
        statsPage_initializer();

        setSize(600, 400);
        setContentPane(mainPanel);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setVisible(false);
    }

    private void menu_initializer() {
        JMenuItem addCollection = new JMenuItem("Add Collection");
        JMenuItem Exit = new JMenuItem("Exit");
        JMenuItem Remove = new JMenuItem("Remove");
        JMenuItem Pause = new JMenuItem("Pause");
        JMenuItem Restart = new JMenuItem("Restart");
        JMenuItem Trigger = new JMenuItem("Add Trigger");
        menu.add(addCollection);
        menu.add(Exit);
        edit.add(Remove);
        edit.add(Pause);
        edit.add(Restart);
        edit.add(Trigger);
        addCollection.addActionListener((e) -> {
            NFT_collection n = new NFT_collection();
            String input = showInputDialog(mainPanel, "Please insert a valid Collection name");
            if (input == null || !n.init(input)) {
                showMessageDialog(mainPanel, "invalid Collection Name", "Error", ERROR_MESSAGE);
                return;
            }

            MonitorThread M = new MonitorThread(tabbedPane, n);
            M.start();
            n.setMonitorTread(M);
            Coll.add(n);
            loadingLabel.setText("Monitoring " + Coll.size() + " Collection");
        });
        Exit.addActionListener(e -> {
            System.exit(0);
        });
        Remove.addActionListener(e -> {
            if (Coll.size() == 0)
                return;
            int index = tabbedPane.getSelectedIndex();
            if (index == 0)
                return;

            tabbedPane.remove(index);
            index--;

            MonitorThread tmp = Coll.get(index).getMonitorThread();
            tmp.RequestStop();

            Coll.remove(index);
            loadingLabel.setText("Monitoring " + Coll.size() + " Collection");
        });
        Pause.addActionListener(e -> {
            if (Coll.size() == 0)
                return;
            int index = tabbedPane.getSelectedIndex();
            if (index == 0)
                return;
            index--;
            MonitorThread tmp = Coll.get(index).getMonitorThread();
            tmp.RequestPause();
        });
        Restart.addActionListener(e -> {
            if (Coll.size() == 0)
                return;
            int index = tabbedPane.getSelectedIndex();
            if (index == 0)
                return;
            index--;
            MonitorThread tmp = Coll.get(index).getMonitorThread();
            tmp.RequestRestart();
        });
        Trigger.addActionListener(e -> {
            if (Coll.size() == 0)
                return;
            int index = tabbedPane.getSelectedIndex();
            if (index == 0)
                return;
            String input = showInputDialog(mainPanel, "Insert a valid trigger value", QUESTION_MESSAGE);

            Object input2 = showInputDialog(mainPanel, "Choose the kind of trigger", "Input", PLAIN_MESSAGE, null,
                    choseOption, choseOption[0]);

            index--;
            MonitorThread tmp = Coll.get(index).getMonitorThread();
            tmp.setTriggerList(Double.parseDouble(input), (String) input2);
        });
    }

    private void tps() {// trasaction per second
        Thread T = new Thread(() -> {
            while (true) {
                try {
                    sleep(10000);
                    GetRequest response = Unirest.get("https://api.solanart.io/get_solana_tps");
                    if (response.asString().getStatusText().equals("OK")) {
                        String tps = JSONParser.parseFromString(response.asString().getBody(), "tps");
                        tpsLabel.setText("TPS: " + tps);
                    }

                } catch (NullPointerException | UnirestException | InterruptedException e) {
                    System.out.println("[" + dtf.format(LocalDateTime.now()) + "] " +
                            "Something went wrong with solanart api");
                }
            }
        }, "TPS");
        T.start();
    }

    private void volumes() {
        Thread T = new Thread(() -> {
            while (true) {
                try {
                    GetRequest response = Unirest.get("https://api-mainnet.magiceden.io/volumes?edge_cache=true");
                    if (response.asString().getStatusText().equals("OK")) {
                        String[] s = {"total", "last24Hrs"};
                        s = JSONParser.parseFromString(response.asString().getBody(), s);
                        volume24HLabel.setText("24H Volume: " + (int) Double.parseDouble(s[1]) + " SOL");
                        totalVolumeLabel.setText("Total Volume: " + (int) Double.parseDouble(s[0]) + " SOL");
                    }
                    sleep(10000);
                } catch (UnirestException | NullPointerException | InterruptedException e) {
                    System.out.println("[" + dtf.format(LocalDateTime.now()) + "] " +
                            "Exception from retriving ME volumes");
                }
            }
        }, "Volumes");
        T.start();
    }

    private void imageSlider() {
        Thread T = new Thread(() -> {
            ArrayList<Object[]> iconArrayList = new ArrayList<>();
            Integer count = 100;
            while (true) {
                try {
                    if(count>=100) {
                        // si ricavano le 5 collezioni più popolari negli ultii 7 giorni da mostrare poi nella stats page
                        URL url = null;
                        ImageIcon icon;
                        Image resizedIcon;
                        String[] s = {"image", "name", "symbol"};
                        String[][] result = JSONParser.parseFromString(Unirest.get(
                                        "https://api-mainnet.magiceden.dev/popular_collections?more=true&timeRange=7d&edge_cache=true")
                                .asString()
                                .getBody(), s, 5);
                        for (String[] str : result) {
                            url = new URL("https://img-cdn.magiceden.dev/rs:fill:320:320:0:0/plain/" + str[0]);
                            icon = new ImageIcon(url);
                            resizedIcon = icon.getImage().getScaledInstance(200, 200, Image.SCALE_DEFAULT);
                            iconArrayList.add(new Object[]{str[1], resizedIcon, "https://magiceden.io/marketplace/" + str[2]});
                        }
                        count=0;
                    }
                    else{count++;}
                    for (int i = 0; i < 5; i++) {
                        radioButton1.setSelected(i == 0);
                        radioButton2.setSelected(i == 1);
                        radioButton3.setSelected(i == 2);
                        radioButton4.setSelected(i == 3);
                        radioButton5.setSelected(i == 4);
                        Object[] obj = iconArrayList.get(i);
                        ImageIcon tmp = new ImageIcon((Image) obj[1]);
                        slideImageLabel.setIcon(tmp);
                        slideNameLabel.setText((String) obj[0]);
                        slideNameLabel.setToolTipText((String) obj[2]);

                        sleep(3000);

                    }

                } catch (NullPointerException | UnirestException | MalformedURLException | InterruptedException e) {
                    System.out.println("[" + dtf.format(LocalDateTime.now()) + "] "+
                            "Some error about the image slider");
                    System.out.println(e.getMessage());
                }
            }
        }, "ImageSlider");
        T.start();
    }

    private void exchange() {
        Thread T = new Thread(() -> {
            while (true) {
                try {
                    GetRequest response = Unirest.get("https://api.binance.com/api/v3/avgPrice?symbol=SOLUSDT");
                    if (response.asString().getStatusText().equals("OK")) {

                        String s = JSONParser.parseFromString(Unirest
                                        .get("https://api.binance.com/api/v3/avgPrice?symbol=SOLUSDT").asString().getBody(),
                                "price");
                        solusdtLabel.setText("SOL/USDT: " + s.substring(0, 4) + "$");
                    }
                    sleep(10000);
                } catch (NullPointerException | UnirestException | InterruptedException e) {
                    System.out.println("[" + dtf.format(LocalDateTime.now()) + "] "+
                            "Exception from " + this.getName() + " regarding exchange class");
                }
            }
        }, "Exchange");

        T.start();
    }

    private void statsPage_initializer() {
        // setup ME image
        ImageIcon image = new ImageIcon("me.png");
        imageLabel.setIcon(image);
        imageLabel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("https://magiceden.io/collections?type=popular"));
                } catch (IOException | URISyntaxException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        ImageIcon gif = new ImageIcon("rotate.gif");
        gifLabel.setIcon(gif);
        slideNameLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(slideNameLabel.getToolTipText()));
                } catch (IOException | URISyntaxException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        imageSlider();
        tps();
        volumes();
        exchange();
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
        mainPanel.setLayout(new GridLayoutManager(3, 4, new Insets(0, 0, 0, 0), -1, -1));
        final JMenuBar menuBar1 = new JMenuBar();
        menuBar1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(menuBar1,
                new GridConstraints(0, 0, 1, 4, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null,
                        new Dimension(107, 42), null, 0, false));
        menu = new JMenu();
        menu.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        menu.setText("Menu");
        menuBar1.add(menu,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null,
                        new Dimension(44, 17), null, 0, false));
        edit = new JMenu();
        edit.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        edit.setText("Edit");
        menuBar1.add(edit,
                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null,
                        0, false));
        final Spacer spacer1 = new Spacer();
        menuBar1.add(spacer1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        tabbedPane = new JTabbedPane();
        tabbedPane.setForeground(new Color(-3276545));
        mainPanel.add(tabbedPane,
                new GridConstraints(2, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null,
                        new Dimension(200, 200), null, 0, false));
        statsPage = new JPanel();
        statsPage.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
        statsPage.setBackground(new Color(-16514044));
        statsPage.setForeground(new Color(-16514044));
        statsPage.setOpaque(false);
        tabbedPane.addTab("Stats Page", statsPage);
        imageLabel = new JLabel();
        imageLabel.setForeground(new Color(-16514044));
        imageLabel.setText("");
        statsPage.add(imageLabel,
                new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                        false));
        gifLabel = new JLabel();
        gifLabel.setText("");
        statsPage.add(gifLabel,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                        false));
        loadingLabel = new JLabel();
        loadingLabel.setText("Monitoring 0 \nCollection");
        statsPage.add(loadingLabel,
                new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                        false));
        sliderPanel = new JPanel();
        sliderPanel.setLayout(new GridLayoutManager(3, 5, new Insets(0, 0, 0, 0), -1, -1));
        sliderPanel.setBackground(new Color(-12828863));
        sliderPanel.setForeground(new Color(-4473925));
        statsPage.add(sliderPanel,
                new GridConstraints(0, 2, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null,
                        0, false));
        radioButton2 = new JRadioButton();
        radioButton2.setEnabled(false);
        radioButton2.setMargin(new Insets(2, 2, 2, 2));
        radioButton2.setOpaque(false);
        radioButton2.setSelected(false);
        radioButton2.setText("");
        sliderPanel.add(radioButton2,
                new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        radioButton3 = new JRadioButton();
        radioButton3.setEnabled(false);
        radioButton3.setMargin(new Insets(2, 2, 2, 2));
        radioButton3.setSelected(false);
        radioButton3.setText("");
        sliderPanel.add(radioButton3,
                new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        slideImageLabel = new JLabel();
        slideImageLabel.setText("");
        sliderPanel.add(slideImageLabel,
                new GridConstraints(1, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                        false));
        radioButton1 = new JRadioButton();
        radioButton1.setBackground(new Color(-12828863));
        radioButton1.setEnabled(false);
        radioButton1.setForeground(new Color(-1));
        radioButton1.setHideActionText(true);
        radioButton1.setMargin(new Insets(2, 2, 2, 2));
        radioButton1.setOpaque(false);
        radioButton1.setSelected(false);
        radioButton1.setText("");
        sliderPanel.add(radioButton1,
                new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        radioButton4 = new JRadioButton();
        radioButton4.setEnabled(false);
        radioButton4.setMargin(new Insets(2, 2, 2, 2));
        radioButton4.setSelected(false);
        radioButton4.setText("");
        sliderPanel.add(radioButton4,
                new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        radioButton5 = new JRadioButton();
        radioButton5.setEnabled(false);
        radioButton5.setMargin(new Insets(2, 2, 2, 2));
        radioButton5.setSelected(false);
        radioButton5.setText("");
        sliderPanel.add(radioButton5,
                new GridConstraints(2, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        slideNameLabel = new JLabel();
        Font slideNameLabelFont = this.$$$getFont$$$("Cooper Black", -1, 20, slideNameLabel.getFont());
        if (slideNameLabelFont != null)
            slideNameLabel.setFont(slideNameLabelFont);
        slideNameLabel.setText("");
        slideNameLabel.putClientProperty("html.disable", Boolean.FALSE);
        sliderPanel.add(slideNameLabel,
                new GridConstraints(0, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                        false));
        tpsLabel = new JLabel();
        tpsLabel.setForeground(new Color(-3276545));
        tpsLabel.setText("TPS:--");
        tpsLabel.setToolTipText("transaction per second solana can handle");
        mainPanel.add(tpsLabel,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                        false));
        volume24HLabel = new JLabel();
        volume24HLabel.setForeground(new Color(-3276545));
        volume24HLabel.setText("24h Volume:--");
        mainPanel.add(volume24HLabel,
                new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                        false));
        totalVolumeLabel = new JLabel();
        totalVolumeLabel.setForeground(new Color(-3276545));
        totalVolumeLabel.setText("Total Volume:--");
        mainPanel.add(totalVolumeLabel,
                new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                        false));
        solusdtLabel = new JLabel();
        solusdtLabel.setForeground(new Color(-3276545));
        solusdtLabel.setText("SOL/USDT:--");
        mainPanel.add(solusdtLabel,
                new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                        false));
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
