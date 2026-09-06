package application;

/**
 * Representa um cliente ou serviço que realiza saques (envio de Pix).
 * Implementa Runnable para ser executado concorrentemente em uma Thread.
 */
public class ClienteSacador implements Runnable {
    private final Pix pix;
    private final double valor;
    private final int totalOperacoes;
    private final boolean usarSemaforo;

    public ClienteSacador(Pix pix, double valor, int totalOperacoes, boolean usarSemaforo) {
        this.pix = pix;
        this.valor = valor;
        this.totalOperacoes = totalOperacoes;
        this.usarSemaforo = usarSemaforo;
    }

    @Override
    public void run() {
        for (int i = 0; i < totalOperacoes; i++) {
            if (usarSemaforo) {
                pix.realizarPixComSemaforo(valor);
            } else {
                pix.realizarPixSemSemaforo(valor);
            }
        }
    }
}
