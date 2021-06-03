package net.project;

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

public class Manager{
  //Member Vars
  public boolean open = true;
  public int day;
  public int month;
  public int year;
  public String date;
  public Connection conn;

  public Manager(){
    this.conn = connect("jdbc:sqlite:E:/sqlite/db/chinook.db");
    this.get_date();
    this.date = String.valueOf(year) + "-" + String.valueOf(month) + "-" + String.valueOf(day);
  }

  public Connection connect(String url){
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

  public void get_date(){
    String query = "SELECT * FROM Calendar";
    try{
      Statement s = conn.createStatement();
      ResultSet rs = s.executeQuery(query);
      this.day = rs.getInt("day");
      this.month = rs.getInt("month");
      this.year = rs.getInt("year");
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
  }

  public void close_market(){
    this.open = false;
  }

  public void set_date(int day, int month, int year){
    String query = "UPDATE Calendar SET day = ?, month = ?, year = ?";
    try{
      PreparedStatement ps = this.conn.prepareStatement(query);
      ps.setInt(1, day);
      ps.setInt(2, month);
      ps.setInt(3, year);
      ps.executeUpdate();
      ps.close();

      this.get_date();
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
  }

  public void create_customer(String name, String addy, String state, String pnumber, String email, String username, String password, String tax_id){
    String[] args = {name, addy, state, pnumber, email, username, password};
    String query = "INSERT INTO Customers(name, address, state, pnumber, email, username, password, tax_id) VALUES(?,?,?,?,?,?,?,?)";
    try{
      PreparedStatement ps = this.conn.prepareStatement(query);
      for(int i = 1; i < 8; i++){
        ps.setString(i, args[i - 1]);
      }
      ps.setInt(8, Integer.parseInt(tax_id));
      ps.executeUpdate();
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
  }
  public void create_admin(String username, String password){
    System.out.println("In create_admin in manager class");
    System.out.println("Attributes passed username: " + username + "password: " + password);
  }
  public void login(String username, String password){
    System.out.println("In login in manager class");
    System.out.println("Attributes passed username: " + username + "password: " + password);
  }
  public void deposit(int acc_id, double amount){
    String query = "SELECT balance FROM Accounts WHERE Accounts.ID = ?";
    String query2 = "UPDATE Accounts SET balance = ? WHERE Accounts.ID = ?";
    String query3 = "INSERT INTO Market_Transactions(ID, type, amount, date, balance) VALUES(?,?,?,?,?)";

    try{
      //Get current balance
      PreparedStatement ps = this.conn.prepareStatement(query);
      ps.setInt(1, acc_id);
      ResultSet rs = ps.executeQuery();
      double balance = rs.getDouble("balance");
      double new_balance = balance + amount;
      rs.close();
      ps.close();

      //Set new balance
      PreparedStatement ps2 = this.conn.prepareStatement(query2);
      ps2.setDouble(1, new_balance);
      ps2.setInt(2, acc_id);
      ps2.executeUpdate();
      ps2.close();

      //add entry in Market_Transactions
      PreparedStatement ps3 = this.conn.prepareStatement(query3);
      ps3.setInt(1, acc_id);
      ps3.setInt(2, 1);
      ps3.setDouble(3, amount);
      ps3.setString(4, this.date);
      ps3.setDouble(5, new_balance);
      ps3.executeUpdate();
      ps3.close();
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
  }

  public void withdraw(int acc_id, int amount){
    String query = "SELECT balance FROM Accounts WHERE Accounts.ID = ?";
    String query2 = "UPDATE Accounts SET balance = ? WHERE Accounts.ID = ?";
    String query3 = "INSERT INTO Market_Transactions(ID, type, amount, date, balance) VALUES(?,?,?,?,?)";

    try{
      //Get current balance
      PreparedStatement ps = this.conn.prepareStatement(query);
      ps.setInt(1, acc_id);
      ResultSet rs = ps.executeQuery();
      double balance = rs.getDouble("balance");
      double new_balance = balance - amount;
      rs.close();
      ps.close();

      //set new balance
      PreparedStatement ps2 = this.conn.prepareStatement(query2);
      ps2.setDouble(1, new_balance);
      ps2.setInt(2, acc_id);
      ps2.executeUpdate();
      ps2.close();

      //add entry in Market_Transactions
      PreparedStatement ps3 = this.conn.prepareStatement(query3);
      ps3.setInt(1, acc_id);
      ps3.setInt(2, 0);
      ps3.setDouble(3, amount);
      ps3.setString(4, this.date);
      ps3.setDouble(5, new_balance);
      ps3.executeUpdate();
      ps3.close();
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
  }

  public void add_balance(int acc_id, double amount){
    String query = "UPDATE Accounts SET balance = balance + ? WHERE Accounts.ID = ?";
    try{
      PreparedStatement ps = this.conn.prepareStatement(query);
      ps.setDouble(1, amount);
      ps.setInt(2, acc_id);
      ps.executeUpdate();
      ps.close();
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
  }

  public void subtract_balance(int acc_id, double amount){
    String query = "UPDATE Accounts SET balance = balance - ? WHERE Accounts.ID = ?";
    try{
      PreparedStatement ps = this.conn.prepareStatement(query);
      ps.setDouble(1, amount);
      ps.setInt(2, acc_id);
      ps.executeUpdate();
      ps.close();
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
  }

  public void buy(int acc_id, String symbol, int amount){
    String query0 = "SELECT balance FROM Accounts WHERE Accounts.ID = ?";
    String query = "SELECT ID, balance FROM Acounts WHERE Accounts.user = (SELECT user FROM Accounts WHERE ID = ?) AND Accounts.type = 0";
    String query2 = "SELECT price FROM Actors WHERE Actors.symbol = ?";
    String query3 = "INSERT INTO Stock_Transactions(ID, symbol, type, date, price, amount, balance) VALUES(?,?,?,?,?,?,?)";
    String query4 = "SELECT * FROM Owns WHERE ID = ? AND symbol = ?";
    String query_first_buy = "INSERT INTO Owns(ID, symbol, amount) VALUES(?,?,?)";
    String query_already_owns = "UPDATE Owns SET amount = amount + ? WHERE ID = ? AND symbol = ?";
    String query_update_market_balance = "UPDATE Accounts SET balance = ? WHERE Accounts.ID = ?";

    try{
      //Get initial stock account balance
      PreparedStatement ps0 = this.conn.prepareStatement(query0);
      ps0.setInt(1, acc_id);
      ResultSet rs0 = ps0.executeQuery();
      double balance = rs0.getDouble("balance");
      rs0.close();
      ps0.close();

      //Get ID and balance from market account
      PreparedStatement ps = this.conn.prepareStatement(query);
      ps.setInt(1, acc_id);
      ResultSet rs = ps.executeQuery();
      double market_balance = rs.getDouble("balance");
      int market_id = rs.getInt("ID");
      rs.close();
      ps.close();

      //Get current stock price
      PreparedStatement ps2 = this.conn.prepareStatement(query2);
      ps2.setString(1, symbol);
      ResultSet rs2 = ps2.executeQuery();
      double price = rs2.getDouble("price");
      double total_price = (amount * price) + 20;
      ps2.close();

      if(total_price > market_balance){
        System.out.println("Insuffienct Funds in Market Account! You need $" + String.valueOf(total_price) + ". \nCurrent Balance: " + String.valueOf(balance));
      }else{
        //add entry in Stock_Transactions
        PreparedStatement ps3 = this.conn.prepareStatement(query3);
        ps3.setInt(1, acc_id);
        ps3.setString(2, symbol);
        ps3.setInt(3, 1);
        ps3.setString(4, this.date);
        ps3.setDouble(5, price);
        ps3.setDouble(6, amount);
        ps3.setDouble(7, balance);
        ps3.executeUpdate();
        ps3.close();

        //check to see if customer already owns this stock
        PreparedStatement ps4 = this.conn.prepareStatement(query4);
        ps4.setInt(1, acc_id);
        ps4.setString(2, symbol);
        ResultSet rs4 = ps4.executeQuery();
        if(!rs4.next()){
          rs4.close();
          ps4.close();
          //If doesn't own stock, create new entry in owns
          PreparedStatement ps5 = this.conn.prepareStatement(query_first_buy);
          ps5.setInt(1, acc_id);
          ps5.setString(2, symbol);
          ps5.setInt(3, amount);
          ps5.executeUpdate();
          ps5.close();
        }else{
          rs4.close();
          ps4.close();
          //If owns stock, update entry in owns
          PreparedStatement ps5 = this.conn.prepareStatement(query_already_owns);
          ps5.setInt(1, amount);
          ps5.setInt(2, acc_id);
          ps5.setString(3, symbol);
          ps5.executeUpdate();
          ps5.close();
        }
        //subtract total price from market account
        this.subtract_balance(market_id, total_price);
      }
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
  }

  public void sell(int acc_id, String symbol, int amount){
    String query0 = "SELECT amount FROM Owns WHERE ID = ? AND symbol = ?";
    String query = "SELECT ID, balance FROM Acounts WHERE Accounts.user = (SELECT user FROM Accounts WHERE ID = ?) AND Accounts.type = 0";
    String query_get_stock_balance = "SELECT balance FROM Acounts WHERE Accounts.ID = ?";
    String query2 = "SELECT price FROM Actors WHERE Actors.symbol = ?";
    String query3 = "INSERT INTO Stock_Transactions(ID, symbol, type, date, price, amount, balance) VALUES(?,?,?,?,?,?,?)";
    String query4 = "UPDATE Owns SET amount = amount - ? WHERE ID = ? AND symbol = ?";
    String query5 = "DELETE FROM Owns WHERE ID = ? AND symbol = ?";

    try{
      //Get number of stocks owned
      PreparedStatement ps0 = this.conn.prepareStatement(query0);
      ps0.setInt(1, acc_id);
      ResultSet rs0 = ps0.executeQuery();
      int amount_owned = rs0.getInt("amount");
      rs0.close();
      ps0.close();

      //Check amount owned > amount trying to sell
      if(amount_owned < amount){
        System.out.println("Don't own enough stock! You own: " + String.valueOf(amount_owned) + " \nAttempting to sell: " + String.valueOf(amount));
      }else{
        //get market account id and balance
        PreparedStatement ps = this.conn.prepareStatement(query);
        ps.setInt(1, acc_id);
        ResultSet rs = ps.executeQuery();
        double market_balance = rs.getDouble("balance");
        int market_id = rs.getInt("ID");
        rs.close();
        ps.close();

        //Check market account has > $20
        if(market_balance < 20){
          System.out.println("Not enough money in Market Account! Need: $20 \nYou Have: " + String.valueOf(market_balance));
        }else{
          //Get current price of stock
          PreparedStatement ps2 = this.conn.prepareStatement(query2);
          ps2.setString(1, symbol);
          ResultSet rs2 = ps2.executeQuery();
          double curr_price = rs2.getDouble("price");
          ps2.close();

          //Get current balance of stock account
          PreparedStatement ps_get_stock_balance = this.conn.prepareStatement(query_get_stock_balance);
          ps_get_stock_balance.setInt(1, acc_id);
          ResultSet rs_get_stock_balance = ps_get_stock_balance.executeQuery();
          double balance = rs_get_stock_balance.getDouble("balance");
          ps2.close();

          //Enter into Stock_Transactions
          PreparedStatement ps3 = this.conn.prepareStatement(query3);
          ps3.setInt(1, acc_id);
          ps3.setString(2, symbol);
          ps3.setInt(3, 0);
          ps3.setString(4, this.date);
          ps3.setDouble(5, curr_price);
          ps3.setDouble(6, amount);
          ps3.setDouble(7, balance);
          ps3.executeUpdate();
          ps3.close();

          //Edit owned
          if(amount_owned - amount == 0){
            //Delete row if num of stocks owned is 0
            PreparedStatement ps4 = this.conn.prepareStatement(query5);
            ps4.setInt(1, acc_id);
            ps4.setString(2, symbol);
            ps4.executeUpdate();
            ps4.close();
          }else{
            //Decrement number of stocks owned
            PreparedStatement ps4 = this.conn.prepareStatement(query4);
            ps4.setInt(1, amount);
            ps4.setInt(2, acc_id);
            ps4.setString(3, symbol);
            ps4.executeUpdate();
            ps4.close();
          }

          //Add earnings to Market account
          double amount_to_add = (amount * curr_price) - 20;
          this.add_balance(market_id, amount_to_add);
        }
      }
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
  }

  public String select_user(int tax_id){
    String query = "SELECT username FROM Customers WHERE Customers.tax_id = ?";
    String result = "";
    try{
      PreparedStatement ps = this.conn.prepareStatement(query);
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

  public int select_account(int tax_id, int type){
    String user = select_user(tax_id);
    String query = "SELECT ID FROM Accounts WHERE Accounts.user = ? AND Accounts.type = ?";
    int result = -1;
    try{
      PreparedStatement ps = this.conn.prepareStatement(query);
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

  public void insert_stock(int acc_id, String symbol, int amount){
    String query = "INSERT INTO Owns(ID, symbol, amount) \n"
            + "SELECT ?, ?, ? \n"
            + "WHERE NOT EXISTS (SELECT * FROM Owns WHERE ID = ? AND symbol = ?)";
    try{
      PreparedStatement ps = this.conn.prepareStatement(query);
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

  public void insert_actor(String symbol, String name, String dob, String price, String amount){
    String[] args = {symbol, name, dob};
    String query = "INSERT INTO Actors(symbol, name, DOB, price, amount) VALUES(?,?,?,?,?)";
    try{
      PreparedStatement ps = this.conn.prepareStatement(query);
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

  public void insert_stock_account(String user){
    // Inserts stock account with given username
    String query = "INSERT INTO Accounts(user, type, balance) \n"
            + "SELECT ?, \"1\", 0 \n"
            + "WHERE NOT EXISTS (SELECT * FROM Accounts WHERE user = ? AND type = \"1\")";
    try{
      PreparedStatement ps = this.conn.prepareStatement(query);
      ps.setString(1, user);
      ps.setString(2, user);
      ps.executeUpdate();
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
  }

  public void insert_market_account(String user, int balance){
    //Inserts Market Account with given username and balance
    String query = "INSERT INTO Accounts(user, type, balance) \n"
            + "SELECT ?, \"0\", ? \n"
            + "WHERE NOT EXISTS (SELECT * FROM Accounts WHERE user = ? AND type = \"0\")";
    try{
      PreparedStatement ps = this.conn.prepareStatement(query);
      ps.setString(1, user);
      ps.setInt(2, balance);
      ps.setString(3, user);
      ps.executeUpdate();
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
  }

  public void update_stock_balance(int acc_id){
    String query1 = "SELECT * FROM Owns WHERE owns.ID = ?";
    // Connection conn = connect("jdbc:sqlite:E:/sqlite/db/chinook.db");
    try{
      PreparedStatement ps = this.conn.prepareStatement(query1);
      ps.setInt(1, acc_id);
      ResultSet rs = ps.executeQuery();
      double balance = 0;
      while(rs.next()){
        String stock = rs.getString("symbol");
        int amount = rs.getInt("amount");
        String query2 = "SELECT price FROM Actors where Actors.symbol = ?";
        try{
          PreparedStatement ps2 = this.conn.prepareStatement(query2);
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
        PreparedStatement ps3 = this.conn.prepareStatement(query3);
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

  public void update_all_stock_balances(){
    String query = "SELECT ID FROM Accounts WHERE Accounts.type = 1";
    try{
      Statement s = this.conn.createStatement();
      ResultSet rs = s.executeQuery(query);
      while(rs.next()){
        int acc_id = rs.getInt("ID");
        this.update_stock_balance(acc_id);
      }
      rs.close();
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
  }

  public void run_create_query(String query){
    try{
      Statement s = this.conn.createStatement();
      s.execute(query);
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
  }

  public static void main(String[] args){
    Manager m = new Manager();
  }
}
