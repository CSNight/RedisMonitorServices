define(function (require) {
    let org_tree = null;
    let org_select = null;
    let org_list = null;
    let init = function () {
        setHtmlFrame();
        //表格数据加载
        tableLoad();
        //绑定表格外部添加按钮事件
        bind_events();
    };

    //初始化内容
    let setHtmlFrame = function () {
        let html = '<div class="row">';
        html += '<div class="col-lg"><div id="toolbar">';
        html += '<button type="button" id="new_org" class="layui-btn layui-btn-normal"><i class="fa fa-plus"></i>新增部门</button></div><table id="org_tree"></table></div>';
        html += "</div>";
        $('#main-area').html(html);

    };
    let tableLoad = function () {
        $('#org_tree').html('');
        //部门目录树加载
        G.request.GET("/org/get_org_tree", "", true).then(function (resp) {
            if (resp.hasOwnProperty("message") && resp.status === 200) {
                org_tree = resp.message;
            }
        });
        //部门目录树列表请求
        G.request.GET("/org/get_org_list", "", true).then(function (resp) {
            if (resp.hasOwnProperty("message") && resp.status === 200) {
                org_list = resp.message;
                tableInit(org_list, "org_tree");
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

    function bind_events() {
        $('#new_org').click(function add(id) {
            layer.open({
                title: "添加部门",
                content: setNewOrgDialog()
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
                    org_tree_dropdown();
                }
            });
        });
    }

    function del(row) {
        layer.confirm("本操作将连带所属子部门一起删除，是否删除该部门？", {icon: 0, title: ["警告", "color:red"]}, function (index) {
            layer.close(index);
            request_del(row);
        })
    }

    function update(row) {
        let dept_old_name = undefined;
        for (let i = 0; i < org_list.length; i++) {
            if (org_list[i].id === row.pid) {
                dept_old_name = org_list[i].name;
            }
        }
        layer.open({
            title: '修改部门',
            content: setNewOrgDialog(row.name, row.enabled, dept_old_name)
            , btn: ['修改', '取消']
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
                        id: row.id,
                        name: dept_name,
                        enabled: enabled,
                        pid: dept_pid,
                        create_user: row.create_user,
                        create_time: row.create_time
                    };
                    request_update(org_ent, index);
                } else {
                    layer.msg('部门名称不能为空', {
                        time: 2000, icon: 0
                    });
                }

            }, cancel: function () {
            }, success: function () {
                layui.form.render();
                org_tree_dropdown();
            }
        });
    }

    function unlock(row) {
        let org_ent = {
            id: row.id,
            name: row.name,
            enabled: true,
            pid: row.pid,
            create_user: row.create_user,
            create_time: row.create_time
        };
        layer.confirm("本操作将连带所属子部门一起解锁，是否继续？", {icon: 0, title: ["警告", "color:orange"]}, function (index) {
            request_update(org_ent, index);
        })

    }

    function lock(row) {
        let org_ent = {
            id: row.id,
            name: row.name,
            enabled: false,
            pid: row.pid,
            create_user: row.create_user,
            create_time: row.create_time
        };
        request_update(org_ent, -1);
    }

    let setNewOrgDialog = function (name, enable, dept_parent) {
        let form = '<form class="layui-form" action="" style="box-sizing:unset !important;">';
        let name_html = '<div class="layui-form-item"><label class="layui-form-label form-imp">部门名称</label>';
        name_html += '<div class="layui-input-block">';
        let dept_name = name ? name : '';
        name_html += '<input type="text" name="name" id="dept_name" autocomplete="off" value="' + dept_name + '" placeholder="请输入部门名称" class="layui-input">';
        name_html += '</div></div>';
        let status_html = '<div class="layui-form-item"><label class="layui-form-label">状态</label>';
        status_html += '<div class="layui-input-block">';
        let en_c = enable === true || enable === undefined ? 'checked' : '';
        let en_d = enable === false && enable !== undefined ? 'checked' : '';
        status_html += '<input class="dept_enable" type="radio" name="enabled" value="true" title="启用" ' + en_c + '>';
        status_html += '<input class="dept_disable" type="radio" name="enabled" value="false" title="禁用" ' + en_d + '>';
        status_html += '</div></div>';
        let belong_html = '<div class="layui-form-item"><label class="layui-form-label">上级部门</label>';
        belong_html += '<div class="layui-input-block"><div class=" layui-form-select downpanel">';
        belong_html += '<div class="layui-select-title">';
        let value = org_select === null ? 'Top' : org_select.name;
        value = dept_parent ? dept_parent : value;
        belong_html += '<input type="text" id="select_dept" readonly name="parent_dept" value="' + value + '" placeholder="选择部门" class="layui-input">';
        belong_html += '<i class="layui-edge"></i></div><dl class="layui-anim layui-anim-upbit"><dd>';
        belong_html += '<ul id="org_tree_drop"></ul></dd></dl></div></div></div>';
        form += name_html + status_html + belong_html + "</form>";
        return form;
    };

    function org_tree_dropdown() {
        $('#select_dept').click(function () {
            $('#org_tree_drop').html('');
            layui.tree.render({
                elem: '#org_tree_drop',
                data: [JSON.parse(JSON.stringify(org_tree).replace(/name/g, "title"))],
                id: 'demoId',
                click: function (e) {
                    $('#select_dept').val(e.data.title);
                    $('.downpanel').removeClass('layui-form-selected');
                },
                showLine: false
            });
            $('.downpanel').addClass('layui-form-selected');
        })
    }

    function request_add(org_ent, layer_index) {
        G.request.POST('/org/new_org', {'org_ent': JSON.stringify(org_ent)}).then(function (res) {
            layer.close(layer_index);
            if (res.hasOwnProperty("message") && res.status === 200) {
                layer.msg('添加成功', {
                    time: 2000, icon: 1
                });
                return;
            }
            layer.msg('插入失败', {
                time: 2000, icon: 0
            });
        }).finally(function () {
            tableLoad();
        });
    }

    function request_update(org_ent, layer_index) {
        G.request.PUT('/org/modify_org', {'org_ent': JSON.stringify(org_ent)}).then(function (res) {
            layer.close(layer_index);
            if (res.hasOwnProperty("message") && res.status === 200) {
                layer.msg('修改成功', {
                    time: 2000, icon: 1
                });
                return;
            }
            layer.msg('修改失败', {
                time: 2000, icon: 0
            });
        }).finally(function () {
            tableLoad();
        });
    }

    function request_del(row) {
        G.request.DELETE("/org/delete_org/" + row.id).then(function (resp) {
            if (resp.hasOwnProperty("message") && resp.status === 200) {
                if (resp.message === "success") {
                    layer.msg('删除成功', {
                        time: 2000, icon: 1
                    });
                    return;
                }
            }
            layer.msg('删除失败', {
                time: 2000, icon: 0
            });
        }).catch(function () {
            layer.msg('删除失败', {
                time: 2000, icon: 0
            });
        }).finally(function () {
            tableLoad();
        })
    }

    return {
        init: init
    }
})
;