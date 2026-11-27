(function () {
    'use strict';

    var statusNames = {
        PAYMENT_PENDING: 'Aguardando pagamento',
        RECEIVED: 'Pedido recebido',
        PREPARING: 'Pedido em preparo',
        OUT_FOR_DELIVERY: 'Saiu para entrega',
        DELIVERED: 'Pedido entregue'
    };
    var statusMessages = {
        PAYMENT_PENDING: 'Conclua o pagamento para enviar seu pedido à cozinha.',
        RECEIVED: 'Seu pedido foi recebido pela cozinha.',
        PREPARING: 'A Slicer começou a preparar seu pedido.',
        OUT_FOR_DELIVERY: 'O motoboy saiu para a entrega.',
        DELIVERED: 'Seu pedido foi entregue.'
    };

    function createNotificationButton() {
        if (document.querySelector('[data-notification-button]')) {
            return;
        }
        var topbar = document.querySelector('.topbar');
        if (!topbar) {
            return;
        }
        var button = document.createElement('button');
        button.type = 'button';
        button.className = 'notification-button';
        button.dataset.notificationButton = 'true';
        button.textContent = 'Ativar notificações';
        button.addEventListener('click', function () {
            if (!('Notification' in window)) {
                button.textContent = 'Notificações não suportadas';
                return;
            }
            Notification.requestPermission().then(function (permission) {
                button.textContent = permission === 'granted'
                    ? 'Notificações ativadas'
                    : 'Ativar notificações';
                if (permission === 'granted') {
                    showNativeNotification('Notificações ativadas',
                        'Você receberá os próximos status do pedido.');
                }
            });
        });
        topbar.appendChild(button);
    }

    function showNativeNotification(title, body) {
        if ('Notification' in window && Notification.permission === 'granted') {
            new Notification(title, { body: body, icon: '/favicon.ico' });
        }
    }

    function updateCardStatus(card, status) {
        card.dataset.orderStatus = status.status;
        var pill = card.querySelector('.status-pill');
        if (pill) {
            pill.textContent = status.displayName;
            pill.className = 'status-pill status-' + status.status.toLowerCase();
        }

        var trackingLink = card.querySelector('.tracking-link');
        if (status.status === 'OUT_FOR_DELIVERY' || status.status === 'DELIVERED') {
            if (!trackingLink) {
                var link = document.createElement('a');
                link.className = 'button button-outline tracking-link';
                link.href = '/orders/' + encodeURIComponent(card.dataset.orderId) + '/tracking';
                link.textContent = status.status === 'DELIVERED' ? 'Ver trajeto' : 'Acompanhar no mapa';
                card.appendChild(link);
            } else {
                trackingLink.textContent =
                    status.status === 'DELIVERED' ? 'Ver trajeto' : 'Acompanhar no mapa';
            }
        }

        var paymentLink = card.querySelector('.payment-link');
        if (status.status === 'PAYMENT_PENDING') {
            if (!paymentLink) {
                paymentLink = document.createElement('a');
                paymentLink.className = 'button button-primary payment-link';
                paymentLink.href = '/orders/' + encodeURIComponent(card.dataset.orderId) + '/payment';
                paymentLink.textContent = 'Pagar pedido';
                card.appendChild(paymentLink);
            }
        } else if (paymentLink) {
            paymentLink.remove();
        }

        var automaticStatus = card.querySelector('.automatic-status');
        if (automaticStatus
                && status.status !== 'RECEIVED'
                && status.status !== 'PREPARING') {
            automaticStatus.remove();
        }
    }

    function notifyStatus(card, status) {
        var key = 'slicer-status-' + card.dataset.orderId;
        var previous = localStorage.getItem(key);
        if (previous && previous !== status.status) {
            showNativeNotification(statusNames[status.status] || status.displayName,
                statusMessages[status.status] || status.displayName);
        }
        localStorage.setItem(key, status.status);
    }

    function subscribeToStatus(card) {
        fetch('/orders/' + encodeURIComponent(card.dataset.orderId) + '/status')
            .then(function (response) {
                if (!response.ok) {
                    return null;
                }
                return response.json();
            })
            .then(function (status) {
                if (!status) {
                    return;
                }
                localStorage.setItem('slicer-status-' + card.dataset.orderId, status.status);
                updateCardStatus(card, status);
            })
            .catch(function () {
                // The SSE connection is still the source of status updates.
            });

        var events = new EventSource(
            '/orders/' + encodeURIComponent(card.dataset.orderId) + '/status/events');
        events.addEventListener('status', function (event) {
            var statusChanged = JSON.parse(event.data);
            var status = {
                status: statusChanged.status,
                displayName: statusNames[statusChanged.status] || statusChanged.status
            };
            var previousStatus = card.dataset.orderStatus;
            if (previousStatus !== status.status) {
                notifyStatus(card, status);
            }
            updateCardStatus(card, status);
        });
    }

    createNotificationButton();

    var cards = document.querySelectorAll('[data-order-id][data-order-status]');
    cards.forEach(function (card) {
        var previousStatus = card.dataset.orderStatus;
        localStorage.setItem('slicer-status-' + card.dataset.orderId, previousStatus);
        subscribeToStatus(card);
    });
}());
