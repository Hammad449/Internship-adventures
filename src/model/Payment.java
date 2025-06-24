package model;

import jakarta.persistence.*;

@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    @Column(name = "amount_owed")
    private double amountOwed;

    @Column(name = "amount_paid")
    private double amountPaid;

    // Getters and setters
    public int getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getAmountOwed() { return amountOwed; }
    public void setAmountOwed(double amountOwed) { this.amountOwed = amountOwed; }

    public double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(double amountPaid) { this.amountPaid = amountPaid; }
}
