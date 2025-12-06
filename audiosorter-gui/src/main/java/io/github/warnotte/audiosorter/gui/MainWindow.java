package io.github.warnotte.audiosorter.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Main window for the AudioSorter GUI application.
 * Currently a simple "Hello World" placeholder.
 */
public class MainWindow extends JFrame {

    public MainWindow() {
        setTitle("AudioFilesSorter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Main panel with centered label
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(30, 30, 30));

        // Header
        JLabel titleLabel = new JLabel("AudioFilesSorter", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        titleLabel.setForeground(new Color(29, 185, 84)); // Spotify green
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        // Subtitle
        JLabel subtitleLabel = new JLabel("GUI Coming Soon!", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        subtitleLabel.setForeground(Color.LIGHT_GRAY);

        // Center panel
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(new Color(30, 30, 30));
        centerPanel.add(Box.createVerticalGlue());

        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(titleLabel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(subtitleLabel);
        centerPanel.add(Box.createVerticalStrut(30));

        // Info text
        JLabel infoLabel = new JLabel("<html><center>This is a placeholder for the future GUI.<br>" +
                "Use the CLI version for now:<br><br>" +
                "<code>java -jar audiosorter-cli.jar scan D:\\Music</code></center></html>",
                SwingConstants.CENTER);
        infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        infoLabel.setForeground(Color.GRAY);
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(infoLabel);

        centerPanel.add(Box.createVerticalGlue());

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Footer
        JLabel footerLabel = new JLabel("v0.2.0 - Multi-module architecture", SwingConstants.CENTER);
        footerLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        footerLabel.setForeground(Color.DARK_GRAY);
        footerLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        mainPanel.add(footerLabel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Use default
        }

        // Create and show window
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}
