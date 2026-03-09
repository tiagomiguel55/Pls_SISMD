package Assignment2_V2;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TicketsV2 {

    private final Lock lock = new ReentrantLock();

    int availabilitytickets;

    public TicketsV2(int availabilitytickets) {
        this.availabilitytickets = availabilitytickets;
    }

    public boolean sellTicket(String sellerName) { // Method to sell a ticket, returns true if successful, false if no tickets left
        lock.lock(); // Acquire the lock to ensure thread safety
        try {
            if (availabilitytickets > 0) { // Check if there are tickets available
                System.out.println(sellerName + " sold 1 ticket. Remaining: " + (availabilitytickets - 1));
                availabilitytickets--; // Decrement the number of available tickets
                return true; // Return true to indicate a successful sale
            } else {
                System.out.println(sellerName + " tried to sell but no tickets left.");
                return false;
            }
        } finally { // Ensure that the lock is released even if an exception occurs
            lock.unlock(); // Release the lock
        }


    }
}