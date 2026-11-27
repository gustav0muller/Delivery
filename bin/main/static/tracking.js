(function () {
    var app = document.getElementById('tracking-app');
    if (!app) {
        return;
    }

    var orderId = app.dataset.orderId;
    var route = document.getElementById('delivery-route-' + app.dataset.mapVariant);
    var courier = document.getElementById('courier');
    var connectionLabel = document.getElementById('connection-label');
    var etaLabel = document.getElementById('eta-label');
    var distanceLabel = document.getElementById('distance-label');
    var progressLabel = document.getElementById('progress-label');
    var progressBar = document.getElementById('progress-bar');
    var message = document.getElementById('tracking-message');
    var title = document.getElementById('tracking-title');
    var chatForm = document.getElementById('chat-form');
    var chatCard = chatForm.closest('.chat-card');
    var chatInput = document.getElementById('chat-input');
    var chatMessages = document.getElementById('chat-messages');
    var chatFeedback = document.getElementById('chat-feedback');
    var chatConnectionLabel = document.getElementById('chat-connection-label');
    var renderedMessageIds = Object.create(null);
    Array.prototype.forEach.call(chatMessages.querySelectorAll('[data-message-id]'), function (item) {
        renderedMessageIds[item.dataset.messageId] = true;
    });
    var previousProgress = -1;
    var routeLength = route.getTotalLength();

    function renderLocation(location) {
        var progress = Math.max(0, Math.min(100, Number(location.progress)));
        if (progress < previousProgress) {
            return;
        }
        previousProgress = progress;

        var distance = routeLength * progress / 100;
        var point = route.getPointAtLength(distance);
        var before = route.getPointAtLength(Math.max(0, distance - 2));
        var after = route.getPointAtLength(Math.min(routeLength, distance + 2));
        var angle = Math.atan2(after.y - before.y, after.x - before.x) * 180 / Math.PI;
        courier.setAttribute('transform',
            'translate(' + point.x + ' ' + point.y + ') rotate(' + angle + ')');
        progressLabel.textContent = Math.round(progress) + '%';
        progressBar.style.width = progress + '%';
        distanceLabel.textContent = Number(location.distanceKm).toFixed(1).replace('.', ',') + ' km';

        if (location.completed) {
            title.textContent = 'Seu pedido chegou.';
            etaLabel.textContent = 'Chegou!';
            connectionLabel.textContent = 'Entrega concluída';
            message.textContent = 'O pizzaiolo chegou à sua casa. Bom apetite!';
            app.classList.add('tracking-complete');
        } else {
            etaLabel.textContent = 'Aproximadamente ' + location.etaSeconds + ' s';
            connectionLabel.textContent = 'Entregador em movimento';
            message.textContent = 'O pizzaiolo está a caminho. A posição é atualizada em tempo real.';
        }
    }

    renderLocation({
        progress: app.dataset.progress,
        etaSeconds: app.dataset.eta,
        distanceKm: app.dataset.distance,
        completed: app.dataset.completed === 'true'
    });

    if (app.dataset.completed === 'true') {
        connectionLabel.textContent = 'Entrega concluída';
    } else {
        var events = new EventSource('/orders/' + encodeURIComponent(orderId) + '/tracking/events');
        events.addEventListener('location', function (event) {
            renderLocation(JSON.parse(event.data));
        });
        events.onerror = function () {
            connectionLabel.textContent = 'Reconectando ao entregador...';
        };
    }

    var chatEnabled = chatCard.dataset.chatEnabled === 'true';
    chatForm.querySelector('button').disabled = !chatEnabled;
    chatInput.disabled = !chatEnabled;
    if (!chatEnabled) {
        chatConnectionLabel.textContent = 'Conversa encerrada';
        chatFeedback.textContent = 'A conversa fica disponível enquanto o pedido está a caminho.';
    }

    var chatEvents = new EventSource('/orders/' + encodeURIComponent(orderId) + '/chat/events');
    chatEvents.addEventListener('message', function (event) {
        renderChatMessage(JSON.parse(event.data));
    });
    chatEvents.onerror = function () {
        chatConnectionLabel.textContent = 'Reconectando à conversa...';
    };

    function renderChatMessage(chatMessage) {
        if (renderedMessageIds[chatMessage.id]) {
            return;
        }
        renderedMessageIds[chatMessage.id] = true;

        var emptyMessage = document.getElementById('chat-empty');
        if (emptyMessage) {
            emptyMessage.remove();
        }

        var item = document.createElement('li');
        item.className = 'chat-message chat-message-' + chatMessage.sender.toLowerCase();
        item.dataset.messageId = chatMessage.id;

        var sender = document.createElement('strong');
        sender.textContent = chatMessage.sender === 'CUSTOMER' ? 'Você' : 'Entregador';
        var content = document.createElement('p');
        content.textContent = chatMessage.content;
        var timestamp = document.createElement('time');
        timestamp.dateTime = chatMessage.sentAt;
        timestamp.textContent = new Date(chatMessage.sentAt).toLocaleTimeString('pt-BR', {
            hour: '2-digit',
            minute: '2-digit'
        });

        item.appendChild(sender);
        item.appendChild(content);
        item.appendChild(timestamp);
        chatMessages.appendChild(item);
        chatConnectionLabel.textContent = 'Conversa em tempo real';
    }

    chatForm.addEventListener('submit', function (event) {
        event.preventDefault();
        var content = chatInput.value.trim();
        if (!content || chatForm.querySelector('button').disabled) {
            return;
        }

        var sendButton = chatForm.querySelector('button');
        sendButton.disabled = true;
        chatFeedback.textContent = 'Enviando mensagem...';
        fetch('/orders/' + encodeURIComponent(orderId) + '/chat/messages', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ content: content })
        }).then(function (response) {
            if (!response.ok) {
                throw new Error('Não foi possível enviar. Tente novamente.');
            }
            return response.json();
        }).then(function (chatMessage) {
            chatInput.value = '';
            chatFeedback.textContent = 'Mensagem enviada.';
            renderChatMessage(chatMessage);
        }).catch(function (error) {
            chatFeedback.textContent = error.message;
        }).then(function () {
            sendButton.disabled = !chatEnabled;
            chatInput.focus();
        });
    });

}());
