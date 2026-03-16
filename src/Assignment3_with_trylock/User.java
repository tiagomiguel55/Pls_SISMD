package Assignment3_with_trylock;

public class User implements Runnable{

    private Account from;

    private Account to;

    private double amount;

    public User(Account from, Account to, double amount) {
        this.from = from;
        this.to = to;
        this.amount = amount;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < 1; i++) {
                Bank.transfer(from, to, amount); // Attempt to transfer money from one account to another
                Thread.sleep(100); // Sleep for a short time before the next transfer
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }
}
