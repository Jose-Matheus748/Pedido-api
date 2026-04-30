package pedido_api.dto;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class PedidoDTO {

    private Long id;
    private Long clienteId;
    private Long lojaId;
    private Long protocoloId;
    private String status;
}