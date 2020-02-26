package frontdesk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Scanner;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * ManualFUnctions is to manually update database manually.
 * @author elias
 *
 */
abstract class ManualFunctions {
	private static HashMap<String, File> hml = new HashMap<String, File>();
	private static HashMap<String, HashMap<String, String>> readyForPickupResultSet;
	private static HashMap<String, HashMap<String, String>> archiveResultSet;
	
	static void updateArchiveDB() throws SQLException, ParseException, InterruptedException, InvalidPasswordException, IOException {
		File configFile = new File("FrontDesk.cfg");
		String dbFolder="";
		String readyFolder="";//dbFolder;
		String archiveFolder="";
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(configFile));
			while(br.ready()) {
				String trimLine=br.readLine().trim();
				if(trimLine.startsWith("DBFolder:")) {
					dbFolder=trimLine.substring("DBFolder:".length());
				} else if(trimLine.startsWith("ArchiveFolder:")) {
					archiveFolder=trimLine.substring("ArchiveFolder:".length());
				}
			}
			br.close();
		}catch (IOException e) {
			
		}
		WorkOrderDataBaseConnector.msAccDB = dbFolder;
		WorkOrderDataBaseConnector.init();
		readyForPickupResultSet = WorkOrderDataBaseConnector.getReadyResultSet();
		archiveResultSet = WorkOrderDataBaseConnector.getArchiveResultSet();
		HashMap<String, File> fileList = generateListOfFiles(archiveFolder);
		for(String key : fileList.keySet()) {
			if(!archiveResultSet.containsKey(key)) {
				File file = fileList.get(key);
				PDDocument doc;
				doc = PDDocument.load(file);
				PDFTextStripper ps = new PDFTextStripper();
				ps.setSortByPosition(true);
				PDDocumentCatalog pdCatalog = doc.getDocumentCatalog();
				PDAcroForm pdAcroForm = pdCatalog.getAcroForm();
				String numberOfWorkOrder = key;
				String ECN = "";
				String nameOfCustomer = "";
				String nameOfSigner = "";
				String dateOfSign = "";
				String pathOfArchive = "";
				try {
					ECN = readyForPickupResultSet.get(numberOfWorkOrder).get("ECN");
					nameOfCustomer = readyForPickupResultSet.get(numberOfWorkOrder).get("numberOfCustomer");
				} catch (NullPointerException e) {
					String bodyText = ps.getText(doc);
					Scanner sc = new Scanner(bodyText);
					int lineNumber = 0;
					while(sc.hasNextLine()) {
						String line = sc.nextLine();
						if(lineNumber < 3) {
							switch(lineNumber) {
							case 1:
								try {
									String ECN_TEXT_START= "ECN:";
									String ECN_TEXT_END= "NOMENCLATURE:";
									ECN = line.substring(ECN_TEXT_START.length(),line.indexOf(ECN_TEXT_END)).trim();
								} catch(StringIndexOutOfBoundsException se) {
									ECN = "";
								}
								break;
							case 2:
								try {
									nameOfCustomer = line.substring(line.lastIndexOf(":")+1).trim();
								} catch(StringIndexOutOfBoundsException se) {
									nameOfCustomer = "";
								}
								break;
							}
							lineNumber++;
						} else {
							break;
						}
						
					}
					sc.close();
				}
				try {
					nameOfSigner = pdAcroForm.getField("NameField").getValueAsString();
					dateOfSign = pdAcroForm.getField("DateField").getValueAsString();
					pathOfArchive = fileList.get(numberOfWorkOrder).toString();
					String[] archiveItem = {numberOfWorkOrder,ECN,nameOfCustomer,nameOfSigner,dateOfSign,pathOfArchive};
					WorkOrderDataBaseConnector.insertArchiveData("ARCHIVE_TABLE", archiveItem);
					doc.close();
				} catch (NullPointerException ne) {
					doc.close();
					continue;
				}
			}
		}
	}
	
	static boolean isExistInArchive(String wo) throws SQLException {
		archiveResultSet = WorkOrderDataBaseConnector.getArchiveResultSet();
		if(archiveResultSet.containsKey(wo)) {
			return true;
		}
		return false;
	}
	
	static HashMap<String, File> generateListOfFiles(String path) throws ParseException, SQLException, InterruptedException {
		File dir = new File(path);
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				generateListOfFiles(file.toString());
			} else {
				if(file.toString().toLowerCase().endsWith("pdf")) {
					hml.put(file.getName().substring(0, file.getName().length()-4),file);
				}
			}
		}
		return hml;
	}
	
}
