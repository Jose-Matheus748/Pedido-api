package pedido_api.controller;

import pedido_api.dto.PedidoDTO;
import pedido_api.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    public PedidoDTO criar(@RequestBody PedidoDTO dto) {
        return pedidoService.criar(dto);
    }

    @PutMapping("/{id}/concluir")
    public PedidoDTO concluir(@PathVariable Long id) {
        return pedidoService.concluir(id);
    }

    @GetMapping("/cliente/{clienteId}")
    public List<PedidoDTO> listarPorCliente(@PathVariable Long clienteId) {
        return pedidoService.listarPorCliente(clienteId);
    }

    @GetMapping("/loja/{lojaId}")
    public List<PedidoDTO> listarPorLoja(@PathVariable Long lojaId) {
        return pedidoService.listarPorLoja(lojaId);
    }
}