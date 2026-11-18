package olhovivo.util;

/**
 * Thread (Runnable) que repete uma tarefa a cada N segundos enquanto estiver activa.
 * Usada para actualizar automaticamente o painel dos agentes sem bloquear a interface.
 */
public class PainelRefresher implements Runnable {
    private final Runnable tarefa;
    private final long intervaloMs;
    private volatile boolean activo = true;
    private Thread fio;

    public PainelRefresher(Runnable tarefa, int segundos) {
        this.tarefa = tarefa;
        this.intervaloMs = segundos * 1000L;
    }

    public synchronized void iniciar() {
        if (fio != null) return;
        fio = new Thread(this, "painel-refresher");
        fio.setDaemon(true);
        fio.start();
    }

    public synchronized void parar() {
        activo = false;
        if (fio != null) fio.interrupt();
    }

    @Override public void run() {
        while (activo) {
            try {
                Thread.sleep(intervaloMs);
                if (activo) tarefa.run();
            } catch (InterruptedException e) {
                return;
            } catch (RuntimeException e) {
                System.err.println("Refresco falhou: " + e.getMessage());
            }
        }
    }
}
