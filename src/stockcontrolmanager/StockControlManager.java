/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stockcontrolmanager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 *
 * @author Everdt
 */
public class StockControlManager {

   
    public Connection dbcon;
    public PreparedStatement connStatement;
    public PreparedStatement searchStatement;
    public PreparedStatement lowStatement;
    public PreparedStatement updateStatement;
    public View view;
    public StockControlManager sCManager;
    
    private final String url = "jdbc:mysql://localhost:3306/";
    private final String database = "stock";
    private final String parameters = "?autoReconnect=true&useSSL=false";
    private final String username = "root";
    private final String password = "Something23";
    
    
    public static void main(String[] args) {
        StockControlManager stockControl = new StockControlManager();
        stockControl.sCManager = stockControl;
        stockControl.createView();
        stockControl.dbConnect();
    }
    
     public void createView() {
        view = new View(sCManager);
        view.setVisible(true);
    }
     
     public void dbConnect() {
         
        try {
            
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            dbcon = DriverManager.getConnection(url + database + parameters, username, password);
            System.out.println("Connected to DB!");
            if (!dbcon.isClosed()) {
                view.dbLabelStatus.setText("Status: Online");
            } else {
                view.dbLabelStatus.setText("Status: Offline");
            }
            connStatement = dbcon.prepareStatement("SELECT clinicName FROM clinic");
            ResultSet rs = connStatement.executeQuery();

            while (rs.next()) {
                view.clinicCB.addItem(rs.getString("clinicName"));
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException ex) {
            System.err.println("Error connecting to the database: " + ex);
        }
    }
     
     public void checkDB(int activeClinic) {
        try {
            searchStatement = dbcon.prepareStatement("SELECT s.clinicID, clinicName, medicineName, quantity FROM stock s JOIN medicine "
                    + "ON s.medicineID = medicine.medicineID JOIN clinic ON s.clinicID = clinic.clinicID WHERE s.clinicID = " + activeClinic);
            ResultSet rs = searchStatement.executeQuery();
            view.taOutput.append("CHECK STOCK:\n");
            while (rs.next()) {
                view.taOutput.append("There are " + rs.getString("quantity") + " avaliable stock of " + rs.getString("medicineName") + " in "
                        + rs.getString("clinicName") + "\n");
                if ((Integer.parseInt(rs.getString("quantity"))) < 5) {
                    JOptionPane.showMessageDialog(null, "WARNING: " + rs.getString("clinicName") + " has low stock of " + rs.getString("medicineName")
                            + " (" + rs.getString("quantity") + " units left)", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error searching the database: " + ex);
        }
    }
     
      public void updateStockDB(int activeClinic, String medicineName, int newQty) {
        try {
            view.taOutput.append("UPDATING STOCK\n");
            updateStatement = dbcon.prepareStatement("UPDATE stock s JOIN medicine ON s.medicineID = medicine.medicineID JOIN clinic ON "
                    + "s.clinicID = clinic.clinicID SET quantity = " + newQty + " WHERE medicineName = '" + medicineName 
                    + "' AND clinic.clinicID = " + activeClinic);
            int rowsAffected = updateStatement.executeUpdate();
            if (rowsAffected > 0) {
                view.taOutput.append("Update has been performed successfully.\n");
            } else {
                view.taOutput.append("No such record exists, update failed.\n");
            }
            view.taOutput.append("\n");
        } catch (SQLException ex) {
            System.err.println("Error updating the database: " + ex);
        }
    }
      
      public void stockRunningLow() {
        try {
            view.taOutput.append("CLINICS RUNNING LOW ON STOCK:\n");
            lowStatement = dbcon.prepareStatement("SELECT s.clinicID, clinicName, medicineName, quantity FROM stock s JOIN medicine ON "
                    + "s.medicineID = medicine.medicineID JOIN clinic ON s.clinicID = clinic.clinicID ORDER BY clinicName ASC");
            ResultSet rs = lowStatement.executeQuery();
            while (rs.next()) {
                if ((Integer.parseInt(rs.getString("quantity"))) < 5) {
                    int difference = (5 - Integer.parseInt(rs.getString("quantity")));
                    view.taOutput.append(rs.getString("clinicName") + " is low on " + rs.getString("medicineName") + " by at least " + difference 
                            + " units. \n");
                }
            }
            view.taOutput.append("\n");
        } catch (SQLException ex) {
            System.err.println("Error searching the database: " + ex);
        }
    }
}
