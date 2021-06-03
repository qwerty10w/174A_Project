// package net.project;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.awt.event.*;
import javax.swing.*;

public class Launcher extends JFrame {

	JTextField userTextField;
	JTextField passTextField;
	JFrame frame;
	Container content;
	JButton button;
	JPanel userPanel;
	JLabel userLabel;
	JPanel passPanel;
	JPanel loginPanel;
	JLabel passLabel;
	JPanel panel;
	JCheckBox isAdmin;
	JCheckBox signUp;
	JPanel boxPanel;
	JPanel checkIt;
	
	// Dataloader dl;
	// Manager mn;
	Customer cs; 
	public Launcher(){
		cs = new Customer();
		// dl = new Dataloader();
		// mn = new Manager();

		// try{
		// 	db.start();
		// }
		// catch( Exception e){
		// 	e.printStackTrace();
		// }
		

		String title = "Login";
		frame = new JFrame(title);
		content = frame.getContentPane();

		isAdmin = new JCheckBox("Login as Admin");
		isAdmin.setMnemonic(KeyEvent.VK_C);

		signUp = new JCheckBox("Sign Up");
		isAdmin.setMnemonic(KeyEvent.VK_C);

		button = new JButton("Login");
		userPanel = new JPanel(new BorderLayout());
		userLabel = new JLabel("Username: ");
		userLabel.setDisplayedMnemonic(KeyEvent.VK_U);
		userTextField = new JTextField();
		userLabel.setLabelFor(userTextField);
		userPanel.add(userLabel, BorderLayout.WEST);
		userPanel.add(userTextField, BorderLayout.CENTER);

		passPanel = new JPanel(new BorderLayout());
		passLabel = new JLabel("Password: ");
		passLabel.setDisplayedMnemonic(KeyEvent.VK_P);
		passTextField = new JTextField();
		passLabel.setLabelFor(passTextField);
		passPanel.add(passLabel, BorderLayout.WEST);
		passPanel.add(passTextField, BorderLayout.CENTER);

		boxPanel = new JPanel(new BorderLayout());
		checkIt = new JPanel(new BorderLayout());
		boxPanel.add(button, BorderLayout.NORTH);
		checkIt.add(isAdmin, BorderLayout.EAST);
		checkIt.add(signUp, BorderLayout.WEST);


		boxPanel.add(checkIt, BorderLayout.SOUTH);

		passPanel.add(boxPanel, BorderLayout.SOUTH);

		panel = new JPanel(new BorderLayout());
		panel.add(userPanel, BorderLayout.NORTH);
		panel.add(passPanel, BorderLayout.SOUTH);
		content.add(panel, BorderLayout.NORTH);

		// The only thing we want to wait for is a click on the button
		MyHandler handler = new MyHandler();
		button.addActionListener(handler);
		isAdmin.addActionListener(handler);
		signUp.addActionListener(handler);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(250, 150);
		frame.setVisible(true);
		frame.setResizable(false);
    } // MyJFrame

    // inner class
    private class MyHandler implements ActionListener {
    	public void actionPerformed(ActionEvent event) {
    		int id = 1;
    		if (event.getSource() == button){

    			String name = userTextField.getText();
    			String password = passTextField.getText();

    			if (isAdmin.isSelected()){

    				try{
    					// id = dl.loginAdmin(name, password);
    					// id = mn.create_admin(name,password);
    					boolean adminCreated = cs.login_admin(name,password);
    				}
    				catch (Exception e){
    					e.printStackTrace();
    				}
    				
    				if(id != 0){
    					//call new Admin gui
    					// AdminView ad = new AdminView(dl, id);
    					AdminView ad = new AdminView(cs, id);
    					ad.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    					ad.setSize(1400,800);
    					ad.setVisible(true);
    					ad.setResizable(false);
    					frame.dispose();
    				}

    			
    			}
    			else if (signUp.isSelected()){
    				//call db signup function with username and password
    				// Signup su = new Signup(name, password, dl, id);
    				Signup su = new Signup(name, password, cs, id);
    				frame.dispose();
    			}
    			else{
    				//proceed as normal to login user who already has an account
    				
    				try{
    					// id = dl.login(name, password);
    					// id = mn.login(name, password);
    					boolean customerCreated = cs.login(name, password);
    				}
    				catch (Exception e){
    					e.printStackTrace();
    				}

    				if(id != 0){
    					// GUI gui = new GUI(dl, id);
    					GUI gui = new GUI(cs, id);
    					gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    					gui.setSize(700,800);
    					gui.setVisible(true);
    					gui.setResizable(false);
    					frame.dispose();
    				}
    			}
    		}
	} // actionPerformed
}
}