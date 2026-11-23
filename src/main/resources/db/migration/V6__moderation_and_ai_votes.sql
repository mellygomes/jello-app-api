-- Posts banidos pelo moderador e classificacao "arte humana" (a de IA ja existia em ai_classified)
ALTER TABLE tb_post
    ADD COLUMN banned BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE tb_post
    ADD COLUMN human_classified BOOLEAN NOT NULL DEFAULT FALSE;

-- Os usuarios padrao do V3 foram criados como moderadores (role 2) sem querer, volta eles pra ROLE_USER
DELETE
FROM tb_user_role
WHERE role_id = (SELECT id FROM tb_role WHERE name = 'ROLE_MODERATOR')
  AND user_id IN (SELECT id FROM tb_user WHERE email IN ('ana@dev.com', 'bruno@db.com', 'carlos@marketing.com'));

INSERT INTO tb_user_role (role_id, user_id)
SELECT (SELECT id FROM tb_role WHERE name = 'ROLE_USER'), id
FROM tb_user
WHERE email IN ('ana@dev.com', 'bruno@db.com', 'carlos@marketing.com')
ON CONFLICT DO NOTHING;

-- Todo moderador precisa de uma linha em tb_moderator (usada nos reports e na classificacao)
INSERT INTO tb_moderator (user_id)
SELECT ur.user_id
FROM tb_user_role ur
         JOIN tb_role r ON r.id = ur.role_id
WHERE r.name = 'ROLE_MODERATOR'
  AND NOT EXISTS (SELECT 1 FROM tb_moderator m WHERE m.user_id = ur.user_id);

-- Indices pras consultas da fila de moderacao e do feed
CREATE INDEX IF NOT EXISTS idx_vote_ai_post ON tb_vote_ai (post_id);
CREATE INDEX IF NOT EXISTS idx_post_created_at ON tb_post (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_post_user ON tb_post (user_id);
