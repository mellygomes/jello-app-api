package com.jello.jello_app.enumeration;

import com.jello.jello_app.post.model.Post;

public enum PostClassification {
    NONE, AI, HUMAN;

    // Descobre a classificacao atual do post pelas duas flags
    public static PostClassification of(Post post) {
        if (post.isAiClassified()) return AI;
        if (post.isHumanClassified()) return HUMAN;
        return NONE;
    }
}
