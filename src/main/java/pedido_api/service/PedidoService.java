package pedido_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pedido_api.client.StockPlusClient;
import pedido_api.dto.PedidoDTO;
import pedido_api.dto.ProtocoloPedidoDTO;
import pedido_api.entity.ItemPedido;
import pedido_api.entity.Pedido;
import pedido_api.entity.StatusPedido;
import pedido_api.repository.PedidoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final StockPlusClient stockPlusClient;
    private final PedidoRepository pedidoRepository;

    public PedidoDTO criar(PedidoDTO dto) {
        validarProtocolos(dto.getProtocoloIds());

        Pedido pedido = new Pedido();
        pedido.setClienteId(dto.getClienteId());
        pedido.setLojaId(dto.getLojaId());
        pedido.setStatus(StatusPedido.EM_ANDAMENTO);
        pedido.setDataCriacao(LocalDateTime.now());

        for (Long protocoloId : dto.getProtocoloIds()) {
            pedido.getItens().add(criarItem(pedido, protocoloId));
        }

        return toDTO(pedidoRepository.save(pedido));
    }

    public PedidoDTO concluir(Long id) {
        Pedido pedido = buscarPedido(id);

        validarPodeConcluir(pedido);
        stockPlusClient.baixarEstoque(getProtocoloIds(pedido));

        pedido.setStatus(StatusPedido.CONCLUIDO);
        return toDTO(pedidoRepository.save(pedido));
    }

    public PedidoDTO cancelar(Long id) {
        Pedido pedido = buscarPedido(id);

        validarPodeCancelar(pedido);
        pedido.setStatus(StatusPedido.CANCELADO);

        return toDTO(pedidoRepository.save(pedido));
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
        String nomeLoja = stockPlusClient.getNomeLoja(pedido.getLojaId());
        List<Long> protocoloIds = getProtocoloIds(pedido);

        Map<Long, StockPlusClient.ProtocoloResponse> protocolosPorId =
                stockPlusClient.getProtocolos(protocoloIds)
                        .stream()
                        .filter(protocolo -> protocolo.getId() != null)
                        .collect(Collectors.toMap(
                                StockPlusClient.ProtocoloResponse::getId,
                                Function.identity(),
                                (primeiro, repetido) -> primeiro
                        ));

        List<ProtocoloPedidoDTO> protocolos = protocoloIds.stream()
                .map(protocoloId -> {
                    StockPlusClient.ProtocoloResponse protocolo = protocolosPorId.get(protocoloId);

                    return new ProtocoloPedidoDTO(
                            protocoloId,
                            protocolo != null ? protocolo.getNome() : "Protocolo removido",
                            protocolo != null ? protocolo.getPreco() : 0.0
                    );
                })
                .toList();

        double valorTotal = protocolos.stream()
                .mapToDouble(protocolo -> protocolo.getValorTotal() != null ? protocolo.getValorTotal() : 0.0)
                .sum();

        return new PedidoDTO(
                pedido.getId(),
                pedido.getClienteId(),
                nomeCliente,
                pedido.getLojaId(),
                nomeLoja,
                protocoloIds,
                protocolos,
                valorTotal,
                pedido.getStatus().name(),
                pedido.getDataCriacao()
        );
    }

    private Pedido buscarPedido(Long id) {
        return pedidoRepository.findByIdWithItens(id)
                .orElseThrow(() -> new RuntimeException("Pedido nao encontrado"));
    }

    private void validarProtocolos(List<Long> protocoloIds) {
        if (protocoloIds == null || protocoloIds.isEmpty()) {
            throw new RuntimeException("Pedido deve conter pelo menos um protocolo");
        }

        if (protocoloIds.stream().anyMatch(Objects::isNull)) {
            throw new RuntimeException("Pedido possui protocolo invalido");
        }
    }

    private void validarPodeConcluir(Pedido pedido) {
        if (pedido.getStatus() == StatusPedido.CONCLUIDO) {
            throw new RuntimeException("Pedido ja foi concluido");
        }

        if (pedido.getStatus() == StatusPedido.CANCELADO) {
            throw new RuntimeException("Pedido cancelado nao pode ser concluido");
        }
    }

    private void validarPodeCancelar(Pedido pedido) {
        if (pedido.getStatus() == StatusPedido.CONCLUIDO) {
            throw new RuntimeException("Pedido ja concluido nao pode ser cancelado");
        }

        if (pedido.getStatus() == StatusPedido.CANCELADO) {
            throw new RuntimeException("Pedido ja foi cancelado");
        }
    }

    private ItemPedido criarItem(Pedido pedido, Long protocoloId) {
        ItemPedido item = new ItemPedido();
        item.setPedido(pedido);
        item.setProtocoloId(protocoloId);
        return item;
    }

    private List<Long> getProtocoloIds(Pedido pedido) {
        return pedido.getItens()
                .stream()
                .map(ItemPedido::getProtocoloId)
                .toList();
    }
}
