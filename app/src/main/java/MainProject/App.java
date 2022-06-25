/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package MainProject;

import MainProject.Graphics.GUI;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;


public class App {
    public static void main(String[] args) throws UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(new FlatDarkLaf());
        EventQueue.invokeLater(GUI::new);
    }
}
