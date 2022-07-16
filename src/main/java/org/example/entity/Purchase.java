package org.example.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Table(name = "purchases")
public class Purchase {
    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "date")
    @Temporal(TemporalType.TIME)
    @CreationTimestamp
    private Date date;

    @Column(name = "price")
    private int price;

    public Purchase(Item item, Customer customer) {
        this.item = item;
        this.customer = customer;
        this.price = item.getPrice();
    }

    public Purchase() {

    }

    @Override
    public String toString() {
        return "Purchase {" +
                "id=" + id +
                ", date=" + date +
                ", item=" + item +
                ", customer=" + customer +
                ", price=" + price +
                '}';
    }
}
