-- https://bcrypt-generator.com/
-- https://www.uuidgenerator.net/version4

-- insert into role (name) values ('ROLE_USER') on conflict (name) do nothing;
-- insert into role (name) values ('ROLE_ADMIN') on conflict (name) do nothing;
--
-- insert into sensor_user (name, password, username) values ('sjdms265', '1234', 'sjdms265') on conflict (name) do nothing;
-- insert into sensor_user (name, password, username) values ('admin', '1234', 'admin') on conflict (name) do nothing;

-- insert into sensor_user_roles (sensor_user_id, roles_id) values ((select id from sensor_user where name = 'sjdms265'), (select id from role where name = 'ROLE_USER')) on conflict (sensor_user_id, roles_id) do nothing;
-- insert into sensor_user_roles (sensor_user_id, roles_id) values ((select id from sensor_user where name = 'admin'), (select id from role where name = 'ROLE_ADMIN')) on conflict (sensor_user_id, roles_id) ;
-- insert into sensor_user_roles (sensor_user_id, roles_id) values ((select id from sensor_user where name = 'admin'), (select id from role where name = 'ROLE_USER')) on conflict (sensor_user_id, roles_id) ;

INSERT INTO role(name) SELECT 'ROLE_USER' WHERE NOT EXISTS (SELECT id FROM role WHERE name = 'ROLE_USER');
INSERT INTO role(name) SELECT 'ROLE_ADMIN' WHERE NOT EXISTS (SELECT id FROM role WHERE name = 'ROLE_ADMIN');

INSERT INTO sensor_user (name, password, username) SELECT 'sjdms265', '1234', 'sjdms265' WHERE NOT EXISTS (SELECT id FROM sensor_user WHERE username = 'sjdms265');
INSERT INTO sensor_user (name, password, username) SELECT 'admin', '1234', 'admin' WHERE NOT EXISTS (SELECT id FROM sensor_user WHERE username = 'admin');

-- INSERT INTO sensor_user_roles (username, role) SELECT 'sjdms265',  'ROLE_USER' WHERE NOT EXISTS (SELECT 1 FROM sensor_user_roles WHERE username = 'sjdms265' and role = 'ROLE_USER');
-- INSERT INTO sensor_user_roles (username, role) SELECT 'admin',  'ROLE_ADMIN' WHERE NOT EXISTS (SELECT 1 FROM sensor_user_roles WHERE username = 'admin' and role = 'ROLE_USER');
-- INSERT INTO sensor_user_roles (username, role) SELECT 'admin',  'ROLE_USER' WHERE NOT EXISTS (SELECT 1 FROM sensor_user_roles WHERE username = 'admin' and role = 'ROLE_USER');

