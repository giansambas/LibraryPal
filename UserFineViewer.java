import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.io.FileOutputStream;
import java.sql.*;

import com.itextpdf.text.*;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.*;

public class UserFineViewer extends JFrame {
    private Connection conn;
    private JTextField searchField;
    private JTable table;
    private DefaultTableModel model;

    public UserFineViewer() {
        setTitle("LibraryPal - User Fine Viewer");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
        connectDatabase();
    }

    private void connectDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            String dbPath = "C:/Users/Gian Sambas/Desktop/java/LibraryPal/librarypal.db";
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initUI() {
    JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    mainPanel.setBackground(Color.decode("#ffffff"));

    JPanel topPanel = new JPanel(new BorderLayout(10, 10));
    topPanel.setBackground(Color.decode("#ffffff"));

    try {
        ImageIcon logoIcon = new ImageIcon("resources/LibPal_Logo.png");
        java.awt.Image scaledImage = logoIcon.getImage().getScaledInstance(110, 40, java.awt.Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(scaledImage));
        logoLabel.setHorizontalAlignment(SwingConstants.LEFT);
        topPanel.add(logoLabel, BorderLayout.WEST);
    } catch (Exception ex) {
        System.err.println("Logo image not found: " + ex.getMessage());
    }

    JLabel titleLabel = new JLabel("Check My Library Fines", SwingConstants.CENTER);
    titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 20));
    titleLabel.setForeground(new Color(40, 70, 90));
    topPanel.add(titleLabel, BorderLayout.NORTH);

    JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    centerPanel.setBackground(Color.decode("#ffffff"));

    searchField = new JTextField(25);
    searchField.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
    searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(160, 200, 160)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
    ));

    JButton searchBtn = new JButton("Search");
    JButton exportBtn = new JButton("Export to PDF");

    styleButton(searchBtn);
    styleButton(exportBtn);

    centerPanel.add(searchField);
    centerPanel.add(searchBtn);
    centerPanel.add(exportBtn);
    topPanel.add(centerPanel, BorderLayout.CENTER);

    mainPanel.add(topPanel, BorderLayout.NORTH);

    model = new DefaultTableModel(new String[]{
            "ID", "Name", "Amount", "Reason", "Date", "ID Number", "Contact"
    }, 0);

    table = new JTable(model) {
        public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int col) {
            Component c = super.prepareRenderer(renderer, row, col);
            if (!isRowSelected(row)) {
                c.setBackground(row % 2 == 0 ? new Color(245, 250, 245) : Color.WHITE);
            } else {
                c.setBackground(new Color(200, 235, 200));
            }
            return c;
        }
    };

    table.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
    table.setRowHeight(28);
    table.setShowHorizontalLines(true);
    table.setGridColor(new Color(200, 230, 200));
    table.setSelectionBackground(new Color(153, 255, 153));

    JTableHeader header = table.getTableHeader();
    header.setBackground(new Color(34, 139, 87));
    header.setForeground(Color.WHITE);
    header.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));

    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.setPreferredSize(new Dimension(750, 280));
    mainPanel.add(scrollPane, BorderLayout.CENTER);

    add(mainPanel);

    searchBtn.addActionListener(e -> searchFines());
    exportBtn.addActionListener(e -> exportToPDF());
}



    private void styleButton(JButton button) {
        button.setBackground(new Color(34, 139, 87));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        button.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
    }

    private void searchFines() {
        model.setRowCount(0);
        String input = searchField.getText().trim();
        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a name or ID number.");
            return;
        }

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM fines WHERE name LIKE ? OR id_number LIKE ?");
            stmt.setString(1, "%" + input + "%");
            stmt.setString(2, "%" + input + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        String.format("₱%.2f", rs.getDouble("amount")),
                        rs.getString("reason"),
                        rs.getString("date"),
                        rs.getString("id_number"),
                        rs.getString("contact_number")
                });
            }

            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No fines found for: " + input);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Query error: " + e.getMessage());
            e.printStackTrace();
        }
    }

   private void exportToPDF() {
    if (model.getRowCount() == 0) {
        JOptionPane.showMessageDialog(this, "No data to export.");
        return;
    }

    Document document = new Document();
    try {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File("fines.pdf"));
        int result = fileChooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        PdfWriter.getInstance(document, new FileOutputStream(fileChooser.getSelectedFile()));
        document.open();

        try {
            com.itextpdf.text.Image logo = com.itextpdf.text.Image.getInstance("resources/LibPal_Logo.png");
            logo.scaleToFit(80, 50);
            logo.setAlignment(com.itextpdf.text.Image.ALIGN_LEFT);

            com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 16, com.itextpdf.text.Font.BOLD, BaseColor.WHITE
            );
            com.itextpdf.text.Font smallFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.NORMAL, BaseColor.WHITE
            );

            PdfPTable headerTable = new PdfPTable(3);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new int[]{1, 3, 2});

            PdfPCell logoCell = new PdfPCell(logo, false);
            logoCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
            logoCell.setBackgroundColor(new BaseColor(34, 139, 87));
            headerTable.addCell(logoCell);

            PdfPCell titleCell = new PdfPCell(new Phrase("Fine Report", headerFont));
            titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            titleCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            titleCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
            titleCell.setBackgroundColor(new BaseColor(34, 139, 87));
            headerTable.addCell(titleCell);

            PdfPCell searchCell = new PdfPCell(new Phrase("Search: " + searchField.getText(), headerFont));
            searchCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            searchCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            searchCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
            searchCell.setBackgroundColor(new BaseColor(34, 139, 87));
            headerTable.addCell(searchCell);

            document.add(headerTable);
            document.add(new Paragraph(" "));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Logo could not be loaded.");
        }

        com.itextpdf.text.Font bodyFont = new com.itextpdf.text.Font(
            com.itextpdf.text.Font.FontFamily.COURIER, 12, com.itextpdf.text.Font.NORMAL
        );

        for (int row = 0; row < model.getRowCount(); row++) {
            int id = Integer.parseInt(model.getValueAt(row, 0).toString());
            String name = model.getValueAt(row, 1).toString();
            String amount = model.getValueAt(row, 2).toString();
            String reason = model.getValueAt(row, 3).toString();
            String date = model.getValueAt(row, 4).toString();
            String idNumber = model.getValueAt(row, 5).toString();
            String contactNumber = model.getValueAt(row, 6).toString();

            String fineInfo = String.format(
                "Fine ID     : %d\n" +
                "Name        : %s\n" +
                "Amount      : %s\n" +
                "Reason      : %s\n" +
                "Date Issued : %s\n" +
                "ID Number   : %s\n" +
                "Contact     : %s\n",
                id, name, amount, reason, date, idNumber, contactNumber
            );

            Paragraph fineParagraph = new Paragraph(fineInfo, bodyFont);
            fineParagraph.setSpacingAfter(10f);
            document.add(fineParagraph);
        }

        document.close();
        JOptionPane.showMessageDialog(this, "PDF exported successfully.");

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Failed to export PDF: " + e.getMessage());
        e.printStackTrace();
    }
}





    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UserFineViewer().setVisible(true));
    }
}
