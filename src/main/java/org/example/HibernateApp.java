package org.example;

import jakarta.persistence.LockModeType;
import jakarta.persistence.NoResultException;
import org.example.entity.ConcurrentEntity;
import org.example.entity.Customer;
import org.example.entity.Item;
import org.example.entity.Purchase;
import org.example.repository.CustomerRepository;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;


public class HibernateApp {
    public static void main(String[] args) {
        SessionFactory factory = new Configuration()
                .configure("hibernate.cfg.xml")
                .addAnnotatedClass(Customer.class)
                .addAnnotatedClass(Item.class)
                .addAnnotatedClass(Purchase.class)
                .addAnnotatedClass(ConcurrentEntity.class)
                .buildSessionFactory();
        CustomerRepository customerRepository = new CustomerRepository(factory);

        try (Session session = factory.getCurrentSession()) {
            session.beginTransaction();
            List<Item> items = session.createQuery("select i from Item i", Item.class).getResultList();
            List<Customer> customers = session.createQuery("select c from Customer c", Customer.class).getResultList();
            customers.forEach(customer -> customer.addPurchase(items.get((int) (Math.random() * 3))));
            session.getTransaction().commit();
        }

        Scanner scanner = new Scanner(System.in);
        String command = "";
        Customer customer;
        List<Customer> customers;

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
                    8. Concurrent pessimistic lock write
                    9. There will be an optimistic lock
                    Type 'exit' to terminate
                    --------------------------------""");

            command = scanner.nextLine();

            switch (command) {
                case ("1"):
                    try (Session session = factory.getCurrentSession()) {
                        System.out.println("Items list:");
                        session.beginTransaction();
                        session.createQuery("select i from Item i", Item.class)
                                .getResultList()
                                .forEach(System.out::println);
                        session.getTransaction().commit();
                    }
                    break;

                case ("2"):
                    System.out.println("Customers list:");
                    customers = customerRepository.getAll();
                    if (customers.isEmpty()) {
                        System.out.println("There is no customers");
                    } else {
                        customers.forEach(System.out::println);
                    }
                    break;

                case ("3"):
                    System.out.println("Input customer name");
                    command = scanner.nextLine();
                    customer = customerRepository.findByName(command);
                    if (customer.getId() == 0) {
                        System.out.println("No customer with such name.");
                    } else {
                        if (customer.getPurchases().isEmpty()) {
                            System.out.println("The customer has no purchases.");
                        }
                        customer.getPurchases().forEach(System.out::println);
                    }

                    break;

                case ("4"):
                    System.out.println("Input item name");
                    command = scanner.nextLine();
                    try (Session session = factory.getCurrentSession()) {
                        session.beginTransaction();
                        session.createQuery("SELECT i from Item i WHERE i.name = :name", Item.class)
                                .setParameter("name", command)
                                .getSingleResult()
                                .getPurchases()
                                .forEach(System.out::println);
                        session.getTransaction().commit();
                    } catch (NoResultException e) {
                        System.out.println("No item with such name.");
                    }
                    break;

                case ("5"):
                    System.out.println("Input item name to delete");
                    command = scanner.nextLine();
                    try (Session session = factory.getCurrentSession()) {
                        session.beginTransaction();
                        Item item = session.createQuery("SELECT i from Item i WHERE i.name = :name", Item.class)
                                .setParameter("name", command)
                                .getSingleResult();
                        session.remove(item);
                        session.getTransaction().commit();
                        System.out.println("Item " + command + " removed.");
                    } catch (NoResultException e) {
                        System.out.println("No item with such name.");
                    }
                    break;

                case ("6"):
                    System.out.println("Input customer name to delete");
                    command = scanner.nextLine();
                    try (Session session = factory.getCurrentSession()) {
                        session.beginTransaction();
                        customer = session.createQuery("SELECT c from Customer c WHERE c.name = :name", Customer.class)
                                .setParameter("name", command)
                                .getSingleResult();
                        session.remove(customer);
                        session.getTransaction().commit();
                        System.out.println("Customer " + command + " removed.");
                    } catch (NoResultException e) {
                        System.out.println("No customer with such name.");
                    }
                    break;

                case ("7"):
                    System.out.println("Input customer name and item name separated by ', ' like 'Name, Item'");
                    String[] params = scanner.nextLine().split(", ");
                    try (Session session = factory.getCurrentSession()) {
                        session.beginTransaction();
                        customer = session.createQuery("SELECT c from Customer c WHERE c.name = :name", Customer.class)
                                .setParameter("name", params[0])
                                .getSingleResult();
                        Item item = session.createQuery("SELECT i from Item i WHERE i.name = :name", Item.class)
                                .setParameter("name", params[1])
                                .getSingleResult();
                        customer.addPurchase(item);
                        session.getTransaction().commit();
                        System.out.println("Added " + params[1] + " to " + params[0] + ".");
                    } catch (NoResultException e) {
                        System.out.println("Incorrect input.");
                    }
                    break;

                case ("8"):
                    pessimisticWriteLock(factory);
                    break;

                /*case ("9"):
                    oneThreadWrite(factory);
                    break;*/

                case ("exit"):
                    break;

                default:
                    System.out.println("Wrong command.");
            }
        }
    }

    public static void pessimisticWriteLock(SessionFactory factory) {


        Session session = factory.getCurrentSession();
        session.beginTransaction();
        Stream.generate(ConcurrentEntity::new).limit(40).forEach(session::persist);
        session.getTransaction().commit();

        CountDownLatch count = new CountDownLatch(8);

        Runnable runnable = () -> {

            try {
                int rnd;

                for (int i = 0; i < 1000; i++) {

                    if (Thread.interrupted()) return;

                    rnd = (int) (Math.random() * 40) + 1;
                    //System.out.println(rnd);
                    Session runnableSession = factory.getCurrentSession();
                    runnableSession.beginTransaction();
                    runnableSession.createQuery("select c from ConcurrentEntity c where c.id = :id", ConcurrentEntity.class)
                            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                            .setParameter("id", rnd)
                            .getSingleResult()
                            .incrementValue();
                    try {
                        Thread.sleep(5);

                    } catch (InterruptedException e) {
                        runnableSession.getTransaction().rollback();
                        e.printStackTrace();
                        return;
                    }
                    runnableSession.getTransaction().commit();
                }

                count.countDown();
                System.out.println(count.getCount());
            } catch (Throwable t) {
                t.printStackTrace();
            }
        };

        long timeCounter = System.currentTimeMillis();

        ExecutorService executorService = Executors.newFixedThreadPool(8);
        for (int i = 0; i < 8; i++) {
            executorService.submit(runnable);
        }
        executorService.shutdown();

        try {
            count.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Pessimistic lock took " + (System.currentTimeMillis() - timeCounter));


    }

    /*public static void oneThreadWrite(SessionFactory factory) {
        Session session = factory.getCurrentSession();
        session.beginTransaction();
        session.createNativeQuery("DELETE from concurrent_items", ConcurrentEntity.class).executeUpdate();
        session.getTransaction().commit();

        long timeCounter = System.currentTimeMillis();


        System.out.println("One thread write took " + (System.currentTimeMillis() - timeCounter));

    }*/
}
