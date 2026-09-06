package application;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println("Simulador De Transações Pix com Threads e Semáforos");

        double saldoInicial = 1000.0;
        int numThreadsDepositantes = 5;
        int numThreadsSacadores = 5;
        int operacoesPorThread = 10000;
        double valorOperacao = 10.0;

        // Experimento sem semáforo (Race condition)
        System.out.println("Executando sem semáforo (Inseguro | Possível Race Condition)");
        ResultadoExperimento expSemSemaforo = executarExperimento(
                saldoInicial,
                numThreadsDepositantes,
                numThreadsSacadores,
                operacoesPorThread,
                valorOperacao,
                false // usarSemaforo = false
        );
        exibirResultado(expSemSemaforo);

        // Experimento com semáforo (Exclusão mútua)
        System.out.println("\n Executando com semáforo (Seguro | Exclusão Mútua Garantida)");
        ResultadoExperimento expComSemaforo = executarExperimento(
                saldoInicial,
                numThreadsDepositantes,
                numThreadsSacadores,
                operacoesPorThread,
                valorOperacao,
                true // usarSemaforo = true
        );
        exibirResultado(expComSemaforo);

        // Resultados comparativos
        System.out.println("\n==========================================================");
        System.out.println("                    RESUMO COMPARATIVO                    ");
        System.out.println("==========================================================");
        System.out.printf("Sem Semáforo -> Saldo Final: R$ %.2f | Erro: R$ %.2f | Tempo: %d ms%n",
                expSemSemaforo.saldoFinal, (expSemSemaforo.saldoFinal - expSemSemaforo.saldoEsperado), expSemSemaforo.tempoMs);
        System.out.printf("Com Semáforo -> Saldo Final: R$ %.2f | Erro: R$ %.2f | Tempo: %d ms%n",
                expComSemaforo.saldoFinal, (expComSemaforo.saldoFinal - expComSemaforo.saldoEsperado), expComSemaforo.tempoMs);
        System.out.println("==========================================================");
    }

    private static ResultadoExperimento executarExperimento(
            double saldoInicial,
            int numThreadsDepositantes,
            int numThreadsSacadores,
            int operacoesPorThread,
            double valorOperacao,
            boolean usarSemaforo) {

        Pix pix = new Pix(saldoInicial);
        List<Thread> threads = new ArrayList<>();

        // Cria as threads depositantes
        for (int i = 0; i < numThreadsDepositantes; i++) {
            ClienteDepositante dep = new ClienteDepositante(pix, valorOperacao, operacoesPorThread, usarSemaforo);
            threads.add(new Thread(dep, "Depositante-" + (i + 1)));
        }

        // Cria as threads sacadoras
        for (int i = 0; i < numThreadsSacadores; i++) {
            ClienteSacador sac = new ClienteSacador(pix, valorOperacao, operacoesPorThread, usarSemaforo);
            threads.add(new Thread(sac, "Sacador-" + (i + 1)));
        }

        double totalDepositado = numThreadsDepositantes * operacoesPorThread * valorOperacao;
        double totalSacado = numThreadsSacadores * operacoesPorThread * valorOperacao;
        double saldoEsperado = saldoInicial + totalDepositado - totalSacado;

        // Inicia cronômetro pra contar o tempo no final
        long inicio = System.currentTimeMillis();

        // Dispara todas as threads concorrentemente
        for (Thread t : threads) {
            t.start();
        }

        // Aguarda todas as threads terminarem (join)
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        long fim = System.currentTimeMillis();
        long tempoMs = fim - inicio;

        return new ResultadoExperimento(
                saldoInicial,
                totalDepositado,
                totalSacado,
                saldoEsperado,
                pix.getSaldo(),
                tempoMs,
                usarSemaforo
        );
    }

    private static void exibirResultado(ResultadoExperimento res) {
        System.out.printf("Modo:                  %s%n", res.usouSemaforo ? "Com Semáforo" : "Sem Semáforo");
        System.out.printf("Saldo Inicial:         R$ %.2f%n", res.saldoInicial);
        System.out.printf("Total de Depósitos:    R$ %.2f%n", res.totalDepositado);
        System.out.printf("Total de Saques:       R$ %.2f%n", res.totalSacado);
        System.out.printf("Saldo Esperado:        R$ %.2f%n", res.saldoEsperado);
        System.out.printf("Saldo Final Real:      R$ %.2f%n", res.saldoFinal);
        System.out.printf("Diferença (Inconsist.):R$ %.2f%n", (res.saldoFinal - res.saldoEsperado));
        System.out.printf("Tempo de Execução:     %d ms%n", res.tempoMs);

        if (Math.abs(res.saldoFinal - res.saldoEsperado) < 0.0001) {
            System.out.println("Consistencia mantida com exclusao mutua");
        } else {
            System.out.println("Ocorreu race condition, saldo final inconsistente");
        }
    }

    private static class ResultadoExperimento {
        double saldoInicial;
        double totalDepositado;
        double totalSacado;
        double saldoEsperado;
        double saldoFinal;
        long tempoMs;
        boolean usouSemaforo;

        ResultadoExperimento(double saldoInicial, double totalDepositado, double totalSacado,
                             double saldoEsperado, double saldoFinal, long tempoMs, boolean usouSemaforo) {
            this.saldoInicial = saldoInicial;
            this.totalDepositado = totalDepositado;
            this.totalSacado = totalSacado;
            this.saldoEsperado = saldoEsperado;
            this.saldoFinal = saldoFinal;
            this.tempoMs = tempoMs;
            this.usouSemaforo = usouSemaforo;
        }
    }
}