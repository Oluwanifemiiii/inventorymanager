/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package pointofsalesproject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JOptionPane;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import pointofsalesproject.Backup.BackupScheduler;

/**
 *
 * @author nifem
 */
public class CashierHomePage extends javax.swing.JFrame {
    /**
     * Creates new form CaashierHomePage
     */
    public CashierHomePage() {
        initComponents();
        checkBirthdays();
        checkLowStock();
        checkExpiryDates();
        BackupScheduler backupscheduler = new BackupScheduler();
        backupscheduler.start();
        jTextField4.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterTable();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterTable();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // Not needed for plain text fields
            }
        });
        displayStock();
        try {
            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
            int rowCount = model.getRowCount();

            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/pointofsales", "root", "@Postgres1234");
            // Prepare SQL query
            PreparedStatement ps = con.prepareStatement("SELECT * FROM stock");
            // Execute query and get result set
            ResultSet rs = ps.executeQuery();

            // Populate table model with data
            while (rs.next()) {
                Object[] row = new Object[]{rs.getString("productname"), rs.getString("productcode"), rs.getString("manufacturer"), rs.getString("price")};
                model.addRow(row);
                System.out.println(row);
            }

            // Set table model to JTable component
            jTable1.setModel(model);

        } catch (Exception e) {
            e.printStackTrace(); // Log the exception for debugging
            JOptionPane.showMessageDialog(this, "Error connecting to the database", "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            // Close database resources
        }
    }

public void checkExpiryDates() {
        Calendar oneWeekAhead = Calendar.getInstance();
        oneWeekAhead.add(Calendar.DATE, 7); // Add 7 days to the current date

        try {
            // Connect to the database
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/pointofsales", "root", "@Postgres1234");

            // Prepare statement
            PreparedStatement selectExpiryStatement = con.prepareStatement("SELECT productname, expirydate FROM stock WHERE expirydate <= ?");
            selectExpiryStatement.setDate(1, new java.sql.Date(oneWeekAhead.getTimeInMillis()));

            // Execute query
            ResultSet resultSet = selectExpiryStatement.executeQuery();

            // Check if there are products with expiry dates within a week
            boolean expiryExists = false;
            StringBuilder expiringProducts = new StringBuilder();

            while (resultSet.next()) {
                expiryExists = true;
                String productName = resultSet.getString("productname");
                Date expiryDate = resultSet.getDate("expirydate");

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String formattedExpiryDate = dateFormat.format(expiryDate);

                expiringProducts.append(productName).append(" (Expiry Date: ").append(formattedExpiryDate).append(")\n");
            }


            // Show message if products are expiring within a week
            if (expiryExists) {
                String message = "The following products are expiring within a week:\n\n" + expiringProducts.toString();
                JOptionPane.showMessageDialog(null, message, "Expiry Notification", JOptionPane.WARNING_MESSAGE);
            } else {
                System.out.println("No product is about to expire");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
            JOptionPane.showMessageDialog(null, "Error: Unable to check expiry dates.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

public void checkLowStock() {
        int threshold = 30; // Threshold for low stock

        try {
            // Connect to the database
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/pointofsales", "root", "@Postgres1234");


            // Prepare statement
            PreparedStatement selectLowStockStatement = con.prepareStatement("SELECT productname, quantity FROM stock WHERE quantity < ?");
            selectLowStockStatement.setInt(1, threshold);

            // Execute query
            ResultSet resultSet = selectLowStockStatement.executeQuery();

            // Check if there are items with low stock
            boolean lowStockExists = false;
            StringBuilder lowStockItems = new StringBuilder();

            while (resultSet.next()) {
                lowStockExists = true;
                String itemName = resultSet.getString("productname");
                int quantity = resultSet.getInt("quantity");
                lowStockItems.append(itemName).append(" (Quantity: ").append(quantity).append(")\n");
            }


            // Show message if low stock exists
            if (lowStockExists) {
                String message = "The following items are low in stock:\n\n" + lowStockItems.toString();
                JOptionPane.showMessageDialog(null, message, "Low Stock Notification", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "All items are in stock.", "Stock Status", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }
    }


private void checkBirthdays() {
    String email = LoginPage.jTextEmailField1.getText();

    try {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/pointofsales", "root", "@Postgres1234");

        // First query: search for the email in the database
        PreparedStatement searchEmailStatement = connection.prepareStatement("SELECT * FROM users WHERE email = ?");
        searchEmailStatement.setString(1, email);
        ResultSet emailResultSet = searchEmailStatement.executeQuery();

        if (emailResultSet.next()) { // Email found
            // Retrieve name and date of birth
            String name = emailResultSet.getString(1);
            Date dateofbirth = emailResultSet.getDate(2);

            // Check if it's the person's birthday
            if (isBirthdayToday(dateofbirth)) {
                showBirthdayMessage(name);
                sendBirthdayEmail(email, name);
            } else {
                System.out.println("It's not the person's birthday today.");
            }
        } else {
            System.out.println("No user found for email: " + email);
        }

        connection.close();
    } catch (Exception e) {
        JOptionPane.showMessageDialog(rootPane, e);
    }
}

private void sendBirthdayEmail(String toemail, String name) throws MessagingException {
        final String fromEmail = "cbtsimstest123@gmail.com";
        final String emailPassword = "nxcgduystofawgbg";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, emailPassword);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toemail));
        message.setSubject("Happy Birthday!!!!");
        message.setText("Dear ," + name + "\n\nHappy birthday to you. Best wishes");

        Transport.send(message);
    }

 private static boolean isBirthdayToday(Date dateofbirth) {
        Calendar today = Calendar.getInstance();
        Calendar birthday = Calendar.getInstance();
        birthday.setTime(dateofbirth);

        return today.get(Calendar.MONTH) == birthday.get(Calendar.MONTH)
                && today.get(Calendar.DAY_OF_MONTH) == birthday.get(Calendar.DAY_OF_MONTH);
    }


 private static void showBirthdayMessage(String name) {
        String message = "Happy Birthday, " + name + "!";
        JOptionPane.showMessageDialog(null, message, "Birthday Wishes", JOptionPane.INFORMATION_MESSAGE);
    }

    private void addSelectedItemToCart() {
        // Get the selected row and item details from table1
        int selectedRow = jTable1.getSelectedRow();

        if (selectedRow != -1) { // Check if a row is selected
            String selectedItem = (String) jTable1.getValueAt(selectedRow, 0); // Assuming the item name is in the first column
            double price = Double.parseDouble(jTable1.getValueAt(selectedRow, 3).toString());
            int quantity = (int) jSpinner1.getValue();
            double total = price * quantity;

            // Add the selected item and quantity to table2
            DefaultTableModel model2 = (DefaultTableModel) jReceiptTable.getModel();
            model2.addRow(new Object[]{selectedItem, quantity, price, total});
        } else {
            JOptionPane.showMessageDialog(this, "Please select an item from the first table.");
        }
    }

    private void calculateAndDisplayTotal() {
        // Calculate the sum of the "Total" column in table2
        double totalSum = 0;
        for (int row = 0; row < jReceiptTable.getRowCount(); row++) {
            totalSum += Double.parseDouble(jReceiptTable.getValueAt(row, 3).toString());
        }

        // Display the total sum in the JTextField
        jTextField1.setText(String.valueOf(totalSum));
    }

    private void calculateAndDisplayChange() {
        try {
            // Get the total sum and amount paid from the JTextFields
            double totalSum = Double.parseDouble(jTextField1.getText());
            double amountPaid = Double.parseDouble(jTextField2.getText());

            // Calculate the change
            double change = amountPaid - totalSum;

            // Display the change in the changeTextField
            jTextField3.setText(String.valueOf(change));

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(rootPane, "Invalid input. Please enter valid numbers.");
        }
    }

    public void filterTable() {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        jTable1.setRowSorter(sorter);

        String text = jTextField4.getText();

        if (text.length() == 0) {
            sorter.setRowFilter(null);
        } else {
            try {
                // Case-insensitive filter based on product name
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 0));
            } catch (java.util.regex.PatternSyntaxException e) {
                // Invalid regex pattern, ignore it
            }
        }
    }

    public void displayStock() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/pointofsales", "root", "@Postgres1234");
            java.sql.Date currentDate = new java.sql.Date(System.currentTimeMillis());

            String query = "SELECT productname, productcode, manufacturer, price FROM stock WHERE expiryDate > ?";
            PreparedStatement statement = con.prepareStatement(query);
            statement.setDate(1, currentDate);

            ResultSet rsProducts = statement.executeQuery();

            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
            model.setRowCount(0);

            while (rsProducts.next()) {
                String manufacturer = rsProducts.getString(3);
                String productname = rsProducts.getString(1);
                String productcode = rsProducts.getString(2);
                String price = rsProducts.getString(4);
                model.addRow(new Object[]{productname, productcode, manufacturer, price});
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }
    }


public void updateQuantityOnPayment() {
        // Iterate through the items sold in the table
        for (int i = 0; i < jReceiptTable.getRowCount(); i++) {
            String productname = jReceiptTable.getValueAt(i, 0).toString(); // Get item name from the table
            int quantitySold = Integer.parseInt(jReceiptTable.getValueAt(i, 1).toString()); // Get quantity sold from the table

            try {
                // Connect to the database
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/pointofsales", "root", "@Postgres1234");

                // Retrieve current quantity from the database
                PreparedStatement selectQuantityStatement = con.prepareStatement("SELECT quantity FROM stock WHERE productname = ?");
                selectQuantityStatement.setString(1, productname);
                ResultSet resultSet = selectQuantityStatement.executeQuery();

                if (resultSet.next()) {
                    int currentQuantity = resultSet.getInt("quantity");

                    // Calculate new quantity
                    int newQuantity = currentQuantity - quantitySold;
                    if (newQuantity >= 0) { // Ensure quantity doesn't go negative
                        // Update quantity in the database
                        PreparedStatement updateQuantityStatement = con.prepareStatement("UPDATE stock SET quantity = ? WHERE productname = ?");
                        updateQuantityStatement.setInt(1, newQuantity);
                        updateQuantityStatement.setString(2, productname);
                        int rowsUpdated = updateQuantityStatement.executeUpdate();

                        if (rowsUpdated > 0) {
                            System.out.println("Quantity updated successfully for item: " + productname);
                        } else {
                            System.out.println("Failed to update quantity for item: " + productname);
                        }
                    } else {
                        System.out.println("Insufficient quantity in stock for item: " + productname);
                    }
                } else {
                    System.out.println("Item not found in the database: " + productname);
                }

                con.close();
            } catch (Exception e) {
               JOptionPane.showMessageDialog(rootPane, e);
            }
        }
    }

public void checkExpiry() {
        DefaultTableModel model = (DefaultTableModel) jReceiptTable.getModel();

        try {
            // Connect to the database
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/pointofsales", "root", "@Postgres1234");

            for (int row = 0; row < model.getRowCount(); row++) {
                String productname = model.getValueAt(row, 0).toString(); // Get item name from the table

                // Prepare statement
                PreparedStatement selectExpiryStatement = con.prepareStatement("SELECT expirydate FROM stock WHERE productname = ?");
                selectExpiryStatement.setString(1, productname);

                // Execute query
                ResultSet resultSet = selectExpiryStatement.executeQuery();

                if (resultSet.next()) {
                    Date expiryDate = resultSet.getDate("expirydate");

                    // Check if the product has expired
                    if (hasExpired(expiryDate)) {
                        String formattedExpiryDate = new SimpleDateFormat("yyyy-MM-dd").format(expiryDate);
                        String message = "The item '" + productname + "' has expired (Expiry Date: " + formattedExpiryDate + ").\nPlease remove it from the sale.";
                        JOptionPane.showMessageDialog(null, message, "Expiry Alert", JOptionPane.WARNING_MESSAGE);

                        // Remove the expired item from the table
                        model.removeRow(row);
                        row--; // Decrement row index as the table size decreases after removing the row
                    }
                }
            }          
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: Unable to check expiry dates.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

public void InsertIntoSalesReportDatabase() {
    try {
        DefaultTableModel model = (DefaultTableModel) jReceiptTable.getModel();
        int rowCount = model.getRowCount();

        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/pointofsales", "root", "@Postgres1234");

        for (int i = 0; i < rowCount; i++) {
            String productname = (String) model.getValueAt(i, 0);
            double quantity = Double.parseDouble(model.getValueAt(i, 1).toString());
            double price = Double.parseDouble(model.getValueAt(i, 2).toString());
            double total = Double.parseDouble(model.getValueAt(i, 3).toString());

            // Check if the product already exists in the database
            PreparedStatement checkIfExists = con.prepareStatement("SELECT * FROM stockreport WHERE productname = ?");
            checkIfExists.setString(1, productname);
            ResultSet resultSet = checkIfExists.executeQuery();

            if (resultSet.next()) {
                // Product already exists, update its quantity and total price
                double existingQuantity = resultSet.getDouble("quantitysold");
                double newQuantity = existingQuantity + quantity; // Update quantity
                double existingTotal = resultSet.getDouble("totalprice");
                double newTotal = existingTotal + total; // Update total price

                PreparedStatement updateStatement = con.prepareStatement("UPDATE stockreport SET quantitysold = ?, totalprice = ? WHERE productname = ?");
                updateStatement.setDouble(1, newQuantity);
                updateStatement.setDouble(2, newTotal);
                updateStatement.setString(3, productname);
                updateStatement.executeUpdate();

                updateStatement.close();
            } else {
                // Product does not exist, insert it into the database
                PreparedStatement insertStatement = con.prepareStatement("INSERT INTO stockreport (productname, quantitysold, price, totalprice) VALUES (?, ?, ?, ?)");
                insertStatement.setString(1, productname);
                insertStatement.setDouble(2, quantity);
                insertStatement.setDouble(3, price);
                insertStatement.setDouble(4, total);
                insertStatement.executeUpdate();

                insertStatement.close();
            }

            checkIfExists.close();
            resultSet.close();
        }

        System.out.println("Inserted into database successfully");
    } catch (Exception e) {
        JOptionPane.showMessageDialog(rootPane, e);
    }
}



    private static boolean hasExpired(Date expiryDate) {
        // Get current date
        java.util.Date currentDate = new java.util.Date();

        // Check if expiry date is before current date
        return expiryDate.before(currentDate);
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jSpinner1 = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jReceiptTable = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jButton4 = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("CASHIER HOMEPAGE");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Product Name", "Product  Code", "Manufacturer", "Price"
            }
        ));
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jTable1MousePressed(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);

        jLabel2.setText("Quantity");

        jReceiptTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Product Name", "Quantity", "Price", "Total"
            }
        ));
        jScrollPane2.setViewportView(jReceiptTable);

        jButton1.setText("ADD");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Buy");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel3.setText("Amount Paid");

        jButton3.setText("Pay");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jLabel4.setText("Change Due");

        jLabel5.setText("Amount Due");

        jLabel6.setText("Search");

        jButton4.setText("search");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jLabel7.setText("Logout");
        jLabel7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel7MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(75, 75, 75)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jTextField1)
                    .addComponent(jTextField2)
                    .addComponent(jTextField3, javax.swing.GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(35, 35, 35)
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton4)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 135, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(45, 45, 45)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 496, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(500, 500, 500))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(61, 61, 61))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(3, 3, 3)
                .addComponent(jLabel7)
                .addGap(3, 3, 3)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton4))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 44, Short.MAX_VALUE)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 254, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(33, 33, 33)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addGap(33, 33, 33))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(13, 13, 13)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton2)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5))
                        .addGap(18, 18, 18))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addGap(2, 2, 2)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(jButton3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addGap(151, 151, 151))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTable1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MousePressed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTable1MousePressed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        addSelectedItemToCart();       // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        checkExpiry();        
        calculateAndDisplayTotal();     // TODO add your handling code here:
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        calculateAndDisplayChange();
        updateQuantityOnPayment();
        InsertIntoSalesReportDatabase();
        Receipt receiptpage = new Receipt();
        receiptpage.setVisible(true);       // TODO add your handling code here:
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
      // TODO add your handling code here:
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jLabel7MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel7MouseClicked
int choice = JOptionPane.showConfirmDialog(null, "are you sure you want to log out?");
switch (choice) {
            case JOptionPane.YES_OPTION:
                LoginPage loginPage = new LoginPage();
                loginPage.setVisible(true);
                this.dispose();
                // Perform file deletion here
                break;
            case JOptionPane.NO_OPTION:
                System.out.println("Logout canceled.");
                break;
            case JOptionPane.CANCEL_OPTION:
                System.out.println("Operation canceled.");
                break;
            case JOptionPane.CLOSED_OPTION:
                System.out.println("Dialog closed without selection.");
                break;
            default:
                System.out.println("Unexpected option selected.");
        }        // TODO add your handling code here:
    }//GEN-LAST:event_jLabel7MouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(CashierHomePage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CashierHomePage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CashierHomePage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CashierHomePage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CashierHomePage().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    public static javax.swing.JTable jReceiptTable;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JTable jTable1;
    public static javax.swing.JTextField jTextField1;
    public static javax.swing.JTextField jTextField2;
    public static javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    // End of variables declaration//GEN-END:variables
}
