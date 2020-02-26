package frontdesk;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Setting class to display and handle application setting.
 * @author elias
 *
 */
public class Setting extends JPanel implements ActionListener{
	private GridBagConstraints con;
	
	private JLabel pickUpFolderLabel;
	private JLabel archiveFolderLabel;
	private JLabel dbFolderLabel;
	private JLabel outlookFolderLabel;
	private JLabel autoRefreshLabel;
	private JLabel tmdeTeamLabel;
	private JLabel manualUpdateLabel;
	
	JTextField pickUpFolderField;
	JTextField archiveFolderField;
	JTextField dbFolderField;
	JTextField outlookFolderField;
	JTextField autoRefreshIntervalField;
	JTextField jtfNumber;
	JTextField tmdeTeamField;
	JTextField tmdeTeamNameField;
	
	private JButton pickUpFolderButton;
	private JButton archiveFolderButton;
	private JButton dbFolderButton;
	private JButton outlookFolderButton;
	private JButton autoRefreshButton;
	private JButton tmdeTeamButton;
	private JButton manualUpdateButton;
	
	static int settingOption;
	
	public Setting() {
		con = new GridBagConstraints();
		this.setLayout(new GridBagLayout());
		pickUpFolderLabel = new JLabel("Pick-Up Folder");
		archiveFolderLabel = new JLabel("Archive Folder");
		dbFolderLabel = new JLabel("database file");
		outlookFolderLabel = new JLabel("Outlook file");
		autoRefreshLabel = new JLabel("Auto-Refresh");
		tmdeTeamLabel = new JLabel("TMDE Team Name");
		manualUpdateLabel = new JLabel("Manual DB Update");
		
		pickUpFolderField = new JTextField();
		archiveFolderField = new JTextField();
		dbFolderField = new JTextField();
		outlookFolderField = new JTextField();
		autoRefreshIntervalField = new JTextField();
		tmdeTeamField = new JTextField();
		
		pickUpFolderField.setEditable(false);
		archiveFolderField.setEditable(false);
		dbFolderField.setEditable(false);
		outlookFolderField.setEditable(false);
		autoRefreshIntervalField.setEditable(false);
		tmdeTeamField.setEditable(false);
		
		pickUpFolderButton = new JButton("Browse");
		archiveFolderButton = new JButton("Browse");
		dbFolderButton = new JButton("Browse");
		outlookFolderButton = new JButton("Browse");
		autoRefreshButton = new JButton("Change");
		tmdeTeamButton = new JButton("Change");
		manualUpdateButton = new JButton("Update");
		
		
		addConstraints(0, 0, 1, this, con, pickUpFolderLabel, new Dimension(120,20));
		addConstraints(0, 1, 1, this, con, archiveFolderLabel, new Dimension(120,20));
		addConstraints(0, 2, 1, this, con, dbFolderLabel, new Dimension(120,20));
		addConstraints(0, 3, 1, this, con, outlookFolderLabel, new Dimension(120,20));
		addConstraints(0, 4, 1, this, con, autoRefreshLabel, new Dimension(120,20));
		addConstraints(0, 5, 1, this, con, tmdeTeamLabel, new Dimension(120,20));
		addConstraints(0, 6, 1, this, con, new JLabel(), new Dimension(120,20));
		addConstraints(0, 7, 1, this, con, manualUpdateLabel, new Dimension(120,20));
		
		
		addConstraints(1, 0, 1, this, con, pickUpFolderField, new Dimension(400,20));
		addConstraints(1, 1, 1, this, con, archiveFolderField, new Dimension(400,20));
		addConstraints(1, 2, 1, this, con, dbFolderField, new Dimension(400,20));
		addConstraints(1, 3, 1, this, con, outlookFolderField, new Dimension(400,20));
		addConstraints(1, 4, 1, this, con, autoRefreshIntervalField, new Dimension(400,20));
		addConstraints(1, 5, 1, this, con, tmdeTeamField, new Dimension(400,20));
		addConstraints(1, 6, 1, this, con, new JLabel(), new Dimension(400,20));
		addConstraints(1, 7, 1, this, con, new JLabel(), new Dimension(400,20));
		
		addConstraints(2, 0, 1, this, con, pickUpFolderButton, new Dimension(120,20));
		addConstraints(2, 1, 1, this, con, archiveFolderButton, new Dimension(120,20));
		addConstraints(2, 2, 1, this, con, dbFolderButton, new Dimension(120,20));
		addConstraints(2, 3, 1, this, con, outlookFolderButton, new Dimension(120,20));
		addConstraints(2, 4, 1, this, con, autoRefreshButton, new Dimension(120,20));
		addConstraints(2, 5, 1, this, con, tmdeTeamButton, new Dimension(120,20));
		addConstraints(2, 6, 1, this, con, new JLabel(), new Dimension(120,20));
		addConstraints(2, 7, 1, this, con, manualUpdateButton, new Dimension(120,20));
		
		
//		addConstraints(3, 0, 1, this, con, new JLabel(), new Dimension(120,20));
//		addConstraints(3, 1, 1, this, con, new JLabel(), new Dimension(120,20));
//		addConstraints(3, 2, 1, this, con, new JLabel(), new Dimension(120,20));
//		addConstraints(3, 3, 1, this, con, new JLabel(), new Dimension(120,20));
//		addConstraints(3, 4, 1, this, con, new JLabel(), new Dimension(120,20));
//		addConstraints(3, 5, 1, this, con, new JLabel(), new Dimension(120,20));
//		
//		addConstraints(4, 0, 1, this, con, new JLabel(), new Dimension(120,20));
//		addConstraints(4, 1, 1, this, con, new JLabel(), new Dimension(120,20));
//		addConstraints(4, 2, 1, this, con, new JLabel(), new Dimension(120,20));
//		addConstraints(4, 3, 1, this, con, new JLabel(), new Dimension(120,20));
//		addConstraints(4, 4, 1, this, con, new JLabel(), new Dimension(120,20));
//		addConstraints(4, 5, 1, this, con, new JLabel(), new Dimension(120,20));

		addConstraints(2, 5, 1, this, con, tmdeTeamButton, new Dimension(120,20));	
		pickUpFolderButton.addActionListener(this);
		archiveFolderButton.addActionListener(this);
		dbFolderButton.addActionListener(this);
		outlookFolderButton.addActionListener(this);
		autoRefreshButton.addActionListener(this);
		tmdeTeamButton.addActionListener(this);
		manualUpdateButton.addActionListener(this);
	}
	
	private void addConstraints(int x, int y, int width, JPanel jp, GridBagConstraints constraints, JComponent component, Dimension prefSize) {
		constraints.gridx = x;
		constraints.gridy = y;
		constraints.gridwidth = width;
		component.setPreferredSize(prefSize);
		jp.add(component,constraints);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser jfc = new JFileChooser(".");
		File fp;
		if(e.getSource()==pickUpFolderButton) {
			jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			jfc.setAcceptAllFileFilterUsed(false);
			if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			     fp=jfc.getCurrentDirectory();
			     fp = jfc.getSelectedFile();
			     pickUpFolderField.setText(fp.toString()+"\\");
			} else {
				System.out.println("No Selection");
			}
		} else if(e.getSource()==archiveFolderButton) {
			jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			jfc.setAcceptAllFileFilterUsed(false);
			if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			     fp=jfc.getCurrentDirectory();
			     fp = jfc.getSelectedFile();
			     archiveFolderField.setText(fp.toString()+"\\");
			} else {
				System.out.println("No Selection ");
			}
		} else if(e.getSource()==dbFolderButton) {
			if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				fp = jfc.getSelectedFile();
				dbFolderField.setText(fp.toString());
			} else {
				System.out.println("No Selection ");
			}

		} else if(e.getSource()==outlookFolderButton) {
			if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				fp = jfc.getSelectedFile();
				outlookFolderField.setText(fp.toString());
			} else {
				System.out.println("No Selection ");
			}
		} else if(e.getSource()==autoRefreshButton) {
			Object[] AUTO_OPTIONS = {"Apply", "Cancel"};
			JPanel optJP = new JPanel();
			JTextArea jta = new JTextArea("Enter a number for auto-refresh interval.\nTo disable auto-refresh, enter 0.");
			jta.setBackground(getBackground());
			jta.setEditable(false);
			if(autoRefreshIntervalField.getText().contains("Disabled")) {
				jtfNumber = new JTextField("0");
			} else {
				jtfNumber = new JTextField(autoRefreshIntervalField.getText());
			}
			JLabel jlMin = new JLabel("Minutes");
			optJP.setLayout(new BorderLayout());
			optJP.add(jta,BorderLayout.NORTH);
			optJP.add(jtfNumber,BorderLayout.CENTER);
			optJP.add(jlMin,BorderLayout.EAST);
			
			int settingOption = JOptionPane.showOptionDialog(null, optJP, "Auto-Refresh", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, AUTO_OPTIONS, null);
			if(settingOption==0) {
				if(jtfNumber.getText().equalsIgnoreCase("0")) {
					autoRefreshIntervalField.setText("Disabled");
				} else {
					autoRefreshIntervalField.setText(jtfNumber.getText());
				}
			} 
		} else if(e.getSource()==tmdeTeamButton) {
			Object[] AUTO_OPTIONS = {"Apply", "Cancel"};
			JPanel optJP = new JPanel();
			JTextArea jta = new JTextArea("Enter a TMDE team :");
			jta.setBackground(getBackground());
			jta.setEditable(false);
			tmdeTeamNameField = new JTextField(tmdeTeamField.getText());
			
			JLabel jlMin = new JLabel("Team");
			optJP.setLayout(new BorderLayout());
			optJP.add(jta,BorderLayout.NORTH);
			optJP.add(tmdeTeamNameField,BorderLayout.CENTER);
			optJP.add(jlMin,BorderLayout.EAST);
			
			int settingOption = JOptionPane.showOptionDialog(null, optJP, "TMDE team", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, AUTO_OPTIONS, null);
			if(settingOption==0) {
					tmdeTeamField.setText(tmdeTeamNameField.getText());
			} 
		} else if(e.getSource()==manualUpdateButton) {
			try {
				ManualFunctions.updateArchiveDB();
				
			} catch (SQLException | ParseException | InterruptedException | IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	int getAutoRefreshInterval() {
		if(autoRefreshIntervalField.getText().contains("Disabled")) {
			return 0;
		} else {
			return Integer.parseInt(autoRefreshIntervalField.getText());
		}
	}
	
	String getPickUpFolder() {
		return pickUpFolderField.getText();
	}
	
	String getArchiveFolder() {
		return archiveFolderField.getText();
	}
	
	String getDBFolder() {
		return dbFolderField.getText();
	}
	
	String getOutlookFolder() {
		return outlookFolderField.getText();
	}
	
	String getTMDETeam() {
		return tmdeTeamField.getText();
	}
}
