drop table if exists qrtz_fired_triggers;
drop table if exists qrtz_paused_trigger_grps;
drop table if exists qrtz_scheduler_state;
drop table if exists qrtz_locks;
drop table if exists qrtz_simple_triggers;
drop table if exists qrtz_simprop_triggers;
drop table if exists qrtz_cron_triggers;
drop table if exists qrtz_blob_triggers;
drop table if exists qrtz_triggers;
drop table if exists qrtz_job_details;
drop table if exists qrtz_calendars;
drop table if exists rms_job_relation;
drop table if exists rms_instance;
drop table if exists rms_jobs;
drop table if exists rms_log_rps;
drop table if exists rms_log_rcs;
drop table if exists rms_log_rks;
drop table if exists rms_log_ros;

create table qrtz_job_details
(
    sched_name        varchar(120) not null,
    job_name          varchar(190) not null,
    job_group         varchar(190) not null,
    description       varchar(250) null,
    job_class_name    varchar(250) not null,
    is_durable        varchar(1)   not null,
    is_nonconcurrent  varchar(1)   not null,
    is_update_data    varchar(1)   not null,
    requests_recovery varchar(1)   not null,
    job_data          blob         null,
    primary key (sched_name, job_name, job_group)
)
    engine = innodb;

create table qrtz_triggers
(
    sched_name     varchar(120) not null,
    trigger_name   varchar(190) not null,
    trigger_group  varchar(190) not null,
    job_name       varchar(190) not null,
    job_group      varchar(190) not null,
    description    varchar(250) null,
    next_fire_time bigint(13)   null,
    prev_fire_time bigint(13)   null,
    priority       integer      null,
    trigger_state  varchar(16)  not null,
    trigger_type   varchar(8)   not null,
    start_time     bigint(13)   not null,
    end_time       bigint(13)   null,
    calendar_name  varchar(190) null,
    misfire_instr  smallint(2)  null,
    job_data       blob         null,
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, job_name, job_group)
        references qrtz_job_details (sched_name, job_name, job_group)
)
    engine = innodb;

create table qrtz_simple_triggers
(
    sched_name      varchar(120) not null,
    trigger_name    varchar(190) not null,
    trigger_group   varchar(190) not null,
    repeat_count    bigint(7)    not null,
    repeat_interval bigint(12)   not null,
    times_triggered bigint(10)   not null,
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, trigger_name, trigger_group)
        references qrtz_triggers (sched_name, trigger_name, trigger_group)
)
    engine = innodb;

create table qrtz_cron_triggers
(
    sched_name      varchar(120) not null,
    trigger_name    varchar(190) not null,
    trigger_group   varchar(190) not null,
    cron_expression varchar(120) not null,
    time_zone_id    varchar(80),
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, trigger_name, trigger_group)
        references qrtz_triggers (sched_name, trigger_name, trigger_group)
)
    engine = innodb;

create table qrtz_simprop_triggers
(
    sched_name    varchar(120)   not null,
    trigger_name  varchar(190)   not null,
    trigger_group varchar(190)   not null,
    str_prop_1    varchar(512)   null,
    str_prop_2    varchar(512)   null,
    str_prop_3    varchar(512)   null,
    int_prop_1    int            null,
    int_prop_2    int            null,
    long_prop_1   bigint         null,
    long_prop_2   bigint         null,
    dec_prop_1    numeric(13, 4) null,
    dec_prop_2    numeric(13, 4) null,
    bool_prop_1   varchar(1)     null,
    bool_prop_2   varchar(1)     null,
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, trigger_name, trigger_group)
        references qrtz_triggers (sched_name, trigger_name, trigger_group)
)
    engine = innodb;

create table qrtz_blob_triggers
(
    sched_name    varchar(120) not null,
    trigger_name  varchar(190) not null,
    trigger_group varchar(190) not null,
    blob_data     blob         null,
    primary key (sched_name, trigger_name, trigger_group),
    index (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, trigger_name, trigger_group)
        references qrtz_triggers (sched_name, trigger_name, trigger_group)
)
    engine = innodb;

create table qrtz_calendars
(
    sched_name    varchar(120) not null,
    calendar_name varchar(190) not null,
    calendar      blob         not null,
    primary key (sched_name, calendar_name)
)
    engine = innodb;

create table qrtz_paused_trigger_grps
(
    sched_name    varchar(120) not null,
    trigger_group varchar(190) not null,
    primary key (sched_name, trigger_group)
)
    engine = innodb;

create table qrtz_fired_triggers
(
    sched_name        varchar(120) not null,
    entry_id          varchar(95)  not null,
    trigger_name      varchar(190) not null,
    trigger_group     varchar(190) not null,
    instance_name     varchar(190) not null,
    fired_time        bigint(13)   not null,
    sched_time        bigint(13)   not null,
    priority          integer      not null,
    state             varchar(16)  not null,
    job_name          varchar(190) null,
    job_group         varchar(190) null,
    is_nonconcurrent  varchar(1)   null,
    requests_recovery varchar(1)   null,
    primary key (sched_name, entry_id)
)
    engine = innodb;

create table qrtz_scheduler_state
(
    sched_name        varchar(120) not null,
    instance_name     varchar(190) not null,
    last_checkin_time bigint(13)   not null,
    checkin_interval  bigint(13)   not null,
    primary key (sched_name, instance_name)
)
    engine = innodb;

create table qrtz_locks
(
    sched_name varchar(120) not null,
    lock_name  varchar(40)  not null,
    primary key (sched_name, lock_name)
)
    engine = innodb;

create table rms_instance
(
    id                varchar(200)      not null,
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
    job_name     varchar(50)  not null,
    job_group    varchar(100) not null,
    trigger_type int          null,
    job_func     varchar(100) not null,
    job_class    varchar(100) null,
    job_describe varchar(100) null,
    constraint rms_jobs_id_uindex
        unique (id)
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
    io_os          double default 0 null comment 'instantaneous input kbps'
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
    total_cons_rec bigint default 0 null comment 'total connections received'
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
    ps_pat      int    default 0 null comment 'pubsub patterns'
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


create index idx_qrtz_j_req_recovery on qrtz_job_details (sched_name, requests_recovery);
create index idx_qrtz_j_grp on qrtz_job_details (sched_name, job_group);
create index idx_qrtz_t_j on qrtz_triggers (sched_name, job_name, job_group);
create index idx_qrtz_t_jg on qrtz_triggers (sched_name, job_group);
create index idx_qrtz_t_c on qrtz_triggers (sched_name, calendar_name);
create index idx_qrtz_t_g on qrtz_triggers (sched_name, trigger_group);
create index idx_qrtz_t_state on qrtz_triggers (sched_name, trigger_state);
create index idx_qrtz_t_n_state on qrtz_triggers (sched_name, trigger_name, trigger_group, trigger_state);
create index idx_qrtz_t_n_g_state on qrtz_triggers (sched_name, trigger_group, trigger_state);
create index idx_qrtz_t_next_fire_time on qrtz_triggers (sched_name, next_fire_time);
create index idx_qrtz_t_nft_st on qrtz_triggers (sched_name, trigger_state, next_fire_time);
create index idx_qrtz_t_nft_misfire on qrtz_triggers (sched_name, misfire_instr, next_fire_time);
create index idx_qrtz_t_nft_st_misfire on qrtz_triggers (sched_name, misfire_instr, next_fire_time, trigger_state);
create index idx_qrtz_t_nft_st_misfire_grp on qrtz_triggers (sched_name, misfire_instr, next_fire_time, trigger_group,
                                                             trigger_state);

create index idx_qrtz_ft_trig_inst_name on qrtz_fired_triggers (sched_name, instance_name);
create index idx_qrtz_ft_inst_job_req_rcvry on qrtz_fired_triggers (sched_name, instance_name, requests_recovery);
create index idx_qrtz_ft_j_g on qrtz_fired_triggers (sched_name, job_name, job_group);
create index idx_qrtz_ft_jg on qrtz_fired_triggers (sched_name, job_group);
create index idx_qrtz_ft_t_g on qrtz_fired_triggers (sched_name, trigger_name, trigger_group);
create index idx_qrtz_ft_tg on qrtz_fired_triggers (sched_name, trigger_group);

commit;