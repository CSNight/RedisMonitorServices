define(function (require) {
    let init = function () {
        setMenuHtml();
        $('#dept').click(function () {
            $('#main-area').html('');
            const org = require('js/user/org_widget');
            org.init();
        });
    };
    let setMenuHtml = function () {
        let header_html = ''
        $('#main-menu').append('');
    };
    return {
        init: init
    }
});