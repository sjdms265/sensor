insert into role (id, name) values (1, 'ROLE_USER');
insert into role (id, name) values (2, 'ROLE_ADMIN');

insert into sensor_user (id, name, password, username) values (1, 'sjdms265', 'sjdms265', '1234');
insert into sensor_user (id, name, password, username) values (2, 'sjdms265', 'sjdms265', '1234');
insert into sensor_user (id, name, password, username) values (3, 'admin', 'admin', '1234');

insert into sensor_user_roles (sensor_user_id, roles_id) values ('sjdms265', 'ROLE_USER');
insert into sensor_user_roles (sensor_user_id, roles_id) values ('admin', 'ROLE_ADMIN');
insert into sensor_user_roles (sensor_user_id, roles_id) values ('admin', 'ROLE_USER');

