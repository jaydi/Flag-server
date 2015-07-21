var fileIndexes;
var lastIndex;
var files;
var htmldata = '<div class="dropitem" id="dropitem_${index}"><div class="dropitemcancel" onclick="removeDropItem(${index})">X</div><img class="droppreview" id="droppreview_${index}"><span class="dropfilename">${filename}</span></div>';

var dragenter = function(e) {
    e.stopPropagation();
    e.preventDefault();
    $(this).css('border', '2px solid #0B85A1');
}

var dragover = function (e) {
    e.stopPropagation();
    e.preventDefault();
}

var drop = function (e) {
    e.preventDefault();
    $(this).css('border', '0px');
    var files = e.originalEvent.dataTransfer.files;
    processFiles(files);
}

function showDropzoneAssist() {
    $('.dropzone_assist').css('display', 'block');
}

function hideDropzoneAssist() {
    $('.dropzone_assist').css('display', 'none');
}
    
function initDropForm() {
    $('.dropzone_items').html('');
    fileIndexes = [];
    lastIndex = -1;
    files = [];
    showDropzoneAssist();
    
    var zone = $('.dropzone');
    zone.on('dragenter', dragenter);
    zone.on('dragover', dragover);
    zone.on('drop', drop);
}

function addFromFileBrowser(input) {
    processFiles(input.files);
}

function processFiles(files) {
    for (var i = 0; i < files.length; i++) {
        var index
        if (fileIndexes.length == 0)
            index = lastIndex + 1;
        else
            index = fileIndexes[fileIndexes.length - 1] + 1;
        
        fileIndexes[fileIndexes.length] = index;
        lastIndex = index;
        
        addDropItem(files[i], index, htmldata);
    }
    
    if (fileIndexes.length > 0)
        hideDropzoneAssist();
}

function addDropItem(file, index, htmldata) {
    htmldata = replaceAll('${index}', index, htmldata);
    htmldata = htmldata.replace('${filename}', file.name);
    $('.dropzone_items').append(htmldata);
    
    var reader = new FileReader();
    reader.onload = function(e) {
        $('#droppreview_' + index).attr('src', e.target.result);
    };
    reader.readAsDataURL(file);
    
    files[files.length] = file;
}

function removeDropItem(index) {
    $('#dropitem_' + index).remove();
    
    var newFileIndexes = [];
    for (var i = 0; i < fileIndexes.length; i++)
        if (fileIndexes[i] != index)
            newFileIndexes[newFileIndexes.length] = fileIndexes[i];
    
    fileIndexes = newFileIndexes;
    
    if (fileIndexes.length == 0)
        showDropzoneAssist();
}

var pTotal;
var pCount;
var pFinalCall;

function uploadDropItems(shopId, callback, finalCall) {
    pTotal = Math.ceil(fileIndexes.length / 5) - 1;
    pCount = 0;
    pFinalCall = finalCall;
    uploadDropItemsSerial(pCount, shopId, callback);
}

function uploadDropItemsSerial(pCount, shopId, callback) {
    var start = pCount * 5;
    var end;
    if (pCount == pTotal)
        end = fileIndexes.length - 1;
    else if (pCount < pTotal)
        end = ((pCount + 1) * 5) - 1;
    else {
        pFinalCall();
        return;
    }
    
    uploadDropItemsInterval(start, end, shopId, pCount, callback);
}

var sTotal;
var sCount;

function uploadDropItemsInterval(start, end, shopId, pCount, callback) {
    sTotal = end - start + 1;
    sCount = 0;
    
    for (var i = start; i <= end; i++) {
        var index = fileIndexes[i];
        var file = files[index];
        uploadDropItem(file, shopId, function(data) {
            sCount++;
            if (sCount == sTotal)
                uploadDropItemsSerial(pCount + 1, shopId, callback);
            
            callback(data);
        });
    }
}

function uploadDropItem(file, shopId, afterUpload) {
    var formdata = new FormData();
    formdata.append('image', file);
    formdata.append('shopId', shopId);
    
    gapi.client.flagengine.images.uploadUrl.get().execute(function(res) {
        $.ajax({
            url: res.url,
            data: formdata,
            cache: false,
            contentType: false,
            processData: false,
            type: 'POST',
            success: function(data){
                afterUpload(data);
            }
        });
    });
}













