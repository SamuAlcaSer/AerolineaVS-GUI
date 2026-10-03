package com.aerolineavs.tarifas;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * Ventana de escritorio para consultar una recomendación de tarifa.
 */
public final class PricingWindow extends JFrame {

    private final IPricingService pricingService;
    private final JTextField ageField = new JTextField("30");
    private final JTextField annualFlightsField = new JTextField("6");
    private final JTextField annualIncomeField = new JTextField("25000");
    private final JComboBox<TipoViajero> travelerTypeCombo = new JComboBox<>(TipoViajero.values());
    private final JComboBox<ClaseVuelo> flightClassCombo = new JComboBox<>(ClaseVuelo.values());
    private final JComboBox<RegionDestino> destinationCombo = new JComboBox<>(RegionDestino.values());
    private final JCheckBox travelsWithChildrenCheckBox = new JCheckBox("Viaja con niños menores de 12 años");
    private final JCheckBox livesWithParentsCheckBox = new JCheckBox("Vive con sus padres");
    private final JLabel tariffResultLabel = new JLabel("—", SwingConstants.CENTER);
    private final JLabel discountResultLabel = new JLabel("Completa el perfil y calcula la tarifa.", SwingConstants.CENTER);

    public PricingWindow(IPricingService pricingService) {
        super("Recomendador de tarifas");
        if (pricingService == null) {
            throw new IllegalArgumentException("El servicio de tarifas es obligatorio.");
        }
        this.pricingService = pricingService;

        configureWindow();
        configureComboLabels(travelerTypeCombo);
        configureComboLabels(flightClassCombo);
        configureComboLabels(destinationCombo);
        add(createContent());
        pack();
        setMinimumSize(new Dimension(480, 560));
    }

    private void configureWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationByPlatform(true);
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel heading = new JLabel("Encuentra tu tarifa ideal");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
        content.add(heading, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.add(createProfileForm(), BorderLayout.CENTER);
        center.add(createResultPanel(), BorderLayout.SOUTH);
        content.add(center, BorderLayout.CENTER);
        return content;
    }

    private JPanel createProfileForm() {
        JPanel form = new JPanel(new GridLayout(0, 2, 12, 12));
        form.setBorder(BorderFactory.createTitledBorder("Perfil de viaje"));

        addFormRow(form, "Edad:", ageField);
        addFormRow(form, "Vuelos al año:", annualFlightsField);
        addFormRow(form, "Ingresos anuales (€):", annualIncomeField);
        addFormRow(form, "Tipo de viajero:", travelerTypeCombo);
        addFormRow(form, "Clase preferida:", flightClassCombo);
        addFormRow(form, "Destino preferido:", destinationCombo);
        addFormRow(form, "", travelsWithChildrenCheckBox);
        addFormRow(form, "", livesWithParentsCheckBox);

        JButton calculateButton = new JButton("Calcular tarifa");
        calculateButton.addActionListener(event -> calculateTariff());
        form.add(new JLabel());
        form.add(calculateButton);
        return form;
    }

    private JPanel createResultPanel() {
        JPanel result = new JPanel(new GridLayout(0, 1, 4, 4));
        result.setBorder(BorderFactory.createTitledBorder("Resultado"));
        tariffResultLabel.setFont(tariffResultLabel.getFont().deriveFont(Font.BOLD, 18f));
        result.add(tariffResultLabel);
        result.add(discountResultLabel);
        return result;
    }

    private void calculateTariff() {
        try {
            ClientePotencial cliente = new ClientePotencial(
                    parseNonNegativeInteger(ageField, "La edad"),
                    parseNonNegativeInteger(annualFlightsField, "Los vuelos al año"),
                    (TipoViajero) travelerTypeCombo.getSelectedItem(),
                    (ClaseVuelo) flightClassCombo.getSelectedItem(),
                    (RegionDestino) destinationCombo.getSelectedItem(),
                    parseNonNegativeAmount(annualIncomeField),
                    travelsWithChildrenCheckBox.isSelected(),
                    livesWithParentsCheckBox.isSelected()
            );
            ResultadoTarifa resultado = pricingService.evaluar(cliente);
            tariffResultLabel.setText(resultado.tarifa().getNombre());
            discountResultLabel.setText("Descuento: " + resultado.tarifa().getDescuentoPorcentaje() + "%");
        } catch (IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getMessage(),
                    "Revisa los datos",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private int parseNonNegativeInteger(JTextField field, String description) {
        try {
            int value = Integer.parseInt(field.getText().trim());
            if (value < 0) {
                throw new IllegalArgumentException(description + " no pueden ser negativos.");
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(description + " deben ser un número entero válido.");
        }
    }

    private double parseNonNegativeAmount(JTextField field) {
        String text = field.getText().trim();
        ParsePosition position = new ParsePosition(0);
        Number parsed = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-ES")).parse(text, position);
        if (parsed == null || position.getIndex() != text.length()) {
            throw new IllegalArgumentException("Los ingresos deben ser un importe válido.");
        }
        double amount = parsed.doubleValue();
        if (!Double.isFinite(amount) || amount < 0) {
            throw new IllegalArgumentException("Los ingresos no pueden ser negativos.");
        }
        return amount;
    }

    private void addFormRow(JPanel form, String label, Component input) {
        form.add(new JLabel(label));
        form.add(input);
    }

    private <E extends Enum<E>> void configureComboLabels(JComboBox<E> combo) {
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
            ) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Enum<?> enumValue) {
                    String label = enumValue.name().replace('_', ' ').toLowerCase(Locale.forLanguageTag("es-ES"));
                    setText(Character.toUpperCase(label.charAt(0)) + label.substring(1));
                }
                return this;
            }
        });
    }
}
