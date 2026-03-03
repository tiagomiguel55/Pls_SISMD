package Assignment1;

import java.util.LinkedList;

public class OrderQueue {
    LinkedList<Order> l = new LinkedList();
    private int nextId;
    private final int MAX_ORDERS = 100;

    public synchronized void addOrder(Order order) throws InterruptedException {
        while(this.l.size() == 100) {
            this.wait();
        }

        order.setOrderId(this.nextId++);
        this.l.add(order);
        this.notifyAll();
    }

    public synchronized Order removeOrder() throws InterruptedException {
        while(this.l.isEmpty()) {
            this.wait();
        }

        Order order = (Order)this.l.removeFirst();
        this.notifyAll();
        return order;
    }
}
