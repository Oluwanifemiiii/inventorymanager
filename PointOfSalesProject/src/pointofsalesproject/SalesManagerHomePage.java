/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package pointofsalesproject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
import javax.swing.JOptionPane;
import pointofsalesproject.Backup.BackupScheduler;

/**
 *
 * @author nifem
 */
public class SalesManagerHomePage extends javax.swing.JFrame {
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

    /**
     * Creates new form SalesManagerHomePage
     */
    public SalesManagerHomePage() {
        initComponents();
        checkBirthdays();
        checkExpiryDates();
        BackupScheduler backupscheduler = new BackupScheduler();
        backupscheduler.start();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel2.setText("Logout");
        jLabel2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel2MouseClicked(evt);
            }
        });

        jButton1.setText("SALES REPORT");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("CASHIER REPORT");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(746, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addGap(76, 76, 76))
            .addGroup(layout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(151, 151, 151))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(42, 42, 42)
                .addComponent(jLabel2)
                .addGap(137, 137, 137)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(353, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jLabel2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel2MouseClicked
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
        }
     // TODO add your handling code here:
    }//GEN-LAST:event_jLabel2MouseClicked

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
ItemsReportPage itemsreportPage = new ItemsReportPage();
itemsreportPage.setVisible(true);
this.dispose();        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

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
            java.util.logging.Logger.getLogger(SalesManagerHomePage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SalesManagerHomePage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SalesManagerHomePage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SalesManagerHomePage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SalesManagerHomePage().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel2;
    // End of variables declaration//GEN-END:variables
}
