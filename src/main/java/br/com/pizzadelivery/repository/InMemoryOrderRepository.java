package br.com.pizzadelivery.repository;

import br.com.pizzadelivery.domain.PizzaOrder;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryOrderRepository implements OrderRepository {
    private final ConcurrentMap<UUID, PizzaOrder> orders = new ConcurrentHashMap<>();

    @Override
    public PizzaOrder save(PizzaOrder order) {
        orders.put(order.getId(), order);
        return order;
    }

    @Override
    public Optional<PizzaOrder> findById(UUID id) {
        return Optional.ofNullable(orders.get(id));
    }

    @Override
    public List<PizzaOrder> findAll() {
        List<PizzaOrder> result = new ArrayList<>(orders.values());
        return result.stream()
                .sorted(Comparator.comparing(PizzaOrder::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }
}
