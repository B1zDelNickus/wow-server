package com.avp.wow.service.auth.enums

enum class AuthResponse {
    /**
     * that one is not being sent to client, it's only for internal use. Everything is OK
     */
    AUTHED,

    /**
     * System error.
     */
    SYSTEM_ERROR,

    /**
     * ID or password does not match.
     */
    INVALID_PASSWORD,

    /**
     * ID or password does not match.
     */
    INVALID_PASSWORD2,

    /**
     * Failed to load your account info.
     */
    FAILED_ACCOUNT_INFO,

    /**
     * Failed to load your social security number.
     */
    FAILED_SOCIAL_NUMBER,

    /**
     * No game server is registered to the authorization server.
     */
    NO_GS_REGISTERED,

    /**
     * You are already logged in.
     */
    ALREADY_LOGGED_IN,

    /**
     * The selected server is down and you cannot access it.
     */
    SERVER_DOWN,

    /**
     * The login information does not match the information you provided.
     */
    INVALID_PASSWORD3,

    /**
     * No Login info available.
     */
    NO_SUCH_ACCOUNT,

    /**
     * You have been disconnected from the server. Please try connecting again later.
     */
    DISCONNECTED,

    /**
     * You are not old enough to play the game.
     */
    AGE_LIMIT,

    /**
     * Double login attempts have been detected.
     */
    ALREADY_LOGGED_IN2,

    /**
     * You are already logged in.
     */
    ALREADY_LOGGED_IN3,

    /**
     * You cannot connect to the server because there are too many users right now.
     */
    SERVER_FULL,

    /**
     * The server is being normalized. Please try connecting again later.
     */
    GM_ONLY,

    /**
     * Please login to the game after you have changed your password.
     */
    ERROR_17,

    /**
     * You have used all your allowed playing time.
     */
    TIME_EXPIRED,

    /**
     * You have used up your allocated time and there is no time left on this account.
     */
    TIME_EXPIRED2,

    /**
     * System error.
     */
    SYSTEM_ERROR2,

    /**
     * The IP is already in use.
     */
    ALREADY_USED_IP,

    /**
     * You cannot access the game through this IP.
     */
    BAN_IP;
}