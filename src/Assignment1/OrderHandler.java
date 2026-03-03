

package Assignment1;

public class OrderHandler implements Runnable {
    private OrderQueue queue;

    public OrderHandler(OrderQueue queue) {
        this.queue = queue;
    }

    public void run() {
        while(true) {
            try {
                Order order = this.queue.removeOrder();
                System.out.println("Order Handling: " + order.getOrderId());
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
