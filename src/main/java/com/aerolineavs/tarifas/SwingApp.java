package com.aerolineavs.tarifas;

import javax.swing.SwingUtilities;

/**
 * Punto de entrada de la aplicación de escritorio.
 */
public final class SwingApp {

    private SwingApp() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PricingWindow(new PricingServiceImpl()).setVisible(true));
    }
}
