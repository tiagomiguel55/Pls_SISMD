package Assignment1;

public class OrderTaker implements Runnable {
    private OrderQueue queue;

    public OrderTaker(OrderQueue queue) {
        this.queue = queue;
    }

    public void run() {
        while(true) {
            try {
                Order order = new Order();
                this.queue.addOrder(order);
                System.out.println("Order Taken: " + order.getOrderId());
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
