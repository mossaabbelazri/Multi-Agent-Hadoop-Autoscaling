package com.projet.hadoop;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class MonitoringGui extends JFrame {
    private JProgressBar progressBar;
    private JLabel labelMakespan;
    private DefaultTableModel tableModel;
    private JTextArea logArea;

    public MonitoringGui() {
        setTitle("SMA Hadoop Cloud - Dashboard Monitoring");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Configuration du look & feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        // Changement vers un thème sombre
        getContentPane().setBackground(new Color(30, 30, 30));
        setLayout(new BorderLayout(10, 10));

        // -- PANEL NORD : MAKESPAN --
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBackground(new Color(45, 45, 48));
        northPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        labelMakespan = new JLabel("Makespan Global : 0.00%");
        labelMakespan.setForeground(Color.WHITE);
        labelMakespan.setFont(new Font("Segoe UI", Font.BOLD, 18));
        northPanel.add(labelMakespan, BorderLayout.NORTH);

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setForeground(Color.GREEN);
        progressBar.setBackground(Color.DARK_GRAY);
        progressBar.setPreferredSize(new Dimension(0, 30));
        northPanel.add(progressBar, BorderLayout.SOUTH);

        add(northPanel, BorderLayout.NORTH);

        // -- PANEL CENTRAL : TABLEAU DES AGENTS --
        String[] columns = {"Nom de l'Agent", "Charge CPU (%)", "Statut"};
        tableModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(tableModel);
        table.setBackground(new Color(37, 37, 38));
        table.setForeground(Color.WHITE);
        table.setGridColor(Color.GRAY);
        table.setRowHeight(25);
        table.getTableHeader().setBackground(new Color(50, 50, 50));
        table.getTableHeader().setForeground(Color.WHITE);

        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "État du Cluster", 0, 0, null, Color.WHITE));
        tableScrollPane.getViewport().setBackground(new Color(30,30,30));
        add(tableScrollPane, BorderLayout.CENTER);

        // -- PANEL SUD : LOGS --
        logArea = new JTextArea(8, 50);
        logArea.setEditable(false);
        logArea.setBackground(new Color(25, 25, 25));
        logArea.setForeground(new Color(0, 255, 0)); // Vert Matrix
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        
        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Console de Décision", 0, 0, null, Color.WHITE));
        add(logScrollPane, BorderLayout.SOUTH);

        setVisible(true);
    }

    public synchronized void updateAgent(String agentName, double cpu) {
        // Mise à jour de la table
        boolean found = false;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 0).equals(agentName)) {
                tableModel.setValueAt(String.format("%.2f %%", cpu), i, 1);
                tableModel.setValueAt(cpu > 70 ? "SATURATION" : "OK", i, 2);
                found = true;
                break;
            }
        }
        if (!found) {
            tableModel.addRow(new Object[]{agentName, String.format("%.2f %%", cpu), cpu > 70 ? "SATURATION" : "OK"});
        }
    }

    public synchronized void updateMakespan(double makespan) {
        labelMakespan.setText("Makespan Global : " + String.format("%.2f", makespan) + "%");
        progressBar.setValue((int) makespan);

        // Couleur dynamique
        if (makespan < 50) progressBar.setForeground(new Color(0, 200, 0)); // Vert
        else if (makespan < 80) progressBar.setForeground(new Color(255, 165, 0)); // Orange
        else progressBar.setForeground(new Color(220, 20, 60)); // Rouge
    }

    public synchronized void addLog(String message) {
        logArea.append("[" + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) + "] " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
