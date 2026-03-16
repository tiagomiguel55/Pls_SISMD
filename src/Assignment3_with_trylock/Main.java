package Assignment3_with_trylock;

public class Main {

    public static void main(String[] args) {

        Account A = new Account(1, 1000);
        Account B = new Account(2, 1000);
        Account C = new Account(3, 1000);

        Thread user1 = new Thread(new User(A,   B, 100), "User-1");
        Thread user2 = new Thread(new User(B, C, 50), "User-2");
        Thread user3 = new Thread(new User(C, A, 70), "User-3");

        user1.start();
        user2.start();
        user3.start();
    }
}