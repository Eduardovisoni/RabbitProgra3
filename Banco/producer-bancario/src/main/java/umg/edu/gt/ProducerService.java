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

        if (lote.getTransacciones() == null || lote.getTransacciones().isEmpty()) {
            System.out.println("No hay transacciones en el lote: " + lote.getLoteId());
            return 0;
        }

        int enviados = 0;

        for (Transaccion t : lote.getTransacciones()) {
            String banco = t.getBancoDestino();

            if (banco == null || banco.isBlank()) {
                System.out.println("⚠️ Transacción sin bancoDestino. id=" + t.getIdTransaccion());
                continue;
            }

            String json = objectMapper.writeValueAsString(t);
            publisher.publicarJsonEnCola(banco, json);
            enviados++;
        }

        System.out.println("✅ Lote " + lote.getLoteId() + " | Transacciones enviadas: " + enviados);
        return enviados;
    }
}