package org.example.repository;

import jakarta.persistence.NoResultException;
import org.example.entity.Customer;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;

public class CustomerRepository {
    private final SessionFactory factory;

    public CustomerRepository(SessionFactory factory) {
        this.factory = factory;
    }

    public Customer findByName(String name) {
        try (Session session = factory.getCurrentSession()) {
            session.beginTransaction();
            // TODO Fix NoResultException when customer has no purchases
            Customer customer = session.createQuery("SELECT c from Customer c JOIN FETCH c.purchases WHERE c.name = :name", Customer.class)
                    .setParameter("name", name)
                    .getSingleResult();
            session.getTransaction().commit();
            return customer;
        } catch (NoResultException e) {
            e.printStackTrace();
            return new Customer();
        }
    }

    public List<Customer> getAll() {
        try (Session session = factory.getCurrentSession()) {
            session.beginTransaction();
            List<Customer> customers = session.createQuery("select c from Customer c", Customer.class)
                    .getResultList();
            session.getTransaction().commit();
            return customers;
        }
    }

    public void addOne(Customer customer) {
        try (Session session = factory.getCurrentSession()) {
            session.beginTransaction();
            session.persist(customer);
            session.getTransaction().commit();
        }
    }
}
