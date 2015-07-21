// functions for flag managing

var shopIdForFlags;

function getFlags(shopId) {
    shopIdForFlags = shopId;
    showWaitingDialog();
    gapi.client.flagengine.flags.list.byshop({'shopId': shopId}).execute(function(res) {
        setFlagMenus();
        showFlags(res);
        hideWaitingDialog();
    });
}

function setFlagMenus() {
    showUpIndicator('javascript:backToShopsFromFlags();');
    setAddButton('javascript:showFlagAdderScreen();');
    //$('#flag_adder_screen').on('shown.bs.modal', showFirstFlagAdder);
}

function backToShopsFromFlags() {
    $('#flags').html('');
    getShops();
}

var flags;
var map;
var markers;

function showFlags(res) {
    $('#flags').html('<div id="flag_map"></div><div id="flag_list"></div>');
    flags = res.flags || [];
    getFlagMap(flags);
    getFlagHtml(flags);
};

function getFlagMap(flags) {
    var mapOptions = {
        center: new google.maps.LatLng(37.397, 127.644),
        zoom: 7
    };
    map = new google.maps.Map(document.getElementById('flag_map'), mapOptions);
    
    markers = [];
    for (var i = 0; i < flags.length; i++) {
        appendMarker(i, flags[i]);
    }
}

function appendMarker(i, flag) {
    var latLng = new google.maps.LatLng(flag.lat, flag.lon);
    var marker = new google.maps.Marker({
        position: latLng,
        title: flag.shopName
    });
    
    marker.setMap(map);
    markers[i] = marker;
}

function getFlagHtml(flags) {
    $.get('raw/flag.html', function(data) {
        for (var i = 0; i < flags.length; i++) {
            appendFlag(data, i, flags[i]);
        }
    });
}

function appendFlag(data, i, flag) {
    data = replaceAll('${index}', i, data);
    data = data.replace('${name}', flag.shopName);
    data = data.replace('${addr}', '(' + flag.lat + ', ' + flag.lon + ')');
    $('#flag_list').append(data);
}