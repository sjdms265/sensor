-- https://bcrypt-generator.com/
--    1234: $2a$12$AGuzsTQSpZzi6AcBGyTqYuBDQGmgcF/KTGcTSkrKGUrwCQXYQm.Ty
-- https://www.uuidgenerator.net/version4

INSERT INTO role(id, name) SELECT '1a1dce14-1b50-4319-8b5f-76b783e149fa', 'ROLE_USER' WHERE NOT EXISTS (SELECT id FROM role WHERE name = 'ROLE_USER');
INSERT INTO role(id, name) SELECT '8fc01278-2b3e-4a93-aa52-fbd0cbff4885', 'ROLE_ADMIN' WHERE NOT EXISTS (SELECT id FROM role WHERE name = 'ROLE_ADMIN');

INSERT INTO sensor_user (id, name, password, username) SELECT '4e8c40af-80e2-42e0-812e-3f6dc5ee4bd6', 'sjdms265', '$2a$12$AGuzsTQSpZzi6AcBGyTqYuBDQGmgcF/KTGcTSkrKGUrwCQXYQm.Ty', 'sjdms265' WHERE NOT EXISTS (SELECT id FROM sensor_user WHERE username = 'sjdms265');
INSERT INTO sensor_user (id, name, password, username) SELECT '61d05da2-a622-4418-bc19-fedf20973e5d', 'admin', '$2a$12$AGuzsTQSpZzi6AcBGyTqYuBDQGmgcF/KTGcTSkrKGUrwCQXYQm.Ty', 'admin' WHERE NOT EXISTS (SELECT id FROM sensor_user WHERE username = 'admin');

INSERT INTO sensor_user_roles (sensor_user_id, roles_id) SELECT '4e8c40af-80e2-42e0-812e-3f6dc5ee4bd6',  '1a1dce14-1b50-4319-8b5f-76b783e149fa' WHERE NOT EXISTS (SELECT 1 FROM sensor_user_roles WHERE sensor_user_id = '4e8c40af-80e2-42e0-812e-3f6dc5ee4bd6' and roles_id = '1a1dce14-1b50-4319-8b5f-76b783e149fa');
INSERT INTO sensor_user_roles (sensor_user_id, roles_id) SELECT '61d05da2-a622-4418-bc19-fedf20973e5d',  '1a1dce14-1b50-4319-8b5f-76b783e149fa' WHERE NOT EXISTS (SELECT 1 FROM sensor_user_roles WHERE sensor_user_id = '61d05da2-a622-4418-bc19-fedf20973e5d' and roles_id = '1a1dce14-1b50-4319-8b5f-76b783e149fa');
INSERT INTO sensor_user_roles (sensor_user_id, roles_id) SELECT '61d05da2-a622-4418-bc19-fedf20973e5d',  '8fc01278-2b3e-4a93-aa52-fbd0cbff4885' WHERE NOT EXISTS (SELECT 1 FROM sensor_user_roles WHERE sensor_user_id = '61d05da2-a622-4418-bc19-fedf20973e5d' and roles_id = '8fc01278-2b3e-4a93-aa52-fbd0cbff4885');

insert into sensor_spec (id, name, sensor_category) SELECT'sensor.10000db11e_h', 'Thermostat temperature', 0 WHERE NOT EXISTS (SELECT id FROM sensor_spec WHERE id = 'sensor.10000db11e_h');
insert into sensor_spec (id, name, sensor_category) SELECT'sensor.10000db11e_t', 'Thermostat humidity', 1 WHERE NOT EXISTS (SELECT id FROM sensor_spec WHERE id = 'sensor.10000db11e_t');
insert into sensor_spec (id, name, sensor_category) SELECT'sensor.10000db501_t', 'Water temperature', 0 WHERE NOT EXISTS (SELECT id FROM sensor_spec WHERE id = 'sensor.10000db501_t');
