DROP TABLE IF EXISTS sys_role_user;
DROP TABLE IF EXISTS sys_permission_role;
DROP TABLE IF EXISTS sys_permission;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS persistent_logins;

create table persistent_logins
(
    username  varchar(64) not null,
    series    varchar(64) not null
        primary key,
    token     varchar(64) not null,
    last_used timestamp   not null
);


create table sys_user
(
    id          varchar(50)                         not null,
    username    varchar(200)                        not null,
    password    varchar(200)                        not null,
    nick_name   varchar(100)                        not null,
    create_time timestamp default CURRENT_TIMESTAMP null,
    enabled     bit                                 not null,
    email       varchar(100)                        null,
    phone       varchar(11)                         null,
    last_login  timestamp                           null,
    login_times int       default 0                 null,
    constraint sys_user_id_uindex
        unique (id)
);

alter table sys_user
    add primary key (id);

create table sys_role
(
    id   varchar(50)  not null
        primary key,
    name varchar(200) not null
);

create table sys_permission
(
    id          varchar(50)  not null
        primary key,
    name        varchar(200) not null,
    description varchar(200) null,
    url         varchar(200) not null,
    pid         varchar(50)  null
) ENGINE = INNODB;

create table sys_role_user
(
    user_id varchar(50) not null,
    role_id varchar(50) not null,
    constraint sys_role_user_sys_role_id_fk
        foreign key (role_id) references sys_role (id)
            on update cascade on delete cascade,
    constraint sys_role_user_sys_user_id_fk
        foreign key (user_id) references sys_user (id)
            on update cascade on delete cascade
) ENGINE = INNODB;

create table sys_permission_role
(
    id            int(11) auto_increment not null primary key,
    role_id       varchar(50)            not null,
    permission_id varchar(50)            not null,
    constraint sys_permission_role_sys_permission_id_fk
        foreign key (permission_id) references sys_permission (id)
            on update cascade on delete cascade,
    constraint sys_permission_role_sys_role_id_fk
        foreign key (role_id) references sys_role (id)
            on update cascade on delete cascade
) ENGINE = INNODB;