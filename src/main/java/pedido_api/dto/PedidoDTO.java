package pedido_api.dto;

import lombok.*;

import java.time.LocalDateTime;

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

    private Long protocoloId;
    private String protocoloNome;

    private Double valorTotal;

    private String status;

    private LocalDateTime dataCriacao;
}