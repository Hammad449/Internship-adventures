import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import model.Payment;
import java.io.*;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in); // Global Scanner

    public static void main(String[] args) {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        System.out.println("Connected to MySQL via Hibernate!");

        readFromFileAndInsert(session, "payments.csv");
        tx.commit();

        while (true) {
            System.out.println("\n--- Menu ---");
            System.out.println("1. Search by id");
            System.out.println("2. Delete by ID");
            System.out.println("3. Update by ID");
            System.out.println("4. Search by Partial Name ");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    getPaymentById(HibernateUtil.getSessionFactory());
                    break;
                case "2":
                    deletePaymentById(sessionFactory);
                    break;
                case "3":
                    updatePaymentById(sessionFactory);
                    break;
                case "4": {
                    System.out.print("Enter a letter or part of a name to search: ");
                    String part = scanner.nextLine();
                    searchPaymentsByPartialName(sessionFactory, part);
                    break;
                }
                case "5":
                    System.out.println("Exiting.");
                    session.close();
                    sessionFactory.close();
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
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

    public static void getPaymentById(SessionFactory sessionFactory) {
        Session session = sessionFactory.openSession();

        System.out.print("Enter ID to search: ");
        int id = Integer.parseInt(scanner.nextLine());

        Payment payment = session.get(Payment.class, id);
        if (payment != null) {
            System.out.println("\n--- Payment Info ---");
            System.out.println("ID: " + payment.getId());
            System.out.println("Name: " + payment.getName());
            System.out.println("Amount Owed: " + payment.getAmountOwed());
            System.out.println("Amount Paid: " + payment.getAmountPaid());
        } else {
            System.out.println("No record found with ID: " + id);
        }

        session.close();
    }


    public static void deletePaymentById(SessionFactory sessionFactory) {
        Session session = sessionFactory.openSession();
        List<Payment> allPayments = session.createQuery("FROM Payment", Payment.class).getResultList();

        System.out.println("Available IDs:");
        for (Payment p : allPayments) {
            System.out.println("ID: " + p.getId() + ", Name: " + p.getName());
        }

        System.out.print("Enter ID to delete: ");
        int id = Integer.parseInt(scanner.nextLine());

        Transaction tx = session.beginTransaction();
        Payment payment = session.get(Payment.class, id);
        if (payment != null) {
            session.remove(payment);
            tx.commit();
            System.out.println("Deleted record with ID: " + id);
        } else {
            tx.rollback();
            System.out.println("No record found with ID: " + id);
        }

        session.close();
    }

    public static void updatePaymentById(SessionFactory sessionFactory) {
        Session session = sessionFactory.openSession();
        List<Payment> allPayments = session.createQuery("FROM Payment", Payment.class).getResultList();

        System.out.println("Available IDs:");
        for (Payment p : allPayments) {
            System.out.println("ID: " + p.getId() + ", Name: " + p.getName());
        }

        System.out.print("Enter ID to update: ");
        int id = Integer.parseInt(scanner.nextLine()); // use nextLine instead of nextInt

        Transaction tx = session.beginTransaction();
        Payment payment = session.get(Payment.class, id);
        if (payment != null) {
            System.out.print("Enter new name: ");
            String newName = scanner.nextLine(); // this works now
            payment.setName(newName);

            System.out.print("Enter new amount owed: ");
            double newOwed = Double.parseDouble(scanner.nextLine());

            System.out.print("Enter new amount paid: ");
            double newPaid = Double.parseDouble(scanner.nextLine());

            payment.setAmountOwed(newOwed);
            payment.setAmountPaid(newPaid);

            session.merge(payment);
            tx.commit();

            System.out.println("Updated record with ID: " + id);
        } else {
            tx.rollback();
            System.out.println("No record found with ID: " + id);
        }

        session.close();
    }

    public static void searchPaymentsByPartialName(SessionFactory sessionFactory, String partial) {
        try (Session session = sessionFactory.openSession()) {
            List<Payment> payments = session.createQuery("FROM Payment WHERE name LIKE :partial", Payment.class)
                    .setParameter("partial", partial)
                    .getResultList();

            if (payments.isEmpty()) {
                System.out.println("No records found with name containing: " + partial);
            } else {
                System.out.println("\n--- Search Results ---");
                for (Payment payment : payments) {
                    System.out.println("ID: " + payment.getId() + ", Name: " + payment.getName() +
                            ", Amount Owed: " + payment.getAmountOwed() +
                            ", Amount Paid: " + payment.getAmountPaid());
                }
            }
        }
    }
}
