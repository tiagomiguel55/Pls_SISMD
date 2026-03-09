package Assignment2;

public class Main {

    public static void main(String[] args) {
        ConcurrentTicketSellingSystem ticketselling = new ConcurrentTicketSellingSystem(1);

        Tickets tickets = new Tickets(100);

        Thread t1 = new Thread(new Sellers(ticketselling, tickets, "Seller1"));
        Thread t2 = new Thread(new Sellers(ticketselling, tickets, "Seller2"));
        Thread t3 = new Thread(new Sellers(ticketselling, tickets, "Seller3"));
        Thread t4 = new Thread(new Sellers(ticketselling, tickets, "Seller4"));
        Thread t5 = new Thread(new Sellers(ticketselling, tickets, "Seller5"));

        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();


    }



}
