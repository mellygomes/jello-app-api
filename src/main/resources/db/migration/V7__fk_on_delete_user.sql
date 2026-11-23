-- Excluir usuario dava erro 500: as FKs de auditoria (created_by / updated_by) bloqueavam o DELETE.
-- Agora elas viram NULL quando o usuario some, e posts/comentarios nao somem so porque alguem editou eles.

ALTER TABLE tb_user
    DROP CONSTRAINT fk_user_created_by,
    DROP CONSTRAINT fk_user_updated_by,
    ADD CONSTRAINT fk_user_created_by FOREIGN KEY (created_by) REFERENCES tb_user (id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_user_updated_by FOREIGN KEY (updated_by) REFERENCES tb_user (id) ON DELETE SET NULL;

ALTER TABLE tb_profile_picture
    DROP CONSTRAINT fk_profile_picture_created_by,
    DROP CONSTRAINT fk_profile_picture_updated_by,
    ADD CONSTRAINT fk_profile_picture_created_by FOREIGN KEY (created_by) REFERENCES tb_user (id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_profile_picture_updated_by FOREIGN KEY (updated_by) REFERENCES tb_user (id) ON DELETE SET NULL;

ALTER TABLE tb_profile_cover
    DROP CONSTRAINT fk_profile_cover_created_by,
    DROP CONSTRAINT fk_profile_cover_updated_by,
    ADD CONSTRAINT fk_profile_cover_created_by FOREIGN KEY (created_by) REFERENCES tb_user (id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_profile_cover_updated_by FOREIGN KEY (updated_by) REFERENCES tb_user (id) ON DELETE SET NULL;

ALTER TABLE tb_image_post
    DROP CONSTRAINT fk_profile_picture_created_by,
    DROP CONSTRAINT fk_profile_picture_updated_by,
    ADD CONSTRAINT fk_image_post_created_by FOREIGN KEY (created_by) REFERENCES tb_user (id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_image_post_updated_by FOREIGN KEY (updated_by) REFERENCES tb_user (id) ON DELETE SET NULL;

ALTER TABLE tb_post
    DROP CONSTRAINT fk_posts_user_created_by,
    DROP CONSTRAINT fk_posts_user_updated_by,
    ADD CONSTRAINT fk_posts_user_created_by FOREIGN KEY (created_by) REFERENCES tb_user (id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_posts_user_updated_by FOREIGN KEY (updated_by) REFERENCES tb_user (id) ON DELETE SET NULL;

ALTER TABLE tb_comment
    DROP CONSTRAINT fk_comment_user_created_by,
    DROP CONSTRAINT fk_comment_user_updated_by,
    ADD CONSTRAINT fk_comment_user_created_by FOREIGN KEY (created_by) REFERENCES tb_user (id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_comment_user_updated_by FOREIGN KEY (updated_by) REFERENCES tb_user (id) ON DELETE SET NULL;

ALTER TABLE tb_report_user
    DROP CONSTRAINT fk_report_user_created_by,
    DROP CONSTRAINT fk_report_user_updated_by,
    ADD CONSTRAINT fk_report_user_created_by FOREIGN KEY (created_by) REFERENCES tb_user (id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_report_user_updated_by FOREIGN KEY (updated_by) REFERENCES tb_user (id) ON DELETE SET NULL;

ALTER TABLE tb_report_post
    DROP CONSTRAINT fk_report_post_created_by,
    DROP CONSTRAINT fk_report_post_updated_by,
    ADD CONSTRAINT fk_report_post_created_by FOREIGN KEY (created_by) REFERENCES tb_user (id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_report_post_updated_by FOREIGN KEY (updated_by) REFERENCES tb_user (id) ON DELETE SET NULL;

-- Post classificado por moderador que sai: limpa o vinculo (antes ficava um id solto e quebrava a listagem)
UPDATE tb_post
SET moderator_classifier_id = NULL
WHERE moderator_classifier_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM tb_moderator m WHERE m.id = tb_post.moderator_classifier_id);

ALTER TABLE tb_post
    ADD CONSTRAINT fk_post_moderator_classifier FOREIGN KEY (moderator_classifier_id) REFERENCES tb_moderator (id) ON DELETE SET NULL;
