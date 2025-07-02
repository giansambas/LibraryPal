import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class M2_Summative extends JFrame {
    private Connection conn;
    private JTable fineTable;
    private JPanel formPanel;
    private DefaultTableModel tableModel;
    private JTextField nameField, amountField, reasonField, idField, contactField;

    public M2_Summative() {
        setTitle("LibraryPal");
        setSize(720, 520);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setUndecorated(false);

        connectDatabase();

        if (conn == null) {
            JOptionPane.showMessageDialog(this,
                "Fatal error: Could not connect to database.\n\n" +
                "Check that:\n" +
                "1. The SQLite JDBC driver is in your classpath.\n" +
                "2. You have permission to write to the directory.\n" +
                "3. The path to the database is correct.");
            System.exit(1);
        }

        initUI();
        loadFines();
    }

    private void connectDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            String dbPath = "C:/Users/Gian Sambas/Desktop/java/LibraryPal/librarypal.db";
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);

            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS fines (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, amount REAL, reason TEXT, date TEXT,  " +
                "id_number TEXT, contact_number TEXT)");

        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "SQLite JDBC driver not found: " + e.getMessage());
            e.printStackTrace();
            conn = null;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage());
            e.printStackTrace();
            conn = null;
        }
    }

    private void initUI() {
    JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    mainPanel.setBackground(Color.decode("#f5f7fa"));

    JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    logoPanel.setBackground(Color.decode("#f5f7fa"));

    try {
        java.net.URL imageUrl = getClass().getClassLoader().getResource("resources/LibPal_Logo.png");
        if (imageUrl != null) {
            ImageIcon logoIcon = new ImageIcon(imageUrl);
            Image scaledImage = logoIcon.getImage().getScaledInstance(110, 40, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(scaledImage));
            logoPanel.add(logoLabel);
        } else {
            System.err.println("⚠️ Logo image not found at: resources/LibPal_Logo.png");
        }
    } catch (Exception ex) {
        ex.printStackTrace();
    }

    JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    searchPanel.setBackground(Color.decode("#f5f7fa"));

    JTextField searchField = new JTextField(20);
    JButton searchButton = new JButton("Search");
    styleButton(searchButton);

    searchPanel.add(new JLabel("Search Name or ID:"));
    searchPanel.add(searchField);
    searchPanel.add(searchButton);

    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.setBackground(Color.decode("#f5f7fa"));
    topPanel.add(logoPanel, BorderLayout.WEST);
    topPanel.add(searchPanel, BorderLayout.CENTER);

    mainPanel.add(topPanel, BorderLayout.NORTH);

    searchButton.addActionListener(e -> searchFine(searchField.getText().trim()));

    tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Amount", "Reason", "Date", "ID Number", "Contact"}, 0);

    fineTable = new JTable(tableModel) {
        @Override
        public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
            Component c = super.prepareRenderer(renderer, row, column);
            if (!isRowSelected(row)) {
                c.setBackground(row % 2 == 0 ? new Color(245, 250, 245) : Color.WHITE);
            } else {
                c.setBackground(new Color(200, 235, 200));
            }
            return c;
        }
    };
    fineTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    fineTable.setRowHeight(28);
    fineTable.setShowHorizontalLines(true);
    fineTable.setGridColor(new Color(200, 230, 200));
    fineTable.setSelectionBackground(new Color(153, 255, 153));
    fineTable.setSelectionForeground(Color.BLACK);

    JTableHeader header = fineTable.getTableHeader();
    header.setBackground(new Color(34, 139, 87));
    header.setForeground(Color.WHITE);
    header.setFont(new Font("Segoe UI", Font.BOLD, 14));

    JScrollPane scrollPane = new JScrollPane(fineTable);
    scrollPane.setPreferredSize(new Dimension(640, 260));
    mainPanel.add(scrollPane, BorderLayout.CENTER);

    formPanel = new JPanel(new GridBagLayout());
    formPanel.setBackground(Color.decode("#f5f7fa"));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(6, 6, 6, 6);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    Font labelFont = new Font("Segoe UI", Font.PLAIN, 14);
    Color borderColor = new Color(100, 180, 100);

    JLabel nameLabel = new JLabel("Name:");
    JLabel amountLabel = new JLabel("Amount:");
    JLabel reasonLabel = new JLabel("Reason:");
    JLabel idLabel = new JLabel("ID Number:");
    JLabel contactLabel = new JLabel("Contact:");

    nameLabel.setFont(labelFont);
    amountLabel.setFont(labelFont);
    reasonLabel.setFont(labelFont);
    idLabel.setFont(labelFont);
    contactLabel.setFont(labelFont);

    nameField = new JTextField(15);
    amountField = new JTextField(10);
    reasonField = new JTextField(15);
    idField = new JTextField(10);
    contactField = new JTextField(12);

    JTextField[] fields = {nameField, amountField, reasonField, idField, contactField, searchField};
    for (JTextField field : fields) {
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    }

    JButton addButton = new JButton("Add Fine");
    JButton viewButton = new JButton("View Fine");
    JButton deleteButton = new JButton("Delete Fine");
    JButton printButton = new JButton("Print Fines");

    JButton[] buttons = {addButton, viewButton, deleteButton, printButton};
    for (JButton button : buttons) {
        styleButton(button);
    }

    gbc.gridx = 0; gbc.gridy = 0; formPanel.add(nameLabel, gbc);
    gbc.gridx = 1; formPanel.add(nameField, gbc);
    gbc.gridx = 2; formPanel.add(idLabel, gbc);
    gbc.gridx = 3; formPanel.add(idField, gbc);

    gbc.gridx = 0; gbc.gridy = 1; formPanel.add(amountLabel, gbc);
    gbc.gridx = 1; formPanel.add(amountField, gbc);
    gbc.gridx = 2; formPanel.add(contactLabel, gbc);
    gbc.gridx = 3; formPanel.add(contactField, gbc);

    gbc.gridx = 0; gbc.gridy = 2; formPanel.add(reasonLabel, gbc);
    gbc.gridx = 1; gbc.gridwidth = 3; formPanel.add(reasonField, gbc);
    gbc.gridwidth = 1;

    gbc.gridx = 0; gbc.gridy = 3; formPanel.add(addButton, gbc);
    gbc.gridx = 1; formPanel.add(viewButton, gbc);
    gbc.gridx = 2; formPanel.add(deleteButton, gbc);
    gbc.gridx = 3; formPanel.add(printButton, gbc);

    mainPanel.add(formPanel, BorderLayout.SOUTH);
    add(mainPanel);

    addButton.addActionListener(e -> addFine());
    viewButton.addActionListener(e -> viewFine());
    deleteButton.addActionListener(e -> deleteFine());
    printButton.addActionListener(e -> printFinesToPDF());
}


    private void styleButton(JButton button) {
        button.setBackground(new Color(34, 139, 87));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
    }

    private void loadFines() {
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Cannot load fines: No database connection.");
            return;
        }

        try {
            tableModel.setRowCount(0);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM fines");

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        String.format("₱%.2f", rs.getDouble("amount")),
                        rs.getString("reason"),
                        rs.getString("date"),
                        rs.getString("id_number"),
                        rs.getString("contact_number")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load fines: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void searchFine(String keyword) {
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Cannot search: No database connection.");
            return;
        }

        try {
            tableModel.setRowCount(0);
            PreparedStatement pstmt = conn.prepareStatement(
             "SELECT * FROM fines WHERE name LIKE ? OR id_number LIKE ?");
            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setString(2, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        String.format("₱%.2f", rs.getDouble("amount")),
                        rs.getString("reason"),
                        rs.getString("date"),
                        rs.getString("id_number"),
                        rs.getString("contact_number")
                });
            }

            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No results found for: " + keyword);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Search error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addFine() {
        String name = nameField.getText().trim();
        String amountText = amountField.getText().trim();
        String reason = reasonField.getText().trim();
        String idNum = idField.getText().trim();
        String contact = contactField.getText().trim();
        String date = java.time.LocalDate.now().toString();

        if (name.isEmpty() || amountText.isEmpty() || reason.isEmpty() || idNum.isEmpty() || contact.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);
            PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO fines (name, amount, reason, date, id_number, contact_number) VALUES (?, ?, ?, ?, ?, ?)");
            pstmt.setString(1, name);
            pstmt.setDouble(2, amount);
            pstmt.setString(3, reason);
            pstmt.setString(4, date);
            pstmt.setString(5, idNum);
            pstmt.setString(6, contact);
            pstmt.executeUpdate();

            nameField.setText("");
            amountField.setText("");
            reasonField.setText("");
            idField.setText("");
            contactField.setText("");
            loadFines();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Amount must be a number.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding fine: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void viewFine() {
    int row = fineTable.getSelectedRow();
    if (row == -1) {
        JOptionPane.showMessageDialog(this, "Please select a fine to view.");
        return;
    }

    int id = (int) tableModel.getValueAt(row, 0);
    String name = (String) tableModel.getValueAt(row, 1);
    String amount = tableModel.getValueAt(row, 2).toString();
    String reason = (String) tableModel.getValueAt(row, 3);
    String date = (String) tableModel.getValueAt(row, 4);
    String idNumber = (String) tableModel.getValueAt(row, 5);
    String contactNumber = (String) tableModel.getValueAt(row, 6);

    String message = String.format(
        "📄 Fine Details\n\n" +
        "Fine ID     : %d\n" +
        "Name        : %s\n" +
        "Amount      : %s\n" +
        "Reason      : %s\n" +
        "Date Issued : %s\n" +
        "ID Number   : %s\n" +
        "Contact     : %s",
        id, name, amount, reason, date, idNumber, contactNumber
    );

    JOptionPane.showMessageDialog(this, message, "Fine Details", JOptionPane.INFORMATION_MESSAGE);
}


    private void deleteFine() {
        int row = fineTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a fine to delete.");
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete the fine for \"" + name + "\"?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            PreparedStatement pstmt = conn.prepareStatement("DELETE FROM fines WHERE id = ?");
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            loadFines();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error deleting fine: " + e.getMessage());
            e.printStackTrace();
        }
    }

  private void printFinesToPDF() {
    com.itextpdf.text.Document document = new com.itextpdf.text.Document();
    try {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save PDF Report");
        fileChooser.setSelectedFile(new java.io.File("Admin_LibraryFines_Report.pdf"));
        int result = fileChooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        java.io.File selectedFile = fileChooser.getSelectedFile();

        com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(selectedFile));
        document.open();

        com.itextpdf.text.Image logo = com.itextpdf.text.Image.getInstance("resources/LibPal_Logo.png");
        logo.scaleToFit(120, 60);
        logo.setAlignment(com.itextpdf.text.Image.ALIGN_LEFT);

        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(
            com.itextpdf.text.Font.FontFamily.HELVETICA, 16, com.itextpdf.text.Font.BOLD,
            new com.itextpdf.text.BaseColor(255, 255, 255)
        );

        com.itextpdf.text.pdf.PdfPTable headerTable = new com.itextpdf.text.pdf.PdfPTable(2);
        headerTable.setWidthPercentage(100);
        float[] columnWidths = {1f, 4f};
        headerTable.setWidths(columnWidths);

        com.itextpdf.text.pdf.PdfPCell logoCell = new com.itextpdf.text.pdf.PdfPCell(logo);
        logoCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
        logoCell.setBackgroundColor(new com.itextpdf.text.BaseColor(0, 153, 0));
        headerTable.addCell(logoCell);

        com.itextpdf.text.pdf.PdfPCell titleCell = new com.itextpdf.text.pdf.PdfPCell(
            new com.itextpdf.text.Phrase("", titleFont)
        );
        titleCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
        titleCell.setVerticalAlignment(com.itextpdf.text.Element.ALIGN_MIDDLE);
        titleCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        titleCell.setBackgroundColor(new com.itextpdf.text.BaseColor(34, 139, 87));
        headerTable.addCell(titleCell);

        document.add(headerTable);
        document.add(new com.itextpdf.text.Paragraph(" "));

        com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(
            com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD
        );
        com.itextpdf.text.Font cellFont = new com.itextpdf.text.Font(
            com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.NORMAL
        );

        com.itextpdf.text.pdf.PdfPTable pdfTable = new com.itextpdf.text.pdf.PdfPTable(7);
        pdfTable.setWidthPercentage(100);
        pdfTable.setSpacingBefore(10f);
        pdfTable.setSpacingAfter(10f);

        String[] columnNames = {"ID", "Name", "Amount", "Reason", "Date", "ID Number", "Contact"};
        for (String col : columnNames) {
            com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(
                new com.itextpdf.text.Phrase(col, headerFont)
            );
            cell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
            cell.setPadding(5);
            pdfTable.addCell(cell);
        }

        for (int row = 0; row < tableModel.getRowCount(); row++) {
            for (int col = 0; col < tableModel.getColumnCount(); col++) {
                String value = tableModel.getValueAt(row, col).toString();
                pdfTable.addCell(new com.itextpdf.text.Phrase(value, cellFont));
            }
        }

        document.add(pdfTable);
        document.close();

        JOptionPane.showMessageDialog(this, "Fines exported to: " + selectedFile.getAbsolutePath());
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error exporting to PDF: " + e.getMessage());
        e.printStackTrace();
    }
}





    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new M2_Summative().setVisible(true));
    }
}
