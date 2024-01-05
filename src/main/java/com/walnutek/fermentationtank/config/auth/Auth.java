package com.walnutek.fermentationtank.config.auth;

public class Auth {
	
	private static final ThreadLocal<AuthUser> authUser = new ThreadLocal<>();

    public static AuthUser getAuthUser() {
        return Auth.authUser.get();
    }

    public static void setAuthUser(AuthUser authUser) {
        Auth.authUser.set(authUser);
    }

    public static void removeAuthUser() {
        Auth.authUser.remove();
    }
}
