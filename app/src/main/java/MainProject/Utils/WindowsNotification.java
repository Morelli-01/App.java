package MainProject.Utils;


import java.awt.*;

public class WindowsNotification {
    public static void sendNotification(String input) throws AWTException {
        SystemTray tray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
        TrayIcon trayIcon = new TrayIcon(image, "");
        //Let the system resize the image if needed
        trayIcon.setImageAutoSize(true);
        //Set tooltip text for the tray icon
        trayIcon.setToolTip("Notification");
        tray.add(trayIcon);
        trayIcon.displayMessage("", input, TrayIcon.MessageType.INFO);
    }
}
