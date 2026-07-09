package com.proyecto.carro;

import com.proyecto.carro.gui.MainFrame;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Integración del aspecto y comportamiento nativo (Look and Feel)
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Ignorar error y continuar
            }

            // Lanzar la ventana principal
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
