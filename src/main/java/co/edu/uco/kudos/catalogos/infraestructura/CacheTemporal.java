package co.edu.uco.kudos.catalogos.infraestructura;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class CacheTemporal<T> {

    private record Entrada<V>(V valor, long venceEnMilisegundos) {
    }

    private final Map<String, Entrada<T>> entradas = new ConcurrentHashMap<>();
    private final long milisegundosDeVida;

    public CacheTemporal(int segundosDeVida) {
        this.milisegundosDeVida = segundosDeVida * 1000L;
    }

    public Optional<T> obtener(String clave, Supplier<Optional<T>> origen) {
        long ahora = System.currentTimeMillis();
        Entrada<T> guardada = entradas.get(clave);
        if (guardada != null && guardada.venceEnMilisegundos() > ahora) {
            return Optional.of(guardada.valor());
        }

        Optional<T> encontrada = origen.get();
        encontrada.ifPresent(valor -> entradas.put(clave, new Entrada<>(valor, ahora + milisegundosDeVida)));
        return encontrada;
    }
}
