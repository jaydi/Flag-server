// functions for shop managing

function getShops() {
    showWaitingDialog();
    gapi.client.flagengine.shops.all().execute(function(res) {
        setShopMenus();
        showShops(res);
        hideWaitingDialog();
    });
};

function setShopMenus() {
    hideUpIndicator();
    setAddButton('javascript:showShopAdderScreen();');
    $('#shop_adder_screen').on('shown.bs.modal', showShopAdder);
}

var shops;

function showShops(res) {
    $('#shops').html('');
    shops = res.shops || [];
    getShopHtml(shops);
};

function getShopHtml(shops) {
    $.get('raw/shop.html', function(data) {
        for (var i = 0; i < shops.length; i++) {
            appendShop(data, i, shops[i]);
        }
    });
};

function appendShop(data, i, shop) {
    data = replaceAll('${index}', i, data);
    data = data.replace('${logo}', shop.logoUrl + '&width=0&height=0');
    data = data.replace('${name}', shop.name);
//    data = data.replace('${img}', shop.imageUrl);
//    data = data.replace('${desc}', replaceAll('\n', '<br/>', shop.description));
    $('#shops').append(data);
};

function showShopAdderScreen() {
    $('#shop_adder_screen').modal({
        backdrop: 'static',
        keyboard: false
    });
};

function hideShopAdderScreen() {
    $('#shop_adder_screen').modal('hide');
}

function showShopAdder() {
    $.get('raw/add_shop.html', function(data) {
        $('#shop_to_be_added').html(data);
        gapi.client.flagengine.images.uploadUrl.get().execute(function(res) {
            $('#add_shop_logo_form').attr('action', res.url);
        });
        gapi.client.flagengine.images.uploadUrl.get().execute(function(res) {
            $('#add_shop_image_form').attr('action', res.url);
        });
    })
}

function showShopEditor(i) {
    $.get('raw/edit_shop.html', function(data) {
        makeShopEditor(i, data);
        gapi.client.flagengine.images.uploadUrl.get().execute(function(res) {
            $('#edit_shop_logo_form_' + i).attr('action', res.url);
        });
    });
};

function makeShopEditor(i, data) {
    var shop = shops[i];
    data = replaceAll('${index}', i, data);
    data = data.replace('${logo}', shop.logoUrl);
    data = data.replace('${name}', shop.name);
    $('#shop_index_' + i).html(data);
};

function hideShopEditor(i) {
    $.get('raw/desc_shop.html', function(data) {
       makeShopDesc(i, data); 
    });
};

function makeShopDesc(i, data) {
    var shop = shops[i];
    data = replaceAll('${index}', i, data);
    data = data.replace('${logo}', shop.logoUrl);
    data = data.replace('${img}', shop.imageUrl);
    data = data.replace('${name}', shop.name);
    data = data.replace('${desc}', replaceAll('\n', '<br/>', shop.description));
    $('#shop_index_' + i).html(data);
};

var shopName;
var shopDesc;
var shopLogoUrl;
var shopImageUrl;
var shopType;
var shopReward;

function addShop() {
    $('#uploading_shop_dialog').modal({
        backdrop: 'static',
        keyboard: false
    });
    
    shopName = $('#add_shop_name').val();
    shopDesc = $('#add_shop_desc').val();
    shopType = 1;
    shopReward = 0;
    
    var i = 0;
    $('#add_shop_logo_form').ajaxSubmit(function(resLogo) {
        shopLogoUrl = 'https://genuine-evening-455.appspot.com/serve?blob-key=' + resLogo.url;
        i++;
        progressShopUpload(i);
        if (i == 2)
            sendAddShop();
    });
    $('#add_shop_image_form').ajaxSubmit(function(resImage) {
        shopImageUrl = 'https://genuine-evening-455.appspot.com/serve?blob-key=' + resImage.url;
        i++;
        progressShopUpload(i);
        if (i == 2)
            sendAddShop();
    });
};

function sendAddShop() {
    gapi.client.flagengine.shops.insert({
        'name': shopName,
        'description': shopDesc,
        'logoUrl': shopLogoUrl,
        'imageUrl': shopImageUrl,
        'type': shopType,
        'reward': shopReward
    }).execute(function(res) {
        progressShopUpload(2);
        finishAddShop();
    });
};

function progressShopUpload(i) {
    var rate = (i / 3) * 100;
    $('#shop_upload_progress_bar').attr('style', 'width: ' + rate + '%');
}

function finishAddShop() {
    $('#uploading_shop_dialog').modal('hide');
    hideShopAdderScreen();
    getShops();
}

function editShop(i) {
    showWaitingDialog();
    
    var j = 0;
    $('#edit_shop_logo_form_' + i).ajaxSubmit(function(resLogo) {
        if (resLogo.url == '')
            $('#edit_shop_logo_url_' + i).val('');
        else
            $('#edit_shop_logo_url_' + i).val('https://genuine-evening-455.appspot.com/serve?blob-key=' + resLogo.url);
        
        if (++j == 2)
            sendEditShop(i);
    });
    $('#edit_shop_image_form_' + i).ajaxSubmit(function(resImage) {
        if (resImage.url == '')
            $('#edit_shop_image_url_' + i).val('');
        else
            $('#edit_shop_image_url_' + i).val('https://genuine-evening-455.appspot.com/serve?blob-key=' + resImage.url);
        
        if (++j == 2)
            sendEditShop(i);
    });
};

function sendEditShop(i) {
    var id = shops[i].id;
    var name = $('#edit_shop_name_' + i).val();
    var logoUrl = $('#edit_shop_logo_url_' + i).val();
    var imageUrl = $('#edit_shop_image_url_' + i).val();
    var desc = $('#edit_shop_desc_' + i).val();
    var reward = shops[i].reward;
    gapi.client.flagengine.shops.update({
        'id': id,
        'name': name,
        'logoUrl': logoUrl,
        'imageUrl': imageUrl,
        'description': desc,
        'reward': reward
    }).execute(function(res) {
        shops[i] = res;
        hideShopEditor(i);
        hideWaitingDialog();
    });
}

function deleteShop(i) {
    if (confirm('Are you sure?')) {
        $('#shop_index_' + i).remove();
        
        var shop = shops[i];
        gapi.client.flagengine.shops.delete({'shopId': shop.id}).execute(function(res) {
            // nothing to do
        });
    }
};

function loadFlags(i) {
    var shop = shops[i];
    $('#shops').html('');
    getFlags(shop.id);
}

function loadItems(i) {
    var shop = shops[i];
    $('#shops').html('');
    getItems(shop.id);
}