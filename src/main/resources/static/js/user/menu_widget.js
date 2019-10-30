define(function () {
    let menu_tree = null;
    let menu_select = null;
    let menu_list = null;
    let icons = [];
    let init = function () {
        setHtmlFrame();
        tableLoad()
    };
    //初始化内容
    let setHtmlFrame = function () {
        let html = '<div class="row">';
        html += '<div class="col-lg"><div id="toolbar">';
        html += '<button type="button" id="new_menu" class="layui-btn layui-btn-normal"><i class="fa fa-plus"></i>添加菜单</button></div>';
        html += '<table id="menu_tree"></table>';
        html += "</div></div>";
        $('#main-area').html(html);
        bindEvent();
        getIcons();
    };
    let getIcons = function () {
        G.request.GET("/menus/get_icons", "", true).then(function (resp) {
            if (resp.hasOwnProperty("message") && resp.status === 200) {
                icons = resp.message;
            }
        }).catch((e) => {
            console.log(e)
        });
    };
    let tableLoad = function () {
        $('#org_tree').html('');
        //菜单目录树加载
        G.request.GET("/menus/get_menu_tree", "", true).then(function (resp) {
            if (resp.hasOwnProperty("message") && resp.status === 200) {
                menu_tree = resp.message;
            }
        }).catch((e) => {
            console.log(e)
        });
        //菜单目录树列表请求
        G.request.GET("/menus/get_menu_list", "", true).then(function (resp) {
            if (resp.hasOwnProperty("message") && resp.status === 200) {
                menu_list = resp.message;
                tableInit(menu_list, "menu_tree");
            }
        }).catch((e) => {
            console.log(e)
        });
    };

    function tableInit(data, dom) {
        let $table = $('#' + dom);
        //表格配置
        $table.bootstrapTable('destroy').bootstrapTable({
            data: data,
            idField: 'id',
            dataType: 'json',
            columns: [
                {field: 'name', title: '目录', searchable: true},
                {
                    field: 'icon', title: '图标', align: 'center', searchable: true, formatter: (value, row, index) => {
                        return '<span><i class="fa ' + value + '"></i></span>'
                    }
                },
                {
                    field: 'sort', title: '排序', align: 'center', searchable: true, formatter: (value, row, index) => {

                        return '<span class="layui-badge">' + value + '</span>';
                    }
                },
                {
                    field: 'hidden', title: '状态', align: 'center', formatter: (value, row, index) => {
                        if (!value) {
                            return '<span class="layui-btn layui-btn-sm">显示</span>';
                        } else {
                            return '<span class="layui-btn layui-btn-disabled layui-btn-sm">隐藏</span>';
                        }
                    }
                },
                {field: 'component_name', title: '组件', searchable: true, align: 'center'},
                {field: 'path', title: '路径', searchable: true, align: 'center'},
                {
                    field: 'iframe', title: '是否为链接', align: 'center', formatter: (value, row, index) => {
                        return value ? "是" : "否"
                    }
                },
                {
                    field: 'create_time', title: '创建时间', align: 'center', formatter: (value, row, index) => {
                        return new Date(value).toLocaleString();
                    }
                },
                {
                    field: 'operate',
                    title: '操作',
                    align: 'center',
                    events: {
                        'click .menu_del': function (e, value, row, index) {
                            del(row);
                        },
                        'click .menu_modify': function (e, value, row, index) {
                            update(row);
                        },
                        'click .menu_hide': function (e, value, row, index) {
                            status(row, true);
                        },
                        'click .menu_show': function (e, value, row, index) {
                            status(row, false);
                        }
                    },
                    formatter: (value, row, index) => {
                        let lock_html = row.enabled ?
                            '<button type="button" class="org_lock layui-btn layui-btn-warm layui-btn-sm"><i class="fa fa-lock" ></i></button>' :
                            '<button type="button" class="org_unlock layui-btn layui-btn-sm"><i class="fa fa-unlock" ></i></button>';
                        return [
                            '<button type="button" class="org_del layui-btn layui-btn-danger layui-btn-sm"><i class="fa fa-trash" ></i></button>',
                            '<button type="button" class="org_modify layui-btn layui-btn-normal  layui-btn-sm"><i class="fa fa-pencil" ></i></button>',
                            lock_html
                        ].join('');
                    }
                },
            ],
            search: true,
            clickToSelect: false,
            toolbar: '#toolbar',//工具栏
            toolbarAlign: 'left',//工具栏的位置
            treeShowField: 'name',
            parentIdField: 'pid',
            singleSelect: true,
            onResetView: function (data) {
                $table.treegrid({
                    initialState: 'collapsed',// 所有节点都折叠
                    treeColumn: 0,
                    expanderExpandedClass: 'tim-icons icon-simple-delete t-expend',  //图标样式
                    expanderCollapsedClass: 'tim-icons icon-simple-add t-collapsed',
                    onChange: function () {
                        $table.bootstrapTable('resetWidth');
                    }
                });
            },
            onClickRow: function (row, el, field) {
                menu_select = row;
            },
            onSearch: function () {
                $table.treegrid('getAllNodes').treegrid('expand');
            }
        });
        //展开节点
        $table.treegrid('getAllNodes').treegrid('expand');
    }

    function bindEvent() {
        $('#new_menu').click(function add(id) {
            layer.open({
                title: "添加菜单",
                area: ['570px', "430px"],
                content: setNewMenuDialog()
                , btn: ['添加', '取消']
                , yes: function (index, layero) {
                    let dept_name = $('#dept_name').val();
                    let enabled = $('.layui-form-radioed').prev().hasClass('dept_enable');
                    let dept_parent = $('#select_dept').val();
                    if (dept_name !== '') {
                        let dept_pid = '';
                        for (let i = 0; i < org_list.length; i++) {
                            if (org_list[i].name === dept_parent) {
                                dept_pid = org_list[i].id;
                            }
                        }
                        let org_ent = {
                            name: dept_name,
                            enabled: enabled,
                            pid: dept_pid
                        };
                        request_add(org_ent, index);
                    } else {
                        layer.msg('部门名称不能为空', {
                            time: 2000, icon: 0
                        });
                    }

                }, cancel: function () {
                }, success: function () {
                    layui.form.render();
                    icon_list_dropdown();
                    menu_tree_dropdown();
                }
            });
        });
    }


    function del(row) {

    }

    function update(row) {

    }

    function status(row, is_hide) {

    }

    let setNewMenuDialog = function (row) {
        let form = '<form class="layui-form" action="" style="box-sizing:content-box !important;">';
        //菜单名称
        let menu_name = row !== undefined ? row.name : '';
        let menu_html = input_template("menu_name", '菜单名称', menu_name, '菜单名称');
        //组件
        let com_name = row !== undefined ? row.component_name : '';
        let com_id = row !== undefined ? row.component : '';
        let line_com = input_inline_template({
            com_id: {tit: '组件编码', value: com_id, place: '组件编码'},
            com_name: {tit: '组件名称', value: com_name, place: '组件名称'}
        });
        let en_c = row !== undefined ? (row.hidden ? 'checked' : '') : '';
        let choice_html = '<div class="layui-form-item">';
        choice_html += '<div class="layui-inline"><label class="layui-form-label">是否隐藏</label><div class="layui-input-inline" style="width: 150px">';
        choice_html += '<input id="show_status" type="checkbox" ' + en_c + ' name="open" lay-skin="switch" lay-filter="switchTest" lay-text="ON|OFF">';
        choice_html += '</div></div>';
        choice_html += '<div class="layui-inline"><label class="layui-form-label form-imp">排序</label><div class="layui-input-inline" style="width: 150px">';
        choice_html += '<input id="sort_order" type="text" name="sort" placeholder="请输入数字" class="layui-input">';
        choice_html += '</div></div></div>';
        let belong_html = '<div class="layui-form-item"><label class="layui-form-label">上级菜单</label>';
        belong_html += '<div class="layui-input-block"  style="width: 411px;margin-left: 92px"><div class=" layui-form-select downpanel dp_menu">';
        belong_html += '<div class="layui-select-title">';
        let value = '';
        belong_html += '<input type="text" id="select_menu" readonly name="parent_menu" value="' + value + '" placeholder="选择上级菜单" class="layui-input">';
        belong_html += '<i class="layui-edge"></i></div><dl class="layui-anim layui-anim-upbit"><dd>';
        belong_html += '<ul id="menu_tree_drop"></ul></dd></dl></div></div></div>';
        form += menu_html + select_icon(row) + line_com + choice_html + belong_html + "</form>";
        return form;
    };

    function icon_list_dropdown() {
        $('#select_icons').click(function () {
            $('.select_icon').click(function () {
                $('#select_icons').val($(this).find('i').attr('class').replace('fa fa-', ''));
                $('.dp_icon').removeClass('layui-form-selected');
            });
            $('.dp_icon').toggleClass('layui-form-selected');
        })
    }

    function menu_tree_dropdown() {
        $('#select_menu').click(function () {
            $('#menu_tree_drop').html('');
            layui.tree.render({
                elem: '#menu_tree_drop',
                data: JSON.parse(JSON.stringify(menu_tree).replace(/name/g, "title")),
                id: 'menu_tree',
                click: function (e) {
                    $('#select_menu').val(e.data.title);
                    $('.dp_menu').removeClass('layui-form-selected');
                },
                showLine: false
            });
            $('.dp_menu').toggleClass('layui-form-selected');
        })
    }

    function input_inline_template(inputs) {
        let html = '<div class="layui-form-item">';
        for (let key in inputs) {
            html += '<div class="layui-inline"><label class="layui-form-label form-imp">' + inputs[key].tit + '</label><div class="layui-input-inline" style="width: 150px">';
            html += '<input type="text" name="name" id="' + key
                + '" autocomplete="off" value="' + inputs[key].value + '" placeholder="请输入' + inputs[key].place + '" class="layui-input"></div></div>';
        }
        html += '</div>';
        return html;
    }

    function select_icon(row) {
        let icon_html = '<div class="layui-form-item"><label class="layui-form-label">菜单图标</label>';
        icon_html += '<div class="layui-input-block" style="width: 411px;margin-left: 92px"><div class=" layui-form-select downpanel dp_icon">';
        icon_html += '<div class="layui-select-title">';
        let value = row !== undefined ? row.icon : 'folder';
        icon_html += '<input type="text" id="select_icons" readonly name="icons" value="' + value + '" placeholder="请选择图标" class="layui-input">';
        icon_html += '<i class="layui-edge"></i></div><dl class="layui-anim layui-anim-upbit" style="width: 100%"><dd>';
        icon_html += '<div class="layui-card">';
        for (let i = 0; i < icons.length; i++) {
            let _class = icons[i]._class;
            let name = icons[i].name.length > 10 ? icons[i].name.substring(0, 7) + "..." : icons[i].name;
            icon_html += '<button type="button" style="width: 120px" class="select_icon layui-btn layui-btn-primary layui-btn-sm">';
            icon_html += '<i class="fa ' + _class + '" ></i> ' + name + '</button>';
            if ((i + 1) % 3 === 0 && i > 1) {
                icon_html += "</br>";
            }
        }
        icon_html += '</div></dd></dl></div></div></div>';
        return icon_html
    }

    function input_template(id, tit, value, place) {
        let html = '<div class="layui-form-item"><label class="layui-form-label form-imp">' + tit + '</label>';
        html += '<div class="layui-input-block" style="width: 411px;margin-left: 92px">';
        html += '<input type="text" name="name" id="' + id + '" autocomplete="off" value="' + value + '" placeholder="请输入' + place + '" class="layui-input">';
        html += '</div></div>';
        return html;
    }

    return {
        init: init
    }
});