# Forno da Vila — Pizza Delivery

Aplicação web de delivery de pizza construída em Java com Spring Boot e Gradle. O fluxo de pedidos usa eventos de domínio para separar a criação do pedido do início do preparo.

## Requisitos

- Java 8 ou superior
- Conexão com a internet na primeira execução do Gradle Wrapper

## Executar

No Windows:

```powershell
.\gradlew.bat bootRun
```

Abra [http://localhost:8081](http://localhost:8081) no navegador.

Para experimentar o rastreamento, faça um pedido, avance-o até **Saiu para entrega** e clique em **Acompanhar no mapa**. O pizzaiolo se move em uma rota ilustrativa por aproximadamente 30 segundos, e o pedido passa automaticamente para **Entregue** ao chegar.

Para executar os testes:

```powershell
.\gradlew.bat test
```

## Fluxo orientado a eventos

1. `OrderService` valida e registra o pedido com status `RECEIVED`.
2. O serviço publica `OrderPlacedEvent`.
3. `OrderEventHandler` recebe o evento e inicia o preparo, mudando o pedido para `PREPARING`.
4. Cada mudança publica `OrderStatusChangedEvent`; o consumidor registra a atualização.
5. Ao chegar a `OUT_FOR_DELIVERY`, `DeliverySimulation` começa a publicar `DeliveryLocationUpdatedEvent` com posições simuladas uma vez por segundo.
6. `DeliveryLocationEventHandler` guarda a posição, envia atualizações ao navegador por Server-Sent Events (SSE) e publica a conclusão como status `DELIVERED`.
7. A tela de rastreamento alterna entre três bairros vetoriais e move o ícone do pizzaiolo de moto pelas ruas da rota selecionada; a simulação dura cerca de 30 segundos.

Os eventos são publicados pelo mecanismo de eventos do Spring. A localização é apenas demonstrativa: não usa GPS, serviços de mapas externos ou endereços reais. A separação entre serviço, eventos, consumidores, rastreamento e repositório deixa os componentes substituíveis e facilita testes.

## Estrutura

- `domain`: pedidos, sabores e estados válidos.
- `service`: regras de criação e transição dos pedidos.
- `events`: eventos de domínio e seus consumidores.
- `repository`: contrato e armazenamento em memória.
- `web`: formulário e rotas MVC.
- `tracking`: simulação da localização e conexões SSE.
- `src/main/resources/templates`: interface Thymeleaf.
- `src/test`: testes unitários e de integração web.

Os pedidos ficam em memória e são perdidos ao reiniciar a aplicação; o repositório pode ser substituído por uma implementação persistente sem alterar o fluxo da interface.
