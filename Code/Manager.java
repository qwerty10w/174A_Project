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

import java.util.ArrayList;

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

  public void get_date(){
    String query = "SELECT * FROM Calendar";
    try{
      Statement s = conn.createStatement();
      ResultSet rs = s.executeQuery(query);
      this.day = rs.getInt("day");
      this.month = rs.getInt("month");
      this.year = rs.getInt("year");
      rs.close();
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
  }

  //ADMINISTRATIVE FUNCTIONS
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

  public void close_market(){
    this.open = false;

    String query = "SELECT symbol, price FROM Actors";
    String query2 = "SELECT ID, balance FROM Accounts WHERE type = 0";

    try{
      //recod stock closing prices
      Statement s = this.conn.createStatement();
      ResultSet rs = s.executeQuery(query);
      while(rs.next()){
        String symbol = rs.getString("symbol");
        double price = rs.getDouble("price");
        this.insert_closing_price(symbol, this.date, price);
      }
      rs.close();

      //record end of day balances for market accounts
      Statement s2 = this.conn.createStatement();
      ResultSet rs2 = s2.executeQuery(query2);
      while(rs2.next()){
        int acc_id = rs2.getInt("ID");
        double bal = rs2.getDouble("balance");

        this.insert_closing_balance(acc_id, bal);
      }
      rs2.close();
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }

    //CHANGE DATE
    if(this.day == 31 && this.month == 12){
      this.set_date(1, 1, this.year + 1);
    }else if((this.day == 30 && this.month == 9) || (this.day == 30 && this.month == 4) || (this.day == 30 && this.month == 6) || (this.day == 30 && this.month == 11)){
      this.set_date(1, this.month + 1, this.year);
    }else if((this.day == 31 && this.month == 1) || (this.day == 31 && this.month == 3) || (this.day == 31 && this.month == 5) || (this.day == 31 && this.month == 7) || (this.day == 31 && this.month == 8) || (this.day == 31 && this.month == 10)){
      this.set_date(1, this.month + 1, this.year);
    }else if(this.day == 28 && this.month == 2){
      this.set_date(1, this.month + 1, this.year);
    }else{
      this.set_date(this.day + 1, this.month, this.year);
    }
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

  public String get_monthly_statement(String username){
    String get_accounts = "SELECT ID, type FROM Accounts WHERE user = ?";
    String get_market_transactions = "SELECT * FROM Market_Transactions WHERE ID = ?";
    String get_stock_transactions = "SELECT * FROM Stock_Transactions WHERE ID = ?";
    String summary = "Monthly Statement: \n";

    int market_id = -1;
    int stock_id = -1;
    double initial_market_balance = -1;
    double final_market_balance = -1;
    try{
      //Get account id's
      PreparedStatement ps = this.conn.prepareStatement(get_accounts);
      ps.setString(1, username);
      ResultSet rs = ps.executeQuery();
      while(rs.next()){
        int type = rs.getInt("type");
        int acc_id = rs.getInt("ID");

        if(type == 0){
          market_id = acc_id;
        }else if(type == 1){
          stock_id = acc_id;
        }
      }
      rs.close();
      ps.close();
      // System.out.println("Market_id: " + String.valueOf(market_id));
      // System.out.println("Stock_id: " + String.valueOf(stock_id));

      initial_market_balance = this.get_initial_market_balance(market_id, this.year, this.month);
      final_market_balance = this.get_final_market_balance(market_id, this.year, this.month);

      PreparedStatement ps2 = this.conn.prepareStatement(get_market_transactions);
      ps2.setInt(1, market_id);
      ResultSet market_actions = ps2.executeQuery();

      //add Market Transactions to result string
      summary += "Market Transactions in month " + String.valueOf(this.month) + ": \n";
      while(market_actions.next()){
        int type = market_actions.getInt("type");
        double amount = market_actions.getDouble("amount");
        String date = market_actions.getString("date");
        double bal = market_actions.getDouble("balance");
        if(type == 0){
          summary += "Withdrew $" + String.valueOf(amount) + " on " + date + " leaving balance: $" + String.valueOf(bal) + "\n";
        }else if(type == 1){
          summary += "Deposited $" + String.valueOf(amount) + " on " + date + " leaving balance: $" + String.valueOf(bal) + "\n";
        }else if(type == 2){
          summary += "Gained $" + String.valueOf(amount) + " interest on " + date + " leaving balance: " + String.valueOf(bal) + "\n";
        }
      }
      market_actions.close();
      ps2.close();

      if(stock_id != -1){
        summary += "\nStock Transactions this month: \n";
        PreparedStatement ps3 = this.conn.prepareStatement(get_stock_transactions);
        ps3.setInt(1, stock_id);
        ResultSet stock_actions = ps3.executeQuery();
        //add Stock Transactions to result string
        while(stock_actions.next()){
          String sym = stock_actions.getString("symbol");
          int type = stock_actions.getInt("type");
          String date = stock_actions.getString("date");
          double amount = stock_actions.getDouble("amount");
          double bal = stock_actions.getDouble("balance");
          double earnings = stock_actions.getDouble("earnings");
          if(type == 0){
            summary += "Sold " + String.valueOf(amount) + " of " + sym + " stock on " + date + " leaving balance: $" + String.valueOf(bal) + " and earning $" + String.valueOf(earnings) + "\n";
          }else if(type == 1){
            summary += "Bought " + String.valueOf(amount) + " of " + sym + " stock on " + date + " leaving balance: $" + String.valueOf(bal) + "\n";
          }
        }
        stock_actions.close();
        ps3.close();

        summary += "\n";

        double total_earnings = this.get_total_earnings(stock_id, this.year, this.month);
        // System.out.println("total stock earnings: " + String.valueOf(total_earnings));
        if(total_earnings != -1){
          summary += "Total stock earnings: $" + String.valueOf(total_earnings) + "\n";
          // System.out.println("congrats you made some money");
          //display earnings on screen
        }
      }

      if((initial_market_balance != -1) && (final_market_balance != -1)){
        double market_diff = final_market_balance - initial_market_balance;
        summary += "Market account went from $" + String.valueOf(initial_market_balance) + " to $" + String.valueOf(final_market_balance) + "\nTotal earnings/loss: $" + String.valueOf(market_diff) + "\n";
      }
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
    return summary;
  }

  public String get_active_customers(int month){
    String query = "SELECT user FROM Accounts WHERE ID IN (\n"
            + "SELECT ID FROM (\n"
                + "SELECT ID, SUM(amount) as total \n"
                + "FROM Stock_Transactions \n"
                + "WHERE date LIKE ? \n"
                + "GROUP BY ID) AS t \n"
            + "WHERE t.total >= 1000)";
    String result = "Active Customers (>=1000 shares traded): \n";

    try{
      String like_string = String.valueOf(this.year) + "-" + String.valueOf(month) + "%";
      PreparedStatement ps = this.conn.prepareStatement(query);
      ps.setString(1, like_string);
      ResultSet rs = ps.executeQuery();
      while(rs.next()){
        String user = rs.getString("user");
        result += user + "\n";
      }
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
    return result;
  }

  public String get_DTER(int month){
    String query = "SELECT username, state, name FROM Customers";
    String dter = "DTER: \n";

    try{
      Statement s = this.conn.createStatement();
      ResultSet rs = s.executeQuery(query);
      while(rs.next()){
        String user = rs.getString("username");
        int market_id = this.get_market_from_user(user);
        double avg_balance = this.get_avg_daily_balance(market_id, this.month, this.year);
        double interest = (0.02/12) * avg_balance;

        int stock_id = this.get_stock_from_user(user);
        double total_earnings = this.get_total_earnings(stock_id, this.year, this.month);

        if((interest + total_earnings) > 10000){
          dter += rs.getString("name") + " : " + rs.getString("state") + "\n";
        }
      }
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
    return dter;
  }

  //help functions
  public int get_market_from_user(String user){
    String query = "SELECT ID FROM Accounts WHERE user = ? AND type = 0";
    int market_id = -1;
    try{
      PreparedStatement ps = this.conn.prepareStatement(query);
      ps.setString(1, user);
      ResultSet rs = ps.executeQuery();
      market_id = rs.getInt("ID");
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
    return market_id;
  }

  public int get_stock_from_user(String user){
    String query = "SELECT ID FROM Accounts WHERE user = ? AND type = 1";
    int stock_id = -1;
    try{
      PreparedStatement ps = this.conn.prepareStatement(query);
      ps.setString(1, user);
      ResultSet rs = ps.executeQuery();
      stock_id = rs.getInt("ID");
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
    return stock_id;
  }

  public double get_initial_market_balance(int acc_id, int year, int month){
    String get_initial_market_balance = "SELECT balance FROM Daily_Market_Balance WHERE ID = ? AND date LIKE ? ORDER BY date LIMIT 1";
    double balance = -1;
    try{
      String like_string = String.valueOf(year) + "-" + String.valueOf(month) + "%";
      PreparedStatement ps = this.conn.prepareStatement(get_initial_market_balance);
      ps.setInt(1, acc_id);
      ps.setString(2, like_string);
      ResultSet rs = ps.executeQuery();
      if(rs.next()){
        balance = rs.getDouble("balance");
      }
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
    // System.out.println("initial balance: " + String.valueOf(balance));
    return balance;
  }

  public double get_final_market_balance(int acc_id, int year, int month){
    String get_final_market_balance = "SELECT balance FROM Daily_Market_Balance WHERE ID = ? AND date LIKE ? ORDER BY date DESC LIMIT 1";
    double balance = -1;
    try{
      String like_string = String.valueOf(year) + "-" + String.valueOf(month) + "%";
      PreparedStatement ps = this.conn.prepareStatement(get_final_market_balance);
      ps.setInt(1, acc_id);
      ps.setString(2, like_string);
      ResultSet rs = ps.executeQuery();
      if(rs.next()){
        balance = rs.getDouble("balance");
      }
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
    // System.out.println("final balance: " + String.valueOf(balance));
    return balance;
  }

  public double get_total_earnings(int acc_id, int year, int month){
    //query
    String get_total_earnings = "SELECT SUM(earnings) AS total FROM (\n"
                                  + "SELECT ID, earnings FROM Stock_Transactions \n"
                                  + "WHERE date LIKE ?) AS t \n"
                                  + "WHERE t.ID = ? \n"
                                  + "GROUP BY t.ID";
    double earnings = -1;
    try{
      String like_string = String.valueOf(year) + "-" + String.valueOf(month) + "%";
      PreparedStatement ps = this.conn.prepareStatement(get_total_earnings);
      ps.setString(1, like_string);
      ps.setInt(2, acc_id);
      ResultSet rs = ps.executeQuery();
      if(rs.next()){
        earnings = rs.getDouble("total");
      }
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
    return earnings;
  }

  //STOCK AND MARKET ACCOUNT FUNCTIONS --------------------------------------------------
  public double get_balance(int acc_id){
    String query = "SELECT balance FROM Accounts WHERE Accounts.ID = ?";
    double balance = -1;
    try{
      //Get current balance
      PreparedStatement ps = this.conn.prepareStatement(query);
      ps.setInt(1, acc_id);
      ResultSet rs = ps.executeQuery();
      balance = rs.getDouble("balance");
      rs.close();
      ps.close();
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
    return balance;
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



  //MARKET ACCOUNT FUNCTIONS -------------------------------------------------------------
  //transactions
  public boolean deposit(int acc_id, double amount){
    if(amount < 0){
      System.out.println("You can only deposit positive amounts");
      return false;
    }

    //get current balance and calculate new balance
    double balance = this.get_balance(acc_id);
    double new_balance = balance + amount;

    //set new balance
    this.add_balance(acc_id, amount);

    //add entry in Market_Transactions
    this.insert_market_transaction(acc_id, 1, amount, new_balance);

    return true;
  }

  public boolean withdraw(int acc_id, int amount){
    if(amount < 0){
      System.out.println("You can only withdraw positive amounts");
      return false;
    }

    //get current balance and calculate new balance
    double balance = this.get_balance(acc_id);
    double new_balance = balance - amount;

    //fail if balance will become < 0
    if(new_balance < 0){
      System.out.println("Not enough $$$");
      return false;
    }

    //set new balance
    this.subtract_balance(acc_id, amount);

    //add entry in Market_Transactions
    this.insert_market_transaction(acc_id, 0, amount, new_balance);

    return true;
  }

  public void accrue_interest(int acc_id){
    //Get avg daily balance and calculate interest
    double avg_balance = this.get_avg_daily_balance(acc_id, this.month, this.year);
    double interest = (0.02/12) * avg_balance;

    //Get current balance and calculate new balance
    double curr_balance = this.get_balance(acc_id);
    double new_balance = curr_balance + interest;

    //record transaction in transaction table
    this.insert_market_transaction(acc_id, 2, interest, new_balance);

    //add money to market account
    this.add_balance(acc_id, interest);
  }

  //helpers
  public double get_avg_daily_balance(int acc_id, int month, int year){
    String query = "SELECT AVG(balance) AS avg FROM (\n"
        + "SELECT ID, balance FROM Daily_Market_Balance \n"
        + "WHERE date LIKE ?) AS t \n"
        + "WHERE t.ID = ? \n"
        + "GROUP BY t.ID";

    double avg_balance = -1;
    try{
      PreparedStatement ps = this.conn.prepareStatement(query);
      String like_string = String.valueOf(year) + "-" + String.valueOf(month) + "%";
      ps.setString(1, like_string);
      ps.setInt(2, acc_id);
      ResultSet rs = ps.executeQuery();
      avg_balance = rs.getDouble("avg");
      rs.close();
      ps.close();
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
    return avg_balance;
  }

  public void insert_market_transaction(int acc_id, int type, double amount, double new_balance){
    String query = "INSERT INTO Market_Transactions(ID, type, amount, date, balance) VALUES(?,?,?,?,?)";
    try{
      //Add entry in Market_Transactions
      PreparedStatement ps = this.conn.prepareStatement(query);
      ps.setInt(1, acc_id);
      ps.setInt(2, type);
      ps.setDouble(3, amount);
      ps.setString(4, this.date);
      ps.setDouble(5, new_balance);
      ps.executeUpdate();
      ps.close();
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
  }

  public void insert_closing_balance(int acc_id, double balance){
    String query = "INSERT INTO Daily_Market_Balance(ID, balance, date) \n"
    + "SELECT ?, ?, ? \n"
    + "WHERE NOT EXISTS (SELECT * FROM Daily_Market_Balance WHERE ID = ? AND date = ?)";
    try{
      PreparedStatement ps = this.conn.prepareStatement(query);
      ps.setInt(1, acc_id);
      ps.setDouble(2, balance);
      ps.setString(3, this.date);
      ps.setInt(4, acc_id);
      ps.setString(5, this.date);
      ps.executeUpdate();
      ps.close();
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
  }

  public boolean set_new_price(String symbol, double price){
    String query = "UPDATE Actors SET price = ? WHERE symbol = ?";
    if(price < 0){
      System.out.println("Price must be greater than $0!");
      return false;
    }

    try{
      PreparedStatement ps = this.conn.prepareStatement(query);
      ps.setDouble(1, price);
      ps.setString(2, symbol);
      ps.executeUpdate();
      ps.close();
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
    return true;
  }




  //STOCK ACCOUNT FUNCTIONS-----------------------------------------------------------------
  //transactions
  public boolean buy(int acc_id, String symbol, int amount){
    //function to buy stocks
    String query3 = "INSERT INTO Stock_Transactions(ID, symbol, type, date, price, amount, balance) VALUES(?,?,?,?,?,?,?)";
    String query4 = "SELECT * FROM Owns WHERE ID = ? AND symbol = ?";
    String query_first_buy = "INSERT INTO Owns(ID, symbol, amount) VALUES(?,?,?)";
    String query_already_owns = "UPDATE Owns SET amount = amount + ? WHERE ID = ? AND symbol = ?";
    String get_market_id = "SELECT ID, balance FROM Accounts WHERE user = (SELECT user FROM Accounts WHERE ID = ?) AND type = 0";
    boolean success = true;

    if(acc_id == -1){
      System.out.println("You do not have a stock account!");
      return false;
    }

    //get market account balance and id
    int market_id = -1;
    double market_balance = -1;
    try{
      PreparedStatement temp = this.conn.prepareStatement(get_market_id);
      temp.setInt(1, acc_id);
      ResultSet results = temp.executeQuery();
      market_id = results.getInt("ID");
      market_balance = results.getDouble("balance");
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }

    //get current stock price and calculate total price of transaction
    double price = this.get_stock_price(symbol);
    double total_price = (amount * price) + 20;

    if((price == -1) || (total_price > market_balance)){
      if(price == -1){
        System.out.println("Stock " + symbol + " does not exist!");
      }else{
        System.out.println("Insuffienct Funds in Market Account! You need $" + String.valueOf(total_price) + ". \nCurrent Balance: " + String.valueOf(market_balance));
      }
      success = false;
    }else{
      try{
        //check to see if customer already owns this stock
        PreparedStatement ps4 = this.conn.prepareStatement(query4);
        ps4.setInt(1, acc_id);
        ps4.setString(2, symbol);
        ResultSet rs4 = ps4.executeQuery();
        if(!rs4.isBeforeFirst()){
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

        double updated_balance = this.update_stock_balance(acc_id);

        //add entry in Stock_Transactions
        PreparedStatement ps3 = this.conn.prepareStatement(query3);
        ps3.setInt(1, acc_id);
        ps3.setString(2, symbol);
        ps3.setInt(3, 1);
        ps3.setString(4, this.date);
        ps3.setDouble(5, price);
        ps3.setDouble(6, amount);
        ps3.setDouble(7, updated_balance);
        ps3.executeUpdate();
        ps3.close();

        //subtract total price from market account
        this.subtract_balance(market_id, total_price);
      }catch (SQLException e){
      System.out.println(e.getMessage());
      }
    }
  return success;
  }

  public boolean sell(int acc_id, String symbol, int amount){
    String get_market_id = "SELECT ID, balance FROM Accounts WHERE user = (SELECT user FROM Accounts WHERE ID = ?) AND type = 0";
    String query3 = "INSERT INTO Stock_Transactions(ID, symbol, type, date, price, amount, balance, earnings) VALUES(?,?,?,?,?,?,?,?)";
    String query4 = "UPDATE Owns SET amount = amount - ? WHERE ID = ? AND symbol = ?";
    String query5 = "DELETE FROM Owns WHERE ID = ? AND symbol = ?";
    boolean success = true;

    int amount_owned = this.get_num_shares(acc_id, symbol);

    try{
      if(amount_owned < amount){
        System.out.println("Don't own enough stock! You own: " + String.valueOf(amount_owned) + " \nAttempting to sell: " + String.valueOf(amount));
        success = false;
      }else{
        int market_id = -1;
        double market_balance = -1;
        //get market account balance
        PreparedStatement temp = this.conn.prepareStatement(get_market_id);
        temp.setInt(1, acc_id);
        ResultSet results = temp.executeQuery();
        market_id = results.getInt("ID");
        market_balance = results.getDouble("balance");

        //Check market account has >= $20
        if(market_balance < 20){
          System.out.println("Not enough money in Market Account! Need: $20 \nYou Have: " + String.valueOf(market_balance));
          success = false;
        }else{
          //Get current price of stock
          double curr_price = this.get_stock_price(symbol);

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

          //Enter into Stock_Transaction
          double earnings = this.compute_earnings(acc_id, symbol, amount, curr_price);
          double updated_balance = this.update_stock_balance(acc_id);

          PreparedStatement ps3 = this.conn.prepareStatement(query3);
          ps3.setInt(1, acc_id);
          ps3.setString(2, symbol);
          ps3.setInt(3, 0);
          ps3.setString(4, this.date);
          ps3.setDouble(5, curr_price);
          ps3.setDouble(6, amount);
          ps3.setDouble(7, updated_balance);
          ps3.setDouble(8, earnings);
          ps3.executeUpdate();
          ps3.close();

          //Add earnings to Market account
          double amount_to_add = (amount * curr_price) - 20;
          this.add_balance(market_id, amount_to_add);
        }
      }
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
    return success;
  }

  public void insert_closing_price(String symbol, String date, double price){
    String query = "INSERT INTO Closing_Prices(symbol, date, price) \n"
    + "SELECT ?, ?, ? \n"
    + "WHERE NOT EXISTS (SELECT * FROM Closing_Prices WHERE symbol = ? AND date = ?)";
    try{
      PreparedStatement ps = this.conn.prepareStatement(query);
      ps.setString(1, symbol);
      ps.setString(2, this.date);
      ps.setDouble(3, price);
      ps.setString(4, symbol);
      ps.setString(5, this.date);
      ps.executeUpdate();
      ps.close();
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
  }

  //helpers
  public double get_stock_price(String symbol){
    //Get current stock price
    String query = "SELECT price FROM Actors WHERE Actors.symbol = ?";
    double price = -1;
    try{
      PreparedStatement ps = this.conn.prepareStatement(query);
      ps.setString(1, symbol);
      ResultSet rs = ps.executeQuery();
      if(rs.next()){
        price = rs.getDouble("price");
      }
      rs.close();
      ps.close();
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
    return price;
  }

  public int get_num_shares(int acc_id, String symbol){
    String query = "SELECT amount FROM Owns WHERE ID = ? AND symbol = ?";
    int result = 0;
    try{
      PreparedStatement ps = this.conn.prepareStatement(query);
      ps.setInt(1, acc_id);
      ps.setString(2, symbol);
      ResultSet rs = ps.executeQuery();
      if(!rs.isBeforeFirst()){
        result = 0;
      }else{
        result = rs.getInt("amount");
      }
      rs.close();
      ps.close();
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
    return result;
  }

  public double compute_earnings(int acc_id, String symbol, int amount, double curr_price){
    String query = "SELECT amount, price FROM Stock_Transactions WHERE ID = ? AND symbol = ?";
    ArrayList<Integer> amounts = new ArrayList<Integer>();
    ArrayList<Double> prices = new ArrayList<Double>();
    double earnings = 0;
    try{
      PreparedStatement ps = this.conn.prepareStatement(query);
      ps.setInt(1, acc_id);
      ps.setString(2, symbol);
      ResultSet rs = ps.executeQuery();
      int gathered = 0;
      while(true){
        if(rs.next()){
          int pile = rs.getInt("amount");
          double price = rs.getDouble("price");

          if((pile + gathered) >= amount){
            int amount_to_add = amount - gathered;
            amounts.add(amount_to_add);
            prices.add(price);
            break;
          }else{
            amounts.add(pile);
            prices.add(price);
            gathered += pile;
          }
        }
      }
      rs.close();
      for(int i = 0; i < amounts.size(); i++){
        earnings += (curr_price - prices.get(i)) * amounts.get(i);
      }
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
    return earnings;
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

  public double update_stock_balance(int acc_id){
    String query1 = "SELECT * FROM Owns WHERE owns.ID = ?";
    double balance = 0;
    // Connection conn = connect("jdbc:sqlite:E:/sqlite/db/chinook.db");
    try{
      PreparedStatement ps = this.conn.prepareStatement(query1);
      ps.setInt(1, acc_id);
      ResultSet rs = ps.executeQuery();
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
    return balance;
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




  //PROBABLY TO REMOVE LATER -----------------------------------------------------------------
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

  public void run_create_query(String query){
    try{
      Statement s = this.conn.createStatement();
      s.execute(query);
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
  }


  public String monthly_statement(Manager mn) {
     return "monthly Statement";
  }
  public String customer_report(String username)  {
    //Generate a list of all accounts associated with a particular customer and the current balance.
    // list all accounts associated with a username
    // use username to find acc_id andcall get_balance(int acc_id) use username to find acc_id
   return ("In customer_report username: " + username);
  }
  public void add_interest() {
    return;
  }
  public String generate_DTER(){
   return "generate_DTER";
  }

  public String active_customers() {
   return "generate_DTER";
  }
  public void delete_transactions()  {
   return;
  }
  public void set_date(String date) {
    return;
   }
  public void change_stock_price(double newPrice, String stockID) {
    return;
  }
  public void toggle_market(Boolean open) {
   return;
  }
  public void insert_data() {
   return;
  }

  public static void main(String[] args){
    Manager m = new Manager();
    System.out.println(m.get_DTER(3));
    // m.deposit(28, 100000);
    // m.buy(27, "SKB", 1000);
    // m.sell(27, "SKB", 1000);
    // System.out.println(m.get_active_customers(3));
    // m.buy(27, "SMD", 3);
    // m.sell(27, "SMD", 3);
    // m.withdraw(28, 2000);
    // m.close_market();
    // System.out.println(m.get_monthly_statement("test2"));
  }
}
