package frontdesk;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import javax.swing.JButton;
/**
 * WorkOrder class for a work order.
 * @author elias
 *
 */
public class WorkOrder {
	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yy");
	private FrontDesk fd;
	String numberOfWorkOrder,nameOfPOC, numberOfPOC, nameOfRequestor, numberOfRequestor, fileName, manufaturer, nomenclature, commonModel, nameplateModel;
	LinkedList<String> listOfContactedDate = new LinkedList<String>();
	boolean isContacted = false;
	private boolean isPickedUp = false;
	private boolean isSigned = false;
	int numbersOfAttempt = 0;
	String remarks="";
	String lastContactMethod = "";
	String dateOfFirstContact;
	String dateOfCompletion;
	String dateOfLastContact;
	String dateOfLastAttempt;
	String dateOfPickup = "";
	String nameOfSigner = "";
	String nameOfCustomer = "";
	String numberOfECN = "";
	private String archivePath = "";
	private Object[] columnData;
	private Object[] archiveColumnData;
	private JButton infoButton = new JButton("Info");
	private JButton checkOutButton = new JButton("Check-out");
	String yearMonthFolderFormat;
	final private String[] MONTHS = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
	
	public WorkOrder(String numberOfWorkOrder, String numberOfECN, String nameOfPOC, String numberOfPOC, String nameOfRequestor, String numberOfRequestor, String nameOfCustomer, Date dateOfCompletion, String manufacturer, String nomenclature, String commonModel, String nameplateModel, String fileName) {
		this.numberOfWorkOrder = numberOfWorkOrder;
		this.numberOfECN = numberOfECN;
		this.nameOfPOC = nameOfPOC;
		this.numberOfPOC = numberOfPOC;
		this.nameOfRequestor = nameOfRequestor;
		this.numberOfRequestor = numberOfRequestor;
		this.nameOfCustomer = nameOfCustomer;
		this.dateOfCompletion = convertDateToSimpleDateFormat(dateOfCompletion);
		this.manufaturer = manufacturer;
		this.nomenclature = nomenclature;
		this.commonModel = commonModel;
		this.nameplateModel = nameplateModel;
		this.fileName = fileName;
		if(numberOfWorkOrder.toUpperCase().startsWith("T")) {
			yearMonthFolderFormat = numberOfWorkOrder.substring(1,5) + "\\" + MONTHS[Integer.parseInt(numberOfWorkOrder.substring(5,7))-1] + "\\";
		} else {
			yearMonthFolderFormat = numberOfWorkOrder.substring(0,4) + "\\" + MONTHS[Integer.parseInt(numberOfWorkOrder.substring(4,6))-1] + "\\";
		}
	}
	
	public WorkOrder(String numberOfWorkOrder, String numberOfECN, String nameOfCustomer, String nameOfSigner, String dateOfPickup, String archivePath) {
		if(numberOfWorkOrder != null) {
			this.numberOfWorkOrder = numberOfWorkOrder;
		}
		if(numberOfECN != null) {
			this.numberOfECN = numberOfECN;
		}
		if(nameOfCustomer != null) {
			this.nameOfCustomer = nameOfCustomer;
		}
		if(dateOfPickup != null) {
			this.dateOfPickup = dateOfPickup;
		}
		if(nameOfSigner != null) {
			this.nameOfSigner = nameOfSigner;
		}
		if(archivePath != null) {
			this.archivePath = archivePath;
		}
	}
	
	String getFileName() {
		return this.fileName;
	}
	
	void setArchivePath(String archivePath) {
		this.archivePath = archivePath;
	}
	
	String getArchivePath() {
		return archivePath;
	}
	
	public WorkOrder() {
	}
	
	void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	
	String getRemarks() {
		return this.remarks;
	}
	
	String getContactHistoryString(){
		String contactedList="";
		for(String cl : listOfContactedDate) {
			contactedList+=cl+":";
		}
		return contactedList;
	}
	
	private String convertDateToSimpleDateFormat(Date date){
		try {
			return dateFormat.format(date);
		} catch (Exception e) {
			return "N/A";
		}
	}
	
	void setFrontDesk(FrontDesk fd) {
		this.fd = fd;
	}
	
	void updateTableObject() {
		fd.updateReadyForPickUpPanel(this);
	}
	
	void setTableObject(Object[] obj, int rowIndex) {
		columnData = obj;
		columnData[0] = numberOfWorkOrder;
		columnData[1] = numberOfECN;
		columnData[2] = nameOfCustomer;
		columnData[3] = nomenclature;
		columnData[4] = isContacted;
		columnData[5] = numbersOfAttempt;
		columnData[6] = dateOfLastAttempt;
		columnData[7] = dateOfLastContact;
	}
	
	void setDateOfPickup() {
		Date date = new Date();
		dateOfPickup = convertDateToSimpleDateFormat(date);
	}
	
	String getDateOfPickup() {
		return dateOfPickup;
	}
	
	void setSigner(String name) {
		nameOfSigner = name;
	}
	String getSigner() {
		return nameOfSigner;
	}
	
	void updateTableObject(Object[] obj, int rowIndex) {
		columnData = obj;
		columnData[0] = numberOfWorkOrder;
		columnData[1] = numberOfECN;
		columnData[2] = nameOfCustomer;
		columnData[3] = nomenclature;
		columnData[4] = isContacted;
		columnData[5] = numbersOfAttempt;
		columnData[6] = dateOfLastAttempt;
		columnData[7] = dateOfLastContact;
	}
	
	void setArchiveTableObject(Object[] obj, int rowIndex) {
		archiveColumnData = obj;
		archiveColumnData[0] = numberOfWorkOrder;
		archiveColumnData[1] = numberOfECN;
		archiveColumnData[2] = nameOfCustomer;
		archiveColumnData[3] = nameOfSigner;
		archiveColumnData[4] = dateOfPickup;
	}
	
	Object[] getTableObject() {
		return columnData;
	}
	
	Object[] getArchiveTableObject() {
		return archiveColumnData;
	}
	
	String itemInfoText() {
		String itemInfoText =  
				"Workorder number: " + numberOfWorkOrder
				+ "\nECN number: " + numberOfECN
				+ "\nDepartment: " + nameOfCustomer
				+ "\nNomenclature: " + nomenclature
				+ "\nManufacturer: " + manufaturer
				+ "\nCommon Model: " + commonModel
				+ "\nNameplate Model: " + nameplateModel ;
		return itemInfoText;
	}
	
	String getIsContacted() {
		if(isContacted) {
			return "Contacted";
		} else {
			return "Not Contacted";
		}
	}
}


