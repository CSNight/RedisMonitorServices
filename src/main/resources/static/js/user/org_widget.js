define(function (require) {
    let init = function () {
        setHtmlFrame();
    };
    //初始化内容
    let setHtmlFrame = function () {
        let html = '<div class="row">';
        html += '<div class="col-lg"><div id="toolbar">';
        html += '<button type="button" id="new_org" class="layui-btn layui-btn-normal"><i class="fa fa-plus"></i>新增部门</button></div><table id="org_tree"></table></div>';
        html += "</div>";
        $('#main-area').html(html);
        tableLoad();
        bind_events();
    };
    let tableLoad = function () {
        $('#org_tree').html('');
        G.request.GET("/org/get_org_list", "", true).then(function (resp) {
            let resp_obj = JSON.parse(resp);
            if (resp_obj.hasOwnProperty("message") && resp_obj.status === 200) {
                let org_tree = resp_obj.message;
                tableInit(org_tree, "org_tree");
            }
        });
    };

    function tableInit(data, dom) {
        let $table = $('#' + dom);
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
            clickToSelect: true,
            toolbar: '#toolbar',//工具栏
            toolbarAlign: 'left',//工具栏的位置
            treeShowField: 'id',
            parentIdField: 'pid',
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
            onSearch: function () {
                $table.treegrid('getAllNodes').treegrid('expand');
            }
        });
        $table.treegrid('getAllNodes').treegrid('expand');

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

    function bind_events() {
        $('#new_org').click(function add(id) {
            layer.open({
                content: 'test'
                , btn: ['添加', '取消']
                , yes: function (index, layero) {
                    console.log(layero);
                }, cancel: function () {
                }
            });
        });
    }

    function del(row) {
        layer.confirm("本操作将连带所属子部门一起删除，是否删除该部门？", {icon: 0, title: ["警告", "color:red"]}, function (index) {
            layer.close(index);
            G.request.DELETE("/org/delete_org/" + row.id).then(function (resp) {
                let resp_obj = JSON.parse(resp);
                if (resp_obj.hasOwnProperty("message") && resp_obj.status === 200) {
                    if (resp_obj.message === "success") {
                        layer.msg('删除成功', {
                            time: 2000, icon: 1
                        });
                    } else {
                        layer.msg('删除失败', {
                            time: 2000, icon: 0
                        });
                    }
                } else {
                    layer.msg('删除失败', {
                        time: 2000, icon: 0
                    });
                }
            }).catch(function () {
                layer.msg('删除失败', {
                    time: 2000, icon: 0
                });
            }).finally(function () {
                tableLoad();
            })
        })
    }

    function update(row) {
    }

    function unlock(row) {
    }

    function lock(row) {
    }

    let setNewOrgDialog = function () {
        let html = '<div class="modal modal-search fade" id="org_add_modal" role="dialog" aria-hidden="true">\n';
        html += '<div class="modal-dialog"  style="max-width: 450px" role="document"><div class="modal-content">';
        html += '<div class="modal-header justify-content-center"><button type="button" class="close" data-dismiss="modal" aria-hidden="true">';
        html += '<i class="tim-icons icon-simple-remove"></i></button><h6 class="title title-up">Modal title</h6></div>';
        html += '<div class="modal-body">';
        html += '';
        html += '</div>';
        html += '<div class="modal-footer"><button type="button" class="btn btn-default btn-sm">Add</button>';
        html += '<button type="button" class="btn btn-danger btn-sm" data-dismiss="modal">Close</button>';

        html += '</div></div></div></div>';
        $('#main-area').append(html);
    };
    return {
        init: init
    }
})
;