# Documentação do Assignment 1 - Sistema de Orders com Múltiplos Produtores e Consumidores

Este sistema simula uma aplicação de pedidos (orders) com múltiplos produtores (`Order Takers`) e consumidores (`OrderHGandlers`), usando uma fila compartilhada (`OrderQueue`). O objetivo é demonstrar a concorrência, sincronização e comunicação entre threads.

A classe `Order` representa uma order. Cada order vai possuir um ID único, atribuído pela `OrderQueue`.

``` java
public class Order {

    private int orderId;

    public Order() {
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int id) {
        this.orderId = id;
    }
} 
```

A classe `OrderQueue` é a fila compartilhada onde os `OrderTakers` adicionam orders e os `OrderHandlers` removem orders.

``` java
public class OrderQueue {

    LinkedList<Order> l = new LinkedList<>();

    private int nextId;

    private final int MAX_ORDERS = 100;

    public synchronized void addOrder(Order order) throws InterruptedException {
        while (l.size() == MAX_ORDERS) {
            wait();
        }
        order.setOrderId(nextId++);
        l.add(order);
        notifyAll();
    }

    public synchronized Order removeOrder() throws InterruptedException {
        while (l.isEmpty()) {
            wait();
        }
        Order order = l.removeFirst();
        notifyAll();
        return order;
    }

}
```

1. `addOrder` espera `wait()` se a lista estiver cheia. Caso contrário, atribui um ID à order antes de adicioná-la à lista e notifica todas as threads que estão à espera. 

2. `removeOrder` espera `wait()` se a lista estiver vazia. Caso contrário, remove a primeira order da lista, notifica todas as threads que estão à espera e retorna a order removida.

A classe `OrderTaker` é um produtor que cria orders e as adiciona à `OrderQueue`.

``` java
public class OrderTaker implements Runnable {

    private OrderQueue queue;



    public OrderTaker(OrderQueue queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Order order = new Order();
                queue.addOrder(order);
                System.out.println("Order Taken: " + order.getOrderId());
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
```

Esta classe cria uma nova order, adiciona-a à fila e imprime o ID da order. Em seguida, a thread dorme por 500ms antes de criar a próxima order.

A classe `OrderHandler` é um consumidor que remove orders da `OrderQueue` e processa-as.

``` java
public class OrderHandler implements Runnable   {

    private OrderQueue queue;

    public OrderHandler(OrderQueue queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Order order = queue.removeOrder();
                System.out.println("Order Handling: " + order.getOrderId());
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
```

Esta classe remove uma order da fila, imprime o ID da order e depois dorme por 500ms antes de processar a próxima order.

A classe `Main` é o ponto de entrada do programa, onde criamos a `OrderQueue`, os `OrderTakers` e os `OrderHandlers`, e iniciamos as threads.

``` java
public class Main {
    public static void main(String[] args) {
        // 1. Criar a queue partilhada
        OrderQueue queue = new OrderQueue();

        // 2. Criar OrderTakers (producers)
        int numTakers = 3; // por exemplo
        for (int i = 0; i < numTakers; i++) { // ciclo for para criar vários OrderTakers
            Thread takerThread = new Thread(new OrderTaker(queue)); // criar um novo thread para cada OrderTaker
            takerThread.start();
        }

        // 3. Criar OrderHandlers (consumers)
        int numHandlers = 2; // por exemplo
        for (int i = 0; i < numHandlers; i++) {
            Thread handlerThread = new Thread(new OrderHandler(queue));
            handlerThread.start();
        }
    }
}
```

Nesta classe, criamos uma fila compartilhada `OrderQueue`, iniciamos um número específico de `OrderTakers` e `OrderHandlers`, e cada um deles é executado numa thread separada. O programa continuará a executar indefinidamente, com os OrderTakers criando orders e os OrderHandlers processando-as.

### Fluxo do sistema:
1. Os `OrderTakers` criam orders e as adicionam à OrderQueue.
2. Os `OrderHandlers` removem orders da OrderQueue e as processam.
3. A sincronização é garantida pela utilização de `synchronized`, `wait()` e `notifyAll()` na classe `OrderQueue`, evitando condições de corrida e garantindo que os produtores e consumidores operem de forma coordenada.

### Nota:

Caso não tivessemos `synchronized` nos métodos `addOrder` e `removeOrder`, várias threads poderiam acessar e modificar a lista ao mesmo tempo o que levaria a race conditions.
Também podia acontecer que duas threads atribuissem o mesmo ID se usassem o contador sem sincronização.
Sem isto, os dois Handlers podiam chamar `removeFirst()` ao mesmo tempo e tentar remover a mesma order.

Com `synchronized`, garantimos acesso exclusivo à lista e mantemos ID's únicos para cada order.