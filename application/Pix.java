package application;
import java.util.concurrent.Semaphore;

public class Pix {
    private double saldo;
    private final Semaphore semaforo = new Semaphore(1);

    public Pix() {
        this.saldo = 0.0;
    }
    
    public Pix(double saldo) {
        this.saldo = saldo;
    }

    public double getSaldo() {
        return saldo;
    }

    // Métodos sem semáforo, com possível race condition
    public void realizarPixSemSemaforo(double valor) {
        // Seção crítica sem proteção
        this.saldo -= valor;
    }

    public void receberPixSemSemaforo(double valor) {
        this.saldo += valor;
    }

    // Métodos com semáforo, com exclusão mútua para evitar race conditions
    public void realizarPixComSemaforo(double valor) {
        try {
            // P() | acquire(), solicita permissão da thread, se tiver ocupada (semaforo = 0), 
            // a thread fica bloqueada até que a permissão seja liberada.
            semaforo.acquire();
            this.saldo -= valor;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // V() | release(), sempre chamado no finally para garantir
            // que a permissão seja devolvida mesmo se houver exceção.
            semaforo.release();
        }
    }

    public void receberPixComSemaforo(double valor) {
        try {
            semaforo.acquire();
            this.saldo += valor;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            semaforo.release();
        }
    }

    // Métodos padrão apontando para a versão segura com semáforo
    public void realizarPix(double valor) {
        realizarPixComSemaforo(valor);
    }

    public void receberPix(double valor) {
        receberPixComSemaforo(valor);
    }
}
