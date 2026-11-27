# Slicer — Pizza Delivery

Aplicação web de delivery de pizza construída em Java com Spring Boot e Gradle. O fluxo de pedidos e pagamentos usa eventos de domínio para separar a criação do pedido, a aprovação do pagamento e o início do preparo. A interface inclui cardápio, bebidas, promoções, cadastro e login.

## Requisitos

- Java 8 ou superior
- Conexão com a internet na primeira execução do Gradle Wrapper

## Executar

No Windows:

```powershell
.\gradlew.bat bootRun
```

Abra [http://localhost:8081](http://localhost:8081) no navegador.

### RabbitMQ

O projeto publica e consome eventos pelo RabbitMQ. Para iniciar a infraestrutura:

```powershell
docker compose up -d
.\gradlew.bat bootRun
```

O painel administrativo ficará disponível em [http://localhost:15672](http://localhost:15672),
com usuário e senha `pizzadelivery`.

Os eventos usam a exchange topic `pizzadelivery.events`. Filas com falha são reprocessadas
até três vezes e, depois disso, encaminhadas para `pizzadelivery.dead-letter`.

Cadastre uma conta em **Criar conta** ou entre em **Entrar**. A autenticação é demonstrativa: usuários ficam em memória e são perdidos ao reiniciar a aplicação. As senhas são armazenadas como hashes PBKDF2 com salt aleatório; o checkout não exige login.

Todos os sabores recebem automaticamente 10% de desconto. Refrigerantes selecionados custam R$ 4,90 com pizza; o resumo do pedido mostra bebidas, desconto e total.

O checkout usa um Pix simulado para demonstração: não gera QR Code, não movimenta dinheiro e não coleta dados financeiros. O pedido só é enviado à cozinha depois da aprovação simulada.

Para experimentar o rastreamento, faça um pedido, avance-o até **Saiu para entrega** e clique em **Acompanhar no mapa**. O pizzaiolo se move em uma rota ilustrativa por aproximadamente 30 segundos, e o pedido passa automaticamente para **Entregue** ao chegar. Durante o trajeto, a tela permite enviar mensagens ao entregador e recebe novas mensagens em tempo real; a conversa é encerrada quando o pedido é entregue.

Para executar os testes:

```powershell
.\gradlew.bat test
```

## Fluxo orientado a eventos

1. `OrderService` valida e registra o pedido com status `PAYMENT_PENDING`, depois publica `OrderPlacedEvent`.
2. `PaymentEventHandler` cria um pagamento pendente em resposta ao evento.
3. Ao confirmar no checkout, `PaymentService` publica `PaymentRequestedEvent`; o processador simulado responde com `PaymentApprovedEvent`.
4. `PaymentEventHandler` registra a aprovação e muda o pedido para `RECEIVED`. `OrderEventHandler` só então inicia o preparo, mudando o pedido para `PREPARING`.
5. Cada mudança publica `OrderStatusChangedEvent`; o consumidor registra a atualização.
6. Ao chegar a `OUT_FOR_DELIVERY`, `DeliverySimulation` começa a publicar `DeliveryLocationUpdatedEvent` com posições simuladas uma vez por segundo.
7. `DeliveryLocationEventHandler` guarda a posição, envia atualizações ao navegador por Server-Sent Events (SSE) e publica a conclusão como status `DELIVERED`.
8. A tela de rastreamento alterna entre três bairros vetoriais e move o ícone do pizzaiolo de moto pelas ruas da rota selecionada; a simulação dura cerca de 30 segundos.
9. Mensagens do cliente ou do entregador são publicadas como `CourierMessageSentEvent`; `CourierMessageEventHandler` guarda o histórico em memória e distribui as mensagens por SSE para a tela de rastreamento.

Os eventos são publicados pelo mecanismo de eventos do Spring e encaminhados ao RabbitMQ. O pagamento é apenas demonstrativo; a localização também é ilustrativa e não usa GPS, serviços de mapas externos ou endereços reais. O chat, os pedidos e os pagamentos ficam em memória; o endpoint `POST /orders/{id}/chat/courier-messages` permite que uma integração do entregador publique respostas. A separação entre serviço, eventos, consumidores, rastreamento e repositório deixa os componentes substituíveis e facilita testes.

## Estrutura

- `domain`: pedidos, sabores, bebidas, promoções e estados válidos.
- `auth`: cadastro e autenticação em memória.
- `service`: regras de criação e transição dos pedidos.
- `events`: eventos de domínio e seus consumidores.
- `repository`: contrato e armazenamento em memória.
- `web`: formulário e rotas MVC.
- `tracking`: simulação da localização e conexões SSE.
- `payment`: fluxo de pagamento simulado e armazenamento em memória.
- `src/main/resources/templates`: interface Thymeleaf.
- `src/test`: testes unitários e de integração web.

Os pedidos ficam em memória e são perdidos ao reiniciar a aplicação; o repositório pode ser substituído por uma implementação persistente sem alterar o fluxo da interface.
