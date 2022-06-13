package MainProject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Map;

public class LoginWindow extends JFrame implements ActionListener, ItemListener {
    private String User;
    private String Psw;
    private final Map M;
    private GUI G;

    private static JButton OKButton = new JButton("Ok");
    private static JButton ResetButton = new JButton("Reset Password");
    private static JLabel  UserNameTF = new JLabel("User Name");
    private static JTextField CampoNome = new JTextField("UserName",20);
    private static JLabel PSWTF = new JLabel("Password");
    private static JPasswordField CampoPSW = new JPasswordField("123456",20);
    private static JMenuItem helpItem =new JMenuItem("?");
    private static JMenuItem exitItem = new JMenuItem("Exit");
    private static JMenu filemenu = new JMenu("file");
    private static JMenu questmenu = new JMenu("help");
    private static JMenuBar menuBar= new JMenuBar();
    private int count =2;

    public LoginWindow(GUI G, Map M) {
        this.M=M;
        setGUI(G);
        OKButton.addActionListener(this);
        ResetButton.addActionListener(this);
        helpItem.addActionListener(this);
        exitItem.addActionListener(this);

        filemenu.add(exitItem);
        questmenu.add(helpItem);
        menuBar.add(filemenu);
        menuBar.add(questmenu);

        JPanel panelName = new JPanel();
        panelName.add(UserNameTF);
        panelName.add(CampoNome);

        JPanel panelPsw = new JPanel();
        panelPsw.add(PSWTF);
        panelPsw.add(CampoPSW);

        JPanel panelButton = new JPanel();
        panelButton.add(OKButton);
        panelButton.add(ResetButton);

        JPanel mainPanel = new JPanel(new GridLayout(3,1));
        mainPanel.add(panelName);
        mainPanel.add(panelPsw);
        mainPanel.add(panelButton);

        setTitle("Login");    //nome della finestra
        setJMenuBar(menuBar);
        setContentPane(mainPanel);
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocation(300,200);           //posizione finestra(0 , 0 è l'angolo in alto dello schermo)
        setSize(350,150);     // dimensioni finestra
        setVisible(true);               // metodo per rendere la finestra visibile su schermo (con true)
    }

    public static void ErrorMessage(){
        JOptionPane.showMessageDialog(null,"la password deve essere lunga almeno 6 caratteri, contenere almeno una " +
                "lettera, un numero e un carattere non alfanumerico","Error Password",JOptionPane.ERROR_MESSAGE);
    }

    public static void exitSystem(){
        System.exit(0);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if(e.getSource()==OKButton){
            String UserName = CampoNome.getText();
            String UserPsw = String.valueOf(CampoPSW.getPassword());
            if(!UserName.equals(getUser())||!UserPsw.equals(getPsw())){
                JOptionPane.showMessageDialog(null,"Credenziali errate, tentativi rimasti "+count+".","Error",
                        JOptionPane.ERROR_MESSAGE);
            }else if(M.get(UserName).equals(UserPsw)){
                User=UserName;
                Psw=UserPsw;
                System.out.println("Succesfull Login"); //Attivazione GUI di nick
                setGUIVisible(true);
                dispose();
            }else{
                JOptionPane.showMessageDialog(null,"Credenziali errate, tentativi rimasti "+count+".","Error",
                        JOptionPane.ERROR_MESSAGE);
            }

            count--;
            if(count<0){
                JOptionPane.showMessageDialog(null,"numero di tentativi finiti, il programma verra' chiuso","Error",
                        JOptionPane.ERROR_MESSAGE);
                exitSystem();
            }
        }
        if(e.getSource()==ResetButton) {
            String NuovaPsw = JOptionPane.showInputDialog(null, "Insert new password");
            if (NuovaPsw.equals(getPsw())) {
                ErrorMessage();
                CampoPSW.setText("");
                return;
            }
            boolean number = false;
            boolean word = false;
            boolean special = false;
            for (char i : NuovaPsw.toCharArray()) {
                if (Character.isDigit(i)) {
                    number = true;
                } else if (Character.isAlphabetic(i)) {
                    word = true;
                } else
                    special = true;
            }
            if (number && word && special && (NuovaPsw.length() >= 6)) {
                JOptionPane.showMessageDialog(null, "Password modificata", "Good", JOptionPane.INFORMATION_MESSAGE);
                setPsw(NuovaPsw);
                CampoPSW.setText(NuovaPsw);
            } else
                ErrorMessage();
            CampoPSW.setText(getPsw());
        }
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

    @Override
    public void itemStateChanged(ItemEvent e) {
        if(e.getItem()==exitItem){
            exitSystem();
        }

        if(e.getItem()==helpItem) {
            JOptionPane.showMessageDialog(null, "Inserire le credenziali per poter accedere all'applicazione", "Help", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void setGUI(GUI G){
        this.G=G;
    }

    public void setGUIVisible(boolean Value){
        if(User!="DebugUser"){
            System.out.close();
        }
        G.setVisible(Value);
    }
}
