define(function () {
    let init = function () {
        setHtmlFrame();
    };
    let setHtmlFrame = function () {
        let html = '<div class="row">';
        html += '<div class="col-8" style="background-color: #fff"></div>';
        html += "</div>";
        $('#main-area').html(html);
    };
    return {
        init: init
    }
});