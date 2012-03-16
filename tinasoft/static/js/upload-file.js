/*
    Copyright (C) 2009-2011 CREA Lab, CNRS/Ecole Polytechnique UMR 7656 (Fr)
#
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
#
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
#
    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

function UploadFileClass(id, server_url) {
    /*var dropContainer,
        dropListing,
        imgPreviewFragment = document.createDocumentFragment(),
        domElements;*/

    var id = id;
    var server_url = server_url;

    var progressXHR= function (event) {
        //$("#importfilenotifier > p").text(Math.round((event.loaded * 100) / event.total)+"%");
    };

    var loadedXHR= function (event) {
        //var currentImageItem = event.target.log;
        $("#importfilenotifier > p").addClass("ui-state-default");
        //$("#importfilenotifier > p").empty();
        //console.log("xhr upload of "+event.target.log.id+" complete");
    };

    var errorXHR= function (error) {
        $("#importfilenotifier > p").addClass("ui-state-error");
        alert("xhr error : " + error);
    };

    var successXHR= function (evt) {
        $("#importfilenotifier > p").text("success !");
    };

    var processXHR= function (evt, file, index) {
        var xhr = new XMLHttpRequest();
        //var getBinaryDataReader = new FileReader();
        $("#importfilenotifier > p").removeClass("ui-state-error");
        $("#importfilenotifier > p").addClass("ui-state-highlight");
        $("#importfilenotifier > p").text("0%");
        /*$.ajax({
            url: server_url,
            type: "POST",
            dataType: "text/plain",
            data: evt.target.result,
            processData: false,
            error: uploadError(evt),
            success: successXHR(evt),
            complete: loadedXHR(evt),
        });*/

        /*xhr.log = container;*/
        xhr.onprogress= progressXHR(evt);
        xhr.onload= loadedXHR(evt);
        xhr.onerror= errorXHR(evt);

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
