//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package Assignment1;

public class Main {
    public static void main(String[] args) {
        OrderQueue queue = new OrderQueue();
        int numTakers = 3;

        for(int i = 0; i < numTakers; ++i) {
            Thread takerThread = new Thread(new OrderTaker(queue));
            takerThread.start();
        }

        int numHandlers = 2;

        for(int i = 0; i < numHandlers; ++i) {
            Thread handlerThread = new Thread(new OrderHandler(queue));
            handlerThread.start();
        }

    }
}
