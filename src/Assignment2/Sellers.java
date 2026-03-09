package Assignment2;

public class Sellers implements Runnable {

    private ConcurrentTicketSellingSystem controller;


    private Tickets tickets;

    private String sellerName;

    public Sellers(ConcurrentTicketSellingSystem controller, Tickets tickets, String sellerName) {
        this.controller = controller;
        this.tickets = tickets;
        this.sellerName = sellerName;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {

                controller.enterProtocol();

                Boolean sell = tickets.sellTicket(sellerName);

                controller.exitProtocol();

                if(!sell) {
                    break;
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println(sellerName + " was interrupted.");
            }
        }
    }
}
