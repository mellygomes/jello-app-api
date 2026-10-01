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
VALUES (gen_random_uuid(),
        'moderator@jello.com',
        'Moderator',
        'One',
        '$2a$10$2JT8fttnQZr6JdvE2QP42OGUIglYcOCf5EeEHIpdt/UBPZPXAw2a6',
        'moderator',
        null,
        null,
        true,
        false);

INSERT INTO tb_user_role (role_id, user_id)
SELECT 2, id
FROM tb_user
WHERE email = 'moderator@jello.com';