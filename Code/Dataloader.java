package net.sqlitetutorial;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;

public class Dataloader{
  public static Connection connect(String url){
    Connection conn = null;
    try{
      conn = DriverManager.getConnection(url);
      System.out.println("Connection to db established");
      return conn;
    }catch (SQLException e){
      System.out.println(e.getMessage());
      return null;
    }
  }

  public static void load_data(){
    Connection conn = connect("jdbc:sqlite:E:/sqlite/db/chinook.db");
    try{
      File file = new File("Stocks.txt");
      FileReader fr = new FileReader(file);
      BufferedReader csvReader = new BufferedReader(fr);
      String row = "";
      while((row = csvReader.readLine()) != null){
        String[] data = row.split(",");
        if(data[0].equals("TAXID")){
          continue;
        }
        int acc_id = select_account(Integer.parseInt(data[0]), 1);
        System.out.println(data[0]);
        System.out.println(acc_id);
        if(acc_id == -1){
          break;
        }
        insert_stock(acc_id, data[2], Integer.parseInt(data[1]));
      }
      csvReader.close();
    }catch (IOException ioe){
      ioe.printStackTrace();
    }
  }

  public static String select_user(int tax_id){
    String query = "SELECT username FROM Customers WHERE Customers.tax_id = ?";
    Connection conn = connect("jdbc:sqlite:E:/sqlite/db/chinook.db");
    String result = "";
    try{
      PreparedStatement ps = conn.prepareStatement(query);
      ps.setInt(1, tax_id);
      ResultSet rs = ps.executeQuery();
      result = rs.getString("username");
      rs.close();
      ps.close();
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
    if(!result.equals("")){
      return result;
    }else{
      return "FAIL";
    }
  }

  public static int select_account(int tax_id, int type){
    String user = select_user(tax_id);
    String query = "SELECT ID FROM Accounts WHERE Accounts.user = ? AND Accounts.type = ?";
    Connection conn = connect("jdbc:sqlite:E:/sqlite/db/chinook.db");
    int result = -1;
    try{
      PreparedStatement ps = conn.prepareStatement(query);
      ps.setString(1, user);
      ps.setInt(2, type);
      ResultSet rs = ps.executeQuery();
      result = rs.getInt("ID");
      rs.close();
      ps.close();
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
    return result;
  }

  public static void insert_stock(int acc_id, String symbol, int amount){
    String query = "INSERT INTO Owns(ID, symbol, amount) \n"
            + "SELECT ?, ?, ? \n"
            + "WHERE NOT EXISTS (SELECT * FROM Owns WHERE ID = ? AND symbol = ?)";
    // int acc_id = select_account(tax_id, 1)
    Connection conn = connect("jdbc:sqlite:E:/sqlite/db/chinook.db");
    try{
      PreparedStatement ps = conn.prepareStatement(query);
      ps.setInt(1, acc_id);
      ps.setString(2, symbol);
      ps.setInt(3, amount);
      ps.setInt(4, acc_id);
      ps.setString(5, symbol);
      ps.executeUpdate();
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
  }

  public static void insert_customer(String name, String addy, String state, String pnumber, String email, String username, String password, String tax_id){
    String[] args = {name, addy, state, pnumber, email, username, password};
    String query = "INSERT INTO Customers(name, address, state, pnumber, email, username, password, tax_id) VALUES(?,?,?,?,?,?,?,?)";
    Connection conn = connect("jdbc:sqlite:E:/sqlite/db/chinook.db");
    try{
      PreparedStatement ps = conn.prepareStatement(query);
      for(int i = 1; i < 8; i++){
        ps.setString(i, args[i - 1]);
      }
      ps.setInt(8, Integer.parseInt(tax_id));
      ps.executeUpdate();
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
  }

  public static void insert_actor(String symbol, String name, String dob, String price, String amount){
    String[] args = {symbol, name, dob};
    String query = "INSERT INTO Actors(symbol, name, DOB, price, amount) VALUES(?,?,?,?,?)";
    Connection conn = connect("jdbc:sqlite:E:/sqlite/db/chinook.db");
    try{
      PreparedStatement ps = conn.prepareStatement(query);
      for(int i = 1; i < 4; i++){
        ps.setString(i, args[i - 1]);
      }
      ps.setFloat(4, Float.parseFloat(price));
      ps.setInt(5, Integer.parseInt(amount));
      ps.executeUpdate();
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
  }

  public static void insert_stock_account(String user){
    // String query = "IF NOT EXISTS (\n"
    //         + "SELECT * FROM Accounts \n"
    //         + "WHERE user = ? \n"
    //         + "AND type = \"1\") \n"
    //         + "INSERT INTO Accounts(user, type, balance) VALUES(?,?,?)";
    String query = "INSERT INTO Accounts(user, type, balance) \n"
            + "SELECT ?, \"1\", 0 \n"
            + "WHERE NOT EXISTS (SELECT * FROM Accounts WHERE user = ? AND type = \"1\")";
    Connection conn = connect("jdbc:sqlite:E:/sqlite/db/chinook.db");
    try{
      PreparedStatement ps = conn.prepareStatement(query);
      ps.setString(1, user);
      ps.setString(2, user);
      ps.executeUpdate();
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
  }

  public static void insert_market_account(String user, int balance){
    // String query = "IF NOT EXISTS (\n"
    //         + "SELECT * FROM Accounts \n"
    //         + "WHERE user = ? \n"
    //         + "AND type = \"1\") \n"
    //         + "INSERT INTO Accounts(user, type, balance) VALUES(?,?,?)";
    String query = "INSERT INTO Accounts(user, type, balance) \n"
            + "SELECT ?, \"0\", ? \n"
            + "WHERE NOT EXISTS (SELECT * FROM Accounts WHERE user = ? AND type = \"0\")";
    Connection conn = connect("jdbc:sqlite:E:/sqlite/db/chinook.db");
    try{
      PreparedStatement ps = conn.prepareStatement(query);
      ps.setString(1, user);
      ps.setInt(2, balance);
      ps.setString(3, user);
      ps.executeUpdate();
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
  }

  public static void update_stock_balance(int acc_id, Connection conn){
    String query1 = "SELECT * FROM Owns WHERE owns.ID = ?";
    // Connection conn = connect("jdbc:sqlite:E:/sqlite/db/chinook.db");
    try{
      PreparedStatement ps = conn.prepareStatement(query1);
      ps.setInt(1, acc_id);
      ResultSet rs = ps.executeQuery();
      double balance = 0;
      while(rs.next()){
        String stock = rs.getString("symbol");
        int amount = rs.getInt("amount");
        String query2 = "SELECT price FROM Actors where Actors.symbol = ?";
        try{
          PreparedStatement ps2 = conn.prepareStatement(query2);
          ps2.setString(1, stock);
          ResultSet rs2 = ps2.executeQuery();
          double price = rs2.getDouble("price");
          balance += price * amount;
          ps2.close();
          rs2.close();
        }catch (SQLException e){
          System.out.println(e.getMessage());
        }
      }
      rs.close();
      ps.close();
      try{
        String query3 = "UPDATE Accounts SET balance = ? WHERE ID = ? AND type = 1";
        PreparedStatement ps3 = conn.prepareStatement(query3);
        ps3.setDouble(1, balance);
        ps3.setInt(2, acc_id);
        ps3.executeUpdate();
        ps3.close();
      }catch (SQLException e){
        System.out.println(e.getMessage());
      }
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
  }

  public static void update_all_stock_balances(){
    String query = "SELECT ID FROM Accounts WHERE Accounts.type = 1";
    Connection conn = connect("jdbc:sqlite:E:/sqlite/db/chinook.db");
    try{
      Statement s = conn.createStatement();
      ResultSet rs = s.executeQuery(query);
      while(rs.next()){
        int acc_id = rs.getInt("ID");
        update_stock_balance(acc_id, conn);
      }
      rs.close();
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
  }

  public static void runSQL(String query){
    Connection conn = connect("jdbc:sqlite:E:/sqlite/db/chinook.db");
    try{
      Statement s = conn.createStatement();
      s.execute(query);
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
  }

  public static void main(String[] args){
    // String query = "CREATE TABLE Customers(\n"
    //     + "name CHAR(20) NOT NULL,\n"
    //     + "address CHAR(20) NOT NULL,\n"
    //     + "state CHAR(2) NOT NULL,\n"
    //     + "pnumber CHAR(10) NOT NULL,\n"
    //     + "email CHAR(30) NOT NULL,\n"
    //     + "tax_id CHAR(9) NOT NULL,\n"
    //     + "username CHAR(20) PRIMARY KEY,\n"
    //     + "password CHAR(10) NOT NULL\n"
    //     +");";

    // String query = "CREATE TABLE Accounts(\n"
    //       + "ID INTEGER PRIMARY KEY AUTOINCREMENT,\n"
    //       + "user CHAR(20) NOT NULL,\n"
    //       + "type CHAR(1) NOT NULL,\n"
    //       + "balance REAL NOT NULL,\n"
    //       + "FOREIGN KEY(user) REFERENCES Customers(username) \n"
    //       + "ON UPDATE CASCADE ON DELETE CASCADE"
    //       + ");";

    // String query = "CREATE TABLE Actors(\n"
    //       + "symbol CHAR(3) PRIMARY KEY,\n"
    //       + "name CHAR(20) NOT NULL,\n"
    //       + "DOB CHAR(10) NOT NULL,\n"
    //       + "price REAL NOT NULL,\n"
    //       + "amount INT NOT NULL\n"
    //       + ");";

    // String query = "CREATE TABLE Closing_Prices(\n"
    //       + "symbol CHAR(3) NOT NULL,\n"
    //       + "date CHAR(10) NOT NULL,\n"
    //       + "price REAL NOT NULL,\n"
    //       + "FOREIGN KEY(symbol) REFERENCES Actors(symbol) ON UPDATE CASCADE ON DELETE CASCADE,\n"
    //       + "PRIMARY KEY(symbol, date)"
    //       + ");";

    // String query = "CREATE TABLE Owns(\n"
    //       + "ID INT NOT NULL,\n"
    //       + "symbol CHAR(3) NOT NULL,\n"
    //       + "amount INT NOT NULL,\n"
    //       + "FOREIGN KEY(ID) REFERENCES Accounts(ID) ON UPDATE CASCADE ON DELETE CASCADE,\n"
    //       + "FOREIGN KEY(symbol) REFERENCES Actors(symbol) ON UPDATE CASCADE ON DELETE CASCADE,\n"
    //       + "PRIMARY KEY(ID, symbol)"
    //       + ");";

    // String query = "CREATE TABLE Movies(\n"
    //       + "ID INT PRIMARY KEY,\n"
    //       + "title CHAR(20) NOT NULL,\n"
    //       + "year CHAR(4) NOT NULL,\n"
    //       + "rank INT NOT NULL\n"
    //       + ");";

    // String query = "CREATE TABLE Contracts(\n"
    //       + "ID INT NOT NULL,\n"
    //       + "symbol CHAR(3) NOT NULL,\n"
    //       + "role CHAR(10) NOT NULL,\n"
    //       + "value REAL NOT NULL,\n"
    //       + "FOREIGN KEY(ID) REFERENCES Movies(ID) ON UPDATE CASCADE,\n"
    //       + "FOREIGN KEY(symbol) REFERENCES Actors(symbol) ON UPDATE CASCADE ON DELETE CASCADE,\n"
    //       + "PRIMARY KEY(symbol, ID)"
    //       + ");";

    // String query = "CREATE TABLE Stock_Transactions(\n"
    //       + "ID INT NOT NULL,\n"
    //       + "symbol CHAR(3) NOT NULL,\n"
    //       + "type CHAR(1) NOT NULL,\n"
    //       + "date CHAR(10) NOT NULL,\n"
    //       + "price REAL NOT NULL,\n"
    //       + "amount INT NOT NULL,\n"
    //       + "balance REAL NOT NULL,\n"
    //       + "FOREIGN KEY(ID) REFERENCES Accounts(ID) ON UPDATE CASCADE ON DELETE CASCADE,\n"
    //       + "FOREIGN KEY(symbol) REFERENCES Actors(symbol) ON UPDATE CASCADE ON DELETE CASCADE\n"
    //       + ");";

    // String query = "CREATE TABLE Market_Transactions(\n"
    //       + "ID INT NOT NULL,\n"
    //       + "type CHAR(1) NOT NULL,\n"
    //       + "amount REAL NOT NULL,\n"
    //       + "date CHAR(10) NOT NULL,\n"
    //       + "balance REAL NOT NULL,\n"
    //       + "FOREIGN KEY(ID) REFERENCES Accounts(ID) ON UPDATE CASCADE ON DELETE CASCADE\n"
    //       + ");";

    // String query = "CREATE TABLE Calendar(\n"
    //           + "day INT NOT NULL, \n"
    //           + "month INT NOT NULL, \n"
    //           + "year INT NOT NULL, \n"
    //           + "PRIMARY KEY(day, month, year));";

    // String query = "INSERT INTO Calendar(day, month, year) VALUES(3, 16, 2013)";

    // runSQL(query);
    // load_data();
    runSQL(query);
  }
}
