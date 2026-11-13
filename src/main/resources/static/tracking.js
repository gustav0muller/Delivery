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
        return;
    }

    var events = new EventSource('/orders/' + encodeURIComponent(orderId) + '/tracking/events');
    events.addEventListener('location', function (event) {
        renderLocation(JSON.parse(event.data));
    });
    events.onerror = function () {
        connectionLabel.textContent = 'Reconectando ao entregador...';
    };
}());
