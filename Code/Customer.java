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
    // this.conn = connect("jdbc:sqlite:E:/sqlite/db/chinook.db");
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
    return success;
  }

  public int signup(String name, String addy, String state, String pnumber, String email, String username, String password, double init_deposit){
    System.out.println("In login in Customer class name: " + name + " addy: " + addy + " state: " + state+ " pnumber: " + pnumber+ 
      " email: " + email + " username: " + username + " password: " + password + " init_deposit: " + init_deposit);
    String query = "SELECT * FROM Customers WHERE username = ?";
    String query2 = "SELECT ID, type from Accounts WHERE user = ?";
    boolean success = true;
    int tax_ID = random.nextInt(9000) + 1000;
    if(init_deposit < 1000){
      System.out.println("Initial deposit must be >= $1000");
      return 0;
    }

    try{
      PreparedStatement ps = this.conn.prepareStatement(query);
      ps.setString(1, username);
      ResultSet rs = ps.executeQuery();
      if(!rs.next()){
        rs.close();

        //insert customer
        this.insert_customer(name, addy, state, pnumber, email, username, password, tax_ID);

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
    return tax_ID;
    // return success;
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

  public void insert_customer(String name, String addy, String state, String pnumber, String email, String username, String password, int tax_id){
    String[] args = {name, addy, state, pnumber, email, username, password};
    String query = "INSERT INTO Customers(name, address, state, pnumber, email, username, password, tax_id) VALUES(?,?,?,?,?,?,?,?)";
    try{
      PreparedStatement ps = this.conn.prepareStatement(query);
      for(int i = 1; i < 8; i++){
        ps.setString(i, args[i - 1]);
      }
      // ps.setInt(8, Integer.parseInt(tax_id));
      ps.executeUpdate();
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
  }

  public void deposit(double amount){
    String query = "SELECT balance FROM Accounts WHERE Accounts.ID = ?";
    String query2 = "UPDATE Accounts SET balance = ? WHERE Accounts.ID = ?";
    String query3 = "INSERT INTO Market_Transactions(ID, type, amount, date, balance) VALUES(?,?,?,?,?)";

    try{
      //Get current balance
      PreparedStatement ps = this.conn.prepareStatement(query);
      ps.setInt(1, this.market_id);
      ResultSet rs = ps.executeQuery();
      double balance = rs.getDouble("balance");
      double new_balance = balance + amount;
      rs.close();
      ps.close();

      //Set new balance
      PreparedStatement ps2 = this.conn.prepareStatement(query2);
      ps2.setDouble(1, new_balance);
      ps2.setInt(2, this.market_id);
      ps2.executeUpdate();
      ps2.close();

      //add entry in Market_Transactions
      PreparedStatement ps3 = this.conn.prepareStatement(query3);
      ps3.setInt(1, this.market_id);
      ps3.setInt(2, 1);
      ps3.setDouble(3, amount);
      ps3.setString(4, this.get_date());
      ps3.setDouble(5, new_balance);
      ps3.executeUpdate();
      ps3.close();
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
  }

  public void withdraw(double amount){
    String query = "SELECT balance FROM Accounts WHERE Accounts.ID = ?";
    String query2 = "UPDATE Accounts SET balance = ? WHERE Accounts.ID = ?";
    String query3 = "INSERT INTO Market_Transactions(ID, type, amount, date, balance) VALUES(?,?,?,?,?)";

    try{
      //Get current balance
      PreparedStatement ps = this.conn.prepareStatement(query);
      ps.setInt(1, this.market_id);
      ResultSet rs = ps.executeQuery();
      double balance = rs.getDouble("balance");
      double new_balance = balance - amount;
      rs.close();
      ps.close();

      //set new balance
      PreparedStatement ps2 = this.conn.prepareStatement(query2);
      ps2.setDouble(1, new_balance);
      ps2.setInt(2, this.market_id);
      ps2.executeUpdate();
      ps2.close();

      //add entry in Market_Transactions
      PreparedStatement ps3 = this.conn.prepareStatement(query3);
      ps3.setInt(1, this.market_id);
      ps3.setInt(2, 0);
      ps3.setDouble(3, amount);
      ps3.setString(4, this.get_date());
      ps3.setDouble(5, new_balance);
      ps3.executeUpdate();
      ps3.close();
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
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

  public boolean buy(String symbol, int amount){
    String query0 = "SELECT balance FROM Accounts WHERE Accounts.ID = ?";
    String query = "SELECT balance FROM Acounts WHERE Accounts.ID = ?";
    String query2 = "SELECT price FROM Actors WHERE Actors.symbol = ?";
    String query3 = "INSERT INTO Stock_Transactions(ID, symbol, type, date, price, amount, balance) VALUES(?,?,?,?,?,?,?)";
    String query4 = "SELECT * FROM Owns WHERE ID = ? AND symbol = ?";
    String query_first_buy = "INSERT INTO Owns(ID, symbol, amount) VALUES(?,?,?)";
    String query_already_owns = "UPDATE Owns SET amount = amount + ? WHERE ID = ? AND symbol = ?";
    String query_update_market_balance = "UPDATE Accounts SET balance = ? WHERE Accounts.ID = ?";
    boolean success = true;

    if(this.stock_id == -1){
      System.out.println("You do not have a stock account!");
      return false;
    }

    try{
      //Get initial stock account balance
      PreparedStatement ps0 = this.conn.prepareStatement(query0);
      ps0.setInt(1, this.stock_id);
      ResultSet rs0 = ps0.executeQuery();
      double balance = rs0.getDouble("balance");
      rs0.close();
      ps0.close();

      //Get balance from market account
      PreparedStatement ps = this.conn.prepareStatement(query);
      ps.setInt(1, this.market_id);
      ResultSet rs = ps.executeQuery();
      double market_balance = rs.getDouble("balance");
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
        success = false;
      }else{
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
      }
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
    return success;
  }

  public boolean sell(String symbol, int amount){
    String query0 = "SELECT amount FROM Owns WHERE ID = ? AND symbol = ?";
    String query = "SELECT balance FROM Acounts WHERE Accounts.ID = ?";
    String query2 = "SELECT price FROM Actors WHERE Actors.symbol = ?";
    String query3 = "INSERT INTO Stock_Transactions(ID, symbol, type, date, price, amount, balance) VALUES(?,?,?,?,?,?,?)";
    String query4 = "UPDATE Owns SET amount = amount - ? WHERE ID = ? AND symbol = ?";
    String query5 = "DELETE FROM Owns WHERE ID = ? AND symbol = ?";
    boolean success = true;

    if(this.stock_id == -1){
      System.out.println("You do not have a stock account!");
      success = false;
    }

    try{
      //Get number of stocks owned
      PreparedStatement ps0 = this.conn.prepareStatement(query0);
      ps0.setInt(1, this.stock_id);
      ResultSet rs0 = ps0.executeQuery();
      int amount_owned = rs0.getInt("amount");
      rs0.close();
      ps0.close();

      //Check amount owned > amount trying to sell
      if(amount_owned < amount){
        System.out.println("Don't own enough stock! You own: " + String.valueOf(amount_owned) + " \nAttempting to sell: " + String.valueOf(amount));
        success = false;
      }else{
        //get market account balance
        PreparedStatement ps = this.conn.prepareStatement(query);
        ps.setInt(1, this.market_id);
        ResultSet rs = ps.executeQuery();
        double market_balance = rs.getDouble("balance");
        rs.close();
        ps.close();

        //Check market account has > $20
        if(market_balance < 20){
          System.out.println("Not enough money in Market Account! Need: $20 \nYou Have: " + String.valueOf(market_balance));
          success = false;
        }else{
          //Get current price of stock
          PreparedStatement ps2 = this.conn.prepareStatement(query2);
          ps2.setString(1, symbol);
          ResultSet rs2 = ps2.executeQuery();
          double curr_price = rs2.getDouble("price");
          ps2.close();

          //Get current balance of stock account
          PreparedStatement ps_get_stock_balance = this.conn.prepareStatement(query);
          ps_get_stock_balance.setInt(1, this.stock_id );
          ResultSet rs_get_stock_balance = ps_get_stock_balance.executeQuery();
          double balance = rs_get_stock_balance.getDouble("balance");
          ps2.close();

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


          //Add earnings to Market account
          double amount_to_add = (amount * curr_price) - 20;
          this.add_market_balance(amount_to_add);
        }
      }
    }catch (SQLException e){
      System.out.println(e.getMessage());
    }
    return success;
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
  }
}

