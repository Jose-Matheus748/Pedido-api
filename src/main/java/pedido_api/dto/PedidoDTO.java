package pedido_api.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PedidoDTO {

    private Long id;

    private Long clienteId;
    private String clienteNome;

    private Long lojaId;
    private String lojaNome;

    private List<Long> protocoloIds;
    private List<ProtocoloPedidoDTO> protocolos;

    private Double valorTotal;

    private String status;

    private LocalDateTime dataCriacao;
}