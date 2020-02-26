package frontdesk;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.RowFilter;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.printing.PDFPageable;

import integrisign.desktop.ValidationException;

/**
 * FrontDesk class is a main class for GUI with loading and setting. 
 * @author elias
 *
 */
public class FrontDesk extends JFrame implements ActionListener {
	private final static int timerSecond = 200;
	private boolean loopBreaker = false;
	private static JButton CheckOutButton, printButton, emailButton, attachButton,refreshButton;
	private static JTabbedPane tabbedPane = new JTabbedPane();
	private static JToolBar toolBar = new JToolBar();
	private File configFile = new File("FrontDesk.cfg");
	private int currentTab;
	private int numberOfTabs;
	private JTextField searchTextField = new JTextField("");
	private JPanel readyForPickUpPanel = new JPanel();
	private JPanel archivePanel = new JPanel();
	private JScrollPane readyForPickUpScrollPane, archivePanelScrollPane;
	private JButton infoButton, checkOutButton;
	private TableModel readyForPickUpTableModel, archiveTableModel;
	private JTable readyForPickUpTable, archiveTable;
	private Object[][] readyForPickupData, archiveData;
	private String[] readyForPickUpColumn = {"WO#","ECN","CUSTOMER", "NOMENCLATURE", "Contacted?", "Attempt", "Last Attempt", "Last Contact"};
	private String[] archiveColumn = {"WO#","ECN","CUSTOMER","Signer", "Pick-up Date"};
	private int autoRefreshInterval=0;
	private TableRowSorter<TableModel> readySorter;
	private TableRowSorter<TableModel> archiveSorter;
	private WorkOrderMapper readyForPickUp;
	private WorkOrderMapper archive;
	private boolean isRemarksUpdated=false;
	static LoadingScreen loadingScreen = new LoadingScreen();
	final static Object lock = new Object(); 
	
	static int overwriteOption = 99;
	String readyFolder,archiveFolder,dbFolder;
	static String outlookFile,tmdeTeam;
	private Timer t;
	private boolean isRunning = false;
	static Thread pgb;
	static Thread rfpt;
	
	public FrontDesk() throws InterruptedException, SQLException {
		super("CEB Front Desk Work Order Check Out Program");
		System.loadLibrary("esjDeskPad");
		System.loadLibrary("ESUtil");
		loadConfigFile();
		loadingScreen.setVisible(true);
		WorkOrderDataBaseConnector.msAccDB = dbFolder;
		WorkOrderDataBaseConnector.init();
		ProgressBarHelper.addFileCounter();
	}

	void initializeFrontDesk() {	
			WorkOrderDataBaseConnector.msAccDB = dbFolder;
			rfpt = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						readyForPickUp = new WorkOrderMapper(readyFolder, "READY_TABLE");
						archive = new WorkOrderMapper(archiveFolder, "ARCHIVE_TABLE");
					} catch (SQLException | InterruptedException e) {
						System.out.println(e);
						setting();
					}
				}
			});
	}
	
	void showUp() {
		setReadyForPickUpPanel();
		setArchivePanel();
		tabbedPane.add("Ready For Pick Up",readyForPickUpPanel);
		tabbedPane.add("archive",archivePanel);
		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if(tabbedPane.getSelectedIndex()==0) {
					printButton.setEnabled(false);
					CheckOutButton.setEnabled(true);
					printButton.setEnabled(false);
					emailButton.setEnabled(true);
					attachButton.setEnabled(false);
				}
				if(tabbedPane.getSelectedIndex()==1) {
					printButton.setEnabled(true);
					CheckOutButton.setEnabled(false);
					printButton.setEnabled(true);
					emailButton.setEnabled(false);
					attachButton.setEnabled(true);
				}
			}
		});
		
		setIcon();
		setPreferredSize(new Dimension(1600,1050));
		setLayout(new BorderLayout());
		addButtons(toolBar);
		add(toolBar, BorderLayout.NORTH);
		add(tabbedPane, BorderLayout.CENTER);
		setResizable(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		autoRefreshReadyPanel(autoRefreshInterval);
		setVisible(true);
	}
	
	void autoRefreshReadyPanel(int time) {
		if(isRunning) {
			t.cancel();
	        isRunning=false;
		}
		
		if(time != 0) {
			t = new Timer("refresh", false);
			t.schedule(new TimerTask() {
	            @Override
	            public void run() {
	            	isRunning = true;
	                refreshReadyForPickUpPanel();
	            }
	        }, time*60000, time*60000);
		} 
	}
	
	void loadConfigFile() {
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(configFile));
			while(br.ready()) {
				String trimLine=br.readLine().trim();
				if(trimLine.startsWith("ReadyFolder:")) {
					readyFolder=trimLine.substring("ReadyFolder:".length());
				} else if(trimLine.startsWith("ArchiveFolder:")) {
					archiveFolder=trimLine.substring("ArchiveFolder:".length());
				} else if(trimLine.startsWith("DBFolder:")) {
					dbFolder=trimLine.substring("DBFolder:".length());
				} else if(trimLine.startsWith("OutlookFile:")) {
					outlookFile=trimLine.substring("OutlookFile:".length());
				} else if(trimLine.startsWith("AutoRefresh:")) {
					autoRefreshInterval=Integer.parseInt(trimLine.substring("AutoRefresh:".length()));
				} else if(trimLine.startsWith("TMDE:")) {
					tmdeTeam=trimLine.substring("TMDE:".length());
				}  
			}
			br.close();
		} catch (FileNotFoundException e) {
				setting();
		} catch (IOException e) {
			JOptionPane.showConfirmDialog(new JFrame(), "Can not access to configuration file.\nplease check if this file is in use.", "Missing" , JOptionPane.CLOSED_OPTION);
		}
	}
	
	void setConfigFile(String readyFolder, String archiveFolder, String dbFolder, String olFile, int autoRefreshInterval, String tmdeTeamName) {
		this.readyFolder = readyFolder;
		this.archiveFolder = archiveFolder;
		this.dbFolder = dbFolder;
		this.autoRefreshInterval = autoRefreshInterval;
		tmdeTeam = tmdeTeamName;
		WorkOrderDataBaseConnector.msAccDB = dbFolder;
		outlookFile = olFile;
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(configFile));
			writer.write("ReadyFolder:"+readyFolder);
			writer.newLine();
			writer.write("ArchiveFolder:"+archiveFolder);
			writer.newLine();
			writer.write("DBFolder:"+dbFolder);
			writer.newLine();
			writer.write("OutlookFile:"+outlookFile);
			writer.newLine();
			writer.write("AutoRefresh:"+autoRefreshInterval);
			writer.newLine();
			writer.write("TMDE:"+tmdeTeam);
			writer.newLine();
			writer.close();
		} catch (IOException e) {
			JOptionPane.showConfirmDialog(new JFrame(), "Can not access to configuration file.\nplease check if this file is in use.", "Missing" , JOptionPane.CLOSED_OPTION);
		}
	}
	
	void setting() {
		Object[] SEARCH_OPTIONS = {"Save", "Cancel"};
		Setting setting = new Setting();
		setting.pickUpFolderField.setText(readyFolder);
		setting.archiveFolderField.setText(archiveFolder);
		setting.dbFolderField.setText(dbFolder);
		setting.outlookFolderField.setText(outlookFile);
		
		if(autoRefreshInterval==0) {
			setting.autoRefreshIntervalField.setText("Disabled");
		} else {
			setting.autoRefreshIntervalField.setText(String.valueOf(autoRefreshInterval));
		}
		setting.tmdeTeamField.setText(tmdeTeam);
		int settingOption = JOptionPane.showOptionDialog(null, setting, "Settings", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, SEARCH_OPTIONS, null);
		if(settingOption==0) {
			if((setting.getPickUpFolder()!=null) && (setting.getArchiveFolder()!=null) && (setting.getDBFolder()!=null) && (setting.getOutlookFolder()!=null)){
				setConfigFile(setting.getPickUpFolder(),setting.getArchiveFolder(),setting.getDBFolder(),setting.getOutlookFolder(),setting.getAutoRefreshInterval(), setting.getTMDETeam());
				autoRefreshReadyPanel(autoRefreshInterval);
			} else {
			}
		} else {
		}
	}
	
	void updateReadyForPickUpPanel(WorkOrder wom) {
		int rowIndexNumber = getRowIndexByWO(readyForPickUpTable,wom.numberOfWorkOrder);
		readyForPickUpTable.setValueAt(wom.isContacted, rowIndexNumber, 4);
		readyForPickUpTable.setValueAt(wom.numbersOfAttempt, rowIndexNumber, 5);
		readyForPickUpTable.setValueAt(wom.dateOfLastAttempt, rowIndexNumber, 6);
		readyForPickUpTable.setValueAt(wom.dateOfLastContact, rowIndexNumber, 7);
	}
	
	void refreshReadyForPickUpPanel() {
		try {
			readyForPickUp = new WorkOrderMapper(readyFolder, "READY_TABLE");
		} catch (SQLException | InterruptedException e) {
		}
		LinkedList<String> updatedWOList = new LinkedList<String>();
		for(WorkOrder wo: readyForPickUp.workOrderMap.values()) {
			if(getRowIndexByWO(readyForPickUpTable, wo.numberOfWorkOrder)==-1) {
				Object[] newData = { wo.numberOfWorkOrder, wo.numberOfECN, wo.nameOfCustomer, wo.nomenclature, wo.isContacted, wo.numbersOfAttempt, wo.dateOfLastAttempt, wo.dateOfLastContact };
				readyForPickUpTableModel.addRow(newData);
			}
			updatedWOList.add(wo.numberOfWorkOrder);
		}
		
		for(String notExistingItemWON : getNotExistingItems(readyForPickUpTableModel, updatedWOList)) {
			readyForPickUpTableModel.removeRow(getRowIndexByWO(readyForPickUpTable, notExistingItemWON));
		}
	}
	
	void refreshArchivePanel() {
		try {
			archive = new WorkOrderMapper(archiveFolder, "ARCHIVE_TABLE");
		} catch (SQLException | InterruptedException e) {
		}
		LinkedList<String> updatedWOList = new LinkedList<String>();
		for(WorkOrder wo: archive.workOrderMap.values()) {
			if(getRowIndexByWO(archiveTable, wo.numberOfWorkOrder)==-1) {
				Object[] newData = {wo.numberOfWorkOrder, wo.numberOfECN,wo.nameOfCustomer,wo.nameOfSigner,wo.dateOfPickup};  //TODO
				archiveTableModel.addRow(newData);
			}
			updatedWOList.add(wo.numberOfWorkOrder);
		}
		
		for(String notExistingItemWON : getNotExistingItems(archiveTableModel, updatedWOList)) {
			archiveTableModel.removeRow(getRowIndexByWO(archiveTable, notExistingItemWON));
		}
	}
	
	void setReadyForPickUpPanel() {
		infoButton = new JButton("Info");
		checkOutButton = new JButton("Check-Out");
		if(readyForPickUp.numberOfWorkOrders!=0) {
			readyForPickupData = new Object[readyForPickUp.numberOfWorkOrders][8];
		}
		int i = 0;
		for(WorkOrder wo: readyForPickUp.workOrderMap.values()) {
			readyForPickUp.rowIndex=i;
			wo.setFrontDesk(this);
			wo.setTableObject(readyForPickupData[i], i);
			i++;
		}
		infoButton.setEnabled(true);
		checkOutButton.setEnabled(true);
		readyForPickUpTableModel = new TableModel(readyForPickupData,readyForPickUpColumn);
		readyForPickUpTable = new JTable();
		readyForPickUpTable.setModel(readyForPickUpTableModel);
		readyForPickUpTable.addMouseListener(new TableListener(readyForPickUpTable));
		readyForPickUpTable.getTableHeader().setReorderingAllowed(false);
		readyForPickUpPanel.setLayout(new BorderLayout());
		readyForPickUpTable.setAutoCreateRowSorter(true);
		readyForPickUpScrollPane = new JScrollPane(readyForPickUpTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		readyForPickUpTable.getColumnModel().getColumn(0).setMinWidth(95);
		readyForPickUpTable.getColumnModel().getColumn(0).setMaxWidth(100);
		readyForPickUpTable.getColumnModel().getColumn(1).setMinWidth(45);
		readyForPickUpTable.getColumnModel().getColumn(1).setMaxWidth(50);
		readyForPickUpTable.getColumnModel().getColumn(4).setMinWidth(65);
		readyForPickUpTable.getColumnModel().getColumn(4).setMaxWidth(70);
		readyForPickUpTable.getColumnModel().getColumn(5).setMinWidth(65);
		readyForPickUpTable.getColumnModel().getColumn(5).setMaxWidth(70);
		readyForPickUpTable.getColumnModel().getColumn(6).setMinWidth(100);
		readyForPickUpTable.getColumnModel().getColumn(6).setMaxWidth(110);
		readyForPickUpTable.getColumnModel().getColumn(7).setMinWidth(100);
		readyForPickUpTable.getColumnModel().getColumn(7).setMaxWidth(110);
		readyForPickUpTable.setFocusable(false);
		readyForPickUpTable.addMouseListener(new MouseListener() {
			boolean isAlreadyOneClick;
			int selectedRow;
			@Override
			public void mouseClicked(MouseEvent e) {
			    if (isAlreadyOneClick && (selectedRow==readyForPickUpTable.getSelectedRow())) {
			    	String woNumber = getWorkOrderNumberFromTableModel(readyForPickUpTable, selectedRow);
			    	openInfoWindow(woNumber);
			        isAlreadyOneClick = false;
			    } else {
			        isAlreadyOneClick = true;
			        selectedRow = readyForPickUpTable.getSelectedRow();
			        Timer t = new Timer("doubleclickTimer", false);
			        t.schedule(new TimerTask() {

			            @Override
			            public void run() {
			                isAlreadyOneClick = false;
			            }
			        }, timerSecond);
			    }
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				
			}

			@Override
			public void mouseExited(MouseEvent e) {
				
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}
		});
		readyForPickUpTable.setDefaultRenderer(JButton.class, new CellRenderer());
		readyForPickUpTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		
		readySorter = new TableRowSorter<TableModel>(readyForPickUpTableModel);
		readyForPickUpTable.setRowSorter(readySorter);
		readyForPickUpPanel.add(readyForPickUpScrollPane);
		
		readyForPickUpPanel.validate();
		readyForPickUpPanel.setVisible(true);
	}
	
	int getNumberOfWorkOrdersByCustomer(String nameOfCustomer) {
		int i = 0;
		for(WorkOrder wo : readyForPickUp.workOrderMap.values()) {
			if(wo.nameOfCustomer.equalsIgnoreCase(nameOfCustomer)) {
				i++;
			}
		}
		return i;
	}
	
	boolean hasMoreThanTwoPickUp(String nameOfCustomer) {
		if(getNumberOfWorkOrdersByCustomer(nameOfCustomer)>1) {
			return true;
		} else {
			return false;
		}
	}
	
	void openInfoWindow(String woNumber) {
		JFrame jf = new JFrame("Information");
		WorkOrder wo = readyForPickUp.workOrderMap.get(woNumber);
		wo.setFrontDesk(this);
		InfoWindow iw = new InfoWindow(wo, wo.nameOfPOC, wo.numberOfPOC, wo.nameOfRequestor,  wo.numberOfRequestor, wo.numbersOfAttempt);
		JPanel remarksPanel = new JPanel();
		
		remarksPanel.setLayout(new BorderLayout());
		remarksPanel.add(new JLabel("Notes"), BorderLayout.NORTH);
		JTextArea remarkJTA = new JTextArea(wo.getRemarks());
		remarkJTA.setRows(10);
		remarkJTA.setLineWrap(true);
		
		JScrollPane remarkScroll = new JScrollPane ( remarkJTA );
		remarkScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		remarksPanel.add(remarkScroll, BorderLayout.CENTER);
		
		JPanel optionButtonPanel = new JPanel();
		optionButtonPanel.setLayout(new FlowLayout());
		JButton saveNoteButton = new JButton("Save");
		JButton closeNoteButton = new JButton("Close");
		optionButtonPanel.add(saveNoteButton);
		optionButtonPanel.add(closeNoteButton);
		remarksPanel.add(optionButtonPanel,BorderLayout.SOUTH);
		isRemarksUpdated=false;
		
		remarkJTA.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent arg0) {
				isRemarksUpdated=true;
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				isRemarksUpdated=true;
			}

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				isRemarksUpdated=true;
			}
		});
		
		saveNoteButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				wo.setRemarks(remarkJTA.getText());
				try {
					WorkOrderDataBaseConnector.updateRemarksReadyData("READY_TABLE", wo.numberOfWorkOrder, remarkJTA.getText());
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				JComponent comp = (JComponent) e.getSource();
				Window win = SwingUtilities.getWindowAncestor(comp);
				win.dispose();				
			}
		});
		
		closeNoteButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(isRemarksUpdated) {
					int savingOption = JOptionPane.showConfirmDialog(new JFrame(), "Changed Note will not be saved, do you want to continue?", "Confirmation",JOptionPane.OK_CANCEL_OPTION);
					if(savingOption==0) {
						JComponent comp = (JComponent) e.getSource();
						Window win = SwingUtilities.getWindowAncestor(comp);
						win.dispose();
					} 
				} else {
					JComponent comp = (JComponent) e.getSource();
					Window win = SwingUtilities.getWindowAncestor(comp);
					win.dispose();
				}
			}
		});
		
		iw.setNumberOfDeptWO(getNumberOfWorkOrdersByCustomer(wo.nameOfCustomer));
		jf.setLocationByPlatform(true);
		JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT,iw.infoPanel,remarksPanel);
		sp.setDividerSize(0);
		jf.add(sp);
		jf.pack();
		jf.setVisible(true);
	}
	
	LinkedList<WorkOrder> getWorkOrdersFromSameCustomer(String nameOfCustomer) {
		LinkedList<WorkOrder> workOrderList = new LinkedList<WorkOrder>();
		for(WorkOrder wo : readyForPickUp.workOrderMap.values()) {
			if(wo.nameOfCustomer.equalsIgnoreCase(nameOfCustomer)) {
				workOrderList.add(wo);
			}
		}
		return workOrderList;
	}
	
	void setArchivePanel() {
		if(archive.numberOfWorkOrders!=0) {
			archiveData = new Object[archive.numberOfWorkOrders][5];
		} else {
			
		}
		int i = 0;
		for(WorkOrder wo: archive.workOrderMap.values()) {
			archive.rowIndex=i;
			wo.setArchiveTableObject(archiveData[i], i);
			i++;
		}
		
		archiveTableModel = new TableModel(archiveData,archiveColumn);
		archiveTable = new JTable(archiveTableModel);
		archiveTable.addMouseListener(new TableListener(archiveTable));
		archiveTable.getColumnModel().getColumn(0).setMinWidth(95);
		archiveTable.getColumnModel().getColumn(0).setMaxWidth(100);
		archiveTable.getColumnModel().getColumn(1).setMinWidth(45);
		archiveTable.getColumnModel().getColumn(1).setMaxWidth(50);
		archiveTable.getColumnModel().getColumn(3).setMinWidth(100);
		archiveTable.getColumnModel().getColumn(3).setMaxWidth(105);
		archiveTable.getColumnModel().getColumn(4).setMinWidth(100);
		archiveTable.getColumnModel().getColumn(4).setMaxWidth(105);
		archiveTable.setAutoCreateRowSorter(true);
		archiveTable.getTableHeader().setReorderingAllowed(false);
		archivePanel.setLayout(new BorderLayout());
		archivePanelScrollPane = new JScrollPane(archiveTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		archiveTable.setFocusable(false);
		archiveTable.addMouseListener(new MouseListener() {
			boolean isAlreadyOneClick;
			int selectedRow;
			@Override
			public void mouseClicked(MouseEvent e) {
			    if (isAlreadyOneClick && (selectedRow==archiveTable.getSelectedRow())) {
			    	String woNumber = getWorkOrderNumberFromTableModel(archiveTable, selectedRow);
			    	try {
						openPDFfile(woNumber);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
			    	
			        isAlreadyOneClick = false;
			    } else {
			        isAlreadyOneClick = true;
			        selectedRow = archiveTable.getSelectedRow();
			        Timer t = new Timer("doubleclickTimer", false);
			        t.schedule(new TimerTask() {

			            @Override
			            public void run() {
			                isAlreadyOneClick = false;
			            }
			        }, timerSecond);
			    }
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}
		});
		archivePanel.add(archivePanelScrollPane);
		archiveSorter = new TableRowSorter<TableModel>(archiveTableModel);
		archiveTable.setRowSorter(archiveSorter);
		archivePanel.validate();
		archivePanel.setVisible(true);
	}
	
	void openPDFfile(String woNumber) throws IOException {
		WorkOrder wo;
		if(readyForPickUp.workOrderMap.containsKey(woNumber)) {
			wo = readyForPickUp.workOrderMap.get(woNumber);
		} else {
			wo = archive.workOrderMap.get(woNumber);
		}
		Path filePath = Paths.get(new File(wo.getArchivePath()).toURI());
		File pdfFile = new File(filePath.toUri());
		if(pdfFile.exists()) {
			Desktop desktop = Desktop.getDesktop();
			desktop.open(pdfFile);
		}
	}
	
	File getFilePath(String woNumber) {
		WorkOrder wo;
		if(readyForPickUp.workOrderMap.containsKey(woNumber)) {
			wo = readyForPickUp.workOrderMap.get(woNumber);
		} else {
			wo = archive.workOrderMap.get(woNumber);
		}
		Path filePath = Paths.get(new File(wo.getArchivePath()).toURI());
		File file = new File(filePath.toUri());
		return file;
	}
	
	void setIcon() {
		try {
			this.setIconImage(ImageIO.read(new File(".\\png\\icon.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	void printFromArchive() throws InvalidPasswordException, IOException, PrinterException {
		if(tabbedPane.getSelectedIndex()==1) {
			int[] listOfWorkOrdersIndex = archiveTable.getSelectedRows();
			for(int i : listOfWorkOrdersIndex) {
				String woNumber = getWorkOrderNumberFromTableModel(archiveTable, i);
				WorkOrder wo = archive.workOrderMap.get(woNumber);
				Path filePath = Paths.get(new File(wo.getArchivePath()).toURI());
				File pdfFile = new File(filePath.toUri());
				PDDocument document = PDDocument.load(pdfFile);
				printWithDialog(document);
			}
		} else {
			JOptionPane.showConfirmDialog(new JFrame(), "You can print ONLY checked out work orders.","Confirmation", JOptionPane.CLOSED_OPTION);
		}
	}
	
	void printByWorkOrder(String workOrderNumber) throws InvalidPasswordException, IOException, PrinterException {
		WorkOrder wo;
		if(readyForPickUp.workOrderMap.containsKey(workOrderNumber)) {
			wo = readyForPickUp.workOrderMap.get(workOrderNumber);
		} else {
			wo = archive.workOrderMap.get(workOrderNumber);
		}
		Path filePath = Paths.get(new File(wo.getArchivePath()).toURI());
		File pdfFile = new File(filePath.toUri());
		PDDocument document = PDDocument.load(pdfFile);
		printWithDialog(document);
	}
	
	void printFromCheckOut(Path filePath) throws InvalidPasswordException, IOException, PrinterException {
		File pdfFile = new File(filePath.toUri());
		PDDocument document = PDDocument.load(pdfFile);
		printWithDialog(document);
	}
	
	private static void printWithDialog(PDDocument document) throws IOException, PrinterException {
		PrinterJob job = PrinterJob.getPrinterJob();
		job.setPageable(new PDFPageable(document));
		
		if (job.printDialog()) {
			job.print();
		}
	}	
	
	void addButtons(JToolBar toolBar) {
		toolBar.setFloatable(false);
		
		JButton viewButton =  makeNavigationButton("View.png", "View",
                "View selected workorders",
                "View");
	    
	    toolBar.add(viewButton);
	    
	    
	    CheckOutButton =  makeNavigationButton("CheckOut.png", "CheckOut",
                "Check-Out selected workorders",
                "Check-Out");
	    
	    toolBar.add(CheckOutButton);
	    
	    
	    printButton =  makeNavigationButton("Print.png", "Print",
                "Print selected workorders",
                "Print");
	    
	    toolBar.add(printButton);
	    
	    
	    emailButton = makeNavigationButton("Email.png", "Email",
                "Email a notification",
                "Email");
	    
	    toolBar.add(emailButton);


	    attachButton = makeNavigationButton("Attach.png", "Attach",
                "Email the attachment",
                "Attach");

	    toolBar.add(attachButton);  
	    
	    
	    toolBar.addSeparator();
	    
	    refreshButton = makeNavigationButton("Refresh.png", "Refresh",
                "Refresh",
                "Refresh");

	    toolBar.add(refreshButton);
	    
	    
	    
	    toolBar.addSeparator();
	    
	    JButton backButton = makeNavigationButton("Back.png", "Previous",
	                                  "Move to previous",
	                                  "Previous");
	    toolBar.add(backButton);

	    
	    JButton nextButton = makeNavigationButton("Next.png", "Next",
	                                  "Move to next tab",
	                                  "Next");
	    
	    toolBar.add(nextButton);
	    

	    toolBar.addSeparator();
	    toolBar.add(Box.createHorizontalGlue());
	    
	    
	    int toolbarHeight = toolBar.getPreferredSize().height-3;
	    searchTextField.setPreferredSize(new Dimension(200,toolbarHeight));
	    searchTextField.setMaximumSize(searchTextField.getPreferredSize());
	    
	    toolBar.add(searchTextField);
	    searchTextField.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				searchBar();			
			}
		});
	    
	    
	    JButton searchButton =  makeNavigationButton("Search.png", "Search",
                "search workorders",
                "Search");
	    
	    toolBar.add(searchButton);
	    
	    
	    JButton resetButton =  makeNavigationButton("Reset.png", "Reset",
                "Reset search field",
                "Reset");
	    
	    toolBar.add(resetButton);
	    
	    
	    toolBar.addSeparator();
	    
	    JButton settingButton =  makeNavigationButton("Setting.png", "Settings",
                "Settings",
                "Settings");
	    
	    toolBar.add(settingButton);
	    
		printButton.setEnabled(false);
		CheckOutButton.setEnabled(true);
		printButton.setEnabled(false);
		emailButton.setEnabled(true);
		attachButton.setEnabled(false);
	}

	JButton makeNavigationButton(String imageName, String actionCommand, String toolTipText, String altText) {
	    String imgLocation = imageName;
		URI imageURI = null;
		URL imageURL = null;
		File abPath = new File(".\\png\\");
		File imageFile = new File(Paths.get(abPath.getAbsolutePath())+"\\"+imageName);
		try {
			imageURI = imageFile.toURI();
			imageURL = imageURI.toURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
	    JButton button = new JButton();
	    button.setActionCommand(actionCommand);
	    button.setToolTipText(toolTipText);
	    button.addActionListener(this);

	    if (imageURL != null) {                      //image found
	        button.setIcon(new ImageIcon(imageURL, altText));
	    } else {                                     //no image found
	        button.setText(altText);
	        System.err.println("Resource not found: " + imgLocation);
	    }

	    return button;
	}
	
	void processCheckout(String workOrderNumber) throws PrinterException {
		WorkOrder wo = readyForPickUp.workOrderMap.get(workOrderNumber);
		int answer = JOptionPane.showConfirmDialog(new JFrame(), "Do you want to check out following item?\n" 
				+ wo.itemInfoText(), "Confirmation", JOptionPane.YES_NO_OPTION);
		Path filePath = Paths.get(new File(wo.getArchivePath()).toURI());
		if(answer==0) {
			if(fileExistChecker(filePath)) {
				overwriteOption = JOptionPane.showConfirmDialog(new JFrame(), "File is already exist.\nTo overwrite, Click OK.\nOtherwise, Cancel.", "Overwrite", JOptionPane.OK_CANCEL_OPTION);
			} else if(!fileExistChecker(filePath) || overwriteOption ==0){
				String signerName = "";
				boolean isNameCorrect = false;
				while(!isNameCorrect) {
					signerName = JOptionPane.showInputDialog("Enter Rank Lastname, Firstname");
					if(signerName==null) {
						break;
					} else if(signerName=="") {
						signerName="";
					}
					int confirmName = JOptionPane.showConfirmDialog(new JFrame(), signerName +" is correct?", "Name Confirmation", JOptionPane.YES_NO_OPTION);
					if(confirmName!=0) {
						isNameCorrect = false;
					} else {
						isNameCorrect = true;
					}
				}
				
				if(signerName == null) {
				} else if(signerName.length() > 2) {
					CheckOutWindow cow = new CheckOutWindow();
					cow.setWO(wo);
					cow.setSignerInfo(signerName);
					Path sourPath = Paths.get(new File(readyFolder+wo.fileName).toURI());
					Path destPath = Paths.get(new File(wo.getArchivePath()).toURI());
					wo.setArchivePath(destPath.toString());
					try {
						cow.sign(sourPath, destPath);
						int printOption = JOptionPane.showConfirmDialog(new JFrame(), "Do you want to print Workorder("+wo.numberOfWorkOrder+")" , "Print", JOptionPane.YES_NO_OPTION);
						if(printOption==0) {
							printFromCheckOut(destPath);
						}
						afterSignProcedure(wo);
					} catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException | CertificateException | ValidationException e) {
						JOptionPane.showConfirmDialog(new JFrame(), "Sign cancelled");
					} catch (FileNotFoundException e) {
						JOptionPane.showMessageDialog(new JFrame(), "Workorder is currently opened or does not exist.\nplease refresh and try again.");
					} catch (IOException e) {
						JOptionPane.showConfirmDialog(new JFrame(), "Sign cancelled");
					}
				} else {
					JOptionPane.showMessageDialog(new JFrame(), "name must be longer than 2 letter.\nplease try again.");
				}
			} 
		} else {
			JOptionPane.showMessageDialog(new JFrame(), "Check-out procedure has been cancelled.");
		}
	}
	
	void processMultipleCheckout(LinkedList<WorkOrder> workOrdersFromSameCustomer) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, ValidationException, IOException, PrinterException {
		String listOfItem="";
		
		for(WorkOrder wo : workOrdersFromSameCustomer) {
			listOfItem+="Work Order Number: " + wo.numberOfWorkOrder + "  ECN: " + wo.numberOfECN +"  Nomenclature: " + wo.nomenclature + "\n";
		}
		
		int answer = JOptionPane.showConfirmDialog(new JFrame(), "Following WorkOrders will be checking out.\nTo process, click OK. \n" 
				+ listOfItem, "Confirmation", JOptionPane.OK_CANCEL_OPTION);
		
		HashMap<WorkOrder, LinkedList<Path>> workOrderPathMap = new HashMap<WorkOrder, LinkedList<Path>>();
		if(answer==0) {
			String signerName = "";
			boolean isNameCorrect = false;
			while(!isNameCorrect) {
				signerName = JOptionPane.showInputDialog("Enter Rank Lastname, Firstname");
				if(signerName==null) {
					signerName="";
					break;
				} else if(signerName=="") {
					signerName="";
				}
				int confirmName = JOptionPane.showConfirmDialog(new JFrame(), signerName +" is correct?", "Name Confirmation", JOptionPane.YES_NO_OPTION);
				if(confirmName!=0) {
					isNameCorrect = false;
				} else {
					isNameCorrect = true;
				}
			}
			if(signerName.length() > 2) {
				CheckOutWindow cow = new CheckOutWindow();
				cow.setSignerInfo(signerName);
				for(WorkOrder wo : workOrdersFromSameCustomer) {
					LinkedList<Path> pathOfWO = new LinkedList<Path>();
					Path sourPath = Paths.get(new File(readyFolder+wo.fileName).toURI());
					Path destPath = Paths.get(new File(wo.getArchivePath()).toURI());
					pathOfWO.addFirst(sourPath);
					pathOfWO.addLast(destPath);
					workOrderPathMap.put(wo,pathOfWO);
				}
				cow.signByDepartment(workOrderPathMap);
				
				for(WorkOrder wo : cow.getCompletedWorkOrderByDepartment()) {
					int printOption = JOptionPane.showConfirmDialog(new JFrame(), "Do you want to print Workorder("+wo.numberOfWorkOrder+")" , "Print", JOptionPane.YES_NO_OPTION);
					if(printOption==0) {
						printFromCheckOut(workOrderPathMap.get(wo).getLast());
					}
					afterSignProcedure(wo);
				}
				
			} else {
				JOptionPane.showMessageDialog(new JFrame(), "name must be longer than 2 letter.\nplease try again.");
			}
		} else if(answer == 2){
			loopBreaker = true;
		} else {
			JOptionPane.showMessageDialog(new JFrame(), "Check-out procedure has been cancelled.");
		}
	}
	
	boolean isRowSelected(JTable jt) {
		if(jt.getSelectedRowCount()==0) {
			return false;
		} else {
			return true;
		}
	}
	
	void navigationMenu(String button) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, ValidationException, IOException, PrinterException, URISyntaxException {
		switch(button){
		case "View":
			if(tabbedPane.getSelectedIndex()==0) {
				if(isRowSelected(readyForPickUpTable)) {
					for(int i : readyForPickUpTable.getSelectedRows()) {
						openInfoWindow(getWorkOrderNumberFromTableModel(readyForPickUpTable, i));
					}
				}
			}
			if(tabbedPane.getSelectedIndex()==1) {
				if(isRowSelected(archiveTable)) {
					for(int i : archiveTable.getSelectedRows()) {
						openPDFfile(getWorkOrderNumberFromTableModel(archiveTable, i));
					}
				}
			}
			break;
		case "Search":
			searchBar();
			break;
		case "CheckOut":
			if(isRowSelected(readyForPickUpTable)) {
				String customerName = getCustomerNameFromTableModel(readyForPickUpTable, readyForPickUpTable.getSelectedRow()) ;
				if(hasMoreThanTwoPickUp(customerName)){
					int option = JOptionPane.showConfirmDialog(new JFrame(),customerName +" has " + getNumberOfWorkOrdersByCustomer(customerName) + " workorders ready to check out.\nDo you want to check out all together?", "Confirmation",JOptionPane.YES_NO_CANCEL_OPTION);
					if(option == 0 && loopBreaker == false) {
						processMultipleCheckout(getWorkOrdersFromSameCustomer(customerName));
					} else if (option == 1) {
						try {
							processCheckout(getWorkOrderNumberFromTableModel(readyForPickUpTable, readyForPickUpTable.getSelectedRow()));
						} catch (PrinterException e1) {
							e1.printStackTrace();
						}
					}
				} else {
					try {
						processCheckout(getWorkOrderNumberFromTableModel(readyForPickUpTable, readyForPickUpTable.getSelectedRow()));
					} catch (PrinterException e1) {
						e1.printStackTrace();
					}
				}
				loopBreaker = false;
			}
			break;
		case "Print":
			try {
				printFromArchive();
			} catch (IOException | PrinterException e1) {
				e1.printStackTrace();
			}
			break;
		case "Next":
			if(currentTab<numberOfTabs)
			tabbedPane.setSelectedIndex(currentTab+1);
			break;
		case "Previous":
			if(currentTab>0)
			tabbedPane.setSelectedIndex(tabbedPane.getSelectedIndex()-1);
			break;
		case "Reset":
			searchTextField.setText("");
			readyFilter();
			archiveFilter();
			break;
		case "Refresh":
			refreshArchivePanel();
			refreshReadyForPickUpPanel();
			break;
		case "Email":
			if(isRowSelected(readyForPickUpTable)) {
				String emailItems="";
				String emailECN="";
				if(readyForPickUpTable.getSelectedRowCount()>0) {
					for(int i : readyForPickUpTable.getSelectedRows()) {
						emailItems+=getWorkOrderNumberFromTableModel(readyForPickUpTable, i) + " ";
						emailECN+=getECNFromTableModel(readyForPickUpTable, i) + " ";
					}
					mail("MAMC Clinical Engineering - Item is ready for Pick-Up", "WO# " + emailItems +"is completed for ECN# "+ emailECN + "\n\nPlease visit Clinical Engineering Branch for pickup.\n\nIf you have any questions, please contact 253-968-1545\n\nThank you.");
				} else {
					JOptionPane.showConfirmDialog(new JFrame(), "You did NOT select any workorders.", "Error", JOptionPane. CLOSED_OPTION);
				}
			}
			break;
		case "Attach":
			if(isRowSelected(archiveTable)) {
				HashMap<String, File> attachmentList = new HashMap<String, File>();
				String customerEmail=JOptionPane.showInputDialog("Enter the email address of customer.");
				if(archiveTable.getSelectedRowCount()>0) {
					for(int i : archiveTable.getSelectedRows()) {
						String woSTR=getWorkOrderNumberFromTableModel(archiveTable, i);
						attachmentList.put(woSTR,getFilePath(woSTR));
					}
					mailWithAttachment(customerEmail,archiveTable, attachmentList);
				} else {
					JOptionPane.showConfirmDialog(new JFrame(), "You did NOT select any workorders.", "Error", JOptionPane. CLOSED_OPTION);
				}
			}
			break;
		case "Settings":
			setting();
			break;
		default:
			break;
		}
	}

	void searchBar() {
		if(tabbedPane.getSelectedIndex()==0) {
			readyFilter();
		}
		
		if(tabbedPane.getSelectedIndex()==1) {
			archiveFilter();	
		}
	}
	
	
	public static void mail(String subject, String body) throws URISyntaxException, IOException {
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported(Desktop.Action.MAIL)) {
				URI mailtoURI = new URI("mailto", null, null, "subject=" + subject + "&body=" + body, null);
				desktop.mail(mailtoURI);
			}
		}
	}
	
	public void mailWithAttachment(String customerEmail,JTable jt, HashMap<String,File> attachmentList) throws URISyntaxException {
		for(String woNumberKey: attachmentList.keySet()) {
			File attachment = attachmentList.get(woNumberKey);
			String subject = "MAMC Clinical Engineering - Pick-Up receipt ";
			String body = "WO# " + woNumberKey +" is completed for ECN# "+ getECNFromTableModel(jt, getRowIndexByWO(jt,woNumberKey)) + "%0A%0APlease visit Clinical Engineering Branch for pickup.%0A%0AIf you have any questions, please contact 253-968-1545%0A%0AThank you.";
			String mailSyntax = outlookFile + " /a " +  attachment.getAbsolutePath() + " /m " + "\"" + customerEmail +"&subject="+subject+"&body="+body + "\"";

			try {
				Runtime.getRuntime().exec(mailSyntax);
			} catch (IOException e) {
				JOptionPane.showConfirmDialog(new JFrame(), "Can not find Outlook application, Please check the location in the settings.","Error", JOptionPane.CLOSED_OPTION);
			}
		}
	}
	
	boolean fileExistChecker(Path filePath) {
		File file = new File(filePath.toUri());
		return file.exists(); 
	}
	
	int getRowIndexFromArchive(WorkOrder wo) {
		boolean isExist = false;
		int i = 0;
		for(String woNumber : archive.workOrderMap.keySet()) {
			if(!woNumber.equalsIgnoreCase(wo.numberOfWorkOrder)) {
				i++;
				isExist = false;
			} else {
				isExist = true;
				break;
			}
		}
		if(isExist) {
			return i;
		} else {
			return -1;
		}
	}
	
	void afterSignProcedure(WorkOrder wo) {
		Path sourPath = Paths.get(new File(readyFolder+wo.fileName).toURI());
		wo.setDateOfPickup();
		String[] archiveRowData = {wo.numberOfWorkOrder, wo.numberOfECN, wo.nameOfCustomer, wo.getSigner(), wo.getDateOfPickup(), wo.getArchivePath()};
		try {
			if(WorkOrderDataBaseConnector.isExist("ARCHIVE_TABLE", wo.numberOfWorkOrder)) {
				WorkOrderDataBaseConnector.updateArchiveData("ARCHIVE_TABLE", wo.numberOfWorkOrder, archiveRowData);
				int arcRow = getRowIndexByWO(archiveTable,wo.numberOfWorkOrder);
				if(arcRow!=-1) {
					archiveTableModel.setValueAt(wo.nameOfSigner, arcRow, 3);
					archiveTableModel.setValueAt(wo.dateOfPickup, arcRow, 4);
				} else {
					archiveTableModel.addRow(archiveRowData);
				} 
			} else {
				WorkOrderDataBaseConnector.insertArchiveData("ARCHIVE_TABLE", archiveRowData);
				archiveTableModel.addRow(archiveRowData);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	int rdyRowIndex = getTableRowByWO(readyForPickUpTableModel,wo.numberOfWorkOrder);
    	readyForPickUpTableModel.removeRow(rdyRowIndex);
		archive.workOrderMap.put(wo.numberOfWorkOrder,wo);
		readyForPickUp.workOrderMap.remove(wo.numberOfWorkOrder);
		try {
			Files.delete(sourPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	boolean isItemExistInTable(TableModel tm, String numOfWO) {
		int countOfRows = tm.getRowCount();
		for(int i = 0; i < countOfRows; i++) {
			if(tm.getValueAt(i, 0).toString().equalsIgnoreCase(numOfWO)) {
				return true;
			}
		}
		return false;
	}
	
	LinkedList<String> getNotExistingItems(TableModel tm, LinkedList<String> comparison) {
		int countOfRows = tm.getRowCount();
		LinkedList<String> notExistingItems = new LinkedList<String>();
		for(int i = 0; i < countOfRows; i++) {
			if(!comparison.contains(tm.getValueAt(i, 0).toString())) {
				notExistingItems.add(tm.getValueAt(i, 0).toString());
			}
		}
		return notExistingItems;
	}
	
	LinkedList<String> getListOfWOFromTableModel(TableModel tm) {
		int countOfRows = tm.getRowCount();
		LinkedList<String> existingItemsList = new LinkedList<String>();
		for(int i = 0; i < countOfRows; i++) {
			existingItemsList.add(tm.getValueAt(i, 0).toString());
		}
		return existingItemsList;
	}
	
	LinkedList<Integer> getTableRowByECN(TableModel tm, String numOfECN) {
		int countOfRows = tm.getRowCount();
		LinkedList<Integer> ecnList = new LinkedList<Integer>();
		for(int i = 0; i < countOfRows; i++) {
			if(tm.getValueAt(i, 1).toString().equalsIgnoreCase(numOfECN)) {
				ecnList.add(i);
			}
		}
		return ecnList;
	}
	
	LinkedList<Integer> getTableRowByCustomer(TableModel tm, String numOfECN) {
		int countOfRows = tm.getRowCount();
		LinkedList<Integer> customerList = new LinkedList<Integer>();
		for(int i = 0; i < countOfRows; i++) {
			if(tm.getValueAt(i, 2).toString().equalsIgnoreCase(numOfECN)) {
				customerList.add(i);
			}
		}
		return customerList;
	}
	
	int getTableRowByWO(TableModel tm, String won) {
		int countOfRows = tm.getRowCount();
		for(int i = 0; i < countOfRows; i++) {
			if(tm.getValueAt(i, 0).toString().equalsIgnoreCase(won)) {
				return i;
			}
		}
		return -1;
	}
	
	int getRowIndexByWO(JTable jt, String wo) {
		TableModel tm = (TableModel)jt.getModel();
		int rowIndex;
		int rowCount = tm.getRowCount(); 
		for(rowIndex = 0;rowIndex<rowCount;rowIndex++) {
			if(tm.getValueAt(jt.convertRowIndexToModel(rowIndex),0).toString().equalsIgnoreCase(wo)) {	
				return rowIndex;
			}
		}
		return -1;
	}
	
	String getWorkOrderNumberFromTableModel(JTable jt, int row) {
		TableModel tm = (TableModel)jt.getModel();
		String woNumberStr = (String) tm.getValueAt(jt.convertRowIndexToModel(row), 0);
		return woNumberStr;
	}
	
	String getCustomerNameFromTableModel(JTable jt, int row) {
		TableModel tm = (TableModel)jt.getModel();
		String customerName = (String) tm.getValueAt(jt.convertRowIndexToModel(row), 2);
		return customerName;
	}
	
	String getECNFromTableModel(JTable jt, int row) {
		TableModel tm = (TableModel)jt.getModel();
		String woNumberStr = (String) tm.getValueAt(jt.convertRowIndexToModel(row), 1);
		return woNumberStr;
	}
	
	private void readyFilter() {
        RowFilter<TableModel, Object> rf = null;
        try {
            rf = RowFilter.regexFilter("(?i)"+searchTextField.getText());
        } catch (java.util.regex.PatternSyntaxException e) {
            return;
        }
        readySorter.setRowFilter(rf);
    }
	
	private void archiveFilter() {
        RowFilter<TableModel, Object> rf = null;
        try {
        	rf = RowFilter.regexFilter("(?i)"+searchTextField.getText());
        } catch (java.util.regex.PatternSyntaxException e) {
            return;
        }
        archiveSorter.setRowFilter(rf);
    }
	
	static void GUI() {
		try {
			new FrontDesk();
		} catch (InterruptedException | SQLException e) {
			e.printStackTrace();
		}
	}
	

	
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						FrontDesk fd = new FrontDesk();
						pgb = new progressBar(loadingScreen.progressBar,fd);
						fd.initializeFrontDesk();
						rfpt.start();
						pgb.start();
					} catch (InterruptedException | SQLException e) {
						System.out.println(pgb.getState());
					}
				}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		currentTab = tabbedPane.getSelectedIndex();
		numberOfTabs = tabbedPane.getTabCount()-1;
		try {
			navigationMenu(e.getActionCommand());
		} catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException | CertificateException
				| ValidationException | IOException | PrinterException e1) {
			JOptionPane.showConfirmDialog(new JFrame(), "Sign cancelled");
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
	}
}

class TableModel extends DefaultTableModel {
	public TableModel() {
		super();
	}
	
	public TableModel(Object[][] objA , String[] strA) {
		super(objA, strA);
	}

    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }
	
	public boolean isCellEditable(int row, int col) {
		return false;
	}
}

class CellRenderer implements TableCellRenderer{

	@Override
	public Component getTableCellRendererComponent(JTable jt, Object obj, boolean isSelected, boolean hasFocus, int row, int col) {
		return (Component) obj;
	}
}

class TableListener extends MouseAdapter{
	JTable table;
	public TableListener(JTable table) {
		this.table = table;
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		int col = table.getColumnModel().getColumnIndexAtX(e.getX());
		int row = e.getY() / table.getRowHeight();
		if(row < table.getRowCount() && row >= 0 && col < table.getColumnCount() && col >= 0) {
			Object obj = table.getValueAt(row, col);
			if(obj instanceof JButton) {
			((JButton) obj).doClick();
			}
		}
	}
}


