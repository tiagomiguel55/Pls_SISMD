package Assignment3_with_synchronized;

import Assignment3_with_trylock.Account;

public class Bank2 {


    public synchronized static void transfer(Account from, Account to, double amount) throws InterruptedException {

       try {
           if (from.getBalance() >= amount) {
               from.withdraw(amount);
               to.deposit(amount);
               System.out.println(Thread.currentThread().getName() + " transferred " + amount + " from Account " + from.getId() + " to Account " + to.getId());

           } else {
               System.out.println("Insufficient funds in Account " + from.getId());
           }
       } catch (Exception e) {
           throw new RuntimeException(e);
       }


    }


}
