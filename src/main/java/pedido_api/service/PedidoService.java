package pedido_api.service;

import org.springframework.beans.factory.annotation.Value;
import pedido_api.client.StockPlusClient;
import pedido_api.dto.PedidoDTO;
import pedido_api.entity.*;
import pedido_api.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final StockPlusClient stockPlusClient;
    private final PedidoRepository pedidoRepository;
    private final RestTemplate restTemplate;

    @Value("${estoque.api.url}")
    private String estoqueApiUrl;



    public PedidoDTO criar(PedidoDTO dto) {
        Pedido pedido = new Pedido();
        pedido.setClienteId(dto.getClienteId());
        pedido.setLojaId(dto.getLojaId());
        pedido.setProtocoloId(dto.getProtocoloId());
        pedido.setStatus(StatusPedido.EM_ANDAMENTO);
        pedido.setDataCriacao(LocalDateTime.now());

        return toDTO(pedidoRepository.save(pedido));
    }

    public PedidoDTO concluir(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        if (pedido.getStatus() == StatusPedido.CONCLUIDO) {
            throw new RuntimeException("Pedido já foi concluído");
        }

        if (pedido.getStatus() == StatusPedido.CANCELADO) {
            throw new RuntimeException("Pedido cancelado não pode ser concluído");
        }

        baixarEstoque(pedido.getProtocoloId());

        pedido.setStatus(StatusPedido.CONCLUIDO);
        return toDTO(pedidoRepository.save(pedido));
    }

    public PedidoDTO cancelar(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        if (pedido.getStatus() == StatusPedido.CONCLUIDO) {
            throw new RuntimeException("Pedido já concluído não pode ser cancelado");
        }

        if (pedido.getStatus() == StatusPedido.CANCELADO) {
            throw new RuntimeException("Pedido já foi cancelado");
        }

        pedido.setStatus(StatusPedido.CANCELADO);
        return toDTO(pedidoRepository.save(pedido));
    }

    private void baixarEstoque(Long protocoloId) {
        Map<String, Object> body = new HashMap<>();
        body.put("protocoloId", protocoloId);

        try {
            restTemplate.postForObject(estoqueApiUrl, body, Void.class);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Map<?, ?> errorBody = mapper.readValue(e.getResponseBodyAsString(), Map.class);
                String mensagem = (String) errorBody.get("message");
                throw new RuntimeException(mensagem != null ? mensagem : "Erro ao baixar estoque");
            } catch (RuntimeException re) {
                throw re;
            } catch (Exception ex) {
                throw new RuntimeException("Erro ao baixar estoque");
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao comunicar com o serviço de estoque: " + e.getMessage());
        }
    }

    public List<PedidoDTO> listarPorCliente(Long clienteId) {
        return pedidoRepository.findByClienteId(clienteId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public List<PedidoDTO> listarPorLoja(Long lojaId) {
        return pedidoRepository.findByLojaId(lojaId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private PedidoDTO toDTO(Pedido pedido) {
        String nomeCliente = stockPlusClient.getNomeCliente(pedido.getClienteId());
        String nomeLoja    = stockPlusClient.getNomeLoja(pedido.getLojaId());
        StockPlusClient.ProtocoloResponse protocolo = stockPlusClient.getProtocolo(pedido.getProtocoloId());

        return new PedidoDTO(
                pedido.getId(),

                pedido.getClienteId(),
                nomeCliente,

                pedido.getLojaId(),
                nomeLoja,

                pedido.getProtocoloId(),
                protocolo != null ? protocolo.getNome() : "Protocolo removido",

                protocolo != null ? protocolo.getPreco() : 0.0,

                pedido.getStatus().name(),
                pedido.getDataCriacao()
        );
    }
}