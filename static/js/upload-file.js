function UploadFileClass(id, server_url) {
    /*var dropContainer,
        dropListing,
        imgPreviewFragment = document.createDocumentFragment(),
        domElements;*/

    var id = id;
    var server_url = server_url;

    var uploadProgressXHR= function (event) {
        $("#importfilenotifier > p").text(Math.round((event.loaded * 100) / event.total)+"%");
        if (event.lengthComputable) {
        var percentage = Math.round((event.loaded * 100) / event.total),
            loaderIndicator = event.target.log;
            if (percentage < 100) {
                //loaderIndicator.width = (percentage*2) + "px";
                loaderIndicator.text( percentage + "%");
            }
        }
    };

    var loadedXHR= function (event) {
        //var currentImageItem = event.target.log;
        $("#importfilenotifier > p").addClass("loaded");
        $("#importfilenotifier > p").empty();
        //console.log("xhr upload of "+event.target.log.id+" complete");
    };

    var uploadError= function (error) {
        alert(error);
    };

    var processXHR= function (evt, file, index) {
        var xhr = new XMLHttpRequest();
        //var getBinaryDataReader = new FileReader();
        var container = $("#importfilenotifier > p");
        //var fileUpload = xhr.upload;
        container.addClass("progressBar");
        container.text("0%");
        xhr.log = container;
        xhr.onprogress= uploadProgressXHR(evt);
        xhr.onload= loadedXHR(evt);
        xhr.onerror= uploadError(evt);

        xhr.open("POST", server_url);
        xhr.setRequestHeader("Content-Type","text/plain;charset=x-user-defined-binary");
        xhr.send(evt.target.result);
    };

    var buildImageListItem= function (event) {
        /*domElements = [
            document.createElement('li'),
            document.createElement('a'),
            document.createElement('img'),
            document.createElement('p')
        ];*/

        var name = event.target.name,
            data = event.target.result,
            index = event.target.index,
            file = event.target.file;

        console.log("into buildImageListItem with : " + name);
        /*domElements[2].src = data // base64 encoded string of local file(s)
        domElements[2].width = 300;
        domElements[2].height = 200;
        domElements[1].appendChild(domElements[2]);
        domElements[0].id = "item"+index;
        domElements[0].appendChild(domElements[1]);

        imgPreviewFragment.appendChild(domElements[0]);

        dropListing.appendChild(imgPreviewFragment);*/

        processXHR(event, file, index);
    };

    return {
        handleDrop: function (event) {

            //var dt = event.dataTransfer,
            var files = this.files;
            var count = files.length;
            console.log("number of files to upload = "+count);
            event.stopPropagation();
            event.preventDefault();

            for (var i = 0; i < count; i++) {
                //if(files[i].size < 1048576) {
                    var file = files[i],
                        droppedFileName = file.name,
                        reader = new FileReader();
                        reader.name = name,
                        reader.index = i,
                        reader.file = file;
                    reader.onloadend = buildImageListItem(event);
                    reader.readAsBinaryString(file);
                    console.log("async read file = "+droppedFileName);
                //} else {
                //    alert("file is too big, needs to be below 1mb");
                //}
            }
        },
    };

    //window.addEventListener("load", this.setup, false);
}
