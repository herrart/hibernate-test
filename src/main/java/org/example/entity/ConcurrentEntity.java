package org.example.entity;

import jakarta.persistence.*;
@Entity
@Table(name = "concurrent_items")
public class ConcurrentEntity {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "val")
    private Integer value;

    public ConcurrentEntity() {
        this.value = 0;
    }
    public void incrementValue() {
        value++;
    }
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
