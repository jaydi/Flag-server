// functions for item managing

var shopIdForItems;

function getItems(shopId) {
    shopIdForItems = shopId;
    showWaitingDialog();
    gapi.client.flagengine.items.list({'shopId': shopId, 'userId': 0}).execute(function(res) {
        setItemMenus();
        showItems(res);
        hideWaitingDialog();
    });
}

function setItemMenus() {
    showUpIndicator('javascript:backToShopsFromItems();');
    setAddButton('javascript:showItemAdderScreen();');
    $('#item_adder_screen').on('shown.bs.modal', showFirstItemAdder);
}

function backToShopsFromItems() {
    $('#items').html('');
    getShops();
}

var items;

function showItems(res) {
    $('#items').html('');
    items = res.items || [];
    getItemHtml(items);
};

function getItemHtml(items) {
    $.get('raw/item.html', function(data) {
        for (var i = 0; i < items.length; i++) {
            appendItem(data, i, items[i]);
        }
        
        alignItems();
    });
};

function appendItem(data, i, item) {
    data = replaceAll('${index}', i, data);
    data = data.replace('${img}', item.thumbnailUrl);
    data = data.replace('${name}', item.name);
    data = data.replace('${desc}', item.description);
    data = data.replace('${sale}', item.sale);
    data = data.replace('${price}', item.price);
    if (item.oldPrice)
        data = data.replace('${oldPrice}', item.oldPrice);
    else
        data = data.replace('${oldPrice}', '');
    data = data.replace('${reward}', item.reward);
    data = data.replace('${barcodeId}', item.barcodeId);
    $('#items').append(data);
};

function showItemAdderScreen() {
    $('#item_adder_screen').modal({
        backdrop: 'static',
        keyboard: false
    });
};

function hideItemAdderScreen() {
    $('#item_adder_screen').modal('hide');
}

var ia_first = true;
var indexArray = [0];

function showFirstItemAdder() {
    if (ia_first) {
        $.get('raw/add_item.html', function(data) {
            data = replaceAll('${index}', 0, data);
            $('#items_to_be_added').append(data);
        
            gapi.client.flagengine.images.uploadUrl.get().execute(function(res) {
                $('#add_item_thumbnail_form_0').attr('action', res.url);
            });
            
            showItemAdderButton();
        });
        
        ia_first = false;
    }
}

function addItemAdder() {
    hideItemAdderButton();
    $.get('raw/add_item.html', function(data) {
        var index = addAndGetIndex();
        data = replaceAll('${index}', index, data);
        $('#items_to_be_added').append(data);
        
        gapi.client.flagengine.images.uploadUrl.get().execute(function(res) {
            $('#add_item_thumbnail_form_' + index).attr('action', res.url);
        });
        
        if (indexArray.length < 5)
            showItemAdderButton();
    });
}

function addAndGetIndex() {
    if (indexArray.length == 0) {
        indexArray[0] = 0;
        return 0;
    }
    
    indexArray[indexArray.length] = indexArray[indexArray.length - 1] + 1;
    return indexArray[indexArray.length - 1];
}

function removeItemAdder(i) {
    $('#add_item_' + i).remove();
    removeIndex(i);
    
    if (indexArray.length == 4)
        showItemAdderButton();
}

function removeIndex(i) {
    var tempArray = indexArray;
    indexArray = [];
    for (var j = 0; j < tempArray.length; j++) {
        if (tempArray[j] != i)
            indexArray.push(tempArray[j]);
    }
}

function showItemAdderButton() {
    var adder = '<div class="add_item adder" onclick="addItemAdder();"></div>';
    $('#items_to_be_added').append(adder);
}

function hideItemAdderButton() {
    $('.add_item.adder').remove();
}

var itemUploadTotal = 0;
var itemUploadCount = 0;
var itemProgressTotal = 0;
var itemProgressCount = 0;

function addItems() {
    hideItemAdderButton();
    $('#item_adder_screen').animate({scrollTop: 0}, 500);
    $('#uploading_items_dialog').modal({
        backdrop: 'static',
        keyboard: false
    });
    
    itemUploadTotal = indexArray.length;
    itemProgressTotal = itemUploadTotal * 2;
    for (var i = 0; i < itemUploadTotal; i++) {
        addItem(indexArray[i]);
    }
}

function addItem(i) {
    $('#add_item_thumbnail_form_' + i).ajaxSubmit(function(res) {
        $('#add_item_thumbnail_url_' + i).val('https://genuine-evening-455.appspot.com/serve?blob-key=' + res.url);
        progressItemUpload();
        sendAddItem(i);
    });
}

function sendAddItem(i) {
    var name = $('#add_item_name_' + i).val();
    var description = $('#add_item_desc_' + i).val();
    var thumbnailUrl = $('#add_item_thumbnail_url_' + i).val();
    var sale = $('#add_item_sale_' + i).val();
    var oldPrice = $('#add_item_old_price_' + i).val();
    var price = $('#add_item_price_' + i).val();
    var reward = $('#add_item_reward_' + i).val();
    var barcode = $('#add_item_barcode_' + i).val();
    var shopId = shopIdForItems;
    gapi.client.flagengine.items.insert({
        'name': name,
        'description': description,
        'thumbnailUrl': thumbnailUrl,
        'sale': sale,
        'oldPrice': oldPrice,
        'price': price,
        'reward': reward,
        'barcodeId': barcode,
        'shopId': shopId
    }).execute(function(res) {
        $('#add_item_' + i).remove();
        progressItemUpload();
        itemUploadCount++;
        if (itemUploadTotal == itemUploadCount)
            finishItemUpload();
    });
}

function progressItemUpload() {
    itemProgressCount++;
    var rate = (itemProgressCount / itemProgressTotal) * 100;
    $('#item_upload_progress_bar').attr('style', 'width: ' + rate + '%');
}

function finishItemUpload() {
    refreshItemUploadIndexes();
    $('#uploading_items_dialog').modal('hide');
    hideItemAdderScreen();
    getItems(shopIdForItems);
}

function refreshItemUploadIndexes() {
    ia_first = true;
    indexArray = [0];
    itemUploadTotal = 0;
    itemUploadCount = 0;
    itemProgressTotal = 0;
    itemProgressCount = 0;
}

function deleteItem(i) {
    if (confirm('are you sure?')) {
        $('#item_index_' + i).remove();
        
        var item = items[i];
        gapi.client.flagengine.items.delete({'itemId': item.id}).execute(function(res) {
           // nothing to do
        });
    }
}