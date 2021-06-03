// package net.project;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.plaf.FontUIResource;

public class AdminView extends JFrame {
	// Dataloader dl;
	Manager mn;

	JButton gmsButton;
	JTextField gmsTextField;
	JLabel gmsLabel;
	JButton crButton;
	JTextField crTextField;
	JLabel crLabel;
	JButton addInterest;
	JTextField addInterestTextField;
	JLabel addInterestLabel;
	JButton generateDTER;
	JButton listActiveCustomers;
	JButton deleteTransactions;
	JButton setDate;
	JTextField dateTextField;
	JLabel dateLabel;
	JButton clear;
	JButton logout;

	JButton updateStockPrice;
	JTextField updateStockPriceField;
	JLabel updateStockPriceLabel;
	JTextField updateStockField;
	JLabel updateStockLabel;

	JTextArea textArea;
	JScrollPane scroller;

	JButton openMarket;
	JButton closeMarket;
	JButton reset;



	public AdminView(Manager mydb){
		super("Admin");
		setLayout(null);

		mn = mydb;

		FontUIResource fbold = new FontUIResource(Font.SANS_SERIF,Font.BOLD,14);
		FontUIResource fplain = new FontUIResource(Font.SANS_SERIF,Font.PLAIN,16);
		UIManager.put("Label.font", fbold);
		UIManager.put("Button.font", fbold);
		UIManager.put("TextField.font", fplain);

		gmsButton = new JButton("Generate Monthly Statement");
		gmsButton.setBounds(20, 20, 300, 50);
		gmsTextField = new JTextField();
		gmsTextField.setBounds(350, 20, 300, 50);
		gmsLabel = new JLabel("Username");
		gmsLabel.setBounds(475, 50, 200, 50);
		gmsLabel.setDisplayedMnemonic(KeyEvent.VK_P);

		crButton = new JButton("Customer Report");
		crButton.setBounds(20, 90, 300, 50);
		crTextField = new JTextField();
		crTextField.setBounds(350, 90, 300, 50);
		crLabel = new JLabel("Username");
		crLabel.setBounds(475, 120, 200, 50);
		crLabel.setDisplayedMnemonic(KeyEvent.VK_P);

		addInterest = new JButton("Add Interest");
		addInterest.setBounds(20, 160, 150, 50);
		addInterestTextField = new JTextField();
		addInterestTextField.setBounds(170, 160, 150, 50);
		addInterestLabel = new JLabel("Account ID");
		addInterestLabel.setBounds(200, 190, 150, 50);
		addInterestLabel.setDisplayedMnemonic(KeyEvent.VK_P);

		generateDTER = new JButton("Generate DTER");
		generateDTER.setBounds(350, 160, 300, 50);

		listActiveCustomers = new JButton("List Active Customers");
		listActiveCustomers.setBounds(20, 230, 300, 50);
		deleteTransactions = new JButton("Delete Transactions");
		deleteTransactions.setBounds(350, 230, 300, 50);

		setDate = new JButton("Set Date");
		setDate.setBounds(20, 300, 300, 50);
		dateTextField = new JTextField();
		dateTextField.setBounds(350, 300, 300, 50);
		dateLabel = new JLabel("Enter Date mm-dd-yy");
		dateLabel.setBounds(425, 330, 200, 50);

		clear = new JButton("Clear");
		clear.setBounds(1270, 740, 100, 30);

		updateStockPrice = new JButton("Update Stock Price");
		updateStockPrice.setBounds(20, 370, 300, 50);
		updateStockPriceField = new JTextField();
		updateStockPriceField.setBounds(350, 370, 150, 50);

		updateStockField = new JTextField();
		updateStockField.setBounds(350, 370, 150, 50);
		updateStockPriceField = new JTextField();
		updateStockPriceField.setBounds(510, 370, 150, 50);
		updateStockLabel = new JLabel("Stock");
		updateStockLabel.setBounds(400, 400, 200, 50);
		updateStockPriceLabel = new JLabel("New Price");
		updateStockPriceLabel.setBounds(550, 400, 200, 50);

		openMarket = new JButton("Open Market");
		openMarket.setBounds(20, 440, 300, 50);

		closeMarket = new JButton("Close Market");
		closeMarket.setBounds(350, 440, 300, 50);

		reset = new JButton("Reset");
		reset.setBounds(20, 510, 300, 50);

		logout = new JButton("Logout");
    	logout.setBounds(350, 510, 300, 50);



		textArea = new JTextArea();
		textArea.setLineWrap(true);
		textArea.setEditable(false);
		scroller = new JScrollPane(textArea);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroller.setBounds(700,20,640,700);

		add(gmsButton); add(gmsTextField); add(gmsLabel); add(crButton);
		add(crTextField); add(crLabel); add(addInterest);add(addInterestTextField);add(addInterestLabel);
		add(generateDTER); add(listActiveCustomers); add(deleteTransactions); add(setDate);
		add(dateTextField); add(dateLabel); add(scroller); add(updateStockPrice);
		add(updateStockPriceLabel);add(updateStockPriceField); add(updateStockField); add(updateStockLabel);
		add(clear); add(openMarket); add(closeMarket); add(reset); add(logout);

		MyHandler handler = new MyHandler();
		gmsButton.addActionListener(handler);
		crButton.addActionListener(handler);
		addInterest.addActionListener(handler);
		generateDTER.addActionListener(handler);
		listActiveCustomers.addActionListener(handler);
		deleteTransactions.addActionListener(handler);
		setDate.addActionListener(handler);
		updateStockPrice.addActionListener(handler);
		openMarket.addActionListener(handler);
		closeMarket.addActionListener(handler);
		clear.addActionListener(handler);
		reset.addActionListener(handler);
		logout.addActionListener(handler);

	}

	private class MyHandler implements ActionListener {
		public void actionPerformed(ActionEvent event) {
    		//DEPOSIT
			if (event.getSource() == gmsButton){
				String get_user_name = gmsTextField.getText();
				if (get_user_name.isEmpty()){
					textArea.append("Please enter a username \n");
				}
				else{
					String result = "";
					try{
						result = mn.get_monthly_statement(get_user_name);
						textArea.append(result);
					}
					catch(Exception e){
						e.printStackTrace();
					}
				}
			}
			else if (event.getSource() == crButton){
				String get_user_name = crTextField.getText();
				if (get_user_name.isEmpty()){
					textArea.append("Please enter a username \n");
				}
				else{
					String result = "";
					try{
						result = mn.customer_report(get_user_name);
						textArea.append(result);
					}
					catch(Exception e){
						e.printStackTrace();
					}
				}

			}
			else if (event.getSource() == addInterest){
				String get_acc_id = addInterestTextField.getText();
				try{// need to pass in acc_id
					mn.accrue_interest(Integer.parseInt(get_acc_id));
					textArea.append("Interest has been added to all accounts \n");
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
			else if (event.getSource() == generateDTER){
				try{
					// need to add month 
					textArea.append(mn.get_DTER());
				}
				catch(Exception e){
						e.printStackTrace();
				}
			}
			else if (event.getSource() == listActiveCustomers){
				try{
					textArea.append(mn.active_customers());
				}
				catch(Exception e){
						e.printStackTrace();
				}
			}
			else if (event.getSource() == deleteTransactions){
				try{
					mn.wipe_transactions();
					textArea.append("List of transactions from each of the accounts was deleted \n");
				}
				catch(Exception e){
					e.printStackTrace();
				}

			}
			else if (event.getSource() == setDate){
				String full_date = dateTextField.getText();
				String[] date_parts = full_date.split("-");
				String mm = date_parts[0]; 
				String dd = date_parts[1]; 
				String yy = date_parts[2];
				 
				try{
					// (int day, int month, int year)
					mn.set_date(Integer.parseInt(dd),Integer.parseInt(mm),Integer.parseInt(yy));
				}
				catch(Exception e){
					e.printStackTrace();
				}
				textArea.append("Date is set to " + full_date + "\n");
			}
			else if(event.getSource() == clear){
				textArea.setText("");
			}
			else if (event.getSource() == updateStockPrice){
				String stockID = updateStockField.getText();
				String stockPrice = updateStockPriceField.getText();
				double stock_price = Double.parseDouble(stockPrice);
				try{
					mn.set_new_price(stockID,stock_price);
					textArea.append("Price of " + stockID + " is now " + stock_price);
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
			else if(event.getSource() == openMarket){
				try{
					mn.toggle_market(true);
				}
				catch(Exception e){
					e.printStackTrace();
				}
				textArea.append("Market is now open \n");
			}
			else if(event.getSource() == closeMarket){
				try{
					mn.toggle_market(false);
				}
				catch(Exception e){
					e.printStackTrace();
				}
				textArea.append("Market is now closed \n");
			}
			else if(event.getSource() == reset){
				try{
					mn.reset_data();
					textArea.append("Tables are restored to original state \n");
				}
				catch(Exception e){
					e.printStackTrace();
				}

			}
			else if(event.getSource() == logout){
				Launcher mjf = new Launcher();
				dispose();
			}
		}
	}

}