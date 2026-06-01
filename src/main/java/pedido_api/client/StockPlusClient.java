package pedido_api.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class StockPlusClient {

    private final RestTemplate restTemplate;

    @Value("${stockplus.api.url}")
    private String baseUrl;

    public String getNomeCliente(Long clienteId) {
        try {
            ClienteResponse resp = restTemplate.getForObject(
                    baseUrl + "/clientes/" + clienteId, ClienteResponse.class);
            return resp != null ? resp.getNome() : "Cliente removido";
        } catch (Exception e) {
            return "Cliente removido";
        }
    }

    public String getNomeLoja(Long lojaId) {
        try {
            LojaResponse resp = restTemplate.getForObject(
                    baseUrl + "/lojas/" + lojaId, LojaResponse.class);
            return resp != null ? resp.getNome() : "Loja removida";
        } catch (Exception e) {
            return "Loja removida";
        }
    }

    public ProtocoloResponse getProtocolo(Long protocoloId) {
        try {
            return restTemplate.getForObject(
                    baseUrl + "/protocolos/" + protocoloId, ProtocoloResponse.class);
        } catch (Exception e) {
            return null;
        }
    }

    // DTOs de resposta internos ao client
    @lombok.Getter @lombok.Setter
    public static class ClienteResponse {
        private Long id;
        private String nome;
    }

    @lombok.Getter @lombok.Setter
    public static class LojaResponse {
        private Long id;
        private String nome;
    }

    @lombok.Getter @lombok.Setter
    public static class ProtocoloResponse {
        private Long id;
        private String nome;
        private Double preco;
    }
}