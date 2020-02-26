package frontdesk;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * WorkOrderMapper class is to create a work order object from file.
 * @author elias
 *
 */
public class WorkOrderMapper {
	HashMap<String, WorkOrder> workOrderMap = new HashMap<String, WorkOrder>();
	HashMap<String, HashMap<String, String>> db = new HashMap<String, HashMap<String,String>>();
	String dirPath;
	String dbPath;
	
	int numberOfWorkOrders = 0;
	final static String DB_NAME = "frontdesk.db";
	final static String WORK_ORDER_TEXT= "MAINTENANCE WORK ORDER WORK ORDER NO:";
	final static String ECN_TEXT_START= "ECN:";
	final static String ECN_TEXT_END= "NOMENCLATURE:";
	final static String POC_NAME_TEXT= "POC:";
	final static String POC_NUMBER_TEXT= "POC PHONE:";
	final static String REQUESTOR_NAME_TEXT = "REQUESTER:";
	final static String REQUESTOR_NUMBER_TEXT = "REQUESTOR PHONE:";
	final static String CUSTOMER_NAME_TEXT = "CUSTOMER:";
	final static String COMPLETION_DATE_START = "COMPLETION DATE:";
	final static String COMPLETION_DATE_END = "CONDITION CODE:";
	final static String MANUFACTURER_TEXT = "MANUFACTURER:";
	final static String NAMEPLATE_MODEL_TEXT_START = "NAMEPLATE MODEL:";
	final static String NAMEPLATE_MODEL_TEXT_END = "WORK LOCATION:";
	final static String COMMON_MODEL_TEXT_START = "COMMON MODEL:";
	final static String COMMON_MODEL_TEXT_END = "RISK LEVEL:";
	final static String NOMENCLATURE_TEXT = "NOMENCLATURE:"; 
	final static String TEAM_TEXT_START = "TEAM:";
	final static String TEAM_TEXT_END = "WARRANTY END DATE PARTS:";
	int rowIndex;

	String workOrderNumber,ecnNumber,pocName,pocNumber, requestorName, requestorNumber, customerName, completionDate = null;
	String manufaturer, nameplateModel, commonModel, nomenclature, team = null;
	String dbTableName = "";
	public WorkOrderMapper() {
	}
	
	public WorkOrderMapper(String dirPath, String dbTableName) throws SQLException, InterruptedException {
		WorkOrderDataBaseConnector.init();
		this.dbTableName = dbTableName;
		if(dbTableName.equalsIgnoreCase("READY_TABLE")) {
			db = WorkOrderDataBaseConnector.getReadyResultSet();
			this.dirPath = dirPath;
			File filePath = new File(dirPath);
			try {
				generateListOfFiles(filePath);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(dbTableName.equalsIgnoreCase("ARCHIVE_TABLE")) {
			db = WorkOrderDataBaseConnector.getArchiveResultSet();
			createWorkOrderFromDB();
		}
	}
	
	void refresh() throws SQLException, InterruptedException {
		File filePath = new File(dirPath);
		try {
			generateListOfFiles(filePath);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	void setDBPath(String dbPath) {
		this.dbPath = dbPath;
	}
	
	String getDirPath() {
		return this.dirPath;
	}
	
	File selectFile() {
		JFileChooser fc = new JFileChooser(".");
		fc.showOpenDialog(new JFrame());
		return fc.getSelectedFile(); 
	}
	
	void generateListOfFiles(File dir) throws ParseException, SQLException, InterruptedException {
		try {
			File[] files = dir.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					generateListOfFiles(file);
				} else {
					if(file.toString().toLowerCase().endsWith("pdf")) {
						ProgressBarHelper.currentFileCounter++;
						createWorkOrder(Paths.get(file.getCanonicalPath()));
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	HashMap<String, WorkOrder> getWorkOrderMap() {
		return workOrderMap;
	}
	
	void openFile(Path path) throws IOException {
		File pdfFile = new File(path.toUri());
		if(pdfFile.exists()) {
			Desktop desktop = Desktop.getDesktop();
			desktop.open(pdfFile);
		}
	}
	
	void createWorkOrderFromDB() {
		for(String numberOfWorkOrder : db.keySet()) {
			WorkOrder wo = new WorkOrder(db.get(numberOfWorkOrder).get("WorkOrder"),db.get(numberOfWorkOrder).get("ECN"),db.get(numberOfWorkOrder).get("nameOfCustomer"),db.get(numberOfWorkOrder).get("nameOfSigner"),db.get(numberOfWorkOrder).get("signedDate"),db.get(numberOfWorkOrder).get("Path"));
			workOrderMap.put(wo.numberOfWorkOrder, wo);
			ProgressBarHelper.currentFileCounter++;
		}
		numberOfWorkOrders = workOrderMap.size();
	}
	
	void createWorkOrder(Path filePath) throws ParseException, SQLException, IOException {
		boolean isWorkOrder = true;
		boolean isCompleted = false;
		while(isWorkOrder && !isCompleted) {
			File file = new File(filePath.toUri());
			PDDocument doc;
			try {
				doc = PDDocument.load(file);
				PDFTextStripper ps = new PDFTextStripper();
				ps.setSortByPosition(true);
				String bodyText = ps.getText(doc);
				Scanner sc = new Scanner(bodyText);
				int lineNumber = 0;
				while(sc.hasNextLine()) {
					String line = sc.nextLine();
					if(lineNumber < 21) {
						switch(lineNumber){
							case 0:
								try {
									workOrderNumber = line.substring(WORK_ORDER_TEXT.length()).trim();
								} catch(StringIndexOutOfBoundsException e) {
									workOrderNumber = "";
								}
								break;
							case 1:
								try {
									ecnNumber = line.substring(ECN_TEXT_START.length(),line.indexOf(ECN_TEXT_END)).trim();
								} catch(StringIndexOutOfBoundsException e) {
									ecnNumber = "";
								}
								try {
									nomenclature = line.substring(line.indexOf(NOMENCLATURE_TEXT)).substring(NOMENCLATURE_TEXT.length()).trim();
								} catch(StringIndexOutOfBoundsException e) {
									nomenclature = "";
								}
								break;
							case 2:
								try {
									customerName = line.substring(line.lastIndexOf(":")+1).trim();
								} catch(StringIndexOutOfBoundsException e) {
									customerName = "";
								}
								break;							
							case 4:
								try {
									pocName = line.substring(line.indexOf(POC_NAME_TEXT)).substring(POC_NAME_TEXT.length()).trim();
								} catch(StringIndexOutOfBoundsException e) {
									pocName = "";
								}
								break;
							case 5:
								try {
									pocNumber = line.substring(line.indexOf(POC_NUMBER_TEXT)).substring(POC_NUMBER_TEXT.length()).trim();
								} catch(StringIndexOutOfBoundsException e) {
									pocNumber = "";
								}
								break;
							case 6:
								try {
									requestorName = line.substring(line.indexOf(REQUESTOR_NAME_TEXT)).substring(REQUESTOR_NAME_TEXT.length()).trim();
								} catch(StringIndexOutOfBoundsException e) {
									requestorName = "";
								}
								break;
							case 7:
								try {
									requestorNumber = line.substring(line.indexOf(REQUESTOR_NUMBER_TEXT)).substring(REQUESTOR_NUMBER_TEXT.length()).trim();
								} catch(StringIndexOutOfBoundsException e) {
									requestorNumber = "";
								}
								
								try {
									manufaturer = line.substring(MANUFACTURER_TEXT.length(),line.indexOf(REQUESTOR_NUMBER_TEXT)).trim();
								} catch(StringIndexOutOfBoundsException e) {
									manufaturer = "";
								}
								break;	
							case 8:
								try {
									commonModel = line.substring(COMMON_MODEL_TEXT_START.length(),line.indexOf(COMMON_MODEL_TEXT_END)).trim();
								} catch(StringIndexOutOfBoundsException e) {
									commonModel ="";
								}
								break;
							case 9:
								try {
									nameplateModel = line.substring(NAMEPLATE_MODEL_TEXT_START.length(),line.indexOf(NAMEPLATE_MODEL_TEXT_END)).trim();
								} catch(StringIndexOutOfBoundsException e) {
									nameplateModel = "";
								}
								break;
							case 19:
								try {
									team = line.substring(TEAM_TEXT_START.length(),line.indexOf(TEAM_TEXT_END)).trim();
								} catch (StringIndexOutOfBoundsException e) {
									team = "";
								}
								if(team.equalsIgnoreCase(FrontDesk.tmdeTeam.trim())) {
									workOrderNumber = "T"+workOrderNumber;
								}
								break;
							default:
								break;
						}
					} else {
						if(line.contains(COMPLETION_DATE_START)) {
							completionDate = line.substring(COMPLETION_DATE_START.length(), line.indexOf(COMPLETION_DATE_END)).trim();
							if(completionDate.length()==0) {
								completionDate = "01 JAN 0000";
							}
						}
					}
					lineNumber++;
				}
				sc.close();
				doc.close();
			} catch (InvalidPasswordException e) {
				System.out.println("Encrypted");
			} catch (IOException e) {
				System.out.println("File Error");
			}
			
			if(ManualFunctions.isExistInArchive(workOrderNumber)) {
				Files.delete(filePath);
				break;
			}
			
			if(requestorName.length()==0 && requestorNumber.length()==0 && (workOrderNumber.length()!=12 || (workOrderNumber.length()==13 && workOrderNumber.toUpperCase().startsWith("T")))) {
				Object[] AUTO_OPTIONS = {"OPEN", "DELETE"};
				JPanel optJP = new JPanel();
				JTextArea jta = new JTextArea("Invalid Work Order found. \nClick \"OPEN\" button to open the invalid WO.\nClick \"DELETE\" to delete the invalid WO.\nFile Path: "+filePath);
				jta.setBackground(optJP.getBackground());
				jta.setEditable(false);
				optJP.add(jta);
				int invalidWOOption=-1;
				while(invalidWOOption!=1) {
					invalidWOOption = JOptionPane.showOptionDialog(null, optJP, "Invalid Work Order", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, AUTO_OPTIONS, null);
					if(invalidWOOption == 0) {
						openFile(filePath);
					} else if(invalidWOOption == 1) {
						try {
							Files.deleteIfExists(filePath);
						} catch (FileSystemException fse) {
							invalidWOOption = -1;
							JOptionPane.showMessageDialog(new JFrame(), "Document might be open. Please close this document and try again.");
						}
					}
				}
				break;
			}
			WorkOrder wo = new WorkOrder(workOrderNumber, ecnNumber, pocName, pocNumber, requestorName, requestorNumber, customerName, new SimpleDateFormat("dd MMM yyyy").parse(completionDate), manufaturer, nomenclature, commonModel, nameplateModel, file.getName());
			wo.setArchivePath(ProgressBarHelper.getArchiveFolder()+wo.yearMonthFolderFormat+wo.numberOfWorkOrder+".pdf");
			if(workOrderMap.containsKey(workOrderNumber)) {
				String parentFolder = filePath.getParent().toString();
				String existingFile=parentFolder+"\\"+workOrderMap.get(workOrderNumber).fileName;
				Path existingFilePath = Paths.get(existingFile);
				Object[] AUTO_OPTIONS = {"OPEN", "DELETE"};
				JPanel optJP = new JPanel();
				JTextArea jta = new JTextArea("Duplicated Work Order found. \nClick \"OPEN\" button to open the duplicated WOs.\nClick \"DELETE\" to delete one of duplicated WOs.\nFile #1 Path: "+filePath + "\nFile #2 Path: "+existingFile);
				jta.setBackground(optJP.getBackground());
				jta.setEditable(false);
				optJP.add(jta);
				int duplicatedWOOption=-1;
				while(duplicatedWOOption!=1) {
					duplicatedWOOption = JOptionPane.showOptionDialog(null, optJP, "Duplicated Work Order", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, AUTO_OPTIONS, null);
					if(duplicatedWOOption == 0) {
						openFile(existingFilePath);
						openFile(filePath);
					} else if(duplicatedWOOption == 1) {
						try {
							Files.deleteIfExists(filePath);
						} catch (FileSystemException fse) {
							duplicatedWOOption = -1;
							JOptionPane.showMessageDialog(new JFrame(), "Document might be open. Please close this document and try again.");
						}
					}
				}
				break;
			}
			
			workOrderMap.put(workOrderNumber, wo);
			if(dbTableName.equalsIgnoreCase("READY_TABLE")) {
				if(db.containsKey(wo.numberOfWorkOrder)) {
					wo.dateOfLastAttempt=db.get(wo.numberOfWorkOrder).get("LastAttemptDate");
					wo.dateOfFirstContact=db.get(wo.numberOfWorkOrder).get("FirstContactedDate");
					wo.dateOfLastContact=db.get(wo.numberOfWorkOrder).get("LastContactedDate");
					if(!db.get(wo.numberOfWorkOrder).get("isContacted").contains("Not Contacted")) {
						wo.isContacted=true;
					} else {
						wo.isContacted=false;
					}
					wo.numbersOfAttempt=Integer.parseInt(db.get(wo.numberOfWorkOrder).get("Attempt"));
					LinkedList<String> cHL = new LinkedList<String>();
					try {
						String[] cH = db.get(wo.numberOfWorkOrder).get("ContactHistory").split(":");
						if(cH.length!=0) {
							for(int i = 0; i < cH.length; i++) {
								cHL.add(cH[i]);
							}
						}
					} catch (NullPointerException e){
					}
					wo.listOfContactedDate=cHL;
					wo.setRemarks(db.get(wo.numberOfWorkOrder).get("Remarks"));
				} else {
					if(wo.dateOfLastAttempt==null) {
						wo.dateOfLastAttempt="";
					}
					if(wo.dateOfFirstContact==null) {
						wo.dateOfFirstContact="";
					}
					if(wo.dateOfLastContact==null) {
						wo.dateOfLastContact="";
					}
					if(wo.remarks==null) {
						wo.remarks="";
					}
					String[] tableDataSet = {wo.numberOfWorkOrder, wo.numberOfECN, wo.nameOfCustomer, wo.dateOfLastAttempt,wo.dateOfFirstContact,wo.dateOfLastContact,wo.getIsContacted(),String.valueOf(wo.numbersOfAttempt),"", wo.remarks};
					WorkOrderDataBaseConnector.insertReadyData("READY_TABLE", tableDataSet);
				}
			} 
			
			if(dbTableName.equalsIgnoreCase("ARCHIVE_TABLE")) {
				if(db.containsKey(wo.numberOfWorkOrder)) {
					wo.nameOfSigner = db.get(wo.numberOfWorkOrder).get("nameOfSigner");
					wo.dateOfPickup = db.get(wo.numberOfWorkOrder).get("signedDate");
				} else {
					if(wo.nameOfSigner==null) {
						wo.nameOfSigner="";
					}
					if(wo.dateOfPickup==null) {
						wo.dateOfPickup="";
					}
				String[] archiveRowData = {wo.numberOfWorkOrder, wo.numberOfECN, wo.nameOfCustomer, wo.getSigner(), wo.getDateOfPickup()};
				WorkOrderDataBaseConnector.insertArchiveData("ARCHIVE_TABLE", archiveRowData);
				}
			}
			numberOfWorkOrders = workOrderMap.size();
			isCompleted=true;
		}
	}
}
