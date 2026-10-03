-- Criação da Sequence Global para os IDs
CREATE SEQUENCE IF NOT EXISTS primary_key_seq START WITH 1 INCREMENT BY 1;

-- Tabela de Usuários (User.java)
CREATE TABLE public.tb_user
(
    id                 BIGINT PRIMARY KEY           DEFAULT nextval('primary_key_seq'),
    -- ID de referencia herdado da super classe Auditable para ser usado nas requisicoes
    reference_id       UUID UNIQUE         NOT NULL,
    email              VARCHAR(255) UNIQUE NOT NULL,
    first_name         VARCHAR(255)        NOT NULL,
    last_name          VARCHAR(255),
    bio                VARCHAR(255),
    password           VARCHAR(255)        NOT NULL,
    username           VARCHAR(255) UNIQUE NOT NULL,
    enabled            BOOLEAN             NOT NULL DEFAULT TRUE,
    last_login         TIMESTAMPTZ                  DEFAULT CURRENT_TIMESTAMP,
    banned             BOOLEAN             NOT NULL DEFAULT FALSE,
    profile_picture_id BIGINT UNIQUE,
    profile_cover_id   BIGINT UNIQUE,

    -- Colunas de auditoria herdadas da supe classe Auditable
    created_at         TIMESTAMPTZ         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMPTZ         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by         BIGINT,
    updated_by         BIGINT,

    CONSTRAINT fk_user_created_by FOREIGN KEY (created_by) REFERENCES tb_user (id),
    CONSTRAINT fk_user_updated_by FOREIGN KEY (updated_by) REFERENCES tb_user (id)
);

-- Tabela de Imagem de perfil de usuário (UserAvatar.java)
CREATE TABLE public.tb_profile_picture
(
    id           BIGINT PRIMARY KEY    DEFAULT nextval('primary_key_seq'),
    -- ID de referencia herdado da super classe Auditable para ser usado nas requisicoes
    reference_id UUID UNIQUE  NOT NULL,
    file_name    VARCHAR(255) NOT NULL,
    file_type    VARCHAR(100) NOT NULL,
    file_size    BIGINT       NOT NULL,
    data         BYTEA        NOT NULL,

    -- Colunas de auditoria herdadas da supe classe Auditable
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by   BIGINT,
    updated_by   BIGINT,

    CONSTRAINT fk_profile_picture_created_by FOREIGN KEY (created_by) REFERENCES tb_user (id),
    CONSTRAINT fk_profile_picture_updated_by FOREIGN KEY (updated_by) REFERENCES tb_user (id)
);

-- Tabela de Imagem de capa de usuário (UserCover.java)
CREATE TABLE public.tb_profile_cover
(
    id           BIGINT PRIMARY KEY    DEFAULT nextval('primary_key_seq'),
    -- ID de referencia herdado da super classe Auditable para ser usado nas requisicoes
    reference_id UUID UNIQUE  NOT NULL,
    file_name    VARCHAR(255) NOT NULL,
    file_type    VARCHAR(100) NOT NULL,
    file_size    BIGINT       NOT NULL,
    data         BYTEA        NOT NULL,

    -- Colunas de auditoria herdadas da super classe Auditable
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by   BIGINT,
    updated_by   BIGINT,

    CONSTRAINT fk_profile_cover_created_by FOREIGN KEY (created_by) REFERENCES tb_user (id),
    CONSTRAINT fk_profile_cover_updated_by FOREIGN KEY (updated_by) REFERENCES tb_user (id)
);

-- Adiciona as constraints de FK de imagens de perfil e capa na tabela de usuarios
ALTER TABLE tb_user
    ADD CONSTRAINT fk_user_profile_picture FOREIGN KEY (profile_picture_id) REFERENCES tb_profile_picture (id);
ALTER TABLE tb_user
    ADD CONSTRAINT fk_user_profile_cover FOREIGN KEY (profile_cover_id) REFERENCES tb_profile_cover (id);

-- Tabela de Cargos da aplicação (Role.java)
CREATE TABLE public.tb_role
(
    id   BIGINT PRIMARY KEY DEFAULT nextval('primary_key_seq'),
    name varchar(255) NOT NULL
);

-- Tabela auxiliar de usuários <-> roles
CREATE TABLE public.tb_user_role
(
    role_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,

    PRIMARY KEY (user_id, role_id),

    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES tb_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES tb_role (id) ON DELETE CASCADE
);

-- Tabela de Postagens (Post.java)
CREATE TABLE public.tb_post
(
    id                      BIGINT PRIMARY KEY    DEFAULT nextval('primary_key_seq'),
    reference_id            UUID UNIQUE  NOT NULL,
    user_id                 BIGINT       NOT NULL,
    moderator_classifier_id BIGINT,
    title                   VARCHAR(255) NOT NULL,
    content                 VARCHAR(255),
    ai_classified           BOOLEAN      NOT NULL DEFAULT FALSE,

    -- Colunas de auditoria herdadas da super classe Auditable
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by              BIGINT,
    updated_by              BIGINT,

    CONSTRAINT fk_posts_user FOREIGN KEY (user_id) REFERENCES tb_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_posts_user_created_by FOREIGN KEY (created_by) REFERENCES tb_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_posts_user_updated_by FOREIGN KEY (updated_by) REFERENCES tb_user (id) ON DELETE CASCADE
);

-- Tabela de comentários de postagens (Comment.java)
CREATE TABLE public.tb_comment
(
    id                  BIGINT PRIMARY KEY    DEFAULT nextval('primary_key_seq'),
    original_comment_id BIGINT,
    reference_id        UUID UNIQUE  NOT NULL,
    post_id             BIGINT       NOT NULL,
    user_id             BIGINT       NOT NULL,
    content             VARCHAR(255) NOT NULL,

    -- Colunas de auditoria herdadas da super classe Auditable
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          BIGINT,
    updated_by          BIGINT,

    CONSTRAINT fk_comment_post FOREIGN KEY (post_id) REFERENCES tb_post (id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES tb_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_user_created_by FOREIGN KEY (created_by) REFERENCES tb_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_user_updated_by FOREIGN KEY (updated_by) REFERENCES tb_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_parent FOREIGN KEY (original_comment_id) REFERENCES tb_comment (id) ON DELETE CASCADE
);

-- Tabela de imagens das postagens (Image.java)
CREATE TABLE public.tb_image_post
(
    id           BIGINT PRIMARY KEY    DEFAULT nextval('primary_key_seq'),
    -- ID de referencia herdado da super classe Auditable para ser usado nas requisicoes
    reference_id UUID UNIQUE  NOT NULL,
    post_id      BIGINT       NOT NULL,
    file_name    VARCHAR(255) NOT NULL,
    file_type    VARCHAR(100) NOT NULL,
    file_size    BIGINT       NOT NULL,
    data         BYTEA        NOT NULL,

    -- Colunas de auditoria herdadas da supe classe Auditable
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by   BIGINT,
    updated_by   BIGINT,

    CONSTRAINT fk_images_post FOREIGN KEY (post_id) REFERENCES tb_post (id) ON DELETE CASCADE,
    CONSTRAINT fk_profile_picture_created_by FOREIGN KEY (created_by) REFERENCES tb_user (id),
    CONSTRAINT fk_profile_picture_updated_by FOREIGN KEY (updated_by) REFERENCES tb_user (id)
);

-- Tabela auxiliar de postagens de marcadas como IA (PostAiVote.java)
CREATE TABLE public.tb_vote_ai
(
    id         BIGINT PRIMARY KEY   DEFAULT nextval('primary_key_seq'),
    post_id    BIGINT      NOT NULL,
    user_id    BIGINT      NOT NULL,
    vote_ai    BOOLEAN     NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_post_ai_votes_post FOREIGN KEY (post_id) REFERENCES tb_post (id) ON DELETE CASCADE,
    CONSTRAINT fk_post_ai_votes_user FOREIGN KEY (user_id) REFERENCES tb_user (id) ON DELETE CASCADE,
    CONSTRAINT uk_vote_ai_post_user UNIQUE (post_id, user_id)
);

-- Tabela de seguidores (Follow.java)
CREATE TABLE public.tb_follow
(
    id           BIGINT PRIMARY KEY   DEFAULT nextval('primary_key_seq'),
    follower_id  BIGINT      NOT NULL,
    following_id BIGINT      NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_follower FOREIGN KEY (follower_id) REFERENCES tb_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_following FOREIGN KEY (following_id) REFERENCES tb_user (id) ON DELETE CASCADE,
    CONSTRAINT unique_follow_relationship UNIQUE (follower_id, following_id),
    CONSTRAINT check_self_follow CHECK (follower_id <> following_id)
);

-- Tabela de confirmações de usuários (Confirmation.java)
CREATE TABLE public.tb_confirmation
(
    id               BIGINT       NOT NULL PRIMARY KEY DEFAULT nextval('primary_key_seq'),
    user_id          BIGINT       NOT NULL UNIQUE,
    confirmation_key VARCHAR(255) NOT NULL UNIQUE,
    created_at       TIMESTAMPTZ  NOT NULL             DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_confirmation_user FOREIGN KEY (user_id) REFERENCES tb_user (id) ON DELETE CASCADE
);

-- Tabela de moderadores (Moderator.java)
CREATE TABLE public.tb_moderator
(
    id      BIGINT PRIMARY KEY DEFAULT nextval('primary_key_seq'),
    user_id BIGINT UNIQUE NOT NULL,

    CONSTRAINT fk_moderator_user FOREIGN KEY (user_id) REFERENCES tb_user (id) ON DELETE CASCADE
);

-- Tabela de denúncia de usuários (UserReport.java)
CREATE TABLE public.tb_report_user
(
    id                   BIGINT PRIMARY KEY   DEFAULT nextval('primary_key_seq'),
    -- ID de referencia herdado da super classe Auditable para ser usado nas requisicoes
    reference_id         UUID UNIQUE NOT NULL,
    reported_user_id     BIGINT      NOT NULL,
    reporter_user_id     BIGINT      NOT NULL,
    moderator_analyst_id BIGINT      NOT NULL,
    is_approved          BOOLEAN     NOT NULL DEFAULT FALSE,

    -- Colunas de auditoria herdadas da super classe Auditable
    created_at           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by           BIGINT,
    updated_by           BIGINT,

    CONSTRAINT fk_report_user_created_by FOREIGN KEY (created_by) REFERENCES tb_user (id),
    CONSTRAINT fk_report_user_updated_by FOREIGN KEY (updated_by) REFERENCES tb_user (id),
    CONSTRAINT fk_reported_user FOREIGN KEY (reported_user_id) REFERENCES tb_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_reporter_user FOREIGN KEY (reporter_user_id) REFERENCES tb_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_moderator_analyst FOREIGN KEY (moderator_analyst_id) REFERENCES tb_moderator (id) ON DELETE CASCADE
);

-- Tabela de denúncia de posts (PostReport.java)
CREATE TABLE public.tb_report_post
(
    id                   BIGINT PRIMARY KEY   DEFAULT nextval('primary_key_seq'),
    -- ID de referencia herdado da super classe Auditable para ser usado nas requisicoes
    reference_id         UUID UNIQUE NOT NULL,
    post_reported_id     BIGINT      NOT NULL,
    user_reporter_id     BIGINT      NOT NULL,
    moderator_analyst_id BIGINT      NOT NULL,
    is_approved          BOOLEAN     NOT NULL DEFAULT FALSE,

    -- Colunas de auditoria herdadas da super classe Auditable
    created_at           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by           BIGINT,
    updated_by           BIGINT,

    CONSTRAINT fk_report_post_created_by FOREIGN KEY (created_by) REFERENCES tb_user (id),
    CONSTRAINT fk_report_post_updated_by FOREIGN KEY (updated_by) REFERENCES tb_user (id),
    CONSTRAINT fk_post_reported FOREIGN KEY (post_reported_id) REFERENCES tb_post (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_reporter FOREIGN KEY (user_reporter_id) REFERENCES tb_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_moderator_analyst FOREIGN KEY (moderator_analyst_id) REFERENCES tb_moderator (id) ON DELETE CASCADE
);

-- Tabela de curtidas (Post#likes)
CREATE TABLE public.tb_like
(
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,

    PRIMARY KEY (post_id, user_id),
    CONSTRAINT fk_post FOREIGN KEY (post_id) REFERENCES tb_post (id) ON DELETE CASCADE,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES tb_user (id) ON DELETE CASCADE
);

-- Tabela de tag (Tag.java)
CREATE TABLE public.tb_tag
(
    id    BIGINT PRIMARY KEY DEFAULT nextval('primary_key_seq'),
    name  VARCHAR(100) NOT NULL,
    -- Hexadecimal da cor da tag
    color VARCHAR(7)   NOT NULL
);

-- Tabela de relacionamento de tags e posts (Post#tags)
CREATE TABLE public.tb_post_tag
(
    post_id BIGINT NOT NULL,
    tag_id  BIGINT NOT NULL,

    PRIMARY KEY (post_id, tag_id),
    CONSTRAINT fk_post FOREIGN KEY (post_id) REFERENCES tb_post (id) ON DELETE CASCADE,
    CONSTRAINT fk_tag FOREIGN KEY (tag_id) REFERENCES tb_tag (id) ON DELETE CASCADE
);

-- TODO: ORGANIZACAO DE PACOTES, MIGRATIONS DE INSERT