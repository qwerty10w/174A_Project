package net.sqlitetutorial;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.BufferedReader;
import java.io.FileReader;
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
      BufferedReader csvReader = new BufferedReader(new FileReader("Actors.txt"));
      String row = "";
      while((row = csvReader.readLine()) != null){
        String[] data = row.split(",");
      }
    }catch (IOException ioe){
      ioe.printStackTrace();
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
    //     + "state CHAR(2) NOT NULL,\n"
    //     + "pnumber CHAR(10) NOT NULL,\n"
    //     + "email CHAR(30) NOT NULL,\n"
    //     + "tax_id CHAR(9) NOT NULL,\n"
    //     + "username CHAR(20) PRIMARY KEY,\n"
    //     + "password CHAR(10) NOT NULL\n"
    //     +");";

    // String query = "CREATE TABLE Accounts(\n"
    //       + "ID INT PRIMARY KEY,\n"
    //       + "user CHAR(20) NOT NULL,\n"
    //       + "type CHAR(1) NOT NULL,\n"
    //       + "balance REAL NOT NULL,\n"
    //       + "FOREIGN KEY(user) REFERENCES Customers(username)"
    //       + ");";

    // String query = "CREATE TABLE Actors(\n"
    //       + "symbol CHAR(3) PRIMARY KEY,\n"
    //       + "name CHAR(20) NOT NULL,\n"
    //       + "DOB CHAR(10) NOT NULL,\n"
    //       + "price REAL NOT NULL\n"
    //       + ");";

    // String query = "CREATE TABLE Closing_Prices(\n"
    //       + "symbol CHAR(3) NOT NULL,\n"
    //       + "date CHAR(10) NOT NULL,\n"
    //       + "price REAL NOT NULL,\n"
    //       + "FOREIGN KEY(symbol) REFERENCES Actors(symbol),\n"
    //       + "PRIMARY KEY(symbol, date)"
    //       + ");";

    // String query = "CREATE TABLE Owns(\n"
    //       + "ID INT NOT NULL,\n"
    //       + "symbol CHAR(3) NOT NULL,\n"
    //       + "amount INT NOT NULL,\n"
    //       + "FOREIGN KEY(ID) REFERENCES Accounts(ID),\n"
    //       + "FOREIGN KEY(symbol) REFERENCES Actors(symbol),\n"
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
    //       + "FOREIGN KEY(ID) REFERENCES Movies(ID),\n"
    //       + "FOREIGN KEY(symbol) REFERENCES Actors(symbol),\n"
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
    //       + "FOREIGN KEY(ID) REFERENCES Accounts(ID),\n"
    //       + "FOREIGN KEY(symbol) REFERENCES Actors(symbol)\n"
    //       + ");";

    // String query = "CREATE TABLE Market_Transactions(\n"
    //       + "ID INT NOT NULL,\n"
    //       + "type CHAR(1) NOT NULL,\n"
    //       + "amount REAL NOT NULL,\n"
    //       + "date CHAR(10) NOT NULL,\n"
    //       + "balance REAL NOT NULL,\n"
    //       + "FOREIGN KEY(ID) REFERENCES Accounts(ID)\n"
    //       + ");";


    runSQL(query);
  }
}
