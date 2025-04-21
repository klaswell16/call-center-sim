import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.*;

import static java.lang.Thread.sleep;

public class CallCenter {

    private static final int CUSTOMERS_PER_AGENT = 5;
    private static final int NUMBER_OF_AGENTS = 3;
    private static final int NUMBER_OF_CUSTOMERS = NUMBER_OF_AGENTS * CUSTOMERS_PER_AGENT;
    private static final int NUMBER_OF_THREADS = 10;


    private static final BlockingQueue<Customer> waitQueue = new LinkedBlockingQueue<>();
    private static final BlockingQueue<Customer> serveQueue = new LinkedBlockingQueue<>();

    public static class Agent implements Runnable {
        private final int ID;

        public Agent(int i) {
            ID = i;
        }

        public void serve(int customerID) {
            System.out.println("Agent " + ID + " is serving customer " + customerID);
            try {
                sleep(ThreadLocalRandom.current().nextInt(10, 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            int customersServed = 0;
            while (customersServed < CUSTOMERS_PER_AGENT) {
                try {
                    Customer customer = serveQueue.take();
                    serve(customer.ID);
                    customersServed++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static class Greeter implements Runnable {
        public void greet(int customerID) {
            System.out.println("Greeting customer " + customerID);
            try {
                sleep(ThreadLocalRandom.current().nextInt(10, 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            int customersGreeted = 0;
            while (customersGreeted < NUMBER_OF_CUSTOMERS) {
                try {
                    Customer customer = waitQueue.take();
                    greet(customer.ID);
                    serveQueue.put(customer);
                    System.out.println("Customer " + customer.ID + " is now in serve queue at position " + serveQueue.size());
                    customersGreeted++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static class Customer implements Runnable {
        private final int ID;

        public Customer(int i) {
            ID = i;
        }

        public void run() {
            try {
                waitQueue.put(this);
                System.out.println("Customer " + ID + " has entered the wait queue");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {

        ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);


        executor.submit(new Greeter());


        for (int i = 0; i < NUMBER_OF_AGENTS; i++) {
            executor.submit(new Agent(i + 1));
        }


        for (int i = 0; i < NUMBER_OF_CUSTOMERS; i++) {
            executor.submit(new Customer(i + 1));


            try {
                sleep(ThreadLocalRandom.current().nextInt(10, 500));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();


        while (!executor.isTerminated()) {
            try {
                sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Call center simulation completed.");
    }
}