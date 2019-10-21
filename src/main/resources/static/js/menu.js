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
        $('#main-menu').append('');
    };
    return {
        init: init
    }
});