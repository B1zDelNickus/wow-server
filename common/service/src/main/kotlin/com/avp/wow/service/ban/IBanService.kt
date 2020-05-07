package com.avp.wow.service.ban

interface IBanService {
    fun isBanned(ip: String) : Boolean
}