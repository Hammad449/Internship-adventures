import java.sql.*;
import java.io.*;
import java.util.Scanner;

public class Main {

    private static final String URL = "jdbc:mysql://localhost:3306/db";
    private static final String USER = "root";
    private static final String PASSWORD = "root#123";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connected to MySQL!");

            // Step 1: Create the table if it doesn't exist
            executeSqlFile(conn, "src/create_table.sql");

            // Step 2: Insert from CSV
            String filePath = "payments.csv";
            readFromFileAndInsert(conn, filePath);

            // Step 3: Search loop
            while (true) {
                System.out.print("\nEnter a name to search (or type 'exit' to quit): ");
                String input = scanner.nextLine().trim();
                if (input.equalsIgnoreCase("exit")) {
                    System.out.println("Exiting.");
                    break;
                }
                getPaymentByName(conn, input);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Reads and executes SQL commands from a file
    private static void executeSqlFile(Connection conn, String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            StringBuilder sqlBuilder = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sqlBuilder.append(line).append("\n");
            }

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sqlBuilder.toString());
                System.out.println("Table check/creation complete.");
            }

        } catch (IOException | SQLException e) {
            System.err.println("Error executing SQL file: " + e.getMessage());
        }
    }

    // CSV reader
    private static void readFromFileAndInsert(Connection conn, String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    String name = parts[0].trim();
                    double owed = Double.parseDouble(parts[1].trim());
                    double paid = Double.parseDouble(parts[2].trim());
                    addPaymentRow(conn, name, owed, paid);
                } else {
                    System.out.println("Skipped invalid line: " + line);
                }
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private static void addPaymentRow(Connection conn, String name, double amountOwed, double amountPaid) throws SQLException {
        String sql = "INSERT IGNORE INTO payments (name, amount_owed, amount_paid) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setDouble(2, amountOwed);
            pstmt.setDouble(3, amountPaid);
            pstmt.executeUpdate();
            System.out.println("Inserted: " + name);
        }
    }

    private static void getPaymentByName(Connection conn, String name) throws SQLException {
        String sql = "SELECT * FROM payments WHERE name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    double owed = rs.getDouble("amount_owed");
                    double paid = rs.getDouble("amount_paid");
                    System.out.println("\n--- Payment Info ---");
                    System.out.println("Name: " + name);
                    System.out.println("Amount Owed: " + owed);
                    System.out.println("Amount Paid: " + paid);
                }
                if (!found) {
                    System.out.println("No records found for name: " + name);
                }
            }
        }
    }
}
