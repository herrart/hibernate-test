package org.example.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "items")
public class Item {
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private List<Purchase> purchases;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "price", nullable = false)

    private int price;

    public Item() {
    }

    public Item(String name, int price) {
        this.name = name;
        this.price = price;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public List<Purchase> getPurchases() {
        return purchases;
    }


    @Override
    public String toString() {
        return "Item {" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                '}';
    }
}