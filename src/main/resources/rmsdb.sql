truncate table rms_log_rps;
truncate table rms_log_rcs;
truncate table rms_log_rks;
truncate table rms_log_ros;
truncate table rms_job_relation;
truncate table rms_jobs;
truncate table rms_instance;

drop table if exists rms_log_rps;
drop table if exists rms_log_rcs;
drop table if exists rms_log_rks;
drop table if exists rms_log_ros;
drop table if exists rms_job_relation;
drop table if exists rms_jobs;
drop table if exists rms_instance;

create table rms_instance
(
    id                varchar(50)       not null,
    server_ip         varchar(50)       null,
    server_port       int               null,
    instance_name     varchar(50)       null,
    cluster_enable    int               null,
    role              varchar(50)       null,
    redis_version     varchar(50)       null,
    redis_mode        varchar(50)       null,
    os                varchar(50)       null,
    arch_bits         int    default 64 null,
    process_id        int    default 0  null,
    uptime_in_seconds bigint default 0  null,
    executable        varchar(200)      null,
    config_file       varchar(200)      null,
    constraint id_unique
        unique (id)
) comment 'redis instances' engine = innodb;

alter table rms_instance
    add primary key (id);

create table rms_jobs
(
    id           varchar(50)  not null,
    ins_id       varchar(50)  not null,
    job_name     varchar(50)  not null,
    job_group    varchar(100) not null,
    trigger_type int          not null,
    job_func     varchar(100) not null,
    job_class    varchar(100) not null,
    job_describe varchar(100) null,
    job_config   varchar(500) not null,
    constraint rms_jobs_id_uindex
        unique (id),
    constraint rms_jobs_rms_instance_id_fk
        foreign key (ins_id) references rms_instance (id)
            on update cascade on delete cascade
) comment 'redis monitor jobs' engine = innodb;

alter table rms_jobs
    add primary key (id);

create table rms_job_relation
(
    id      varchar(50) not null,
    inc_key varchar(50) null,
    job_key varchar(50) null,
    constraint rms_job_relation_id_uindex
        unique (id),
    constraint rms_job_relation_rms_instance_id_fk
        foreign key (inc_key) references rms_instance (id)
            on update cascade on delete cascade,
    constraint rms_job_relation_rms_jobs_id_fk
        foreign key (job_key) references rms_jobs (id)
            on update cascade on delete cascade
) comment 'redis monitor instance job relations' engine = innodb;

alter table rms_job_relation
    add primary key (id);

create table rms_log_rps
(
    id             varchar(50)      not null,
    instance_id    varchar(50)      not null,
    tm             bigint default 0 not null comment 'timestamp',
    mem_us         bigint default 0 null comment 'data mem hold',
    mem_rs         bigint default 0 null comment 'physic mem hold',
    mem_frag_ratio double default 0 null comment 'mem fragmentation ratio',
    cpu_uu         double default 0 null comment 'cpu user usage',
    cpu_su         double default 0 null comment 'data sys usage',
    ioi            bigint default 0 null comment 'total net input bytes',
    ioo            bigint default 0 null comment 'total net output bytes',
    io_is          double default 0 null comment 'instantaneous input kbps',
    io_os          double default 0 null comment 'instantaneous input kbps',
    constraint rms_log_rps_rms_instance_id_fk
        foreign key (instance_id) references rms_instance (id)
            on update cascade on delete cascade
) comment 'redis physic status' engine = innodb;

create unique index rms_log_rps_id_uindex
    on rms_log_rps (id);

alter table rms_log_rps
    add constraint rms_log_rps_pk
        primary key (id);

create table rms_log_rcs
(
    id             varchar(50)      not null,
    instance_id    varchar(50)      not null,
    tm             bigint default 0 not null comment 'timestamp',
    cli_con        int    default 0 null comment 'connected clients',
    cli_blo        int    default 0 null comment 'blocked clients',
    reject_cons    bigint default 0 null comment 'reject connections',
    total_cons_rec bigint default 0 null comment 'total connections received',
    constraint rms_log_rcs_rms_instance_id_fk
        foreign key (instance_id) references rms_instance (id)
            on update cascade on delete cascade
) comment 'redis clients status' engine = innodb;

create unique index rms_log_rcs_id_uindex
    on rms_log_rcs (id);

alter table rms_log_rcs
    add constraint rms_log_rcs_pk
        primary key (id);

create table rms_log_rks
(
    id          varchar(50)      not null,
    instance_id varchar(50)      not null,
    tm          bigint default 0 not null comment 'timestamp',
    key_size    bigint default 0 null comment 'key count',
    exp_keys    int    default 0 null comment 'expired keys',
    exp_kps     double default 0 null comment 'expired keys per second',
    evc_keys    bigint default 0 null comment 'expired keys',
    evc_kps     double default 0 null comment 'expired keys per second',
    ksp_hits    bigint default 0 null comment 'keyspace hits',
    ksp_hits_ps double default 0 null comment 'keyspace hits per second',
    ksp_miss    bigint default 0 null comment 'keyspace miss',
    ksp_miss_ps double default 0 null comment 'keyspace miss per second',
    cmd_count   bigint default 0 null comment 'total commands proceed',
    cmd_ps      double default 0 null comment 'commands proceed per second',
    ps_chl      int    default 0 null comment 'pubsub channels',
    ps_pat      int    default 0 null comment 'pubsub patterns',
    constraint rms_log_rks_rms_instance_id_fk
        foreign key (instance_id) references rms_instance (id)
            on update cascade on delete cascade
) comment 'redis keys status' engine = innodb;

create unique index rms_log_rks_id_uindex
    on rms_log_rks (id);

alter table rms_log_rks
    add constraint rms_log_rks_pk
        primary key (id);

create table rms_log_ros
(
    instance_id varchar(50)      not null,
    tm          bigint default 0 not null comment 'timestamp',
    type        varchar(50)      not null comment 'command type',
    calls       bigint default 0 null comment 'command calls',
    use_c       bigint default 0 null comment 'total cpu time cost(ms)',
    use_pc      double default 0 null comment 'per command cpu time cost(ms)',
    constraint rms_log_ros_rms_instance_id_fk
        foreign key (instance_id) references rms_instance (id)
            on update cascade on delete cascade
) comment 'redis commands status' engine = innodb;

commit;