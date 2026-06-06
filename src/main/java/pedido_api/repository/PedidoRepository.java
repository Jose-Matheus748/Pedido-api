package pedido_api.repository;

import pedido_api.entity.Pedido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @EntityGraph(attributePaths = "itens")
    @Query("select p from Pedido p where p.id = :id")
    Optional<Pedido> findByIdWithItens(@Param("id") Long id);

    @EntityGraph(attributePaths = "itens")
    List<Pedido> findByClienteId(Long clienteId);

    @EntityGraph(attributePaths = "itens")
    List<Pedido> findByLojaId(Long lojaId);
}
