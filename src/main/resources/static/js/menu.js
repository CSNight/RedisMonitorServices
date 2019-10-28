define(function (require) {
    let init = function () {
        setMenuHtml();
        $('#dept').click(function () {
            $('#main-area').html('');
            const org = require('js/user/org_widget');
            org.init();
            $('.navbar-brand').text($(this).find('a').text());
        });
        $('#menus').click(function () {
            $('#main-area').html('');
            const meuns = require('js/user/menu_widget');
            meuns.init();
            $('.navbar-brand').text($(this).find('a').text());
        });
    };
    let setMenuHtml = function () {
        let header_html = '';
        $('#main-menu').append('');
    };
    return {
        init: init
    }
});