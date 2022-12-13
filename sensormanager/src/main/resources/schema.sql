create table role
(
    id   bigint not null
        primary key,
    name varchar(255)
);

alter table role
    owner to postgres;



create table sensor_user
(
    id       bigint not null
        primary key,
    name     varchar(255),
    password varchar(255),
    username varchar(255)
);

alter table sensor_user
    owner to postgres;

create table sensor_user_roles
(
    sensor_user_id bigint not null
        constraint fkepgr0afcsb7wppiu695w0kx32
            references sensor_user,
    roles_id       bigint not null
        constraint fk4g6j8h040y6l9vta7nsp9uj0y
            references role
);

alter table sensor_user_roles
    owner to postgres;

CREATE SEQUENCE IF NOT EXISTS hibernate_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE sensor_endpoint
(
    id        BIGINT NOT NULL,
    user_id   VARCHAR(255),
    sensor_id VARCHAR(255),
    value     FLOAT,
    date      TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_sensorendpoint PRIMARY KEY (id)
);
