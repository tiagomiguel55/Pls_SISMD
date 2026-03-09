package Assignment2;

public class Tickets {

    int availabilitytickets;


    public Tickets(int availabilitytickets) {
        this.availabilitytickets = availabilitytickets;
    }


    public boolean sellTicket(String sellerName) {
        if (availabilitytickets > 0) {
            System.out.println(sellerName + " sold 1 ticket. Remaining: " + (availabilitytickets - 1));
            availabilitytickets--;
            return true;
        } else {
            System.out.println(sellerName + " tried to sell but no tickets left.");
            return false;
        }
    }

}
