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
import java.util.Random;

public class Customer{
  //Member Vars
  Random random;
  public Connection conn;
  public String username;
  public int market_id;
  public int stock_id = -1;

  public Customer(){
    this.conn = connect("jdbc:sqlite:E:/sqlite/db/chinook.db");
    random = new Random();
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


  //LOGISTICAL FUNCTIONS----------------------------------------------------------------------------
  public String get_date(){
    String query = "SELECT * FROM Calendar";
    String date = "FAILED";
    try{
      Statement s = this.conn.createStatement();
      ResultSet rs = s.executeQuery(query);
      int day = rs.getInt("day");
      int month = rs.getInt("month");
      int year = rs.getInt("year");
      date = String.valueOf(year) + "-" + String.valueOf(month) + "-" + String.valueOf(day);
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
    return date;
  }

  public boolean login_admin(String username, String password){
    System.out.println("In login_admin in Customer class username: " + username + " password: " + password);
    return true;
  }

  public boolean login(String username, String password){
    System.out.println("In login in Customer class username: " + username + " password: " + password);
    String query = "SELECT password FROM Customers WHERE username = ?";
    String query2 = "SELECT ID, type from Accounts WHERE user = ?";
    boolean success = true;
    try{
      PreparedStatement ps = this.conn.prepareStatement(query);
      ps.setString(1, username);
      ResultSet rs = ps.executeQuery();
      if(!rs.next()){
        System.out.println("username does not exist!");
        rs.close();
        success = false;
      }else{
        String pw = rs.getString("password");
        if(password.equals(pw)){
          rs.close();
          this.username = username;
          PreparedStatement ps2 = this.conn.prepareStatement(query2);
          ps2.setString(1, username);
          ResultSet rs2 = ps2.executeQuery();
          while(rs2.next()){
            int type = rs2.getInt("type");
            int acc_id = rs2.getInt("ID");
            if(type == 0){
              this.market_id = acc_id;
            }else if(type == 1){
              this.stock_id = acc_id;
            }
          }
        }else{
          System.out.println("INCORRECT PASSWORD!");
          rs.close();
          success = false;
        }
      }
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
    System.out.println("Sucessfully logged in as: " + this.username + ". \nMA: " + String.valueOf(this.market_id) + "\nSA: " + String.valueOf(this.stock_id));
    return success;
  }

  public boolean signup(String name, String addy, String state, String pnumber, String email, String username, String password, double init_deposit){
    System.out.println("In signup in Customer class name: " + name + " addy: " + addy + " state: " + state+ " pnumber: " + pnumber +
      " email: " + email + " username: " + username + " password: " + password + " init_deposit: " + init_deposit);
    String query = "SELECT * FROM Customers WHERE username = ?";
    String query2 = "SELECT ID, type from Accounts WHERE user = ?";
    boolean success = true;
    int tax_ID = random.nextInt(9000) + 1000;
    if(init_deposit < 1000){
      System.out.println("Initial deposit must be >= $1000");
      return false;
    }

    try{
      PreparedStatement ps = this.conn.prepareStatement(query);
      ps.setString(1, username);
      ResultSet rs = ps.executeQuery();
      if(!rs.next()){
        rs.close();

        //insert customer
        this.insert_customer(name, addy, state, pnumber, email, username, password, tax_ID);

        //insert inactive stock account
        this.insert_stock_account(username);

        //insert market account with 0 balance
        this.insert_market_account(username, 0);

        //store new customer info locally
        this.login(username, password);

        //make initial deposit into account
        this.deposit(init_deposit);

      }else{
        rs.close();
        System.out.println("username aready in use!");
        success = false;
      }
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
    return success;
  }

  public void insert_customer(String name, String addy, String state, String pnumber, String email, String username, String password, int tax_id){
    String[] args = {name, addy, state, pnumber, email, username, password};
    String query = "INSERT INTO Customers(name, address, state, pnumber, email, username, password, tax_id) VALUES(?,?,?,?,?,?,?,?)";
    try{
      PreparedStatement ps = this.conn.prepareStatement(query);
      for(int i = 1; i < 8; i++){
        ps.setString(i, args[i - 1]);
      }
      ps.setInt(8, tax_id);
      ps.executeUpdate();
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
  }

  public boolean check_market_open(){
    return true;
  }

  public String top_movies(int start, int end) {
    return "top movies";
  }

  public boolean check_stock_exists(String symbol){
    String query = "SELECT * FROM Actors WHERE symbol = ?";
    boolean result = true;
    try{
      PreparedStatement ps = this.conn.prepareStatement(query);
      ps.setString(1, symbol);
      ResultSet rs = ps.executeQuery();
      if(!rs.next()){
        result = false;
      }
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
    return result;
  }

  public String get_actor_profile(String stockID)  {
    return "get actor profile";
  }

  public String movie_info(String title) throws SQLException{
    return "movie info";
  }

  public String movie_review(String title) throws SQLException {
    return "movie review";
  }

  public Boolean check_transaction_history(Customer cs)  {
    return true;
  }

  public String get_transaction_history(Customer cs)  {
    return "get transaction history";
  }


  //MARKET ACCOUNT FUNCTIONS ----------------------------------------------------------------------
  //transactions
  public boolean deposit(double amount){
    if(amount < 0){
      System.out.println("You can only deposit positive amounts");
      return false;
    }
    //Get current balance and calculate new balance
    double balance = this.get_market_balance();
    double new_balance = balance + amount;

    //Set new balance
    this.add_market_balance(amount);

    //add entry in market transactions
    this.insert_market_transaction(1, amount, new_balance);

    return true;
  }

  public boolean withdraw(double amount){
    if(amount < 0){
      System.out.println("You can only withdraw positive amounts");
      return false;
    }

    //Get current balance and calculate new balance
    double balance = this.get_market_balance();
    double new_balance = balance - amount;

    if(new_balance < 0){
      System.out.println("Insuffienct funds in market account!");
      return false;
    }

    //set new balance
    this.subtract_market_balance(amount);

    //add entry in Market_Transactions
    this.insert_market_transaction(0, amount, new_balance);

    return true;
  }

  //helpers
  public double get_market_balance(){
    String query = "SELECT balance FROM Accounts WHERE Accounts.ID = ?";
    double balance = -1;
    try{
      //Get current balance
      PreparedStatement ps = this.conn.prepareStatement(query);
      ps.setInt(1, this.market_id);
      ResultSet rs = ps.executeQuery();
      balance = rs.getDouble("balance");
      rs.close();
      ps.close();
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
    return balance;
  }

  public void add_market_balance(double amount){
    String query = "UPDATE Accounts SET balance = balance + ? WHERE Accounts.ID = ?";
    try{
      PreparedStatement ps = this.conn.prepareStatement(query);
      ps.setDouble(1, amount);
      ps.setInt(2, this.market_id);
      ps.executeUpdate();
      ps.close();
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
  }

  public void subtract_market_balance(double amount){
    String query = "UPDATE Accounts SET balance = balance - ? WHERE Accounts.ID = ?";
    try{
      PreparedStatement ps = this.conn.prepareStatement(query);
      ps.setDouble(1, amount);
      ps.setInt(2, this.market_id);
      ps.executeUpdate();
      ps.close();
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
  }

  public void insert_market_transaction(int type, double amount, double new_balance){
    String query = "INSERT INTO Market_Transactions(ID, type, amount, date, balance) VALUES(?,?,?,?,?)";
    try{
      //Add entry in Market_Transactions
      PreparedStatement ps = this.conn.prepareStatement(query);
      ps.setInt(1, this.market_id);
      ps.setInt(2, type);
      ps.setDouble(3, amount);
      ps.setString(4, this.get_date());
      ps.setDouble(5, new_balance);
      ps.executeUpdate();
      ps.close();
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
  }




  //STOCK ACCOUNT FUNCTIONS------------------------------------------------------------------------
  //transactions
  public boolean buy(String symbol, int amount){
    //function to buy stocks
    String query3 = "INSERT INTO Stock_Transactions(ID, symbol, type, date, price, amount, balance) VALUES(?,?,?,?,?,?,?)";
    String query4 = "SELECT * FROM Owns WHERE ID = ? AND symbol = ?";
    String query_first_buy = "INSERT INTO Owns(ID, symbol, amount) VALUES(?,?,?)";
    String query_already_owns = "UPDATE Owns SET amount = amount + ? WHERE ID = ? AND symbol = ?";
    boolean success = true;

    if(this.stock_id == -1){
      System.out.println("You do not have a stock account!");
      return false;
    }

    //get market account balance
    double market_balance = this.get_market_balance();

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
        ps4.setInt(1, this.stock_id);
        ps4.setString(2, symbol);
        ResultSet rs4 = ps4.executeQuery();
        if(!rs4.next()){
          rs4.close();
          ps4.close();
          //If doesn't own stock, create new entry in owns
          PreparedStatement ps5 = this.conn.prepareStatement(query_first_buy);
          ps5.setInt(1, this.stock_id);
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
          ps5.setInt(2, this.stock_id);
          ps5.setString(3, symbol);
          ps5.executeUpdate();
          ps5.close();
        }

        double updated_balance = this.update_stock_balance();

        //add entry in Stock_Transactions
        PreparedStatement ps3 = this.conn.prepareStatement(query3);
        ps3.setInt(1, this.stock_id);
        ps3.setString(2, symbol);
        ps3.setInt(3, 1);
        ps3.setString(4, this.get_date());
        ps3.setDouble(5, price);
        ps3.setDouble(6, amount);
        ps3.setDouble(7, updated_balance);
        ps3.executeUpdate();
        ps3.close();

        //subtract total price from market account
        this.subtract_market_balance(total_price);
      }catch (SQLException e){
      System.out.println(e.getMessage());
      }
    }
  return success;
  }

  public boolean sell(String symbol, int amount){
    String query3 = "INSERT INTO Stock_Transactions(ID, symbol, type, date, price, amount, balance, earnings) VALUES(?,?,?,?,?,?,?,?)";
    String query4 = "UPDATE Owns SET amount = amount - ? WHERE ID = ? AND symbol = ?";
    String query5 = "DELETE FROM Owns WHERE ID = ? AND symbol = ?";
    boolean success = true;
    int amount_owned = -1;

    System.out.println("attempting to sell " + String.valueOf(amount) + " shares of " + symbol + " stock.");

    if(this.stock_id == -1){
      System.out.println("You do not have a stock account!");
      success = false;
    }else{
      amount_owned = this.get_num_shares(symbol);
    }

    System.out.println("Currently own " + String.valueOf(amount_owned) + " shares");

    try{
      //Check amount owned > amount trying to sell
      if(amount_owned < amount){
        System.out.println("Don't own enough stock! You own: " + String.valueOf(amount_owned) + " \nAttempting to sell: " + String.valueOf(amount));
        success = false;
      }else{
        //get market account balance
        double market_balance = this.get_market_balance();
        System.out.println("Current market balance = " + String.valueOf(market_balance));

        //Check market account has >= $20
        if(market_balance < 20){
          System.out.println("Not enough money in Market Account! Need: $20 \nYou Have: " + String.valueOf(market_balance));
          success = false;
        }else{
          //Get current price of stock
          double curr_price = this.get_stock_price(symbol);
          System.out.println("after get curr_price");

          //Edit owned
          if(amount_owned - amount == 0){
            //Delete row if num of stocks owned is 0
            PreparedStatement ps4 = this.conn.prepareStatement(query5);
            ps4.setInt(1, this.stock_id);
            ps4.setString(2, symbol);
            ps4.executeUpdate();
            ps4.close();
          }else{
            //Decrement number of stocks owned
            PreparedStatement ps4 = this.conn.prepareStatement(query4);
            ps4.setInt(1, amount);
            ps4.setInt(2, this.stock_id);
            ps4.setString(3, symbol);
            ps4.executeUpdate();
            ps4.close();
          }
          System.out.println("after edit owned");

          //Enter into Stock_Transaction
          double earnings = this.compute_earnings(symbol, amount, curr_price);
          double updated_balance = this.update_stock_balance();

          PreparedStatement ps3 = this.conn.prepareStatement(query3);
          ps3.setInt(1, this.stock_id);
          ps3.setString(2, symbol);
          ps3.setInt(3, 0);
          ps3.setString(4, this.get_date());
          ps3.setDouble(5, curr_price);
          ps3.setDouble(6, amount);
          ps3.setDouble(7, updated_balance);
          ps3.setDouble(8, earnings);
          ps3.executeUpdate();
          ps3.close();

          System.out.println("after get insert into Stock_Transactions");

          //Add earnings to Market account
          double amount_to_add = (amount * curr_price) - 20;
          this.add_market_balance(amount_to_add);
          System.out.println("after get add earnings to market account");
        }
      }
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
    return success;
  }

  //helpers
  public double get_stock_balance(){
    String query = "SELECT balance FROM Accounts WHERE Accounts.ID = ?";
    double balance = -1;
    try{
      //Get current balance
      PreparedStatement ps = this.conn.prepareStatement(query);
      ps.setInt(1, this.stock_id);
      ResultSet rs = ps.executeQuery();
      balance = rs.getDouble("balance");
      rs.close();
      ps.close();
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
    return balance;
  }

  public int get_num_shares(String symbol){
    String query = "SELECT amount FROM Owns WHERE ID = ? AND symbol = ?";
    int result = 0;
    try{
      PreparedStatement ps = this.conn.prepareStatement(query);
      ps.setInt(1, this.stock_id);
      ps.setString(2, symbol);
      ResultSet rs = ps.executeQuery();
      if(!rs.next()){
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

  public double compute_earnings(String symbol, int amount, double curr_price){
    String query = "SELECT amount, price FROM Stock_Transactions WHERE ID = ? AND symbol = ?";
    ArrayList<Integer> amounts = new ArrayList<Integer>();
    ArrayList<Double> prices = new ArrayList<Double>();
    double earnings = 0;
    try{
      PreparedStatement ps = this.conn.prepareStatement(query);
      ps.setInt(1, this.stock_id);
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

  public double update_stock_balance(){
    String query1 = "SELECT * FROM Owns WHERE owns.ID = ?";
    double balance = 0;

    try{
      PreparedStatement ps = this.conn.prepareStatement(query1);
      ps.setInt(1, this.stock_id);
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
        ps3.setInt(2, this.stock_id);
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


  //FUNCTION TO REMOVE LATER PROBABLY ------------------------------------------------------------------------
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

  public void run_create_query(String query){
    try{
      Statement s = this.conn.createStatement();
      s.execute(query);
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
  }

  public static void main(String[] args){
    Customer m = new Customer();
    m.signup("Neil Sadhukhan", "test Address", "CA", "4088960412", "neil.sad@gmail.com", "test2", "te", 10000);
    // m.login("test", "te");
    // m.withdraw(100000000);
  }
}
