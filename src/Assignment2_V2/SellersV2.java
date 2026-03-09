package Assignment2_V2;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SellersV2 implements Runnable {

    private TicketsV2 tickets;

    private String sellerName;

    public SellersV2(TicketsV2 tickets, String sellerName) {
        this.tickets = tickets;
        this.sellerName = sellerName;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) { // Loop until interrupted
            try {
                Boolean sell = tickets.sellTicket(sellerName); // Attempt to sell a ticket
                if (!sell) { // If no tickets left, break the loop
                    break;
                }
            } catch (Exception e) {
                Thread.currentThread().interrupt(); // Interrupt the thread if an exception occurs
                System.out.println(sellerName + " was interrupted.");
            }
        }

    }
}
