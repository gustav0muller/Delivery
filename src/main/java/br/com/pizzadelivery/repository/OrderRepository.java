package br.com.pizzadelivery.repository;

import br.com.pizzadelivery.domain.PizzaOrder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    PizzaOrder save(PizzaOrder order);

    Optional<PizzaOrder> findById(UUID id);

    List<PizzaOrder> findAll();
}
