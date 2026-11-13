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

Para executar os testes:

```powershell
.\gradlew.bat test
```

## Fluxo orientado a eventos

1. `OrderService` valida e registra o pedido com status `RECEIVED`.
2. O serviço publica `OrderPlacedEvent`.
3. `OrderEventHandler` recebe o evento e inicia o preparo, mudando o pedido para `PREPARING`.
4. Cada mudança publica `OrderStatusChangedEvent`; o consumidor registra a atualização.
5. A interface permite avançar o pedido pelas etapas `PREPARING`, `OUT_FOR_DELIVERY` e `DELIVERED`.

Os eventos são publicados pelo mecanismo de eventos do Spring. A separação entre serviço, eventos, consumidor e repositório deixa os componentes substituíveis e facilita testes.

## Estrutura

- `domain`: pedidos, sabores e estados válidos.
- `service`: regras de criação e transição dos pedidos.
- `events`: eventos de domínio e seus consumidores.
- `repository`: contrato e armazenamento em memória.
- `web`: formulário e rotas MVC.
- `src/main/resources/templates`: interface Thymeleaf.
- `src/test`: testes unitários e de integração web.

Os pedidos ficam em memória e são perdidos ao reiniciar a aplicação; o repositório pode ser substituído por uma implementação persistente sem alterar o fluxo da interface.
