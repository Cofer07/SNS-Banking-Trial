package com.example.snsbankingrestapi;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity // This tells Hibernate to make a table out of this class
public class Transaction {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private int ID;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Date date;

    public Date getDate() {
        return date;
    }

    public Transaction(Account sendingAccount, Account receivingAccount, Double amount) {
        this.sendingAccount = sendingAccount;
        this.receivingAccount = receivingAccount;
        this.amount = amount;
    }

    public Transaction() {

    }

    public int getID() {
        return ID;
    }

    private Double amount;

    private String description;

    @ManyToOne
    @JoinColumn(name = "sendingaccountid")
    private Account sendingAccount;

    @ManyToOne
    @JoinColumn(name = "receivingaccountid")
    private Account receivingAccount;

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
