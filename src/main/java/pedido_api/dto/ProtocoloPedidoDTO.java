package pedido_api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProtocoloPedidoDTO {
    private Long protocoloId;
    private String protocoloNome;
    private Double valorTotal;
}
