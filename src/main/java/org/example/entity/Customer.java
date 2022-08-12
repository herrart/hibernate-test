package org.example.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers")
public class Customer {

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Purchase> purchases;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "name")
    @NotNull
    private String name;

    public Customer() {
    }

    public Customer(String name) {
        this.name = name;
        purchases = new ArrayList<>();
    }

    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public List<Purchase> getPurchases() {
        return purchases;
    }

    public void addPurchase(Item item) {
        this.purchases.add(new Purchase(item, this));
    }

    @Override
    public String toString() {
        return "Customer {" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
