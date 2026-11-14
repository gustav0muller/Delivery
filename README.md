# Slicer — Pizza Delivery

Aplicação web de delivery de pizza construída em Java com Spring Boot e Gradle. O fluxo de pedidos usa eventos de domínio para separar a criação do pedido do início do preparo. A interface inclui cardápio, bebidas, promoções, cadastro e login.

## Requisitos

- Java 8 ou superior
- Conexão com a internet na primeira execução do Gradle Wrapper

## Executar

No Windows:

```powershell
.\gradlew.bat bootRun
```

Abra [http://localhost:8081](http://localhost:8081) no navegador.

Cadastre uma conta em **Criar conta** ou entre em **Entrar**. A autenticação é demonstrativa: usuários ficam em memória e são perdidos ao reiniciar a aplicação. As senhas são armazenadas como hashes PBKDF2 com salt aleatório; o checkout não exige login.

Todos os sabores recebem automaticamente 10% de desconto. Refrigerantes selecionados custam R$ 4,90 com pizza; o resumo do pedido mostra bebidas, desconto e total.

Para experimentar o rastreamento, faça um pedido, avance-o até **Saiu para entrega** e clique em **Acompanhar no mapa**. O pizzaiolo se move em uma rota ilustrativa por aproximadamente 30 segundos, e o pedido passa automaticamente para **Entregue** ao chegar. Durante o trajeto, a tela permite enviar mensagens ao entregador e recebe novas mensagens em tempo real; a conversa é encerrada quando o pedido é entregue.

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
8. Mensagens do cliente ou do entregador são publicadas como `CourierMessageSentEvent`; `CourierMessageEventHandler` guarda o histórico em memória e distribui as mensagens por SSE para a tela de rastreamento.

Os eventos são publicados pelo mecanismo de eventos do Spring. A localização é apenas demonstrativa: não usa GPS, serviços de mapas externos ou endereços reais. O chat e os pedidos também ficam em memória; o endpoint `POST /orders/{id}/chat/courier-messages` permite que uma integração do entregador publique respostas. A separação entre serviço, eventos, consumidores, rastreamento e repositório deixa os componentes substituíveis e facilita testes.

## Estrutura

- `domain`: pedidos, sabores, bebidas, promoções e estados válidos.
- `auth`: cadastro e autenticação em memória.
- `service`: regras de criação e transição dos pedidos.
- `events`: eventos de domínio e seus consumidores.
- `repository`: contrato e armazenamento em memória.
- `web`: formulário e rotas MVC.
- `tracking`: simulação da localização e conexões SSE.
- `src/main/resources/templates`: interface Thymeleaf.
- `src/test`: testes unitários e de integração web.

Os pedidos ficam em memória e são perdidos ao reiniciar a aplicação; o repositório pode ser substituído por uma implementação persistente sem alterar o fluxo da interface.
