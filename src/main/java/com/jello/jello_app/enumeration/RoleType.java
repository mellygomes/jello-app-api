package com.jello.jello_app.enumeration;

import lombok.Getter;

@Getter
public enum RoleType {
    ROLE_USER("ROLE_USER"),
    ROLE_MODERATOR("ROLE_MODERATOR");

    private final String name;

    RoleType(String name) {
        this.name = name;
    }

}
