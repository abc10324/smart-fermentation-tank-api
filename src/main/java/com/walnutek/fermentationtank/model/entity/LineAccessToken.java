package com.walnutek.fermentationtank.model.entity;

import lombok.Data;

@Data
public class LineAccessToken {

    private int status;

    private String message;

    private String access_token;
}
