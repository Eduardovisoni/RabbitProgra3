package umg.edu.gt;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ProducerService {

    private final TransaccionesApiClient apiClient;
    private final RabbitPublisher publisher;
    private final ObjectMapper objectMapper;

    public ProducerService(TransaccionesApiClient apiClient, RabbitPublisher publisher, ObjectMapper objectMapper) {
        this.apiClient = apiClient;
        this.publisher = publisher;
        this.objectMapper = objectMapper;
    }

    public int ejecutar() throws Exception {
        LoteTransacciones lote = apiClient.obtenerLote();

        if (lote == null || lote.getTransacciones() == null || lote.getTransacciones().isEmpty()) {
            String loteId = lote != null ? lote.getLoteId() : "SIN_LOTE";
            System.out.println("No hay transacciones en el lote: " + loteId);
            return 0;
        }

        int enviados = 0;

        for (Transaccion transaccion : lote.getTransacciones()) {
            String banco = transaccion.getBancoDestino();

            if (banco == null || banco.isBlank()) {
                System.out.println("Transacción sin bancoDestino. id=" + transaccion.getIdTransaccion());
                continue;
            }

            String json = objectMapper.writeValueAsString(transaccion);
            publisher.publicarJsonEnCola(banco.trim(), json);

            System.out.println(
                    "ID enviado: " + transaccion.getIdTransaccion()
                            + " | Cola: " + banco.trim()
                            + " | Monto: Q." + transaccion.getMonto()
            );

            enviados++;
        }

        System.out.println("Lote " + lote.getLoteId() + " | Transacciones enviadas: " + enviados);
        return enviados;
    }
}