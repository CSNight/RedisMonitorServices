define(function (require) {
    let init = function () {
        setMenuHtml();
        $('#sys').click(function () {
            $('#main-area').html('');
            const org = require('js/user/org_widget');
            org.init();
        });
    };
    let setMenuHtml = function () {
        let html = '<li class="" id="sys">\n' +
            '                    <a data-toggle="collapse" href="#usersetting">\n' +
            '                        <i class="tim-icons icon-single-02"></i>\n' +
            '                        <p>User Settings<b class="caret"></b></p>\n' +
            '                    </a>\n' +
            '                    <div class="collapse" id="usersetting">\n' +
            '                        <ul class="nav">\n' +
            '                            <li>\n' +
            '                                <a href="javascript:void(0)">\n' +
            '                                    <span class="sidebar-mini-icon fa fa-user"></span>\n' +
            '                                    <span class="sidebar-normal">ss</span>\n' +
            '                                </a>\n' +
            '                            </li>\n' +
            '                        </ul>\n' +
            '                    </div>\n' +
            '                </li>';
        $('#main-menu').append(html);
    };
    return {
        init: init
    }
});