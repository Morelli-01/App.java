package MainProject.Graphics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

public class LoginWindow extends JFrame implements ActionListener {
    private String User;
    private String Psw;
    private Map<String, String> Credential;
    private GUI G;

    private static final JButton OKButton = new JButton("Ok");
    private static final JButton ExitButton = new JButton("Exit");
    private static final JLabel UserNameTF = new JLabel("User Name");
    private static final JTextField CampoNome = new JTextField("UserName", 20);
    private static final JLabel PSWTF = new JLabel("Password");
    private static final JPasswordField CampoPSW = new JPasswordField("123456", 20);
    private static final JMenuItem helpItem = new JMenuItem("?");
    private static final JMenu questmenu = new JMenu("help");
    private static final JMenuBar menuBar = new JMenuBar();
    private int Tentativi = 3;
    Boolean isDebugUser = false;

    
    public Boolean getIsDebugUser() {
        return isDebugUser;
    }

    public void setIsDebugUser(Boolean isDebugUser) {
        this.isDebugUser = isDebugUser;
    }

    public String getUser() {
        return User;
    }

    public void setUser(String user) {
        User = user;
    }

    public String getPsw() {
        return Psw;
    }

    public void setPsw(String psw) {
        Psw = psw;
    }

    public void setGUI(GUI G) {
        this.G = G;
    }

    public GUI getGUI() {
        return G;
    }

    public Map<String, String> getCredential() {
        return Credential;
    }

    public void setCredential(Map<String, String> credential) {
        Credential = credential;
    }

    public void setGUIVisible(boolean Value) {
        G.setVisible(Value);
    }

    public LoginWindow(GUI G, Map<String, String> Credential) {
        setCredential(Credential);
        setGUI(G);

        OKButton.addActionListener(this);
        ExitButton.addActionListener(this);
        helpItem.addActionListener(this);
        questmenu.add(helpItem);
        menuBar.add(questmenu);

        JPanel panelName = new JPanel();
        panelName.add(UserNameTF);
        panelName.add(CampoNome);

        JPanel panelPsw = new JPanel();
        panelPsw.add(PSWTF);
        panelPsw.add(CampoPSW);

        JPanel panelButton = new JPanel();
        panelButton.add(OKButton);
        panelButton.add(ExitButton);

        JPanel mainPanel = new JPanel(new GridLayout(3, 1));
        mainPanel.add(panelName);
        mainPanel.add(panelPsw);
        mainPanel.add(panelButton);

        setTitle("Login"); // nome della finestra
        setJMenuBar(menuBar);
        setContentPane(mainPanel);
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocation(300, 200); // posizione finestra(0 , 0 è l'angolo in alto dello schermo)
        setSize(350, 150); // dimensioni finestra
        setVisible(true); // metodo per rendere la finestra visibile su schermo (con true)
    }

    public static void exitSystem() {
        System.exit(0);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==ExitButton){
                exitSystem();
        }
        if(e.getSource()==OKButton){
            String UserName = CampoNome.getText();
            String UserPsw = String.valueOf(CampoPSW.getPassword());
            if (!Credential.containsKey(UserName)){
                Tentativi--;
                JOptionPane.showMessageDialog(null,"Credenziali errate, tentativi rimasti "+Tentativi+".","Error",
                    JOptionPane.ERROR_MESSAGE);
            }
            else if(!Credential.get(UserName).equals(UserPsw)){
                Tentativi--;
                JOptionPane.showMessageDialog(null,"Credenziali errate, tentativi rimasti "+Tentativi+".","Error",
                    JOptionPane.ERROR_MESSAGE);       
            }else{

             User=UserName;
             Psw=UserPsw;
             System.out.println("Succesfull Login"); //Attivazione GUI di nick
             setGUIVisible(true);
             dispose();
             if(User.equals("DebugUser")){
                    setIsDebugUser(true);
                }
            }

            if(Tentativi<=0){
                JOptionPane.showMessageDialog(null,"Numero di tentativi finiti, il programma verrà chiuso","Error",
                        JOptionPane.ERROR_MESSAGE);
                exitSystem();
            }
        }
        if(e.getSource()==helpItem){
            JOptionPane.showMessageDialog(null, "Inserire le credenziali per poter accedere all'applicazione", "Help", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
}
