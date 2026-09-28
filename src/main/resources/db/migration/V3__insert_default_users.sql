INSERT INTO tb_user(reference_id,
                    email,
                    first_name,
                    last_name,
                    password,
                    username,
                    profile_picture_id,
                    profile_cover_id,
                    enabled,
                    banned)
VALUES (gen_random_uuid(),
        'ana@dev.com',
        'Ana',
        'Dev',
        '$2a$10$2JT8fttnQZr6JdvE2QP42OGUIglYcOCf5EeEHIpdt/UBPZPXAw2a6',
        'aninhadev',
        null,
        null,
        true,
        false);

INSERT INTO tb_user (reference_id,
                     email,
                     first_name,
                     last_name,
                     password,
                     username,
                     profile_picture_id,
                     profile_cover_id,
                     enabled,
                     banned)
VALUES (gen_random_uuid(), 'bruno@db.com', 'Bruno', 'Db',
        '$2a$10$2JT8fttnQZr6JdvE2QP42OGUIglYcOCf5EeEHIpdt/UBPZPXAw2a6', 'debenelson', null, null, true, false);

INSERT INTO tb_user (reference_id,
                     email,
                     first_name,
                     last_name,
                     password,
                     username,
                     profile_picture_id,
                     profile_cover_id,
                     enabled,
                     banned)
VALUES (gen_random_uuid(), 'carlos@marketing.com', 'Carlos', 'Marketing',
        '$2a$10$2JT8fttnQZr6JdvE2QP42OGUIglYcOCf5EeEHIpdt/UBPZPXAw2a6', 'carlosmarques', null, null, true, false);

INSERT INTO tb_user_role (role_id, user_id)
SELECT 2, id
FROM tb_user
WHERE email = 'ana@dev.com';

INSERT INTO tb_user_role (role_id, user_id)
SELECT 2, id
FROM tb_user
WHERE email = 'bruno@db.com';

INSERT INTO tb_user_role (role_id, user_id)
SELECT 2, id
FROM tb_user
WHERE email = 'carlos@marketing.com';
