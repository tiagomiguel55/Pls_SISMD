package Assignment3;

public class Bank {

    public static void transfer(Account from, Account to, double amount) throws InterruptedException {

        boolean fromlocked = false;
        boolean tolocked = false;

        fromlocked = from.getLock().tryLock(2, java.util.concurrent.TimeUnit.SECONDS);
        tolocked = to.getLock().tryLock(2, java.util.concurrent.TimeUnit.SECONDS);

        if (fromlocked && tolocked) {
            try {
                if (from.getBalance() >= amount) {
                    from.withdraw(amount);
                    to.deposit(amount);
                    System.out.println(Thread.currentThread().getName() + " transferred " + amount + " from Account " + from.getId() + " to Account " + to.getId());

                } else {
                    System.out.println("Insufficient funds in Account " + from.getId());
                }
            } finally {
                if (fromlocked) {
                    from.getLock().unlock();
                }

                if (tolocked) {
                    to.getLock().unlock();
                }
            }
        } else {
            System.out.println("Could not acquire locks for transfer. Retrying...");
        }


    }


}
