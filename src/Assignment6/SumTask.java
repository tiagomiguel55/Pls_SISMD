package Assignment6;

import java.util.List;
import java.util.concurrent.RecursiveTask;

class SumTask extends RecursiveTask<Long> {
    private static final int THRESHOLD = 10_000; // limite para dividir a tarefa em partes menores

    private final List<Long> list; // a lista a ser somada
    private final int start, end; // os índices que delimitam a parte da lista a ser processada

    public SumTask(List<Long> list) { // construtor para a tarefa inicial, que processa toda a lista
        this(list, 0, list.size());
    }

    public SumTask(List<Long> list, int start, int end) { // construtor para as tarefas que processam partes da lista
        this.list = list;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Long compute() { // se a parte da lista for pequena o suficiente, calcula diretamente
        if (end - start <= THRESHOLD) {
            long sum = 0;
            for (int i = start; i < end; i++) {
                sum += list.get(i);
            }
            return sum;
        }

        int mid = (start + end) / 2; // divide a tarefa em duas partes

        SumTask left = new SumTask(list, start, mid); // cria a tarefa para a primeira metade
        SumTask right = new SumTask(list, mid, end); // cria a tarefa para a segunda metade

        left.fork(); // executa a primeira metade em paralelo
        long rightResult = right.compute(); // calcula a segunda metade diretamente
        long leftResult = left.join(); // espera pelo resultado da primeira metade

        return leftResult + rightResult; // retorna a soma dos resultados das duas metades
    }
}