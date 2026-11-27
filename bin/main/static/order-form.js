(function () {
    var pizzaType = document.getElementById('pizzaType');
    var pizzaQuantity = document.getElementById('quantity');
    var drinkType = document.getElementById('drinkType');
    var drinkQuantity = document.getElementById('drinkQuantity');
    var estimate = document.getElementById('estimate-total');

    if (!pizzaType || !pizzaQuantity || !drinkType || !drinkQuantity || !estimate) {
        return;
    }

    function selectedPrice(select) {
        var option = select.options[select.selectedIndex];
        return option && option.dataset.price ? Number(option.dataset.price) : 0;
    }

    function updateEstimate() {
        var pizzaSubtotal = selectedPrice(pizzaType) * Number(pizzaQuantity.value || 0);
        var drinkSubtotal = selectedPrice(drinkType) * Number(drinkQuantity.value || 0);
        var total = pizzaSubtotal * 0.9 + drinkSubtotal;
        estimate.textContent = pizzaSubtotal > 0
            ? 'R$ ' + total.toFixed(2).replace('.', ',')
            : 'Selecione uma pizza';
    }

    drinkType.addEventListener('change', function () {
        drinkQuantity.value = drinkType.value ? '1' : '0';
        updateEstimate();
    });

    [pizzaType, pizzaQuantity, drinkQuantity].forEach(function (field) {
        field.addEventListener('change', updateEstimate);
        field.addEventListener('input', updateEstimate);
    });

    updateEstimate();
}());
