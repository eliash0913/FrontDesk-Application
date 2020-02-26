package frontdesk;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

/**
 * WorkOrderDataBaseConnector class for data pushing to db and pulling from db.
 * @author elias
 *
 */
abstract class WorkOrderDataBaseConnector {
	private static Connection connection = null;
	private static Statement statement = null;
	private static ResultSet resultSet = null;
	static String msAccDB = "";
	private static String dbURL = "";
	
	static void init() {
		try {
	        Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
	    }
	    catch(ClassNotFoundException cnfex) {
	        System.out.println("Problem in loading or "
	                + "registering MS Access JDBC driver");
	        cnfex.printStackTrace();
	    }
		dbURL = "jdbc:ucanaccess://" + msAccDB;
	}
	
	static int getCountArchive() throws SQLException {
		int count=0;
		connection = DriverManager.getConnection(dbURL); 
        statement = connection.createStatement();
        resultSet = statement.executeQuery("SELECT WorkOrder FROM "+ "ARCHIVE_TABLE");
        while(resultSet.next()) {
           count++; 
        }
        if(null != connection) {
        	resultSet.close();
        	statement.close();
            connection.close();
        }
        
        return count;
	}
	
	static void insertReadyData(String tableName, String[] tableDataSet) throws SQLException {
		connection = DriverManager.getConnection(dbURL); 
        statement = connection.createStatement();
        statement.executeUpdate("INSERT INTO " + tableName + "(WorkOrder, ECN, NameOfCustomer, LastAttemptDate, FirstContactedDate, LastContactedDate, isContacted, Attempt, ContactHistory, Remarks) VALUES('"+ tableDataSet[0] + "','" + tableDataSet[1] + "','" + tableDataSet[2] + "','" + tableDataSet[3] + "','" + tableDataSet[4] + "','" + tableDataSet[5] + "','" + tableDataSet[6] + "','" + tableDataSet[7] + "','" + tableDataSet[8] + "','" + tableDataSet[9] +"')");
        if(null != connection) {
            statement.close();
            connection.close();
        }
	}
	
	static void updateReadyData(String tableName, String numberOfWorkOrder, String[] tableDataSet) throws SQLException {
		connection = DriverManager.getConnection(dbURL); 
        statement = connection.createStatement();
        statement.executeUpdate("UPDATE READY_TABLE SET WorkOrder='" + tableDataSet[0] + "', ECN='" + tableDataSet[1] + "', NameOfCustomer='" + tableDataSet[2] + "', LastAttemptDate='" + tableDataSet[3] + "', FirstContactedDate='" + tableDataSet[4] + "', LastContactedDate='" + tableDataSet[5] + "', isContacted='" + tableDataSet[6] + "', Attempt='" + tableDataSet[7] + "', ContactHistory='" + tableDataSet[8] + "', Remarks='" + tableDataSet[9] +  "' WHERE WorkOrder="+"'"+numberOfWorkOrder+"'");
        if(null != connection) {
            statement.close();
            connection.close();
        }
	}
	
	static void updateRemarksReadyData(String tableName, String numberOfWorkOrder, String remarks) throws SQLException {
		connection = DriverManager.getConnection(dbURL); 
        statement = connection.createStatement();
        statement.executeUpdate("UPDATE READY_TABLE SET Remarks='" + remarks +  "' WHERE WorkOrder="+"'"+numberOfWorkOrder+"'");
        if(null != connection) {
            statement.close();
            connection.close();
        }
	}

	static void insertArchiveData(String tableName, String[] tableDataSet) throws SQLException {
		connection = DriverManager.getConnection(dbURL); 
        statement = connection.createStatement();
        statement.executeUpdate("INSERT INTO " + tableName + "(WorkOrder, ECN, nameOfCustomer, nameOfSigner, signedDate, Path) VALUES('"+ tableDataSet[0] + "','" + tableDataSet[1] + "','" + tableDataSet[2] + "','" + tableDataSet[3] + "','"  + tableDataSet[4] + "','" + tableDataSet[5] + "')");
        if(null != connection) {
            statement.close();
            connection.close();
        }
	}
	
	static void updateArchiveData(String tableName, String numberOfWorkOrder, String[] tableDataSet) throws SQLException {
		connection = DriverManager.getConnection(dbURL); 
        statement = connection.createStatement();
        statement.executeUpdate("UPDATE " +tableName+" SET WorkOrder='" + tableDataSet[0] + "', ECN='" + tableDataSet[1] + "', nameOfCustomer='" + tableDataSet[2] + "', nameOfSigner='" + tableDataSet[3] + "', signedDate='" + tableDataSet[4] + "', Path='" + tableDataSet[5] +  "' WHERE WorkOrder="+numberOfWorkOrder);
        if(null != connection) {
            statement.close();
            connection.close();
        }
	}
	
	static boolean isExist(String tableName, String numberOfWorkOrder) throws SQLException {
		boolean isExist = false;
		connection = DriverManager.getConnection(dbURL); 
        statement = connection.createStatement();
        resultSet = statement.executeQuery("SELECT WorkOrder FROM "+ tableName);
        while(resultSet.next()) {
        	if(resultSet.getNString("WorkOrder").equalsIgnoreCase(numberOfWorkOrder)) {
        		isExist = true;
        		break;
        	}
        }
        if(null != connection) {
        	
        	resultSet.close();
            statement.close();
            connection.close();
        }
        return isExist;
	}
	
	static void removeData(String tableName, String numberOfWorkOrder) throws SQLException {
		connection = DriverManager.getConnection(dbURL); 
        statement = connection.createStatement();
        statement.executeUpdate("DELETE FROM "+ tableName +" WHERE WorkOrder="+"'"+numberOfWorkOrder+"'");
        if(null != connection) {
            statement.close();
            connection.close();
        }
	}
	
	static void displayData(String tableName) throws SQLException {
		connection = DriverManager.getConnection(dbURL); 
        statement = connection.createStatement();
		resultSet = statement.executeQuery("SELECT * FROM "+tableName);
		while(resultSet.next()) {
            System.out.println(
            		resultSet.getNString("WorkOrder")+ "\t\t" +
            				resultSet.getNString("LastAttemptDate")+ "\t\t" + 
            				resultSet.getNString("FirstContactedDate")+ "\t\t" +
            				resultSet.getNString("LastContactedDate")+ "\t\t" + 
            				resultSet.getNString("isContacted")+ "\t\t" + 
            				resultSet.getNString("Attempt")+ "\t\t" + 
            				resultSet.getNString("ContactHistory")+ "\t\t" + 
            				resultSet.getNString("Remarks")); 
        }
        if(null != connection) {
        	resultSet.close();
        	statement.close();
            connection.close();
        }
	}
	
	static HashMap<String, HashMap<String, String>> getReadyResultSet() throws SQLException {
		HashMap<String, HashMap<String, String>> result = new HashMap<String, HashMap<String, String>>();
		connection = DriverManager.getConnection(dbURL); 
        statement = connection.createStatement();
		resultSet = statement.executeQuery("SELECT * FROM READY_TABLE");
		
		while(resultSet.next()) {
			HashMap<String, String> details = new HashMap<String, String>();
			details.put("WorkOrder", resultSet.getNString("WorkOrder"));
			details.put("ECN", resultSet.getNString("ECN"));
			details.put("NameOfCustomer", resultSet.getNString("NameOfCustomer"));
			details.put("LastAttemptDate", resultSet.getNString("LastAttemptDate"));
			details.put("FirstContactedDate", resultSet.getNString("FirstContactedDate"));
			details.put("LastContactedDate", resultSet.getNString("LastContactedDate"));
			details.put("isContacted", resultSet.getNString("isContacted"));
			details.put("Attempt", resultSet.getNString("Attempt"));
			details.put("ContactHistory", resultSet.getNString("ContactHistory"));
			details.put("Remarks", resultSet.getNString("Remarks"));
			result.put(resultSet.getNString("WorkOrder"), details);
		}
        if(null != connection) {
        	resultSet.close();
            statement.close();
            connection.close();
        }
		return result;
	}
	
	static HashMap<String, HashMap<String, String>> getArchiveResultSet() throws SQLException {
		HashMap<String, HashMap<String, String>> result = new HashMap<String, HashMap<String, String>>();
		connection = DriverManager.getConnection(dbURL); 
        statement = connection.createStatement();
		resultSet = statement.executeQuery("SELECT * FROM ARCHIVE_TABLE");
		while(resultSet.next()) {
			HashMap<String, String> details = new HashMap<String, String>();
			details.put("WorkOrder", resultSet.getNString("WorkOrder"));
			details.put("ECN", resultSet.getNString("ECN"));
			details.put("nameOfCustomer", resultSet.getNString("nameOfCustomer"));
			details.put("nameOfSigner", resultSet.getNString("nameOfSigner"));
			details.put("signedDate", resultSet.getNString("signedDate"));
			details.put("Path", resultSet.getNString("Path"));
			result.put(resultSet.getNString("WorkOrder"), details);
		}
        if(null != connection) {
        	resultSet.close();
            statement.close();
            connection.close();
        }
		return result;
	}
	
	static HashMap<String, String> manualDBUpdate(HashMap<String, HashMap<String, String>> archiveResultSet) throws SQLException {
		final String[] MONTHS = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
		String yearMonthFolderFormat;
		HashMap<String, String> tmp = new HashMap<String, String>();
		connection = DriverManager.getConnection(dbURL); 
        statement = connection.createStatement();
		resultSet = statement.executeQuery("SELECT * FROM ARCHIVE_TABLE");
		while(resultSet.next()) {
//			HashMap<String, String> details = new HashMap<String, String>();
			String wo = resultSet.getNString("WorkOrder");
			if(wo.toUpperCase().startsWith("T")) {
				yearMonthFolderFormat = wo.substring(1,5) + "\\" + MONTHS[Integer.parseInt(wo.substring(5,7))-1] + "\\";
			} else {
				yearMonthFolderFormat = wo.substring(0,4) + "\\" + MONTHS[Integer.parseInt(wo.substring(4,6))-1] + "\\";
			}
			String Path = "N:\\WorkOrder\\Archive\\"+yearMonthFolderFormat+wo+".pdf";
			tmp.put(wo,Path);
		}
		if(null != connection) {
			resultSet.close();
			statement.close();
			connection.close();
		}
		return tmp;
	}	
	
	static HashMap<String, String> manualMethod() throws SQLException {
		final String[] MONTHS = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
		String yearMonthFolderFormat;
		HashMap<String, String> tmp = new HashMap<String, String>();
		connection = DriverManager.getConnection(dbURL); 
        statement = connection.createStatement();
		resultSet = statement.executeQuery("SELECT * FROM ARCHIVE_TABLE");
		while(resultSet.next()) {
			String wo = resultSet.getNString("WorkOrder");
			if(wo.toUpperCase().startsWith("T")) {
				yearMonthFolderFormat = wo.substring(1,5) + "\\" + MONTHS[Integer.parseInt(wo.substring(5,7))-1] + "\\";
			} else {
				yearMonthFolderFormat = wo.substring(0,4) + "\\" + MONTHS[Integer.parseInt(wo.substring(4,6))-1] + "\\";
			}
			String Path = "N:\\WorkOrder\\Archive\\"+yearMonthFolderFormat+wo+".pdf";
			tmp.put(wo,Path);
		}
		if(null != connection) {
			resultSet.close();
			statement.close();
			connection.close();
		}
		return tmp;
	}
	
	static void manualUpdate(HashMap<String, String> wp) throws SQLException {
		connection = DriverManager.getConnection(dbURL); 
        statement = connection.createStatement();
        for(String key : wp.keySet()) {
        	statement.executeUpdate("UPDATE ARCHIVE_TABLE SET Path='" + wp.get(key) + "' WHERE WorkOrder="+"'"+key+"'");
        }
        if(null != connection) {
            statement.close();
            connection.close();
        }
	}
}
