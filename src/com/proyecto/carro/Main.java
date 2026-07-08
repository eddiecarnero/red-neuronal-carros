package com.proyecto.carro;

import com.proyecto.carro.gui.MainFrame;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // native L&F integration
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Fallback silently
            }

            // Launch Main Frame
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
