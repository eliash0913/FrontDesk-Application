package frontdesk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * ProgressBarHelper is to help LoadingScreen class
 * @author elias
 *
 */
public abstract class ProgressBarHelper {
	private static File configFile = new File("FrontDesk.cfg");
	private static String readyFolder, archiveFolder;//dbFolder;
	private static int fileCounter=0;
	static int currentFileCounter=0;
	
	private static void countMaximumNumberForProgessBar() throws SQLException{
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(configFile));
			while(br.ready()) {
				String trimLine=br.readLine().trim();
				if(trimLine.startsWith("ReadyFolder:")) {
					readyFolder=trimLine.substring("ReadyFolder:".length());
				} else if(trimLine.startsWith("ArchiveFolder:")) {
					archiveFolder=trimLine.substring("ArchiveFolder:".length());
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			JOptionPane.showConfirmDialog(new JFrame(), "Configuration file does not exist.\nplease check with CEB System Admin team.", "Missing" , JOptionPane.CLOSED_OPTION);
		} catch (IOException e) {
			JOptionPane.showConfirmDialog(new JFrame(), "Can not access to configuration file.\nplease check if this file is in use.", "Missing" , JOptionPane.CLOSED_OPTION);
		}
		File readyFilePath = new File(readyFolder);
		getMaximumNumberForProgessBar(readyFilePath);
	}
	
	static String getArchiveFolder() {
		return archiveFolder;
	}
	
	static void addFileCounter() throws SQLException {
		fileCounter+=WorkOrderDataBaseConnector.getCountArchive();
	}
	
	private static void getMaximumNumberForProgessBar(File dir) {
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				getMaximumNumberForProgessBar(file);
			} else {
				if(file.toString().toLowerCase().endsWith("pdf")) {
					fileCounter++;
				}
			}
		}
	}
	
	static int getMaximumNumberForProgessBar() throws SQLException {
		countMaximumNumberForProgessBar();
		return fileCounter;
	}
	
	static int getCurrentNumberForProgessBar() {
		return currentFileCounter;
	}
}
