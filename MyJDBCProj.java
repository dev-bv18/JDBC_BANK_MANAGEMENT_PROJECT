import java.sql.*;
import java.util.Scanner;

public class MyJDBCProj {
    public static void main(String args[]) {
        Connection con = null;
        Statement stmt = null;

        try {
            // Load the Oracle JDBC driver
            Class.forName("oracle.jdbc.driver.OracleDriver");

            // Create the connection object
            String conurl = "jdbc:oracle:thin:@localhost:1521:xe";
            con = DriverManager.getConnection(conurl, "SYSTEM", "baibhabdbms");
            stmt = con.createStatement();

            // Menu-driven program loop
            Scanner scanner = new Scanner(System.in);
            boolean exit = false;
            while (!exit) {
                System.out.println("\n\n***** Banking Management System *****");
                System.out.println("1. Show Customer Records");
                System.out.println("2. Add Customer Record");
                System.out.println("3. Delete Customer Record");
                System.out.println("4. Update Customer Information");
                System.out.println("5. Show Account Details of a Customer");
                System.out.println("6. Show Loan Details of a Customer");
                System.out.println("7. Deposit Money to an Account");
                System.out.println("8. Withdraw Money from an Account");
                System.out.println("9. Exit");
                System.out.println("10. To Create an Account or Apply for Loan");

                System.out.print("Enter your choice (1-10): ");
                int choice = scanner.nextInt();

                switch (choice) {
                    case 1:
                        // Show Customer Records
                        showCustomerRecords(stmt);
                        break;
                    case 2:
                        // Add Customer Record
                        addCustomerRecord(scanner, stmt);
                        break;
                    case 3:
                        // Delete Customer Record
                        deleteCustomerRecord(scanner, stmt);
                        break;
                    case 4:
                        // Update Customer Information
                        updateCustomerInformation(scanner, stmt);
                        break;
                    case 5:
                        // Show Account Details of a Customer
                        showAccountDetails(scanner, stmt);
                        break;
                    case 6:
                        // Show Loan Details of a Customer
                        showLoanDetails(scanner, stmt);
                        break;
                    case 7:
                        // Deposit Money to an Account
                        depositMoney(scanner, stmt);
                        break;
                    case 8:
                        // Withdraw Money from an Account
                        withdrawMoney(scanner, stmt);
                        break;
                    case 9:
                        // Exit the program
                        exit = true;
                        System.out.println("Exiting the program...");
                        break;
                    case 10:
                        openAccOrLoan(scanner,stmt);
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter a number between 1 and 9.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static void openAccOrLoan(Scanner scanner, Statement stmt) throws SQLException {
        while (true) {
            System.out.println("\n***** Open Account or Apply for Loan *****");
            System.out.println("1. Open an Account");
            System.out.println("2. Apply for Loan");
            System.out.println("3. Back to Main Menu");

            System.out.print("Enter your choice (1-3): ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    openAccount(scanner, stmt);
                    break;
                case 2:
                    applyForLoan(scanner, stmt);
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid choice. Please enter a number between 1 and 3.");
            }
        }
    }

    private static void openAccount(Scanner scanner, Statement stmt) throws SQLException {
        System.out.println("\n***** Open Account *****");
        System.out.print("Enter Account Number: ");
        String accountNo = scanner.next();
        System.out.print("Enter Customer Number: ");
        String custNo = scanner.next();

        // Check if the provided customer number exists in the Customer table
        ResultSet customerCheck = stmt.executeQuery("SELECT * FROM Customer WHERE cust_no = '" + custNo + "'");
        if (!customerCheck.next()) {
            System.out.println("Error: Customer with number " + custNo + " does not exist.");
            return; // Exit the method if customer doesn't exist
        }

        System.out.print("Enter Account Type: ");
        String accType = scanner.next();
        System.out.print("Enter Account Balance: ");
        double balance = scanner.nextDouble();
        System.out.print("Enter Branch Code: ");
        String branchCode = scanner.next();
        System.out.print("Enter Branch Name: ");
        String branchName = scanner.next();
        System.out.print("Enter Branch City: ");
        String branchCity = scanner.next();

        String query = "INSERT INTO Account VALUES ('" + accountNo + "', '" + custNo + "', '" + accType + "', " +
                balance + ", '" + branchCode + "', '" + branchName + "', '" + branchCity + "')";
        int rowsAffected = stmt.executeUpdate(query);

        if (rowsAffected > 0) {
            System.out.println("Account opened successfully.");
        } else {
            System.out.println("Failed to open account.");
        }
    }

    private static void applyForLoan(Scanner scanner, Statement stmt) throws SQLException {
        System.out.println("\n***** Apply for Loan *****");
        System.out.print("Enter Loan Number: ");
        String loanNo = scanner.next();
        System.out.print("Enter Customer Number: ");
        String custNo = scanner.next();

        // Check if the provided customer number exists in the Account table
        ResultSet accountCheck = stmt.executeQuery("SELECT * FROM Account WHERE cust_no = '" + custNo + "'");
        if (!accountCheck.next()) {
            System.out.println("Error: Customer with number " + custNo + " does not have an account in this bank.");
            return; // Exit the method if customer doesn't have an account
        }

        System.out.print("Enter Loan Amount: ");
        double loanAmount = scanner.nextDouble();
        System.out.print("Enter Branch Code: ");
        String branchCode = scanner.next();
        System.out.print("Enter Branch Name: ");
        String branchName = scanner.next();
        System.out.print("Enter Branch City: ");
        String branchCity = scanner.next();

        // Update the account balance to reflect the loan amount
        double newBalance = getUpdatedBalance(stmt, custNo, loanAmount);

        // Insert the loan record
        String query = "INSERT INTO Loan VALUES ('" + loanNo + "', '" + custNo + "', " + loanAmount + ", '" +
                branchCode + "', '" + branchName + "', '" + branchCity + "')";
        int rowsAffected = stmt.executeUpdate(query);

        if (rowsAffected > 0) {
            System.out.println("Loan applied successfully.");
            System.out.println("New Account Balance: " + newBalance);
        } else {
            System.out.println("Failed to apply for loan.");
        }
    }

    private static double getUpdatedBalance(Statement stmt, String custNo, double loanAmount) throws SQLException {
        // Get the current account balance
        ResultSet rs = stmt.executeQuery("SELECT balance FROM Account WHERE cust_no = '" + custNo + "'");
        rs.next();
        double currentBalance = rs.getDouble("balance");

        // Update the balance with the loan amount
        double newBalance = currentBalance + loanAmount;

        // Update the balance in the database
        stmt.executeUpdate("UPDATE Account SET balance = " + newBalance + " WHERE cust_no = '" + custNo + "'");

        rs.close();

        return newBalance;
    }

    private static void showCustomerRecords(Statement stmt) throws SQLException {
        ResultSet rs = stmt.executeQuery("SELECT * FROM customer order by cust_no");
        System.out.println("Customer Records:");
        while (rs.next()) {
            System.out.println("Cust No: " + rs.getString("cust_no") +
                    ", Name: " + rs.getString("name") +
                    ", Phone No: " + rs.getString("phoneno") +
                    ", City: " + rs.getString("city"));
        }
        rs.close();
    }

    private static void addCustomerRecord(Scanner scanner, Statement stmt) throws SQLException {
        System.out.println("Enter Customer No:");
        String custNo = scanner.nextLine(); // Consume newline
        custNo = scanner.nextLine(); // Read actual input

        System.out.println("Enter Name:");
        String name = scanner.nextLine();

        System.out.println("Enter Phone No:");
        String phoneNo = scanner.nextLine();

        System.out.println("Enter City:");
        String city = scanner.nextLine();

        String query = "INSERT INTO customer VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = stmt.getConnection().prepareStatement(query)) {
            pstmt.setString(1, custNo);
            pstmt.setString(2, name);
            pstmt.setString(3, phoneNo);
            pstmt.setString(4, city);

            int rowsAffected = pstmt.executeUpdate();
            System.out.println(rowsAffected > 0 ? "Customer record added successfully." : "Failed to add customer record.");
        }
    }


    public static void deleteCustomerRecord(Scanner scanner, Statement stmt) throws SQLException {
        System.out.println("Enter Customer No to delete:");
        String custNo = scanner.next();

        // Delete associated loan records
        String deleteLoanQuery = "DELETE FROM loan WHERE cust_no='" + custNo + "'";
        int loanRowsAffected = stmt.executeUpdate(deleteLoanQuery);

        // Delete associated account records
        String deleteAccountQuery = "DELETE FROM account WHERE cust_no='" + custNo + "'";
        int accountRowsAffected = stmt.executeUpdate(deleteAccountQuery);

        // Delete customer record
        String deleteCustomerQuery = "DELETE FROM customer WHERE cust_no='" + custNo + "'";
        int customerRowsAffected = stmt.executeUpdate(deleteCustomerQuery);

        if (customerRowsAffected > 0) {
            System.out.println("Customer record deleted successfully.");
        } else {
            System.out.println("No customer found with the given customer number.");
        }
    }

    private static void updateCustomerInformation(Scanner scanner, Statement stmt) throws SQLException {
        System.out.println("Enter Customer No to update:");
        String custNo = scanner.nextLine(); // Consume newline
        custNo = scanner.nextLine(); // Read actual input

        ResultSet customerCheck = stmt.executeQuery("SELECT * FROM customer WHERE cust_no = '" + custNo + "'");
        if (!customerCheck.next()) {
            System.out.println("Error: Customer with number " + custNo + " does not exist.");
            return;
        }

        System.out.println("Enter new Name:");
        String newName = scanner.nextLine();

        System.out.println("Enter new Phone No:");
        String newPhoneNo = scanner.nextLine();

        System.out.println("Enter new City:");
        String newCity = scanner.nextLine();

        String query = "UPDATE customer SET name = ?, phoneno = ?, city = ? WHERE cust_no = ?";
        try (PreparedStatement pstmt = stmt.getConnection().prepareStatement(query)) {
            pstmt.setString(1, newName);
            pstmt.setString(2, newPhoneNo);
            pstmt.setString(3, newCity);
            pstmt.setString(4, custNo);

            int rowsAffected = pstmt.executeUpdate();
            System.out.println(rowsAffected > 0 ? "Customer information updated successfully." : "No customer found with the given customer number.");
        }
    }


    private static void showAccountDetails(Scanner scanner, Statement stmt) throws SQLException {
        System.out.println("Enter Customer No:");
        String custNo = scanner.next();

        String query = "SELECT * FROM account WHERE cust_no = '" + custNo + "'";
        ResultSet rs = stmt.executeQuery(query);

        if (!rs.next()) {
            System.out.println("No account found for the given customer number.");
        } else {
            System.out.println("Account Details for Customer " + custNo + ":");
            do {
                System.out.println("Account No: " + rs.getString("account_no") +
                        ", Customer No: " + rs.getString("cust_no") +
                        ", Type: " + rs.getString("type") +
                        ", Balance: " + rs.getString("balance") +
                        ", Branch Code: " + rs.getString("branch_code") +
                        ", Branch Name: " + rs.getString("branch_name") +
                        ", Branch City: " + rs.getString("branch_city"));
            } while (rs.next());
        }
        rs.close();
    }

    private static void showLoanDetails(Scanner scanner, Statement stmt) throws SQLException {
        System.out.println("Enter Customer No:");
        String custNo = scanner.next();

        String query = "SELECT * FROM loan WHERE cust_no = '" + custNo + "'";
        ResultSet rs = stmt.executeQuery(query);

        if (!rs.next()) {
            System.out.println("No loan found for the given customer number.");
        } else {
            System.out.println("Loan Details for Customer " + custNo + ":");
            do {
                System.out.println("Loan No: " + rs.getString("loan_no") +
                        ", Loan Amount: " + rs.getString("loan_amount") +
                        ", Branch Code: " + rs.getString("branch_code") +
                        ", Branch Name: " + rs.getString("branch_name") +
                        ", Branch City: " + rs.getString("branch_city"));
            } while (rs.next());
        }
        rs.close();
    }

    private static void depositMoney(Scanner scanner, Statement stmt) throws SQLException {
        System.out.println("Enter Account No:");
        String accountNo = scanner.next();
        System.out.println("Enter Amount to Deposit:");
        double amount = scanner.nextDouble();

        String query = "UPDATE account SET balance = balance + " + amount + " WHERE account_no = '" + accountNo + "'";
        int rowsAffected = stmt.executeUpdate(query);

        if (rowsAffected > 0) {
            System.out.println("Deposit successful.");
        } else {
            System.out.println("No account found with the given account number.");
        }
    }

    private static void withdrawMoney(Scanner scanner, Statement stmt) throws SQLException {
        System.out.println("Enter Account No:");
        String accountNo = scanner.next();
        System.out.println("Enter Amount to Withdraw:");
        double amount = scanner.nextDouble();

        String query = "SELECT balance FROM account WHERE account_no = '" + accountNo + "'";
        ResultSet rs = stmt.executeQuery(query);

        if (rs.next()) {
            double balance = rs.getDouble("balance");
            if (balance >= amount) {
                query = "UPDATE account SET balance = balance - " + amount + " WHERE account_no = '" + accountNo + "'";
                int rowsAffected = stmt.executeUpdate(query);
                if (rowsAffected > 0) {
                    System.out.println("Withdrawal successful.");
                } else {
                    System.out.println("Failed to update balance.");
                }
            } else {
                System.out.println("Insufficient balance.");
            }
        } else {
            System.out.println("No account found with the given account number.");
        }
        rs.close();
    }
}
