package pedido_api.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class StockPlusClient {

    private final RestTemplate restTemplate;

    @Value("${stockplus.api.url}")
    private String baseUrl;

    @Value("${estoque.api.url}")
    private String estoqueApiUrl;

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
        if (protocoloId == null) {
            return null;
        }

        return getProtocolos(List.of(protocoloId))
                .stream()
                .findFirst()
                .orElse(null);
    }

    public List<ProtocoloResponse> getProtocolos(List<Long> protocoloIds) {
        if (protocoloIds == null || protocoloIds.isEmpty()) {
            return List.of();
        }

        try {
            URI uri = UriComponentsBuilder.fromUriString(baseUrl + "/protocolos/lote")
                    .queryParam("ids", protocoloIds.toArray())
                    .build()
                    .toUri();

            ProtocoloResponse[] response = restTemplate.getForObject(uri, ProtocoloResponse[].class);
            return response != null ? Arrays.asList(response) : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }

    public void baixarEstoque(List<Long> protocoloIds) {
        try {
            restTemplate.postForObject(estoqueApiUrl, Map.of("protocoloIds", protocoloIds), Void.class);
        } catch (HttpClientErrorException e) {
            throw new RuntimeException(extrairMensagemErro(e, "Erro ao baixar estoque"));
        } catch (Exception e) {
            throw new RuntimeException("Erro ao comunicar com o servico de estoque: " + e.getMessage());
        }
    }

    private String extrairMensagemErro(HttpClientErrorException e, String fallback) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<?, ?> errorBody = objectMapper.readValue(e.getResponseBodyAsString(), Map.class);
            Object mensagem = errorBody.get("message");

            return mensagem instanceof String texto && !texto.isBlank() ? texto : fallback;
        } catch (Exception ex) {
            return fallback;
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
