/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package pointofsalesproject;
import java.awt.Component;
import java.time.LocalTime;
import javax.swing.JOptionPane;
/**
 *
 * @author nifem
 */
public class Backup {
private static final String MYSQLDUMP_PATH = "C:\\Users\\nifem\\OneDrive\\Documents\\dumps\\Dump20240210";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "@Postgres1234";
    private static final String DB_NAME = "pointofsales";
    private static final String OUTPUT_PATH = "C:\\Users\\nifem\\OneDrive\\Documents\\NetBeansProjects\\POS Project\\dump backup";

  public static void main(String[] args) {
BackupScheduler backupschedule = new BackupScheduler();
backupschedule.start();
        // TODO code application logic here
    }

public static class BackupScheduler extends Thread {

        @Override
        public void run() {
            while (true) {
                LocalTime currentTime = LocalTime.now();
                if (currentTime.getHour() == 14 && currentTime.getMinute() == 00) {
                    performBackup(MYSQLDUMP_PATH, USERNAME, PASSWORD, DB_NAME, OUTPUT_PATH, null);
                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(60000); // Sleep for 1 minute
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void performBackup(String mysqldumpPath, String username, String password, String dbName, String outputPath, Component parentComponent) {
        // Backup logic remains the same
        try {
            System.out.println("Starting database backup...");

            ProcessBuilder processBuilder = new ProcessBuilder(
                    mysqldumpPath, "-u", username, "-p" + password,
                    "--databases", dbName, "-r", outputPath);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            if (process == null) {
                System.out.println("Error starting the backup process. Check if mysqldump is installed and the path is correct.");
                return;
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                JOptionPane.showMessageDialog(parentComponent, "Automatic backup done");
                System.out.println("Database backup completed successfully.");
            } else {
                System.out.println("Error during database backup. Exit code: " + exitCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param args the command line arguments
     */
  
    
}
