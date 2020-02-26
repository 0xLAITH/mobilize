/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobilyze2;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Laith
 */
public class MobApp {

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            new mobJFrame().setVisible(true);
        });
    }
    String name = "";
    int androidVersion = 0;
    //location of DB in Nougat: version = 1
    String location1 = "";
    //location of DB in Oreo: version = 2
    String location2 = "";
    //location of DB in Pie: version = 3
    String location3 = "";
    //table in DB of interest
    String table = "";
    //all columns of interest
    ArrayList<String> tableColumns= new ArrayList<String>();
    
    public MobApp(String name, String loc1, String loc2, String loc3, String table, ArrayList tabCols){
        this.name = name;
        this.location1 = loc1;
        this.location2 = loc2;
        this.location3 = loc3;
        this.table = table;
        this.tableColumns = tabCols;
    }

    public int getAndroidVersion() {
        return androidVersion;
    }

    public void setAndroidVersion(int androidVersion) {
        this.androidVersion = androidVersion;
    }
    
    public Connection connect(){
        //String url = "jdbc:sqlite:C:/sqlite/db/msgstore.db";
        String url = "";
        switch (this.getAndroidVersion()) {
            case 1:
                {
                    url = this.location1;
                    break;
                }
            case 2:
                {
                    url = this.location2;
                    break;
                }
            default:
                {
                    url = this.location3;
                    break;
                }
        }
        
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            System.out.println(e.getMessage());
        }
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }
    
    public void FillTable(JTable table){
        int size = this.tableColumns.size();
        String columnList = "";
        int ctr = 0;
        while (ctr < size){
            if (ctr == size-1){
                columnList = columnList + this.tableColumns.get(ctr);
            }
            else {
                columnList = columnList + this.tableColumns.get(ctr) + ", ";
            }
            ctr ++;
        }
        DefaultTableModel model = new DefaultTableModel(1, this.tableColumns.size());
        table.setModel(model);
        table.repaint();
        
        String Query = "SELECT " + columnList + " FROM " + this.table;
        try{
            try (Connection conn = this.connect(); Statement stat = conn.createStatement(); ResultSet rs = stat.executeQuery(Query)) {

                //To remove previously added rows
                while(table.getRowCount() > 0)
                {
                    ((DefaultTableModel) table.getModel()).removeRow(0);
                }
                int columns = rs.getMetaData().getColumnCount();
                while(rs.next())
                {
                    Object[] row = new Object[columns];
                    for (int i = 1; i <= columns; i++)
                    {
                        row[i - 1] = rs.getObject(i);
                    }
                    ((DefaultTableModel) table.getModel()).insertRow(rs.getRow()-1,row);
                }
//                table.getTableHeader().getColumnModel().getColumn(0).setHeaderValue(null);
//                table.getTableHeader().getColumnModel().getColumn(1).setHeaderValue(null);
//                table.getTableHeader().getColumnModel().getColumn(2).setHeaderValue(null);
//                table.getTableHeader().getColumnModel().getColumn(3).setHeaderValue(null);
//                table.getTableHeader().getColumnModel().getColumn(4).setHeaderValue(null);
//                table.getTableHeader().getColumnModel().getColumn(5).setHeaderValue(null);
                
                ctr = 0;
                while (ctr < size){
                    table.getTableHeader().getColumnModel().getColumn(ctr).setHeaderValue(this.tableColumns.get(ctr));
                    ctr++;
                }
                
                table.repaint();
                }
                
            Object[] empty = new Object[1];
            model = (DefaultTableModel) table.getModel();
            model.addRow(empty);
                
        } catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }
    
    public void insert(ArrayList<String> args){
        int size = this.tableColumns.size();
        String columnList = "";
        String qList = "";
        int ctr = 0;
        while (ctr < size){
            if (ctr == size-1){
                columnList = columnList + this.tableColumns.get(ctr);
                qList = qList + "?";
            }
            else {
                columnList = columnList + this.tableColumns.get(ctr) + ", ";
                qList = qList + "?,";
            }
            ctr ++;
        }
        
        String sql = "INSERT INTO " + this.table + "("+ columnList + ")"+ "VALUES("+ qList +")";

        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ctr = 0;
                while (ctr < args.size()){
                    pstmt.setString(ctr+1, args.get(ctr));
                    ctr++;
                }
            //pstmt.setString(6, LocalDateTime.now().toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public void update(ArrayList<String> args) {
        int size = this.tableColumns.size();
        String columnList = "";
        int ctr = 0;
        while (ctr < size){
            if (ctr == size-2){
                columnList = columnList + this.tableColumns.get(ctr+1) + " = ?";
                columnList = columnList + " WHERE " + this.tableColumns.get(0) + " = ?";
                ctr = size;
            }
            else {
                columnList = columnList + this.tableColumns.get(ctr+1) + " = ? , ";
                ctr ++;
            } 
        }
        
        
        String sql = "UPDATE "+ this.table + " SET "+ columnList;
        
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ctr = 0;
            while (ctr < args.size()){
            //last argument in sql statement is the KEY column, here we need to select it directly as its the first in the array at index 0
                if (ctr == args.size()-1){
                    pstmt.setString(ctr+1, args.get(0));
                }
                else{
                    pstmt.setString(ctr+1, args.get(ctr+1));
                }
                ctr++;
            }
            // update 
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public void delete(String id) {
        
        String sql = "DELETE FROM " + this.table + " WHERE " + this.tableColumns.get(0) + " = ?";

        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // set the corresponding param
            pstmt.setString(1, id);
            // execute the delete statement
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public void deleteRow(JTable table){
        int row = table.getSelectedRow();
        this.delete(table.getValueAt(row, 0).toString());
        this.FillTable(table);
    }
    
    public void deleteAllRows(){
        String sql = "DELETE FROM " + this.table;

        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public void insertRow(JTable table){
         
         int row = table.getRowCount()-1;
         ArrayList<String> args = new ArrayList<>();
         int ctr = 0;
         while (ctr < this.tableColumns.size()){
             args.add(table.getValueAt(row, ctr).toString());
             ctr++;
         }
         this.insert(args);
         this.FillTable(table);
    }
    
    public void updateRow(JTable table){
        int row = table.getSelectedRow();
        ArrayList<String> args = new ArrayList<>();
        int ctr = 0;
        while (ctr < this.tableColumns.size()){
            if (table.getValueAt(row, ctr) != null) {
                args.add(table.getValueAt(row, ctr).toString());
                ctr++;
            }
            else{
                args.add("");
                ctr++;
            }
        }
        this.update(args);
        this.FillTable(table);
    }
    
    public void importFile(JTable table, String path) {
        int ctr = 0;
        try {
            this.FillTable(table);
            
            BufferedReader in = new BufferedReader(new FileReader(path));
            in.readLine();
            String currentLine;
            // Read objects
            ArrayList<String> args = new ArrayList<>();
            while ((currentLine = in.readLine()) != null) {
                String[] parts = currentLine.split("\t");
                args = new ArrayList<>();
                ctr = 0;
                while (ctr < parts.length){
                    args.add(parts[ctr]);
                    ctr++;
                }
                this.insert(args);
            }
       
            in.close();

        } catch (FileNotFoundException e) {
            System.out.println("File not found");

        } catch (IOException e) {
            System.out.println("Error initializing stream");
        }
        this.FillTable(table);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }


    
}
