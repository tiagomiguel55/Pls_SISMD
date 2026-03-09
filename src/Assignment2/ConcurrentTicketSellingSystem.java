package Assignment2;

import java.util.concurrent.Semaphore;

public class ConcurrentTicketSellingSystem {

    private Semaphore TicketControll;

    public ConcurrentTicketSellingSystem(int ticketControll) {
        TicketControll = new Semaphore(ticketControll);
    }

    public void enterProtocol() throws InterruptedException {
        TicketControll.acquire();
    }

    public void exitProtocol() throws InterruptedException {
        TicketControll.release();
    }

}