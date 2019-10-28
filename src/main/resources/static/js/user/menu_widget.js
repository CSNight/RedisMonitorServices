define(function () {
    let menu_tree = null;
    let menu_select = null;
    let menu_list = null;
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
    };
    let tableLoad = function () {
        $('#org_tree').html('');
        //菜单目录树加载
        G.request.GET("/menus/get_menu_tree", "", true).then(function (resp) {
            if (resp.hasOwnProperty("message") && resp.status === 200) {
                menu_tree = resp.message;
            }
        });
        //菜单目录树列表请求
        G.request.GET("/menus/get_menu_list", "", true).then(function (resp) {
            if (resp.hasOwnProperty("message") && resp.status === 200) {
                menu_list = resp.message;
                tableInit(menu_list, "menu_tree");
            }
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
                {field: 'id', title: '名称', align: 'center', searchable: true},
                {field: 'name', title: '目录', searchable: true},
                {
                    field: 'enabled', title: '状态', align: 'center', formatter: function (value, row, index) {
                        if (value) {
                            return '<span class="layui-btn layui-btn-sm">正常</span>';
                        } else {
                            return '<span class="layui-btn layui-btn-disabled layui-btn-sm">锁定</span>';
                        }
                    }
                },
                {
                    field: 'create_time', title: '创建时间', align: 'center', formatter: function (value, row, index) {
                        return new Date(value).toLocaleString();
                    }
                },
                {field: 'create_user', title: '创建人', align: 'center'},
                {
                    field: 'operate',
                    title: '操作',
                    align: 'center',
                    events: {
                        'click .org_del': function (e, value, row, index) {
                            del(row);
                        },
                        'click .org_modify': function (e, value, row, index) {
                            update(row);
                        },
                        'click .org_lock': function (e, value, row, index) {
                            lock(row);
                        },
                        'click .org_unlock': function (e, value, row, index) {
                            unlock(row);
                        }
                    },
                    formatter: operateFormatter
                },
            ],
            search: true,
            clickToSelect: false,
            toolbar: '#toolbar',//工具栏
            toolbarAlign: 'left',//工具栏的位置
            treeShowField: 'id',
            parentIdField: 'pid',
            singleSelect: true,
            onResetView: function (data) {
                $table.treegrid({
                    initialState: 'collapsed',// 所有节点都折叠
                    treeColumn: 1,
                    expanderExpandedClass: 'tim-icons icon-simple-delete t-expend',  //图标样式
                    expanderCollapsedClass: 'tim-icons icon-simple-add t-collapsed',
                    onChange: function () {
                        $table.bootstrapTable('resetWidth');
                    }
                });
            },
            onClickRow: function (row, el, field) {
                org_select = row;
            },
            onSearch: function () {
                $table.treegrid('getAllNodes').treegrid('expand');
            }
        });
        //展开节点
        $table.treegrid('getAllNodes').treegrid('expand');

        //操作列表按钮模板
        function operateFormatter(value, row, index) {
            if (row.id === 1) {
                return '';
            }
            let lock_html = row.enabled ?
                '<button type="button" class="org_lock layui-btn layui-btn-warm layui-btn-sm"><i class="fa fa-lock" ></i></button>' :
                '<button type="button" class="org_unlock layui-btn layui-btn-sm"><i class="fa fa-unlock" ></i></button>';
            return [
                '<button type="button" class="org_del layui-btn layui-btn-danger layui-btn-sm"><i class="fa fa-trash" ></i></button>',
                '<button type="button" class="org_modify layui-btn layui-btn-normal  layui-btn-sm"><i class="fa fa-pencil" ></i></button>',
                lock_html
            ].join('');
        }
    }


    return {
        init: init
    }
});