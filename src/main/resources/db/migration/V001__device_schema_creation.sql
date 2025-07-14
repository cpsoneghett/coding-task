create table device
(
    id         bigint       not null auto_increment,
    name       varchar(100) not null,
    brand      varchar(100) not null,
    state      varchar(20)  not null,
    dt_created datetime     not null default CURRENT_TIMESTAMP,

    primary key (id)
) engine = InnoDB
  default charset = utf8;


