package Assignment2_V2;

public class MainV2 {

    public static void main(String[] args) {
        int totalTickets = 100;
        TicketsV2 tickets = new TicketsV2(totalTickets);

        Thread seller1 = new Thread(new SellersV2(tickets, "Seller 1"));
        Thread seller2 = new Thread(new SellersV2(tickets, "Seller 2"));
        Thread seller3 = new Thread(new SellersV2(tickets, "Seller 3"));

        seller1.start();
        seller2.start();
        seller3.start();
    }



}
