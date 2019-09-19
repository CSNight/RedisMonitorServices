DROP TABLE IF EXISTS sys_role_user;
DROP TABLE IF EXISTS sys_permission_role;
DROP TABLE IF EXISTS sys_permission;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS sys_role;

CREATE TABLE sys_user
(
    id          VARCHAR(50)  NOT NULL,
    username    VARCHAR(200) NOT NULL,
    password    VARCHAR(200) NOT NULL,
    nick_name    varchar(100) NOT NULL,
    create_time  timestamp default now(),
    enabled     bit(1)       NOT NULL,
    email       varchar(100) NULL,
    phone       varchar(11)  NULL,
    last_login  timestamp    NULL,
    login_times int       default 0,
    PRIMARY KEY (id)
) ENGINE = INNODB;

CREATE TABLE sys_role
(
    id   VARCHAR(50)  NOT NULL,
    name VARCHAR(200) NOT NULL,
    PRIMARY KEY (id)
) ENGINE = INNODB;

CREATE TABLE sys_permission
(
    id          VARCHAR(50)  NOT NULL,
    name        VARCHAR(200) NOT NULL,
    description VARCHAR(200) DEFAULT NULL,
    url         VARCHAR(200) NOT NULL,
    pid         VARCHAR(50)  DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE = INNODB;

CREATE TABLE sys_role_user
(
    id      VARCHAR(50) NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    role_id VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    constraint sys_role_user_sys_user_id_fk
        foreign key (user_id) references sys_user (id)
            on update cascade on delete cascade,
    constraint sys_role_user_sys_role_id_fk
        foreign key (role_id) references sys_role (id)
            on update cascade on delete cascade
) ENGINE = INNODB;

create table sys_permission_role
(
    id            varchar(50) not null primary key,
    role_id       varchar(50) not null,
    permission_id varchar(50) not null,
    constraint sys_permission_role_sys_permission_id_fk
        foreign key (permission_id) references sys_permission (id)
            on update cascade on delete cascade,
    constraint sys_permission_role_sys_role_id_fk
        foreign key (role_id) references sys_role (id)
            on update cascade on delete cascade
) ENGINE = INNODB;