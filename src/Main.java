import org.hibernate.Session;
import org.hibernate.Transaction;
import model.Payment;
import java.io.*;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Transaction tx = session.beginTransaction();

            System.out.println("Connected to MySQL via Hibernate!");

            // Step 1: Hibernate auto-creates the table

            // Step 2: Insert from CSV
            readFromFileAndInsert(session, "payments.csv");

            tx.commit();

            // Step 3: Search loop
            while (true) {
                System.out.print("\nEnter a name to search (or type 'exit' to quit): ");
                String input = scanner.nextLine().trim();
                if (input.equalsIgnoreCase("exit")) {
                    System.out.println("Exiting.");
                    break;
                }
                getPaymentByName(session, input);
            }

            session.close();
        }
    }

    private static void readFromFileAndInsert(Session session, String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    String name = parts[0].trim();
                    double owed = Double.parseDouble(parts[1].trim());
                    double paid = Double.parseDouble(parts[2].trim());

                    // Check for existing name
                    List<Payment> existing = session
                            .createQuery("FROM Payment WHERE name = :name", Payment.class)
                            .setParameter("name", name)
                            .getResultList();

                    if (existing.isEmpty()) {
                        Payment payment = new Payment();
                        payment.setName(name);
                        payment.setAmountOwed(owed);
                        payment.setAmountPaid(paid);

                        session.persist(payment);
                        System.out.println("Inserted: " + name);
                    } else {
                        System.out.println("Skipped duplicate: " + name);
                    }
                } else {
                    System.out.println("Skipped invalid line: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void getPaymentByName(Session session, String name) {
        List<Payment> results = session
                .createQuery("FROM Payment WHERE name = :name", Payment.class)
                .setParameter("name", name)
                .getResultList();

        if (results.isEmpty()) {
            System.out.println("No records found for name: " + name);
        } else {
            for (Payment p : results) {
                System.out.println("\n--- Payment Info ---");
                System.out.println("Name: " + p.getName());
                System.out.println("Amount Owed: " + p.getAmountOwed());
                System.out.println("Amount Paid: " + p.getAmountPaid());
            }
        }
    }
}
