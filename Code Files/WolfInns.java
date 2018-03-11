/**
 * 
 * CSC 540
 * 
 * Wolf Inns
 * Hotel Management Database System
 * 
 * Team C
 * Abhay Soni                   (asoni3)
 * Aurora Tiffany-Davis         (attiffan)
 * Manjusha Trilochan Awasthi   (mawasth)
 * Samantha Scoggins            (smscoggi)
 *
 */

// Imports
import java.util.Scanner;
import java.sql.*;

// WolfInns class
public class WolfInns {
    
    // DECLARATIONS
    
    // Declare constants - commands
    
    private static final String CMD_MAIN =                  "MAIN";
    private static final String CMD_QUIT =                  "QUIT";
    private static final String CMD_REPORTS =               "REPORTS";
    private static final String CMD_MANAGE =                "MANAGE";
    
    private static final String CMD_REPORT_HOTELS =         "HOTELS";
    private static final String CMD_REPORT_ROOMS =          "ROOMS";
    private static final String CMD_REPORT_STAFF =          "STAFF";
    private static final String CMD_REPORT_CUSTOMERS =      "CUSTOMERS";
    private static final String CMD_REPORT_STAYS =          "STAYS";
    private static final String CMD_REPORT_SERVICES =       "SERVICES";
    private static final String CMD_REPORT_PROVIDED =       "PROVIDED";
    
    private static final String CMD_MANAGE_HOTEL_ADD =      "ADDHOTEL";
    private static final String CMD_MANAGE_HOTEL_DELETE =   "DELETEHOTEL";
    
    // Declare constants - connection parameters
    private static final String JDBC_URL = "jdbc:mariadb://classdb2.csc.ncsu.edu:3306/smscoggi";
    private static final String JDBC_USER = "smscoggi";
    private static final String JDBC_PASSWORD = "200157888";
    
    // Declare variables
    private static Connection jdbc_connection;
    private static Statement jdbc_statement;
    private static ResultSet jdbc_result;
    private static String currentMenu;
    
    /* Why is the scanner outside of any method?
     * See https://stackoverflow.com/questions/13042008/java-util-nosuchelementexception-scanner-reading-user-input
     */
    private static Scanner scanner;
    
    // SETUP
    
    /** 
     * Print available commands
     * 
     * Arguments -  menu -  The menu we are currently in (determines available commands).
     *                      For example main, reports, etc.
     * Return -     None
     * 
     * Modifications:   03/07/18 -  ATTD -  Created method.
     *                  03/08/18 -  ATTD -  Add ability to print entire Provided table.
     *                  03/09/18 -  ATTD -  Add ability to delete a hotel.
     */
    public static void printAvailableCommands(String menu) {
        
        try {
            
            System.out.println("");
            System.out.println(menu + " Menu available commands:");
            System.out.println("");
            
            switch (menu) {
                case CMD_MAIN:
                    System.out.println("'" + CMD_REPORTS + "'");
                    System.out.println("\t- run reports");
                    System.out.println("'" + CMD_MANAGE + "'");
                    System.out.println("\t- manage the hotel chain (add hotels, etc)");
                    System.out.println("'" + CMD_QUIT + "'");
                    System.out.println("\t- exit the program");
                    System.out.println("");
                    break;
                case CMD_REPORTS:
                    System.out.println("'" + CMD_REPORT_HOTELS + "'");
                    System.out.println("\t- run report on hotels");
                    System.out.println("'" + CMD_REPORT_ROOMS + "'");
                    System.out.println("\t- run report on rooms");
                    System.out.println("'" + CMD_REPORT_STAFF + "'");
                    System.out.println("\t- run report on staff");
                    System.out.println("'" + CMD_REPORT_CUSTOMERS + "'");
                    System.out.println("\t- run report on customers");
                    System.out.println("'" + CMD_REPORT_STAYS + "'");
                    System.out.println("\t- run report on stays");
                    System.out.println("'" + CMD_REPORT_SERVICES + "'");
                    System.out.println("\t- run report on service types");
                    System.out.println("'" + CMD_REPORT_PROVIDED + "'");
                    System.out.println("\t- run report on services provided to guests");
                    System.out.println("'" + CMD_MAIN + "'");
                    System.out.println("\t- go back to the main menu");
                    System.out.println("");
                    break;
                case CMD_MANAGE:
                    System.out.println("'" + CMD_MANAGE_HOTEL_ADD + "'");
                    System.out.println("\t- add a hotel");
                    System.out.println("'" + CMD_MANAGE_HOTEL_DELETE + "'");
                    System.out.println("\t- delete a hotel");
                    System.out.println("'" + CMD_MAIN + "'");
                    System.out.println("\t- go back to the main menu");
                    System.out.println("");
                    break;
                default:
                    break;
            }
            

        
        }
        catch (Throwable err) {
            err.printStackTrace();
        }
        
    }
    
    /** 
     * Establish a connection to the database
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   03/07/18 -  ATTD -  Created method.
     */
    public static void connectToDatabase() {
        
        try {
            
            // Get JDBC driver
            Class.forName("org.mariadb.jdbc.Driver");
            
            // Initialize JDBC stuff to null
            jdbc_connection = null;
            jdbc_statement = null;
            jdbc_result = null;
            
            // Establish connection
            jdbc_connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
            jdbc_statement = jdbc_connection.createStatement();
            
        }
        catch (Throwable err) {
            err.printStackTrace();
        }
        
    }
        
    // TABLE CREATION
    
    /** 
     * Drop database tables, if they exist
     * (to support running program many times)
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   03/07/18 -  ATTD -  Created method.
     */
    public static void dropExistingTables() {

        try {
            
            // Declare variables
            DatabaseMetaData metaData;
            String tableName;

            /* Find out what tables already exist
             * https://docs.oracle.com/javase/8/docs/api/java/sql/DatabaseMetaData.html
             */
            metaData = jdbc_connection.getMetaData();
            jdbc_result = metaData.getTables(null, null, "%", null);
            
            // Go through and delete each existing table
            while (jdbc_result.next()) {
                // Get table name
                tableName = jdbc_result.getString(3);
                /* Drop disable foreign key checks to avoid complaint
                 * https://stackoverflow.com/questions/4120482/foreign-key-problem-in-jdbc
                 */
                jdbc_statement.executeUpdate("SET FOREIGN_KEY_CHECKS=0");
                // Drop table
                jdbc_statement.executeUpdate("DROP TABLE " + tableName);
                // Re-establish normal foreign key checks
                jdbc_statement.executeUpdate("SET FOREIGN_KEY_CHECKS=1");
            }
            
        }
        catch (Throwable err) {
            err.printStackTrace();
        }
        
    }
    
    /** 
     * Create database tables
     * 
     * Note:    CHECK    
     *              Per https://dev.mysql.com/doc/refman/5.7/en/create-table.html,
     *              "The CHECK clause is parsed but ignored by all storage engines",
     *          ASSERTION
     *              Per https://stackoverflow.com/questions/34769321/unexplainable-mysql-error-when-trying-to-create-assertion
     *              "This list does not include CREATE ASSERTION, so MariaDB does not support this functionality"
     *          So unfortunately there are some data entry error checks that we must perform
     *          in the application rather than letting the DBMS do it for us
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   03/07/18 -  ATTD -  Created method.
     *                  03/08/18 -  ATTD -  Changed state to CHAR(2).
     *                  03/09/18 -  ATTD -  Added on delete rules for foreign keys.
     *                  03/11/18 -  ATTD -  Added amount owed to Stays relation.
     */
    public static void createTables() {
        
        try {

            // Drop all tables that already exist, so that we may run repeatedly
            dropExistingTables();
            
            // Start transaction
            jdbc_connection.setAutoCommit(false);
            
            /* Create table: Customers
             * phone number to be entered as 10 digit int ex: 9993335555
             * requires "BIGINT" instead of just "INT"
             * SSN to be entered as 9 digit int ex: 100101000
             * requires "BIGINT" instead of just "INT"
             */
            jdbc_statement.executeUpdate("CREATE TABLE Customers ("+
                "SSN BIGINT NOT NULL,"+
                "Name VARCHAR(255) NOT NULL,"+
                "DOB DATE NOT NULL,"+
                "PhoneNum BIGINT NOT NULL,"+
                "Email VARCHAR(255) NOT NULL,"+
                "PRIMARY KEY (SSN)"+
            ")");

            // Create table: ServiceTypes
            jdbc_statement.executeUpdate("CREATE TABLE ServiceTypes ("+
                "Name VARCHAR(255) NOT NULL,"+
                "Cost INT NOT NULL,"+
                "PRIMARY KEY (Name)"+
            ")");

            /* Create table: Staff
             * phone number to be entered as 10 digit int ex: 9993335555
             * requires "BIGINT" instead of just "INT"
             */
            jdbc_statement.executeUpdate("CREATE TABLE Staff ("+
                "ID INT NOT NULL AUTO_INCREMENT,"+
                "Name VARCHAR(225) NOT NULL,"+
                "DOB DATE NOT NULL,"+
                "JobTitle VARCHAR(225),"+
                "Dep VARCHAR(225) NOT NULL,"+
                "PhoneNum BIGINT NOT NULL,"+
                "Address VARCHAR(225) NOT NULL,"+
                "HotelID INT,"+
                "PRIMARY KEY(ID)"+
            ")");

            /* Create table: Hotels
             * this is done after Staff table is created
             * because manager ID references Staff table
             * phone number to be entered as 10 digit int ex: 9993335555
             * requires "BIGINT" instead of just "INT"
             */
            jdbc_statement.executeUpdate("CREATE TABLE Hotels ("+
                "ID INT NOT NULL AUTO_INCREMENT,"+
                "Name VARCHAR(225) NOT NULL,"+
                "StreetAddress VARCHAR(225) NOT NULL,"+
                "City VARCHAR(225) NOT NULL,"+
                "State CHAR(2) NOT NULL,"+
                "PhoneNum BIGINT Not Null,"+
                "ManagerID INT Not Null,"+
                "Primary Key(ID),"+
                "CONSTRAINT UC_HACS UNIQUE (StreetAddress, City, State),"+
                "CONSTRAINT UC_HPN UNIQUE (PhoneNum),"+
                "CONSTRAINT UC_HMID UNIQUE (ManagerID),"+
                /* If a manager is deleted from the system and not replaced in the same transaction, no choice but to delete hotel
                 * A hotel cannot be without a manager
                 */
                "CONSTRAINT FK_HMID FOREIGN KEY (ManagerID) REFERENCES Staff(ID) ON DELETE CASCADE"+
            ")");

            /* Alter table: Staff
             * needs to happen after Hotels table is created
             * because hotel ID references Hotels table
             */
            jdbc_statement.executeUpdate("ALTER TABLE Staff "+
                "ADD CONSTRAINT FK_STAFFHID "+
                 /* If a hotel is deleted, no need to delete the staff that work there,
                  * NULL is allowed (currently unassigned staff)
                  */
                "FOREIGN KEY (HotelID) REFERENCES Hotels(ID) ON DELETE SET NULL"
            ); 

            // Create table: Rooms
            jdbc_statement.executeUpdate("CREATE TABLE Rooms ("+
                "RoomNum INT NOT NULL,"+
                "HotelID INT NOT NULL,"+
                "Category VARCHAR(225) NOT NULL,"+
                "MaxOcc INT NOT NULL,"+
                "NightlyRate DOUBLE NOT NULL,"+
                "DRSStaff INT,"+
                "DCStaff INT,"+
                "PRIMARY KEY(RoomNum,HotelID),"+
                // If a hotel is deleted, then the rooms within it should also be deleted
                "CONSTRAINT FK_ROOMHID FOREIGN KEY (HotelID) REFERENCES Hotels(ID) ON DELETE CASCADE,"+
                /* If a staff member dedicated to a room is deleted by the end of a transaction
                 * then something has probably gone wrong, because that staff member should have been replaced
                 * to maintain continuous service
                 * Nonetheless, not appropriate to delete the room in this case
                 * NULL is allowed
                */
                "CONSTRAINT FK_ROOMDRSID FOREIGN KEY (DRSStaff) REFERENCES Staff(ID) ON DELETE SET NULL,"+
                "CONSTRAINT FK_ROOMDCID FOREIGN KEY (DCStaff) REFERENCES Staff(ID) ON DELETE SET NULL"+
            ")");

            // Create table: Stays
            jdbc_statement.executeUpdate("CREATE TABLE Stays ("+
                "ID INT NOT NULL AUTO_INCREMENT,"+
                "StartDate DATE NOT NULL,"+
                "CheckInTime TIME NOT NULL,"+
                "RoomNum INT NOT NULL,"+
                "HotelID INT NOT NULL,"+
                "CustomerSSN BIGINT NOT NULL,"+
                "NumGuests INT NOT NULL,"+
                "CheckOutTime TIME,"+
                "EndDate DATE,"+
                "AmountOwed DOUBLE,"+
                "PaymentMethod ENUM('CASH','CARD') NOT NULL,"+
                "CardType ENUM('VISA','MASTERCARD','HOTEL'),"+
                "CardNumber BIGINT,"+
                "BillingAddress VARCHAR(255) NOT NULL,"+
                "PRIMARY KEY(ID),"+
                "CONSTRAINT UC_STAYKEY UNIQUE (StartDate, CheckInTime,RoomNum, HotelID),"+
                /* If a room is deleted, then the stay no longer makes sense and should be deleted
                 * Need to handle room/hotel together as a single foreign key
                 * Because a foreign key is supposed to point to a unique tuple
                 * And room number by itself is not unique
                */
                "CONSTRAINT FK_STAYRID FOREIGN KEY (RoomNum, HotelID) REFERENCES Rooms(RoomNum, HotelID) ON DELETE CASCADE,"+
                // If a customer is deleted, then the stay no longer makes sense and should be deleted
                "CONSTRAINT FK_STAYCSSN FOREIGN KEY (CustomerSSN) REFERENCES Customers(SSN) ON DELETE CASCADE"+
            ")");

            // Create table: Provided
            jdbc_statement.executeUpdate("CREATE TABLE Provided ("+
                "ID INT NOT NULL AUTO_INCREMENT,"+
                "StayID INT NOT NULL,"+
                "StaffID INT NOT NULL,"+
                "ServiceName VARCHAR(255) NOT NULL,"+
                "PRIMARY KEY(ID),"+
                // If a stay is deleted, then the service provided record no longer makes sense and should be deleted
                "CONSTRAINT FK_PROVSTAYID FOREIGN KEY (StayID) REFERENCES Stays(ID) ON DELETE CASCADE,"+
                // If a staff member is deleted, then the service provided record no longer makes sense and should be deleted
                "CONSTRAINT FK_PROVSTAFFID FOREIGN KEY (StaffID) REFERENCES Staff(ID) ON DELETE CASCADE,"+
                // If a service type is deleted, then the service provided record no longer makes sense and should be deleted
                "CONSTRAINT FK_PROVSERV FOREIGN KEY (ServiceName) REFERENCES ServiceTypes(Name) ON DELETE CASCADE"+
            ")");
            
            System.out.println("Tables created successfully!");
            
            // End transaction
            jdbc_connection.commit();
            jdbc_connection.setAutoCommit(true);
            
        }
        catch (Throwable err) {
            err.printStackTrace();
        }
        
    }
    
    // TABLE POPULATION 
    
    /** 
     * Populate Customers Table
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   03/07/18 -  ATTD -  Created method.
     *                  03/07/18 -  MTA -   Populated method.
     *                  03/08/18 -  ATTD -  Shifted some string constants purely for readability (no functional changes).
     */
    public static void populateCustomersTable() {
        
        try {
            
            // Start transaction
            jdbc_connection.setAutoCommit(false);
            
            // Populating data for Customers
            jdbc_statement.executeUpdate("INSERT INTO Customers"+
				"(SSN, Name, DOB, PhoneNum, Email) VALUES "+
				"(555284568, 'Isaac Gray', '1982-11-12', '9194562158', 'issac.gray@gmail.com');");
            jdbc_statement.executeUpdate("INSERT INTO Customers"+ 
				"(SSN, Name, DOB, PhoneNum, Email) VALUES "+ 
				"(111038548, 'Jay Sharp', '1956-07-09', '9191237548', 'jay.sharp@gmail.com');"); 
            jdbc_statement.executeUpdate("INSERT INTO Customers "+ 
				"(SSN, Name, DOB, PhoneNum, Email) VALUES "+ 
				"(222075875, 'Jenson Lee', '1968-09-25', '9194563217', 'jenson.lee@gmail.com');");
            jdbc_statement.executeUpdate("INSERT INTO Customers "+ 
				"(SSN, Name, DOB, PhoneNum, Email) VALUES "+ 
				" (333127845, 'Benjamin Cooke', '1964-01-07', '9191256324', 'benjamin.cooke@gmail.com');");
            jdbc_statement.executeUpdate("INSERT INTO Customers "+ 
				"(SSN, Name, DOB, PhoneNum, Email) VALUES "+ 
				" (444167216, 'Joe Bradley', '1954-04-07', '9194587569', 'joe.bradley@gmail.com');");
            jdbc_statement.executeUpdate("INSERT INTO Customers "+ 
				"(SSN, Name, DOB, PhoneNum, Email) VALUES "+ 
				" (666034568, 'Conor Stone', '1975-06-04', '9194567216', 'conor.stone@gmail.com');");
            jdbc_statement.executeUpdate("INSERT INTO Customers "+ 
				"(SSN, Name, DOB, PhoneNum, Email) VALUES "+ 
				" (777021654, 'Elizabeth Davis', '1964-07-26', '9195432187', 'elizabeth.davis@gmail.com');");
            jdbc_statement.executeUpdate("INSERT INTO Customers "+ 
				"(SSN, Name, DOB, PhoneNum, Email) VALUES "+ 
				" (888091545, 'Natasha Moore', '1966-08-14', '9194562347', 'natasha.moore@gmail.com');");
            
            System.out.println("Customers table loaded!");
    		
            // End transaction
            jdbc_connection.commit();
            jdbc_connection.setAutoCommit(true);
            
        }
        catch (Throwable err) {
            err.printStackTrace();
        }
        
    }
    
    /** 
     * Populate ServiceTypes Table
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   03/07/18 -  ATTD -  Created method.
     *                  03/07/18 -  MTA -   Populated method.
     *                  03/08/18 -  ATTD -  Shifted some string constants purely for readability (no functional changes).
     */
    public static void populateServiceTypesTable() {
        
        try {
            
            // Start transaction
            jdbc_connection.setAutoCommit(false);
            
            // Populating data for ServiceTypes
            jdbc_statement.executeUpdate("INSERT INTO ServiceTypes "+ 
				"(Name, Cost) VALUES "+
				"('PHONE_BILL', 25);");
			jdbc_statement.executeUpdate("INSERT INTO ServiceTypes "+ 
				"(Name, Cost) VALUES "+
				"('DRY_CLEANING', 20);");
			jdbc_statement.executeUpdate("INSERT INTO ServiceTypes "+ 
				"(Name, Cost) VALUES "+
				"('GYM', 35);");
			jdbc_statement.executeUpdate("INSERT INTO ServiceTypes "+ 
				"(Name, Cost) VALUES "+
				"('ROOM_SERVICE', 25);");
			jdbc_statement.executeUpdate("INSERT INTO ServiceTypes "+ 
				"(Name, Cost) VALUES "+
				"('CATERING', 50);");
			jdbc_statement.executeUpdate("INSERT INTO ServiceTypes "+ 
				"(Name, Cost) VALUES "+
				"('SPECIAL_SERVICE', 40);");
			
			System.out.println("ServiceTypes table loaded!");

            // End transaction
            jdbc_connection.commit();
            jdbc_connection.setAutoCommit(true);
            
        }
        catch (Throwable err) {
            err.printStackTrace();
        }
        
    }
    
    /** 
     * Populate Staff Table
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   03/07/18 -  ATTD -  Created method.
     *                  03/07/18 -  MTA -   Populated method.
     *                  03/08/18 -  ATTD -  Shifted some string constants purely for readability (no functional changes).
     *                  03/09/18 -  ATTD -  Removed explicit setting of ID (this is auto incremented).
     *                  03/10/18 -  ATTD -  Removed explicit setting of hotel ID to null.
     */
    public static void populateStaffTable() {
        
        try {
            
            // Start transaction
            jdbc_connection.setAutoCommit(false);
            
            // Populating data for Staff
    		// Staff for Hotel#1
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Zoe Holmes', '1980-10-02', 'Manager', 'Manager', 8141113134, '123 6th St. Melbourne, FL 32904');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Katelyn Weeks', '1970-04-20', 'Front Desk Representative', 'Front Desk Representative', 6926641058, '123 6th St. Melbourne, FL 32904');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Abby Huffman', '1990-12-14', 'Room Service', 'Room Service', 6738742135, '71 Pilgrim Avenue Chevy Chase, MD 20815');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Oliver Gibson', '1985-05-12', 'Room Service', 'Room Service', 1515218329, '70 Bowman St. South Windsor, CT 06074');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Michael Day', '1983-02-25', 'Catering', 'Catering', 3294931245, '4 Goldfield Rd. Honolulu, HI 96815');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('David Adams', '1985-01-17', 'Dry Cleaning', 'Dry Cleaning', 9194153214, '44 Shirley Ave. West Chicago, IL 60185');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Ishaan Goodman', '1993-04-19', 'Gym', 'Gym', 5203201425, '514 S. Magnolia St. Orlando, FL 32806');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Nicholas Read', '1981-01-14', 'Catering', 'Catering', 2564132017, '236 Pumpkin Hill Court Leesburg, VA 20175');");
    		
    		// Staff for Hotel#2
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Dominic Mitchell', '1971-03-13', 'Manager', 'Manager', 2922497845, '7005 South Franklin St. Somerset, NJ 08873');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Oliver Lucas', '1961-05-11', 'Front Desk Representative', 'Front Desk Representative', 2519881245, '7 Edgefield St. Augusta, GA 30906');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Molly Thomas', '1987-07-10', 'Room Service', 'Room Service', 5425871245, '541 S. Holly Street Norcross, GA 30092');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
		        "('Caitlin Cole', '1989-08-15', 'Catering', 'Catering', 4997845612, '7 Ivy Ave. Traverse City, MI 49684');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Victoria Medina', '1989-02-04', 'Dry Cleaning', 'Dry Cleaning', 1341702154, '8221 Trenton St. Jamestown, NY 14701');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Will Rollins', '1982-07-06', 'Gym', 'Gym', 7071264587, '346 Beacon Lane Quakertown, PA 18951');");
    		
    		// Staff for Hotel#3
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Masen Shepard', '1983-01-09', 'Manager', 'Manager', 8995412364, '3 Fulton Ave. Bountiful, UT 84010');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Willow Roberts', '1987-02-08', 'Front Desk Representative', 'Front Desk Representative', 5535531245, '7868 N. Lees Creek Street Chandler, AZ 85224');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Maddison Davies', '1981-03-07', 'Room Service', 'Room Service', 6784561245, '61 New Road Ithaca, NY 14850');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Crystal Barr', '1989-04-06', 'Catering', 'Catering', 4591247845, '9094 6th Ave. Macomb, MI 48042');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Dayana Tyson', '1980-05-05', 'Dry Cleaning', 'Dry Cleaning', 4072134587, '837 W. 10th St. Jonesboro, GA 30236');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Tommy Perry', '1979-06-04', 'Gym', 'Gym', 5774812456, '785 Bohemia Street Jupiter, FL 33458');");
    		
    		// Staff for Hotel#4
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Joshua Burke', '1972-01-10', 'Manager', 'Manager', 1245214521, '8947 Briarwood St. Baldwin, NY 11510');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Bobby Matthews', '1982-02-14', 'Front Desk Representative', 'Front Desk Representative', 5771812456, '25 W. Dogwood Lane Bemidji, MN 56601');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Pedro Cohen', '1983-04-24', 'Room Service', 'Room Service', 8774812456, '9708 Brickyard Ave. Elyria, OH 44035');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Alessandro Beck', '1981-06-12', 'Catering', 'Catering', 5774812452, '682 Glen Ridge St. Leesburg, VA 20175');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Emily Petty', '1984-08-19', 'Dry Cleaning', 'Dry Cleaning', 5772812456, '7604 Courtland St. Easley, SC 29640');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Rudy Cole', '1972-01-09', 'Gym', 'Gym', 5774812856, '37 Marconi Drive Owensboro, KY 42301');");
    		
    		// Staff for Hotel#5
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Blair Ball', '1981-01-10', 'Manager', 'Manager', 8854124568, '551 New Saddle Ave. Cape Coral, FL 33904');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Billy Lopez', '1982-05-11', 'Front Desk Representative', 'Front Desk Representative', 5124562123, '99 Miles Road Danbury, CT 06810');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Lee Ward', '1983-06-12', 'Room Service', 'Room Service', 9209124562, '959 S. Tailwater St. Ridgewood, NJ 07450');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Ryan Parker', '1972-08-13', 'Catering', 'Catering', 1183024152, '157 State Dr. Attleboro, MA 02703');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Glen Elliott', '1971-09-14', 'Catering', 'Catering', 6502134785, '9775 Clinton Dr. Thornton, CO 80241');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Ash Harrison', '1977-02-15', 'Dry Cleaning', 'Dry Cleaning', 9192451365, '9924 Jefferson Ave. Plainfield, NJ 07060');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Leslie Little', '1979-12-16', 'Gym', 'Gym', 9192014512, '7371 Pin Oak St. Dalton, GA 30721');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Mason West', '1970-10-17', 'Gym', 'Gym', 6501231245, '798 W. Valley Farms Lane Saint Petersburg, FL 33702');");
    		
    		//Staff for Hotel#6
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Riley Dawson', '1975-01-09', 'Manager', 'Manager', 1183021245, '898 Ocean Court Hilliard, OH 43026');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Gabe Howard', '1987-03-01', 'Front Desk Representative', 'Front Desk Representative', 6501421523, '914 Edgefield Dr. Hartselle, AL 35640');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Jessie Nielsen', '1982-06-02', 'Room Service', 'Room Service', 7574124587, '7973 Edgewood Road Gallatin, TN 37066');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Gabe Carlson', '1983-08-03', 'Room Service', 'Room Service', 5771245865, '339 Pine Lane Tampa, FL 33604');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Carmen Lee', '1976-01-04', 'Catering', 'Catering', 9885234562, '120 Longbranch Drive Port Richey, FL 34668');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
		        "(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Mell Tran', '1979-06-05', 'Dry Cleaning', 'Dry Cleaning', 9162451245, '32 Pearl St. Peoria, IL 61604');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Leslie Cook', '1970-10-08', 'Gym', 'Gym', 6501245126, '59 W. High Ridge Street Iowa City, IA 52240');");
    		
    		//Staff for Hotel#7
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Rory Burke', '1971-01-05', 'Manager', 'Manager', 7702653764, '9273 Ridge Drive Winter Springs, FL 32708');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Macy Fuller', '1972-02-07', 'Front Desk Representative', 'Front Desk Representative', 7485612345, '676 Myers Street Baldwin, NY 11510');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Megan Lloyd', '1973-03-01', 'Room Service', 'Room Service', 7221452315, '849 George Lane Park Ridge, IL 60068');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Grace Francis', '1974-04-09', 'Catering', 'Catering', 3425612345, '282 Old York Court Mechanicsburg, PA 17050');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Macy Fuller', '1975-05-02', 'Dry Cleaning', 'Dry Cleaning', 4665127845, '57 Shadow Brook St. Hudson, NH 03051');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Cory Hoover', '1976-06-12', 'Gym', 'Gym', 9252210735, '892 Roosevelt Street Ithaca, NY 14850');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Sam Graham', '1977-07-25', 'Gym', 'Gym', 7226251245, '262 Bayberry St. Dorchester, MA 02125');");
    		
    		//Staff for Hotel#8
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Charlie Adams', '1981-01-01', 'Manager', 'Manager', 6084254152, '9716 Glen Creek Dr. Newark, NJ 07103');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Kiran West', '1985-02-02', 'Front Desk Representative', 'Front Desk Representative', 9623154125, '68 Smith Dr. Lexington, NC 27292');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Franky John', '1986-03-03', 'Room Service', 'Room Service', 8748544152, '6 Shirley Road Fairborn, OH 45324');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Charlie Bell', '1985-04-04', 'Room Service', 'Room Service', 9845124562, '66 Elm Street Jupiter, FL 33458');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Jamie Young', '1986-06-05', 'Catering', 'Catering', 9892145214, '8111 Birch Hill Avenue Ravenna, OH 44266');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Jackie Miller', '1978-08-06', 'Dry Cleaning', 'Dry Cleaning', 9795486234, '9895 Redwood Court Glenview, IL 60025');");
    		jdbc_statement.executeUpdate("INSERT INTO Staff "+
				"(Name, DOB, JobTitle, Dep, PhoneNum, Address) VALUES "+
				"('Jude Cole', '1979-03-07', 'Gym', 'Gym', 9195642251, '8512 Cambridge Ave. Lake In The Hills, IL 60156');");
         
    		System.out.println("Staff table loaded!");
    		
            // End transaction
            jdbc_connection.commit();
            jdbc_connection.setAutoCommit(true);
            
        }
        catch (Throwable err) {
            err.printStackTrace();
        }
        
    }
    
    /** 
     * Populate Hotels Table
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   03/07/18 -  ATTD -  Created method.
     *                  03/07/18 -  MTA -   Populated method.
     *                  03/08/18 -  ATTD -  Shifted some string constants purely for readability (no functional changes).
     *                  03/09/18 -  ATTD -  Calling method to insert hotels (which also update's new manager's staff info).
     */
    public static void populateHotelsTable() {
        
        try {
            
            // Start transaction
            jdbc_connection.setAutoCommit(false);
            
            // Populating data for Hotels
            updateInsertHotel("The Plaza", "768 5th Ave", "New York", "NY", 9194152368L, 1, false);
            updateInsertHotel("DoubleTree", "4810 Page Creek Ln", "Raleigh", "NC", 9192012364L, 9, false);
    		updateInsertHotel("Ramada", "1520 Blue Ridge Rd", "Raleigh", "NC", 9190174632L, 15, false);
    		updateInsertHotel("Embassy Suites", "201 Harrison Oaks Blvd", "Raleigh", "NC", 6502137942L, 21, false);
    		updateInsertHotel("Four Seasons", "57 E 57th St", "New York", "NY", 6501236874L, 27, false);
    		updateInsertHotel("The Pierre", "2 E 61st St", "New York", "NY", 6501836874L, 35, false);
    		updateInsertHotel("Fairfield Inn & Suites", "0040 Sellona St", "Raleigh", "NC", 6501236074L, 42, false);
    		updateInsertHotel("Mandarin Oriental", "80 Columbus Cir", "New York", "NY", 6591236874L, 49, false);
    		
    		System.out.println("Hotels table loaded!");
            
            // End transaction
            jdbc_connection.commit();
            jdbc_connection.setAutoCommit(true);
            
        }
        catch (Throwable err) {
            err.printStackTrace();
        }
        
    }
    
    /** 
     * Update Staff Table
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   03/07/18 -  MTA -   Created method.
     */
    public static void updateHotelIdForStaff() {
    	
    	 try {
             
    	     // Start transaction
             jdbc_connection.setAutoCommit(false);
             
             // Update(Assign) HotelId for Staff 
             jdbc_statement.executeUpdate("UPDATE Staff SET HotelID = 1 WHERE ID >=1 AND ID <=8;");
             jdbc_statement.executeUpdate("UPDATE Staff SET HotelID = 2 WHERE ID >=9 AND ID <=14;");
             jdbc_statement.executeUpdate("UPDATE Staff SET HotelID = 3 WHERE ID >=15 AND ID <=20;");
             jdbc_statement.executeUpdate("UPDATE Staff SET HotelID = 4 WHERE ID >=21 AND ID <=26;");
             jdbc_statement.executeUpdate("UPDATE Staff SET HotelID = 5 WHERE ID >=27 AND ID <=34;");
             jdbc_statement.executeUpdate("UPDATE Staff SET HotelID = 6 WHERE ID >=35 AND ID <=41;");
             jdbc_statement.executeUpdate("UPDATE Staff SET HotelID = 7 WHERE ID >=42 AND ID <=48;");
             jdbc_statement.executeUpdate("UPDATE Staff SET HotelID = 8 WHERE ID >=49 AND ID <=55;");
			
			System.out.println("Hotel Id's updated for Staff!");
             
             // End transaction
             jdbc_connection.commit();
             jdbc_connection.setAutoCommit(true);
             
         }
         catch (Throwable err) {
             err.printStackTrace();
         }
    }
        
    /** 
     * Populate Rooms Table
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   03/07/18 -  ATTD -  Created method.
     *                  03/07/18 -  MTA -   Populated method.
     *                  03/08/18 -  ATTD -  Shifted some string constants purely for readability (no functional changes).
     */
    public static void populateRoomsTable() {
        
        try {
            
            // Start transaction
            jdbc_connection.setAutoCommit(false);
            
            // Populating data for Rooms
    		jdbc_statement.executeUpdate("INSERT INTO Rooms "+
				" (RoomNum, HotelID, Category, MaxOcc, NightlyRate, DRSStaff, DCStaff) VALUES " +
				" (1, 1, 'ECONOMY', 3, 150, NULL, NULL);");
    		jdbc_statement.executeUpdate("INSERT INTO Rooms "+
				" (RoomNum, HotelID, Category, MaxOcc, NightlyRate, DRSStaff, DCStaff) VALUES " +
				" (2, 1, 'PRESIDENTIAL_SUITE', 4, 450, 3, 5);");
    		jdbc_statement.executeUpdate("INSERT INTO Rooms "+
				" (RoomNum, HotelID, Category, MaxOcc, NightlyRate, DRSStaff, DCStaff) VALUES " +
				" (3, 1, 'EXECUTIVE_SUITE', 4, 300, NULL, NULL);");
    		jdbc_statement.executeUpdate("INSERT INTO Rooms "+
				" (RoomNum, HotelID, Category, MaxOcc, NightlyRate, DRSStaff, DCStaff) VALUES " +
				" (1, 2, 'DELUXE', 3, 200, NULL, NULL);");
    		jdbc_statement.executeUpdate("INSERT INTO Rooms "+
				" (RoomNum, HotelID, Category, MaxOcc, NightlyRate, DRSStaff, DCStaff) VALUES " +
				" (2, 2, 'ECONOMY', 3, 125, NULL, NULL);");
    		jdbc_statement.executeUpdate("INSERT INTO Rooms "+
				" (RoomNum, HotelID, Category, MaxOcc, NightlyRate, DRSStaff, DCStaff) VALUES " +
				" (3, 2, 'EXECUTIVE_SUITE', 4, 250, NULL, NULL);");
    		jdbc_statement.executeUpdate("INSERT INTO Rooms "+
				" (RoomNum, HotelID, Category, MaxOcc, NightlyRate, DRSStaff, DCStaff) VALUES " +
				" (1, 3, 'PRESIDENTIAL_SUITE', 3, 550, 17, 18);");
    		jdbc_statement.executeUpdate("INSERT INTO Rooms "+
				" (RoomNum, HotelID, Category, MaxOcc, NightlyRate, DRSStaff, DCStaff) VALUES " +
				" (2, 3, 'ECONOMY', 2, 350, NULL, NULL);");
    		jdbc_statement.executeUpdate("INSERT INTO Rooms "+
				" (RoomNum, HotelID, Category, MaxOcc, NightlyRate, DRSStaff, DCStaff) VALUES " +
				" (3, 3, 'DELUXE', 3, 450, NULL, NULL);");
    		jdbc_statement.executeUpdate("INSERT INTO Rooms "+
				" (RoomNum, HotelID, Category, MaxOcc, NightlyRate, DRSStaff, DCStaff) VALUES " +
				" (1, 4, 'ECONOMY', 4, 100, NULL, NULL);");
    		jdbc_statement.executeUpdate("INSERT INTO Rooms "+
				" (RoomNum, HotelID, Category, MaxOcc, NightlyRate, DRSStaff, DCStaff) VALUES " +
				" (2, 4, 'EXECUTIVE_SUITE', 4, 250, NULL, NULL);");
    		jdbc_statement.executeUpdate("INSERT INTO Rooms "+
				" (RoomNum, HotelID, Category, MaxOcc, NightlyRate, DRSStaff, DCStaff) VALUES " +
				" (1, 5, 'DELUXE', 3, 300, NULL, NULL);");
    		jdbc_statement.executeUpdate("INSERT INTO Rooms "+
				" (RoomNum, HotelID, Category, MaxOcc, NightlyRate, DRSStaff, DCStaff) VALUES " +
				" (2, 5, 'EXECUTIVE_SUITE', 4, 400, NULL, NULL);");
    		jdbc_statement.executeUpdate("INSERT INTO Rooms "+
				" (RoomNum, HotelID, Category, MaxOcc, NightlyRate, DRSStaff, DCStaff) VALUES " +
				" (3, 5, 'PRESIDENTIAL_SUITE', 4, 500, 29, 30);");
    		jdbc_statement.executeUpdate("INSERT INTO Rooms "+
				" (RoomNum, HotelID, Category, MaxOcc, NightlyRate, DRSStaff, DCStaff) VALUES " +
				" (1, 6, 'ECONOMY', 2, 220, NULL, NULL);");
    		jdbc_statement.executeUpdate("INSERT INTO Rooms "+
		        " (RoomNum, HotelID, Category, MaxOcc, NightlyRate, DRSStaff, DCStaff) VALUES " +
				" (2, 6, 'DELUXE', 4, 350, NULL, NULL);");
    		jdbc_statement.executeUpdate("INSERT INTO Rooms "+
				" (RoomNum, HotelID, Category, MaxOcc, NightlyRate, DRSStaff, DCStaff) VALUES " +
				" (1, 7, 'ECONOMY', 2, 125, NULL, NULL);");
    		jdbc_statement.executeUpdate("INSERT INTO Rooms "+
				" (RoomNum, HotelID, Category, MaxOcc, NightlyRate, DRSStaff, DCStaff) VALUES " +
				" (2, 7, 'EXECUTIVE_SUITE', 4, 400, NULL, NULL);");
    		jdbc_statement.executeUpdate("INSERT INTO Rooms "+
				" (RoomNum, HotelID, Category, MaxOcc, NightlyRate, DRSStaff, DCStaff) VALUES " +
				" (1, 8, 'ECONOMY', 2, 200, NULL, NULL);");
    		jdbc_statement.executeUpdate("INSERT INTO Rooms "+
				" (RoomNum, HotelID, Category, MaxOcc, NightlyRate, DRSStaff, DCStaff) VALUES " +
				" (2, 8, 'DELUXE', 3, 250, NULL, NULL);");
    		jdbc_statement.executeUpdate("INSERT INTO Rooms "+
				" (RoomNum, HotelID, Category, MaxOcc, NightlyRate, DRSStaff, DCStaff) VALUES " +
				" (3, 8, 'EXECUTIVE_SUITE', 3, 300, NULL, NULL);");
    		jdbc_statement.executeUpdate("INSERT INTO Rooms "+
				" (RoomNum, HotelID, Category, MaxOcc, NightlyRate, DRSStaff, DCStaff) VALUES " +
				" (4, 8, 'PRESIDENTIAL_SUITE', 4, 450, 51, 53);");
            System.out.println("Rooms Table loaded!");
            
            // End transaction
            jdbc_connection.commit();
            jdbc_connection.setAutoCommit(true);
            
        }
        catch (Throwable err) {
            err.printStackTrace();
        }
        
    }
    
    /** 
     * Populate Stays Table
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   03/07/18 -  ATTD -  Created method.
     *                  03/07/18 -  MTA -   Populated method.
     *                  03/08/18 -  ATTD -  Shifted some string constants purely for readability (no functional changes).
     *                  03/09/18 -  ATTD -  Removed explicit setting of ID (this is auto incremented).
     *                  03/11/18 -  ATTD -  Added amount owed to Stays relation.
     */
    public static void populateStaysTable() {
        
        try {
            
            // Start transaction
            jdbc_connection.setAutoCommit(false);
            
            // Populating data for Stays
    		jdbc_statement.executeUpdate("INSERT INTO Stays "+
				" (StartDate, CheckInTime, RoomNum, HotelID, CustomerSSN, NumGuests, CheckOutTime, EndDate, AmountOwed, PaymentMethod, CardType, CardNumber, BillingAddress) VALUES "+ 
				" ('2018-01-12', '20:10:00', 1, 1, 555284568, 3, '10:00:00', '2018-01-20', 235.00, 'CARD', 'VISA', '4400123454126587', '7178 Kent St. Enterprise, AL 36330');");
    		jdbc_statement.executeUpdate("INSERT INTO Stays "+
				" (StartDate, CheckInTime, RoomNum, HotelID, CustomerSSN, NumGuests, CheckOutTime, EndDate, AmountOwed, PaymentMethod, CardType, CardNumber, BillingAddress) VALUES "+ 
				" ('2018-02-15', '10:20:00', 3, 2, 111038548, 2, '08:00:00', '2018-02-18', 275.00, 'CASH', NULL, NULL, '754 East Walt Whitman St. Hopkins, MN 55343');");
    		jdbc_statement.executeUpdate("INSERT INTO Stays "+
				" (StartDate, CheckInTime, RoomNum, HotelID, CustomerSSN, NumGuests, CheckOutTime, EndDate, AmountOwed, PaymentMethod, CardType, CardNumber, BillingAddress) VALUES "+ 
				" ('2018-03-01', '15:00:00', 1, 3, 222075875, 1, '13:00:00', '2018-03-05', 570.00, 'CARD', 'HOTEL', '1100214521684512', '178 Shadow Brook St. West Chicago, IL 60185');");
    		jdbc_statement.executeUpdate("INSERT INTO Stays "+
				" (StartDate, CheckInTime, RoomNum, HotelID, CustomerSSN, NumGuests, CheckOutTime, EndDate, AmountOwed, PaymentMethod, CardType, CardNumber, BillingAddress) VALUES "+ 
				" ('2018-02-20', '07:00:00', 2, 4, 333127845, 4, '15:00:00', '2018-02-27', 285.00, 'CARD', 'MASTERCARD', '4400124565874591', '802B Studebaker Drive Clinton Township, MI 48035');");
    		jdbc_statement.executeUpdate("INSERT INTO Stays "+
				" (StartDate, CheckInTime, RoomNum, HotelID, CustomerSSN, NumGuests, CheckOutTime, EndDate, AmountOwed, PaymentMethod, CardType, CardNumber, BillingAddress) VALUES "+ 
				" ('2018-03-05', '11:00:00', 3, 5, 444167216, 4, '08:00:00', '2018-03-12', 520.00, 'CARD', 'VISA', '4400127465892145', '83 Inverness Court Longwood, FL 32779');");
    		jdbc_statement.executeUpdate("INSERT INTO Stays "+
				" (StartDate, CheckInTime, RoomNum, HotelID, CustomerSSN, NumGuests, CheckOutTime, EndDate, AmountOwed, PaymentMethod, CardType, CardNumber, BillingAddress) VALUES "+ 
				" ('2018-03-01', '18:00:00', 1, 6, 666034568, 1, '23:00:00', '2018-03-01', 245.00, 'CASH', NULL, NULL, '55 Livingston Ave. Selden, NY 11784');");
    		jdbc_statement.executeUpdate("INSERT INTO Stays "+
				" (StartDate, CheckInTime, RoomNum, HotelID, CustomerSSN, NumGuests, CheckOutTime, EndDate, AmountOwed, PaymentMethod, CardType, CardNumber, BillingAddress) VALUES "+ 
				" ('2018-01-20', '06:00:00', 2, 7, 777021654, 3, '10:00:00', '2018-02-01', 435.00, 'CARD', 'HOTEL', '1100214532567845', '87 Gregory Street Lawndale, CA 90260');");
    		jdbc_statement.executeUpdate("INSERT INTO Stays "+
				" (StartDate, CheckInTime, RoomNum, HotelID, CustomerSSN, NumGuests, CheckOutTime, EndDate, AmountOwed, PaymentMethod, CardType, CardNumber, BillingAddress) VALUES "+ 
				" ('2018-02-14', '09:00:00', 4, 8, 888091545, 2, '10:00:00', '2018-02-18', 470.00, 'CARD', 'VISA', '4400178498564512', '34 Hall Ave. Cranberry Twp, PA 16066');"); 
    	    System.out.println("Stays table loaded!");
            
            // End transaction
            jdbc_connection.commit();
            jdbc_connection.setAutoCommit(true);
            
        }
        catch (Throwable err) {
            err.printStackTrace();
        }
        
    }
    
    /** 
     * Populate Provided Table
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   03/07/18 -  ATTD -  Created method.
     *                  03/07/18 -  MTA -   Populated method.
     *                  03/08/18 -  ATTD -  Shifted some string constants purely for readability (no functional changes).
     *                  03/09/18 -  ATTD -  Removed explicit setting of ID (this is auto incremented).
     */
    public static void populateProvidedTable() {
        
        try {
            
            // Start transaction
            jdbc_connection.setAutoCommit(false);
            
            // Populating data for Provided
    		jdbc_statement.executeUpdate("INSERT INTO Provided " + 
				" (StayID, StaffID, ServiceName) VALUES " +
				" (1, 7, 'GYM')");
    		jdbc_statement.executeUpdate("INSERT INTO Provided " + 
				" (StayID, StaffID, ServiceName) VALUES " +
				" (1, 5, 'CATERING')");
    		jdbc_statement.executeUpdate("INSERT INTO Provided " + 
				" (StayID, StaffID, ServiceName) VALUES " +
				" (2, 11, 'ROOM_SERVICE')");
    		jdbc_statement.executeUpdate("INSERT INTO Provided " + 
				" (StayID, StaffID, ServiceName) VALUES " +
				" (3, 19, 'DRY_CLEANING')");
    		jdbc_statement.executeUpdate("INSERT INTO Provided " + 
				" (StayID, StaffID, ServiceName) VALUES " +
				" (4, 26, 'GYM')");
    		jdbc_statement.executeUpdate("INSERT INTO Provided " + 
				" (StayID, StaffID, ServiceName) VALUES " +
				" (5, 32, 'DRY_CLEANING')");
    		jdbc_statement.executeUpdate("INSERT INTO Provided " + 
				" (StayID, StaffID, ServiceName) VALUES " +
				" (6, 38, 'ROOM_SERVICE')");
    		jdbc_statement.executeUpdate("INSERT INTO Provided " + 
				" (StayID, StaffID, ServiceName) VALUES " +
				" (7, 48, 'GYM')");
    		jdbc_statement.executeUpdate("INSERT INTO Provided " + 
				" (StayID, StaffID, ServiceName) VALUES " +
				" (8, 54, 'DRY_CLEANING')");
    		System.out.println("Provided table loaded!");
    		
            
            // End transaction
            jdbc_connection.commit();
            jdbc_connection.setAutoCommit(true);
            
        }
        catch (Throwable err) {
            err.printStackTrace();
        }
        
    }
    
    // PRINT
    
    /** 
     * Print all results from a given table
     * 
     * Arguments -  tableName - The table to print out
     * Return -     None
     * 
     * Modifications:   03/07/18 -  ATTD -  Created method.
     */
    public static void printEntireTable(String tableName) {

        try {
            
            System.out.println("\nEntries in the " + tableName + " table:\n");
            jdbc_result = jdbc_statement.executeQuery("SELECT * FROM " + tableName);
            printQueryResultSet(jdbc_result);
            
        }
        catch (Throwable err) {
            err.printStackTrace();
        }
        
    }
    
    /** 
     * Print query result set
     * Modified from, but inspired by: https://coderwall.com/p/609ppa/printing-the-result-of-resultset
     * 
     * Arguments -  resultSetToPrint -  The result set to print
     * Return -     None
     * 
     * Modifications:   03/07/18 -  ATTD -  Created method.
     *                  03/08/18 -  ATTD -  Made printout slightly prettier.
     *                  03/08/18 -  ATTD -  At the end, print number of records in result set.
     */
    public static void printQueryResultSet(ResultSet resultSetToPrint) {
        
        try {
            
            // Declare variables
            ResultSetMetaData metaData;
            String columnName;
            String tupleValue;
            int numColumns;
            int i;
            int numTuples;

            // Is there anything useful in the result set?
            if (jdbc_result.next()) {
                
                // Get metadata
                metaData = jdbc_result.getMetaData();
                numColumns = metaData.getColumnCount();
                
                // Print column headers
                for (i = 1; i <= numColumns; i++) {
                    columnName = metaData.getColumnName(i);
                    System.out.print(padRight(columnName, getNumPadChars(metaData,i)));
                }
                System.out.println("");
                System.out.println("");
                
                // Go through the result set tuple by tuple
                numTuples = 0;
                do {
                    for (i = 1; i <= numColumns; i++) {
                        tupleValue = jdbc_result.getString(i);
                        System.out.print(padRight(tupleValue, getNumPadChars(metaData,i)));
                    }
                    System.out.print("\n");
                    numTuples++;
                } while(jdbc_result.next());
                
                // Print number of records found
                System.out.println("");
                System.out.println("(" + numTuples + " entries)");
                System.out.println("");
                
            } else {
                // Tell the user that the result set is empty
                System.out.println("(no results)\n");
            }
            
        }
        catch (Throwable err) {
            err.printStackTrace();
        }
        
    }
    
    /** 
     * Figure out how many characters to include in a padded string for a given column of a given result,
     * based on the result set metadata
     * 
     * Arguments -  metaData -          Meta data for the result set
     *              colNum -            The column number
     * Return -     numPadChars -       The number of characters to include in a padded string
     * 
     * Modifications:   03/08/18 -  ATTD -  Created method.
     *                  03/09/18 -  ATTD -  Another minor tweak.
     */
    public static int getNumPadChars(ResultSetMetaData metaData, int colNum) {
        
        // Declare constants
        final int NUM_PAD_CHARS_STATE =         6;
        final int NUM_PAD_CHARS_NUMBER =        12;
        final int NUM_PAD_CHARS_DATE_TIME =     11;
        final int NUM_PAD_CHARS_DEFAULT =       30;
        final int NUM_PAD_CHARS_FULL_ADDRESS =  55;
        final int NUM_PAD_CHARS_NAME =          20;
        
        // Declare variables
        int numPadChars = NUM_PAD_CHARS_DEFAULT;
        String columnType;
        String columnName;
        
        try {

            columnType = metaData.getColumnTypeName(colNum);
            columnName = metaData.getColumnName(colNum);
            if (columnType == "INTEGER" || columnType == "BIGINT" || columnType == "DOUBLE") {
                numPadChars = NUM_PAD_CHARS_NUMBER;
            }
            else if (columnType == "DATE" || columnType == "TIME") {
                numPadChars = NUM_PAD_CHARS_DATE_TIME;
            }
            else if (columnName.equals("Address")) {
                numPadChars = NUM_PAD_CHARS_FULL_ADDRESS;
            }
            else if (columnName.equals("State")) {
                numPadChars = NUM_PAD_CHARS_STATE;
            }
            else if (columnName.equals("Name")) {
                numPadChars = NUM_PAD_CHARS_NAME;
            }
            
        }
        catch (Throwable err) {
            err.printStackTrace();
        }
        
        return numPadChars;
         
    }
    
    /** 
     * Pad a string with space characters to reach a given number of total characters
     * https://stackoverflow.com/questions/388461/how-can-i-pad-a-string-in-java/391978#391978
     * 
     * Arguments -  stringIn -          The result set to print
     *              numDesiredChars -   The desired number of characters in the padded result
     * Return -     stringOut -         The padded string
     * 
     * Modifications:   03/08/18 -  ATTD -  Created method.
     */
    public static String padRight(String stringIn, int numDesiredChars) {
        
        // Declare variables
        String stringOut = stringIn;
        
        try {
            // Pad string
            stringOut = String.format("%1$-" + numDesiredChars + "s", stringIn); 
        }
        catch (Throwable err) {
            err.printStackTrace();
        }
        
        return stringOut;
         
    }

    // MANAGE
    
    /** 
     * Management task: Add a new hotel
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   03/08/18 -  ATTD -  Created method.
     */
    public static void manageHotelAdd() {
        
        String errorMessage; 
        try {
            
            // Declare local variables
            String hotelName = "";
            String streetAddress = "";
            String city = "";
            String state = "";
            String phoneNumAsString = "";
            long phoneNum = 0;
            int managerID = 0;
            boolean okaySoFar = true;
            
            // Get name
            System.out.print("\nEnter the hotel name\n> ");
            hotelName = scanner.nextLine();
            // Don't know of a way to have DBMS check string length (CHECK not supported)
            if (hotelName.length() == 0) {
                System.out.println("Hotel name not entered (cannot add hotel)\n");
                okaySoFar = false;
            }
            
            // Get street address
            if (okaySoFar) {
                System.out.print("\nEnter the hotel's street address\n> ");
                streetAddress = scanner.nextLine();
                // Don't know of a way to have DBMS check string length (CHECK not supported)
                if (streetAddress.length() == 0) {
                    System.out.println("Street address not entered (cannot add hotel)\n");
                    okaySoFar = false;
                }
            }

            // Get city
            if (okaySoFar) {
                System.out.print("\nEnter the hotel's city\n> ");
                city = scanner.nextLine();
                // Don't know of a way to have DBMS check string length (CHECK not supported)
                if (city.length() == 0) {
                    System.out.println("City not entered (cannot add hotel)\n");
                    okaySoFar = false;
                }
            }
            
            // Get state
            if (okaySoFar) {
                System.out.print("\nEnter the hotel's state\n> ");
                state = scanner.nextLine();
                // Don't know of a way to have DBMS check string length (CHECK not supported)
                if (state.length() != 2) {
                    System.out.println("State '" + state + "' malformed, should have 2 letters (cannot add hotel)\n");
                    okaySoFar = false;
                }
            }

            // Get phone number
            if (okaySoFar) {
                System.out.print("\nEnter the hotel's phone number\n> ");
                phoneNumAsString = scanner.nextLine();
                if (phoneNumAsString.length() != 10) {
                    System.out.println("Phone number '" + phoneNumAsString + "' malformed, should have 10 digits (cannot add hotel)\n");
                    okaySoFar = false;
                }
                else {
                    phoneNum = Long.parseLong(phoneNumAsString);
                }
            }

            // Get manager
            if (okaySoFar) {
                System.out.print("\nEnter the hotel's manager's staff ID\n> ");
                managerID = Integer.parseInt(scanner.nextLine());
                // Don't know of a way to have DBMS check that manager isn't dedicated to a presidential suite (ASSERTION not supported)
                jdbc_result = jdbc_statement.executeQuery("SELECT Staff.ID, Staff.Name, Rooms.RoomNum, Rooms.hotelID " + 
                        "FROM Staff, Rooms WHERE Staff.ID = " + 
                        managerID + 
                        " AND (Rooms.DRSStaff = " + managerID + " OR Rooms.DCStaff = " + managerID + ")");
                
                if (jdbc_result.next()) {
                    System.out.println("\nThis manager cannot manage the '" + 
                            hotelName + 
                            "' hotel, because they are already dedicated to serving a presidential suite\n");
                    jdbc_result.beforeFirst();
                    printQueryResultSet(jdbc_result);
                    okaySoFar = false;
                }
            }

            // Okay, at this point everything else I can think of can be caught by a Java exception or a SQL exception
            if (okaySoFar) {
                updateInsertHotel(hotelName, streetAddress, city, state, phoneNum, managerID, true);
            }
            
        }
        catch (Throwable err) {
            
            // Handle non-SQL errors
            errorMessage = err.toString();
            if (errorMessage.contains("NumberFormatException")) {
                // Tried to enter a number that wasn't quite a number
                System.out.println("This needs to be a number (cannot add hotel)\n");
            }
            else {
                err.printStackTrace();
            }
        }
        
    }
    
    /** 
     * Management task: Delete a hotel
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   03/09/18 -  ATTD -  Created method.
     */
    public static void manageHotelDelete() {

        try {
            
            // Declare local variables
            long hotelID = 0;
            
            // Get name
            System.out.print("\nEnter the hotel ID\n> ");
            hotelID = Long.parseLong(scanner.nextLine());

            // Call method to actually interact with the DB
            updateDeleteHotel(hotelID, true);
            
        }
        catch (Throwable err) {
            err.printStackTrace();
        }
        
    }
    
    // UPDATES
    
    /** 
     * DB Update: Insert Hotel
     * 
     * Arguments -  hotelName -     The name of the hotel to insert
     *              streetAddress - The street address of the hotel to insert
     *              city -          The city in which the hotel is located
     *              state -         The state in which the hotel is located
     *              phoneNum -      The phone number of the hotel
     *              managerID -     The staff ID of the person to promote to manager of the new hotel
     *              reportSuccess - True if we should print success message to console (should be false for mass population of hotels)
     * Return -     None
     * 
     * Modifications:   03/09/18 -  ATTD -  Created method.
     */
    public static void updateInsertHotel (String hotelName, String streetAddress, String city, String state, long phoneNum, int managerID, boolean reportSuccess) {
        
        // Declare variables
        int hotelID;
        String errorMessage;
        
        try {
            
            // Start transaction
            jdbc_connection.setAutoCommit(false);
            
            try {
                
                // Insert the new hotel into the Hotels table
                jdbc_statement.executeUpdate("INSERT INTO Hotels "+
                    " (Name, StreetAddress, City, State, PhoneNum, ManagerID) VALUES " +
                    " ('" + hotelName + "', '" + streetAddress + "', '" + city + "', '" + state + "', " + phoneNum + ", " + managerID + ");");
                
                // We have made a staff member the manager of the hotel - we must update that staff member too
                jdbc_result = jdbc_statement.executeQuery("SELECT MAX(ID) FROM Hotels;");
                jdbc_result.next();
                hotelID = jdbc_result.getInt(1);
                jdbc_statement.executeUpdate("UPDATE Staff "+
                        " SET JobTitle = 'Manager', Dep = 'Manager', HotelID = " + hotelID + " WHERE ID = " + managerID + ";");            
                
                // If success, commit multiple updates
                jdbc_connection.commit();
                
                // Then, tell the user about the success
                if (reportSuccess) {
                    System.out.println("\n'" + hotelName + "' hotel added (hotel ID: " + hotelID + ")!\n");
                }
                
            }
            catch (Throwable err) {
                
                // Handle SQL errors
                errorMessage = err.toString();
                // UNIQUE constraint violated
                if (errorMessage.contains("UC_HMID")) {
                    // Tried to enter an ID for a manager that is already managing some other hotel
                    System.out.println("\nThis manager cannot manage the '" + 
                            hotelName + 
                            "' hotel, because they are already managing another hotel\n");
                    jdbc_result = jdbc_statement.executeQuery("SELECT ID, Name, hotelID FROM Staff WHERE ID = " + managerID);
                    printQueryResultSet(jdbc_result);
                }
                // FOREIGN KEY constraint violated
                else if (errorMessage.contains("FK_HMID")) {
                    // Tried to enter an ID for a manager that does not exist
                    System.out.println("\nThere is no staff member with the ID '" + managerID + "' (cannot add hotel)\n");
                }
                else {
                    err.printStackTrace();
                }
                
                // Roll back the entire transaction
                jdbc_connection.rollback();
                
            }
            finally {
                // Restore normal auto-commit mode
                jdbc_connection.setAutoCommit(true);
            }
            
        }
        catch (Throwable err) {
            err.printStackTrace();
        }
        
    }
    
    /** 
     * DB Update: Delete Hotel
     * 
     * Why does this method exist at all when it is so dead simple?
     * 1. To keep with the pattern of isolating methods that directly interact with the DBMS, 
     * from those that interact with the user (readability of code)
     * 2. In case in the future we find some need for mass deletes.
     * 
     * Arguments -  hotelID -       The ID of the hotel
     *              reportSuccess - True if we should print success message to console (should be false for mass population of hotels)
     * Return -     None
     * 
     * Modifications:   03/09/18 -  ATTD -  Created method.
     */
    public static void updateDeleteHotel (long hotelID, boolean reportSuccess) {

        try {

            /* Remove the hotel from the Hotels table
             * No need to explicitly set up a transaction, because only one SQL command is needed
             */
            jdbc_statement.executeUpdate("DELETE FROM Hotels "+
                "WHERE ID = " + hotelID);
            
            // Tell the user about the success
            if (reportSuccess) {
                System.out.println("\nHotel ID " + hotelID + " deleted!\n");
            }
            
        }
        catch (Throwable err) {
            err.printStackTrace();
        }
        
    }
    
    // MAIN
    
    /* MAIN function
     * 
     * Welcomes the user, states available commands, listens to and acts on user commands
     * 
     * Arguments -  None
     * Return -     None
     * 
     * Modifications:   03/07/18 -  ATTD -  Created method.
     *                  03/08/18 -  ATTD -  Add ability to print entire Provided table.
     *                  03/08/18 -  ATTD -  Add sub-menus (report, etc) off of main menu.
     *                  03/09/18 -  ATTD -  Add ability to delete a hotel.
     */
    public static void main(String[] args) {
        
        try {
        
            // Declare local variables
            boolean quit = false;
            String command;
            
            // Print welcome
            System.out.println("\nWelcome to Wolf Inns Hotel Management System");
            
            // Connect to database
            System.out.println("\nConnecting to database...");
            connectToDatabase();
            
            // Create tables
            System.out.println("\nCreating tables...");
            createTables();
            
            // Populate tables
            System.out.println("\nPopulating tables...");
            populateCustomersTable();
            populateServiceTypesTable();
            populateStaffTable();
            populateHotelsTable();
            updateHotelIdForStaff();
            populateRoomsTable();
            populateStaysTable();
            populateProvidedTable();
            
            // Print available commands
            printAvailableCommands(CMD_MAIN);
            
            // Watch for user input
            currentMenu = CMD_MAIN;
            scanner = new Scanner(System.in);
            while (quit == false) {
                System.out.print("> ");
                command = scanner.nextLine();
                switch (currentMenu) {
                    case CMD_MAIN:
                        // Check user's input (case insensitively)
                        switch (command.toUpperCase()) {
                            case CMD_REPORTS:
                                // Tell the user their options in this new menu
                                printAvailableCommands(CMD_REPORTS);
                                // Remember what menu we're in
                                currentMenu = CMD_REPORTS;
                                break;
                            case CMD_MANAGE:
                                // Tell the user their options in this new menu
                                printAvailableCommands(CMD_MANAGE);
                                // Remember what menu we're in
                                currentMenu = CMD_MANAGE;
                                break;
                            case CMD_QUIT:
                                quit = true;
                                break;
                            default:
                                // Remind the user about what commands are available
                                System.out.println("\nCommand not recognized");
                                printAvailableCommands(CMD_MAIN);
                                break;
                        }
                        break;
                    case CMD_REPORTS:
                        // Check user's input (case insensitively)
                        switch (command.toUpperCase()) {
                            case CMD_REPORT_HOTELS:
                                printEntireTable("Hotels");
                                break;
                            case CMD_REPORT_ROOMS:
                                printEntireTable("Rooms");
                                break;
                            case CMD_REPORT_STAFF:
                                printEntireTable("Staff");
                                break;
                            case CMD_REPORT_CUSTOMERS:
                                printEntireTable("Customers");
                                break;
                            case CMD_REPORT_STAYS:
                                printEntireTable("Stays");
                                break;
                            case CMD_REPORT_SERVICES:
                                printEntireTable("ServiceTypes");
                                break;
                            case CMD_REPORT_PROVIDED:
                                printEntireTable("Provided");
                                break;
                            case CMD_MAIN:
                                // Tell the user their options in this new menu
                                printAvailableCommands(CMD_MAIN);
                                // Remember what menu we're in
                                currentMenu = CMD_MAIN;
                                break;
                            default:
                                // Remind the user about what commands are available
                                System.out.println("\nCommand not recognized");
                                printAvailableCommands(CMD_REPORTS);
                                break;
                        }
                        break;
                    case CMD_MANAGE:
                        // Check user's input (case insensitively)
                        switch (command.toUpperCase()) {
                        case CMD_MANAGE_HOTEL_ADD:
                            manageHotelAdd();
                            break;
                        case CMD_MANAGE_HOTEL_DELETE:
                            manageHotelDelete();
                            break;
                        case CMD_MAIN:
                            // Tell the user their options in this new menu
                            printAvailableCommands(CMD_MAIN);
                            // Remember what menu we're in
                            currentMenu = CMD_MAIN;
                            break;
                        default:
                            // Remind the user about what commands are available
                            System.out.println("\nCommand not recognized");
                            printAvailableCommands(CMD_MANAGE);
                            break;
                        }
                        break;
                    default:
                        break;
                }
            }
            
            // Clean up
            scanner.close();
        
        }
        catch (Throwable err) {
            err.printStackTrace();
        }

    }

}
