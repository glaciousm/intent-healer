package com.intenthealer.intellij.ui;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Panel showing locator stability scores.
 */
public class LocatorStabilityPanel extends JBPanel<LocatorStabilityPanel> {

    private final Project project;
    private final JBTable stabilityTable;
    private final StabilityTableModel tableModel;

    public LocatorStabilityPanel(@NotNull Project project) {
        super(new BorderLayout());
        this.project = project;
        this.tableModel = new StabilityTableModel();
        this.stabilityTable = new JBTable(tableModel);

        initComponents();
    }

    private void initComponents() {
        setBorder(JBUI.Borders.empty(5));

        // Header with summary
        JPanel headerPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        headerPanel.setBorder(JBUI.Borders.empty(5, 0, 10, 0));

        headerPanel.add(createSummaryCard("Total Locators", "0", Color.BLUE));
        headerPanel.add(createSummaryCard("Stable", "0", new Color(0, 150, 0)));
        headerPanel.add(createSummaryCard("Moderate", "0", Color.ORANGE));
        headerPanel.add(createSummaryCard("Unstable", "0", Color.RED));

        // Configure table
        stabilityTable.setRowHeight(25);
        stabilityTable.getColumnModel().getColumn(0).setPreferredWidth(250); // Locator
        stabilityTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // Score
        stabilityTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Level
        stabilityTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Successes
        stabilityTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Failures
        stabilityTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // Heals

        // Score column renderer
        stabilityTable.getColumnModel().getColumn(1).setCellRenderer(new ScoreCellRenderer());
        stabilityTable.getColumnModel().getColumn(2).setCellRenderer(new LevelCellRenderer());

        // Toolbar
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refresh());
        toolbar.add(refreshBtn);

        JButton exportBtn = new JButton("Export Report");
        exportBtn.addActionListener(e -> exportReport());
        toolbar.add(exportBtn);

        // Info label
        JBLabel infoLabel = new JBLabel(
                "<html><i>Locator stability is tracked automatically. " +
                "Lower scores indicate locators that frequently need healing.</i></html>"
        );
        infoLabel.setBorder(JBUI.Borders.empty(10, 0));

        // Layout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(toolbar, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(new JBScrollPane(stabilityTable), BorderLayout.CENTER);
        add(infoLabel, BorderLayout.SOUTH);
    }

    private JPanel createSummaryCard(String label, String value, Color color) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(color, 1));

        JBLabel valueLabel = new JBLabel(value);
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 20f));
        valueLabel.setForeground(color);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JBLabel titleLabel = new JBLabel(label);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN, 10f));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(valueLabel, BorderLayout.CENTER);
        panel.add(titleLabel, BorderLayout.SOUTH);
        panel.setBorder(JBUI.Borders.empty(5));

        return panel;
    }

    private void refresh() {
        // TODO: Load data from LocatorStabilityScorer
        JOptionPane.showMessageDialog(this,
                "Stability data will be loaded from the running test suite.",
                "Refresh", JOptionPane.INFORMATION_MESSAGE);
    }

    private void exportReport() {
        JOptionPane.showMessageDialog(this,
                "Export functionality will be added in a future update.",
                "Export", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Table model for stability data.
     */
    private static class StabilityTableModel extends AbstractTableModel {
        private final String[] columns = {"Locator", "Score", "Level", "Successes", "Failures", "Heals"};

        @Override
        public int getRowCount() {
            return 0; // Placeholder
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int row, int column) {
            return ""; // Placeholder
        }
    }

    /**
     * Renderer for score column.
     */
    private static class ScoreCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (value instanceof Number score) {
                double scoreVal = score.doubleValue();
                if (scoreVal >= 75) {
                    setForeground(new Color(0, 150, 0));
                } else if (scoreVal >= 50) {
                    setForeground(Color.ORANGE);
                } else {
                    setForeground(Color.RED);
                }
            }

            return c;
        }
    }

    /**
     * Renderer for level column.
     */
    private static class LevelCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (value instanceof String level) {
                switch (level) {
                    case "VERY_STABLE", "STABLE" -> setForeground(new Color(0, 150, 0));
                    case "MODERATE" -> setForeground(Color.ORANGE);
                    case "UNSTABLE", "VERY_UNSTABLE" -> setForeground(Color.RED);
                }
            }

            return c;
        }
    }
}
