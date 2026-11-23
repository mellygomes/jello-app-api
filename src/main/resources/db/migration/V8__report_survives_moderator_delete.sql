-- Apagar um moderador apagava junto as denuncias que estavam com ele (FK com CASCADE e coluna NOT NULL).
-- Agora a denuncia fica, so sem analista (NULL), e outro moderador pode assumir ao analisar.

ALTER TABLE tb_report_post
    ALTER COLUMN moderator_analyst_id DROP NOT NULL,
    DROP CONSTRAINT fk_moderator_analyst,
    ADD CONSTRAINT fk_report_post_moderator_analyst FOREIGN KEY (moderator_analyst_id) REFERENCES tb_moderator (id) ON DELETE SET NULL;

ALTER TABLE tb_report_user
    ALTER COLUMN moderator_analyst_id DROP NOT NULL,
    DROP CONSTRAINT fk_moderator_analyst,
    ADD CONSTRAINT fk_report_user_moderator_analyst FOREIGN KEY (moderator_analyst_id) REFERENCES tb_moderator (id) ON DELETE SET NULL;
