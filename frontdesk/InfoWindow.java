package frontdesk;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.TextArea;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

/**
 * InfoWindow is to display information of work order.
 * @author elias
 *
 */
public class InfoWindow extends JPanel implements ActionListener{
	private WorkOrder wo;
	JPanel infoPanel;
	private int numberOfdeptWO=0;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yy");
	private JLabel woNumberLabel = new JLabel("WO#");
	private JTextField woNumberTextField = new JTextField("Work Order Number");
	private JLabel ecnNumberLabel = new JLabel("ECN");
	private JTextField ecnNumberTextField = new JTextField("ECN");
	
	private JLabel POCNameLabel = new JLabel("Name Of POC");
	private JTextField POCNameTextField = new JTextField();
	private JLabel POCNumberLabel = new JLabel("Number Of POC");
	private JTextField POCNumberTextField = new JTextField();
	private JLabel requesterNameLabel = new JLabel("Name Of Requester");
	private JTextField requesterNameTextField = new JTextField();
	private JLabel requesterNumberLabel = new JLabel("Number Of Requester");
	private JTextField requesterNumberTextField = new JTextField();
	private JLabel firstContactDateLabel = new JLabel("First Contact Date");
	private JTextField firstContactDateTextField = new JTextField("N/A");
	private JLabel lastAttemptDateLabel = new JLabel("Last Attempt");
	private JTextField lastAttemptDateTextField = new JTextField("N/A");
	private JLabel lastContactDateLabel = new JLabel("Last Contact");
	private JTextField lastContactDateTextField = new JTextField("N/A");
	private JLabel numberOfAttemptsLabel = new JLabel("Attempt");
	private JTextField numberOfAttemptsTextField = new JTextField();
	private JButton contactButton = new JButton("Update");
	
	private JLabel lastContactMethodLabel = new JLabel("Last Contact Method");
	private JTextField lastContactMethodTextField = new JTextField("");
	
	private JLabel departmentWOLabel = new JLabel("numbers of WO");
	private JTextField departmentWOTextField = new JTextField("1");
	
	private JLabel isNotifiedrLabel = new JLabel("Notified?");
	private JTextField isNotifiedTextField = new JTextField();
	
	private JComboBox<String> contactMethod;
	final static String[] CONTACT_METHOD_ITEM = { "Phone" , "E-MAIL" }; 
	public InfoWindow(WorkOrder wo, String POCName, String POCNumber, String requesterName, String requesterNumber, int attempt) {
		this.wo = wo; 
		infoPanel = new JPanel();
		infoPanel.setLayout(new GridBagLayout());
		POCNameLabel = new JLabel("Name Of POC");
		POCNameTextField = new JTextField(POCName);
		POCNumberTextField = new JTextField(POCNumber);
		requesterNameTextField = new JTextField(requesterName);
		requesterNumberTextField = new JTextField(requesterNumber);
		contactMethod = new JComboBox<String>(CONTACT_METHOD_ITEM);
		woNumberTextField = new JTextField(wo.numberOfWorkOrder);
		ecnNumberTextField = new JTextField(wo.numberOfECN);
		
		if(wo.isContacted==true) {
			firstContactDateTextField.setText(wo.dateOfFirstContact);
			lastContactDateTextField.setText(wo.dateOfLastContact);
		}
		
		if(wo.dateOfLastAttempt!=null) {
			lastAttemptDateTextField.setText(wo.dateOfLastAttempt);
		}
		
		numberOfAttemptsLabel = new JLabel("Attempt");
		numberOfAttemptsTextField = new JTextField(attempt);
		contactButton = new JButton("Contact");
		
		POCNameTextField.setEditable(false);
		POCNumberTextField.setEditable(false);
		requesterNameTextField.setEditable(false);
		requesterNumberTextField.setEditable(false);
		firstContactDateTextField.setEditable(false);
		lastContactDateTextField.setEditable(false);
		woNumberTextField.setEditable(false);
		ecnNumberTextField.setEditable(false);
		departmentWOTextField.setEditable(false);
		
		GridBagConstraints con = new GridBagConstraints();
		
		addConstraints(0, 0, 1, infoPanel, con, woNumberLabel, new Dimension(120,20));
		addConstraints(1, 0, 1, infoPanel, con, woNumberTextField, new Dimension(200,20));
		addConstraints(2, 0, 1, infoPanel, con, new JLabel(""), new Dimension(50,20));
		addConstraints(3, 0, 1, infoPanel, con, ecnNumberLabel, new Dimension(130,20));
		addConstraints(4, 0, 1, infoPanel, con, ecnNumberTextField, new Dimension(200,20));
		
		addConstraints(0, 1, 1, infoPanel, con, POCNameLabel, new Dimension(120,20));
		addConstraints(1, 1, 1, infoPanel, con, POCNameTextField, new Dimension(200,20));
		addConstraints(2, 1, 1, infoPanel, con, new JLabel(""), new Dimension(50,20));
		addConstraints(3, 1, 1, infoPanel, con, POCNumberLabel, new Dimension(130,20));
		addConstraints(4, 1, 1, infoPanel, con, POCNumberTextField, new Dimension(200,20));
		
		addConstraints(0, 2, 1, infoPanel, con, requesterNameLabel, new Dimension(120,20));
		addConstraints(1, 2, 1, infoPanel, con, requesterNameTextField, new Dimension(200,20));
		addConstraints(2, 2, 1, infoPanel, con, new JLabel(""), new Dimension(50,20));
		addConstraints(3, 2, 1, infoPanel, con, requesterNumberLabel, new Dimension(130,20));
		addConstraints(4, 2, 1, infoPanel, con, requesterNumberTextField, new Dimension(200,20));
		
		addConstraints(0, 3, 1, infoPanel, con, firstContactDateLabel, new Dimension(120,20));
		addConstraints(1, 3, 1, infoPanel, con, firstContactDateTextField, new Dimension(200,20));
		addConstraints(2, 3, 1, infoPanel, con, new JLabel(""), new Dimension(50,20));
		addConstraints(3, 3, 1, infoPanel, con, lastContactDateLabel, new Dimension(130,20));
		addConstraints(4, 3, 1, infoPanel, con, lastContactDateTextField, new Dimension(200,20));

		addConstraints(0, 4, 1, infoPanel, con, new JLabel(""), new Dimension(120,20));
		addConstraints(2, 4, 1, infoPanel, con, new JLabel(""), new Dimension(50,20));
		addConstraints(1, 4, 1, infoPanel, con, new JLabel(""), new Dimension(200,20));
		addConstraints(3, 4, 1, infoPanel, con, new JLabel(""), new Dimension(130,20));
		addConstraints(4, 4, 1, infoPanel, con, new JLabel(""), new Dimension(200,20));
		
		
//		JTextArea remarkJTA = new JTextArea();
//		addConstraints(0, 5, 0, infoPanel, con, new JLabel("Note"), new Dimension(120,20));
//		addConstraints(1, 5, 1, infoPanel, con, remarkJTA, new Dimension(500,50));
		
		
		addConstraints(0, 6, 1, infoPanel, con, departmentWOLabel, new Dimension(120,20));
		addConstraints(1, 6, 1, infoPanel, con, departmentWOTextField, new Dimension(200,20));
		addConstraints(2, 6, 1, infoPanel, con, new JLabel(""), new Dimension(50,20));
		addConstraints(3, 6, 1, infoPanel, con, new JLabel(""), new Dimension(130,20));
		addConstraints(4, 6, 1, infoPanel, con, contactButton, new Dimension(200,20));
		
		
		String historyString="";
		for(String x : wo.listOfContactedDate) {
			if(x.length()>0) {
				historyString+=x+"<br>";
			} else {
				continue;
			}
		}
		ToolTipManager.sharedInstance().setInitialDelay(0);
		lastContactDateTextField.setToolTipText("<html><p style=\"text-align:center\">" +historyString+"</p></html>");
		
		infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		contactButton.addActionListener(this);
	}
	
	void setNumberOfDeptWO(int number) {
		numberOfdeptWO = number;
		departmentWOTextField.setText(String.valueOf(number));
	}
	
	private void addConstraints(int x, int y, int width, JPanel jp, GridBagConstraints constraints, JComponent component, Dimension prefSize) {
		constraints.gridx = x;
		constraints.gridy = y;
		constraints.gridwidth = width;
		component.setPreferredSize(prefSize);
		jp.add(component,constraints);
	}
	
	private String convertDateToSimpleDateFormat(Date date){
		return dateFormat.format(date);
	}
	
	@Override
	public void actionPerformed(ActionEvent ae) {
		int answer = JOptionPane.showConfirmDialog(new JFrame(), "Did the customer answer?");
		if(answer == 0) {
			Date date = new Date();
			if(!wo.isContacted) {
				wo.dateOfFirstContact = convertDateToSimpleDateFormat(date);
				wo.dateOfLastAttempt = convertDateToSimpleDateFormat(date);
				wo.dateOfLastContact = convertDateToSimpleDateFormat(date);
				firstContactDateTextField.setText(wo.dateOfFirstContact);
				lastContactDateTextField.setText(wo.dateOfLastContact);
			} else {
				wo.dateOfLastAttempt = convertDateToSimpleDateFormat(date);
				wo.dateOfLastContact = convertDateToSimpleDateFormat(date);
				lastAttemptDateTextField.setText(wo.dateOfLastAttempt);
				lastContactDateTextField.setText(wo.dateOfLastContact);
			}
			if(!wo.listOfContactedDate.contains(convertDateToSimpleDateFormat(date))) {
				wo.listOfContactedDate.add(convertDateToSimpleDateFormat(date));
			}
			wo.numbersOfAttempt++;
			wo.isContacted = true;
			wo.lastContactMethod = contactMethod.getName();
			wo.updateTableObject();
		} else if(answer == 1) {
			Date date = new Date();
			wo.dateOfLastAttempt = convertDateToSimpleDateFormat(date);
			lastAttemptDateTextField.setText(wo.dateOfLastAttempt);
			wo.numbersOfAttempt++;
			wo.updateTableObject();
		}
		String[] uds = {wo.numberOfWorkOrder, wo.nameOfCustomer, wo.numberOfECN, wo.dateOfLastAttempt, wo.dateOfFirstContact, wo.dateOfLastContact, wo.getIsContacted(), String.valueOf(wo.numbersOfAttempt), wo.getContactHistoryString(), wo.remarks};
		try {
			WorkOrderDataBaseConnector.updateReadyData("READY_TABLE", wo.numberOfWorkOrder, uds);
		} catch (SQLException e) {
			e.printStackTrace();
		}
//		JComponent comp = (JComponent) ae.getSource();
//		Window win = SwingUtilities.getWindowAncestor(comp);
//		win.dispose();	
	}
}
