insert into role (name) values ('ROLE_USER');
insert into role (name) values ('ROLE_ADMIN');

insert into sensor_user (name, password, username) values ('sjdms265', '1234', 'sjdms265');
insert into sensor_user (name, password, username) values ('admin', '1234', 'admin');

insert into sensor_user_roles (sensor_user_id, roles_id) values ((select id from sensor_user where name = 'sjdms265'), (select id from role where name = 'ROLE_USER'));
insert into sensor_user_roles (sensor_user_id, roles_id) values ((select id from sensor_user where name = 'admin'), (select id from role where name = 'ROLE_ADMIN'));
insert into sensor_user_roles (sensor_user_id, roles_id) values ((select id from sensor_user where name = 'admin'), (select id from role where name = 'ROLE_USER'));

