package com.avp.wow.service.gs.enums

enum class GsRegisterResponse(val code: Int) {

    REGISTERED(1),
    ALREADY_REGISTERED(2),
    SYSTEM_ERROR(99)

}