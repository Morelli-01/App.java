
package MainProject;

import MainProject.Graphics.GUI;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
/*
 * The GUI get started from here
 */

public class App {

    public static void main(String[] args) throws UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(new FlatDarkLaf());
        new GUI();
    }
}
