package application;

/**
 * Representa um cliente ou serviço que realiza depósitos (recebimento de Pix).
 * Implementa Runnable para ser executado concorrentemente em uma Thread.
 */
public class ClienteDepositante implements Runnable {
    private final Pix pix;
    private final double valor;
    private final int totalOperacoes;
    private final boolean usarSemaforo;

    public ClienteDepositante(Pix pix, double valor, int totalOperacoes, boolean usarSemaforo) {
        this.pix = pix;
        this.valor = valor;
        this.totalOperacoes = totalOperacoes;
        this.usarSemaforo = usarSemaforo;
    }

    @Override
    public void run() {
        for (int i = 0; i < totalOperacoes; i++) {
            if (usarSemaforo) {
                pix.receberPixComSemaforo(valor);
            } else {
                pix.receberPixSemSemaforo(valor);
            }
        }
    }
}
