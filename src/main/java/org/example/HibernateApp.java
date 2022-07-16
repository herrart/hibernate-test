package org.example;

import jakarta.persistence.NoResultException;
import org.example.entity.Customer;
import org.example.entity.Item;
import org.example.entity.Purchase;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import org.hibernate.cfg.Configuration;

import java.util.List;
import java.util.Scanner;


public class HibernateApp {
    public static void main(String[] args) {
        SessionFactory factory = new Configuration()
                .configure("hibernate.cfg.xml")
                .addAnnotatedClass(Customer.class)
                .addAnnotatedClass(Item.class)
                .addAnnotatedClass(Purchase.class)
                .buildSessionFactory();

        try (Session session = factory.getCurrentSession()) {
            session.beginTransaction();
            List<Item> items = session.createQuery("select i from Item i", Item.class).getResultList();
            List<Customer> customers = session.createQuery("select c from Customer c", Customer.class).getResultList();
            customers.forEach(customer -> customer.addPurchase(items.get((int) (Math.random() * 3))));
            session.getTransaction().commit();
        }

        Scanner scanner = new Scanner(System.in);
        String command = "";

        while (!command.equals("exit")) {
            System.out.println("""
                    --------------------------------
                    1. Show all items
                    2. Show all customers
                    3. Show purchases of a customer
                    4. Show purchases of an item
                    5. Delete item
                    6. Delete customer
                    7. Make purchase
                    Type 'exit' to terminate
                    --------------------------------""");
            command = scanner.nextLine();

            switch (command) {
                case ("1"):
                    try (Session session = factory.getCurrentSession()) {
                        System.out.println("Items list:");
                        session.beginTransaction();
                        List<Item> items = session.createQuery("select i from Item i", Item.class).getResultList();
                        items.forEach(System.out::println);
                        session.getTransaction().commit();
                    }
                    break;

                case ("2"):
                    System.out.println("Customers list:");
                    try (Session session = factory.getCurrentSession()) {
                        session.beginTransaction();
                        List<Customer> customers = session.createQuery("select c from Customer c", Customer.class).getResultList();
                        customers.forEach(System.out::println);
                        session.getTransaction().commit();
                    }
                    break;

                case ("3"):
                    System.out.println("Input customer name");
                    String name = scanner.nextLine();
                    try (Session session = factory.getCurrentSession()) {
                        session.beginTransaction();
                        Customer customer = session.createQuery("SELECT c from Customer c WHERE c.name = :name", Customer.class).setParameter("name", name).getSingleResult();
                        customer.getPurchases().forEach(System.out::println);
                        session.getTransaction().commit();

                    } catch (NoResultException e) {
                        System.out.println("No customer with such name.");
                    }
                    break;

                case ("4"):
                    System.out.println("Input item name");
                    name = scanner.nextLine();
                    try (Session session = factory.getCurrentSession()) {
                        session.beginTransaction();
                        session.createQuery("SELECT i from Item i WHERE i.name = :name", Item.class).setParameter("name", name).getSingleResult().getPurchases().forEach(System.out::println);
                        session.getTransaction().commit();
                    } catch (NoResultException e) {
                        System.out.println("No item with such name.");
                    }
                    break;

                case ("5"):
                    System.out.println("Input item name");
                    name = scanner.nextLine();
                    try (Session session = factory.getCurrentSession()) {
                        session.beginTransaction();
                        Item item = session.createQuery("SELECT i from Item i WHERE i.name = :name", Item.class).setParameter("name", name).getSingleResult();
                        session.remove(item);
                        session.getTransaction().commit();
                    } catch (NoResultException e) {
                        System.out.println("No item with such name.");
                    }
                    break;

                case ("6"):
                    System.out.println("Input customer name");
                    name = scanner.nextLine();
                    try (Session session = factory.getCurrentSession()) {
                        session.beginTransaction();
                        Customer customer = session.createQuery("SELECT c from Customer c WHERE c.name = :name", Customer.class).setParameter("name", name).getSingleResult();
                        session.remove(customer);
                        session.getTransaction().commit();
                    } catch (NoResultException e) {
                        System.out.println("No customer with such name.");
                    }
                    break;

                case ("7"):
                    System.out.println("Input customer name and item name separated by ', ' like 'Name, Item'");
                    String[] params = scanner.nextLine().split(", ");
                    try (Session session = factory.getCurrentSession()) {
                        session.beginTransaction();
                        Customer customer = session.createQuery("SELECT c from Customer c WHERE c.name = :name", Customer.class).setParameter("name", params[0]).getSingleResult();
                        Item item = session.createQuery("SELECT i from Item i WHERE i.name = :name", Item.class).setParameter("name", params[1]).getSingleResult();
                        customer.addPurchase(item);
                        session.getTransaction().commit();
                    } catch (NoResultException e) {
                        System.out.println("Incorrect input.");
                    }

                case ("exit"):
                    break;

                default:
                    System.out.println("Wrong command.");
                    break;
            }
        }
    }
}
