package com.avp.wow.network.ncrypt

import kotlin.experimental.and

/**
 * Blowfish Cipher - symmetric 16-round cipher, uses 64-bit blocks, 32-448 key size and key-dependend 32-bit four S-boxes
 * @author EvilSpirit
 */
class BlowfishCipher
/**
 * Default constructor. Initializes the Blowfish Cipher with blowfishKey
 * @param blowfishKey Blowfish Key
 */
    (blowfishKey: ByteArray) {
    /**
     * Blowfish cipher secret key
     */
    private var blowfishKey: ByteArray? = null
    /**
     * pArray
     */
    private val pArray: IntArray = IntArray(18)
    /**
     * sBoxes
     */
    private val sBoxes: Array<IntArray> = Array(4) { IntArray(256) }

    init {
        updateKey(blowfishKey)
    }

    /**
     * Update current blowfish key with the new one and reinitialize the P-array and S-boxes
     * @param blowfishKey new blowfish key
     */
    fun updateKey(blowfishKey: ByteArray) {
        this.blowfishKey = blowfishKey
        System.arraycopy(SBOX_INIT_0, 0, sBoxes[0], 0, 256)
        System.arraycopy(SBOX_INIT_1, 0, sBoxes[1], 0, 256)
        System.arraycopy(SBOX_INIT_2, 0, sBoxes[2], 0, 256)
        System.arraycopy(SBOX_INIT_3, 0, sBoxes[3], 0, 256)
        initArrays()
    }

    /**
     * Initialise the P-array and S-boxes
     */
    private fun initArrays() {
        var keyIndex = 0

        for (i in 0..17) {
            var data = 0

            for (j in 0..3) {
                data = data shl 8 or (blowfishKey!![keyIndex++].toInt() and 0xFF)

                if (keyIndex >= blowfishKey!!.size) {
                    keyIndex = 0
                }
            }

            pArray[i] = PARRAY_INIT[i] xor data
        }

        val b = ByteArray(8)

        run {
            var i = 0
            while (i < 18) {
                cipher(b)
                pArray[i] = byteArrayToInteger(b, 0)
                pArray[i + 1] = byteArrayToInteger(b, 4)
                i += 2
            }
        }

        for (i in 0..3) {
            initSBox(b, sBoxes[i])
        }
    }

    /**
     * @param b
     * @param sBox
     */
    private fun initSBox(b: ByteArray, sBox: IntArray) {
        var j = 0
        while (j < 256) {
            cipher(b)
            sBox[j] = byteArrayToInteger(b, 0)
            sBox[j + 1] = byteArrayToInteger(b, 4)
            j += 2
        }
    }

    /**
     * Cipher the given byte-array with Blowfish cipher
     * @param data byte array to be ciphered
     * @param offset byte array offset
     * @param length byte array length
     */
    @JvmOverloads
    fun cipher(data: ByteArray, offset: Int = 0, length: Int = data.size) {
        val blockNumber = length shr 3
        var p: Int

        for (k in 0 until blockNumber) {
            p = offset + (k shl 3)

            var xl = byteArrayToInteger(data, p)
            var xr = byteArrayToInteger(data, p + 4)
            var tmp: Int

            for (i in 0..15) {
                xl = xl xor pArray[i]
                xr = F(xl) xor xr
                tmp = xl
                xl = xr
                xr = tmp
            }

            tmp = xl
            xl = xr
            xr = tmp
            xr = xr xor pArray[16]
            xl = xl xor pArray[17]
            integerToByteArray(xl, data, p)
            integerToByteArray(xr, data, p + 4)
        }
    }

    /**
     * Decipher the given byte-array with Blowfish cipher
     * @param data byte array to be deciphered
     * @param offset byte array offset
     * @param length byte array length
     */
    @JvmOverloads
    fun decipher(data: ByteArray, offset: Int = 0, length: Int = data.size) {
        val blocks = length shr 3
        var p: Int

        for (k in 0 until blocks) {
            p = offset + (k shl 3)

            var lb = byteArrayToInteger(data, p)
            var rb = byteArrayToInteger(data, p + 4)
            var tmp: Int

            for (i in 17 downTo 2) {
                lb = lb xor pArray[i]
                rb = F(lb) xor rb
                tmp = lb
                lb = rb
                rb = tmp
            }

            tmp = lb
            lb = rb
            rb = tmp
            rb = rb xor pArray[1]
            lb = lb xor pArray[0]
            integerToByteArray(lb, data, p)
            integerToByteArray(rb, data, p + 4)
        }
    }

    /**
     * The round (Feistel) function of Blowfish cipher
     * @param x
     * @return y
     */
    private fun F(x: Int): Int {
        var x = x
        val a: Int
        val b: Int
        val c: Int
        val d: Int

        d = x and 0xFF
        x = x shr 8
        c = x and 0xFF
        x = x shr 8
        b = x and 0xFF
        x = x shr 8
        a = x and 0xFF

        var y = sBoxes[0][a] + sBoxes[1][b]

        y = y xor sBoxes[2][c]
        y += sBoxes[3][d]

        return y
    }

    /**
     * Convert 4 bytes to Integer
     * @param b byte array (4 bytes)
     * @param offset byte array offset
     * @return integer value
     */
    private fun byteArrayToInteger(b: ByteArray, offset: Int): Int {
        return (b[offset + 3] and 0xFF.toByte()).toInt() shl 24 or (b[offset + 2].toInt() and 0xFF shl 16) or (b[offset + 1].toInt() and 0xFF shl 8) or (b[offset].toInt() and 0xFF)
    }

    /**
     * Convert integer value to bytes
     * @param value integer value
     * @param b dest byte array
     * @param offset byte array offset
     */
    private fun integerToByteArray(value: Int, b: ByteArray, offset: Int) {
        b[offset] = (value and 0xFF).toByte()
        b[offset + 1] = (value shr 8 and 0xFF).toByte()
        b[offset + 2] = (value shr 16 and 0xFF).toByte()
        b[offset + 3] = (value shr 24 and 0xFF).toByte()
    }

    companion object {
        /**
         * HEX-digits of Pi (Math), arranged in four S boxes and one P array for Blowfish Cipher
         */
        // 18 32-bits cipher keys
        private val PARRAY_INIT = intArrayOf(
            0x243F6A88,
            -0x7a5cf72d,
            0x13198A2E,
            0x03707344,
            -0x5bf6c7de,
            0x299F31D0,
            0x082EFA98,
            -0x13b19377,
            0x452821E6,
            0x38D01377,
            -0x41ab9931,
            0x34E90C6C,
            -0x3f53d649,
            -0x3683af23,
            0x3F84D5B5,
            -0x4ab8f6e9,
            -0x6de92a27,
            -0x768604e5
        )
        /**
         * Constants - Pi value
         */
        private val SBOX_INIT_0 = intArrayOf(
            -0x2ecef45a,
            -0x67204a54,
            0x2FFD72DB,
            -0x2fe52049,
            -0x471e5013,
            0x6A267E96,
            -0x45836fbb,
            -0xed38067,
            0x24A19947,
            -0x4c6e9309,
            0x0801F2E2,
            -0x7a7103ea,
            0x636920D8,
            0x71574E69,
            -0x5ba7015d,
            -0xb6cc282,
            0x0D95748F,
            0x728EB658,
            0x718BCD58,
            -0x7deab512,
            0x7B54A41D,
            -0x3da5a64b,
            -0x63cf2ac7,
            0x2AF26013,
            -0x3a2e4fdd,
            0x286085F0,
            -0x35be86e8,
            -0x4724c711,
            -0x71862350,
            0x603A180E,
            0x6C9E0E8B,
            -0x4fe175c2,
            -0x28ea883f,
            -0x42ceb4d9,
            0x78AF2FDA,
            0x55605C60,
            -0x19aada0d,
            -0x55aa546c,
            0x57489862,
            0x63E81440,
            0x55CA396A,
            0x2AAB10B6,
            -0x4b33a3cc,
            0x1141E8CE,
            -0x5eab7951,
            0x7C72E993,
            -0x4c11ebef,
            0x636FBC2A,
            0x2BA9C55D,
            0x741831F6,
            -0x31a3c1ea,
            -0x64786ce2,
            -0x502945cd,
            0x6C24CF5C,
            0x7A325381,
            0x28958677,
            0x3B8F4898,
            0x6B4BB9AF,
            -0x3b4017e5,
            0x66282193,
            0x61D809CC,
            -0x4de566f,
            0x487CAC60,
            0x5DEC8032,
            -0x107ba2a3,
            -0x167a8a4f,
            -0x23d9dcfe,
            -0x149ae478,
            0x23893E81,
            -0x2c69533b,
            0x0F6D6FF3,
            -0x7c0bbdc7,
            0x2E0B4482,
            -0x5b7bdffc,
            0x69C8F04A,
            -0x61e064a2,
            0x21C66842,
            -0x9169366,
            0x670C9C61,
            -0x542c7710,
            0x6A51A0D2,
            -0x27abd098,
            -0x69f058d8,
            -0x54aecc5d,
            0x6EEF0B6C,
            0x137A3BE4,
            -0x45c40fb0,
            0x7EFB2A98,
            -0x5e0e9ae3,
            0x39AF0176,
            0x66CA593E,
            -0x7dbcf178,
            -0x731179e7,
            0x456F9FB4,
            0x7D84A5C3,
            0x3B8B5EBE,
            -0x1f908a28,
            -0x7a3edf8d,
            0x401A449F,
            0x56C16AA6,
            0x4ED3AA62,
            0x363F7706,
            0x1BFEDF72,
            0x429B023D,
            0x37D0D724,
            -0x2ff5edb8,
            -0x24f0152d,
            0x49F1C09B,
            0x075372C9,
            -0x7f66e485,
            0x25D479D8,
            -0x9172109,
            -0x1c01afe6,
            -0x4986b3c5,
            -0x68931f43,
            0x04C006BA,
            -0x3e56b04a,
            0x409F60C4,
            0x5E5C9EC2,
            0x196A2463,
            0x68FB6FAF,
            0x3E6C53B5,
            0x1339B2EB,
            0x3B52EC6F,
            0x6DFC511F,
            -0x64cf6ad4,
            -0x337ebabc,
            -0x50a142f7,
            -0x411c2ffc,
            -0x21ccb503,
            0x660F2807,
            0x192E4BB3,
            -0x3f3457a9,
            0x45C8740F,
            -0x2df4a0c7,
            -0x462c0425,
            0x5579C0BD,
            0x1A60320A,
            -0x295eff3a,
            0x402C7279,
            0x679F25FE,
            -0x4e05c34,
            -0x715a1608,
            -0x24cddd08,
            0x3C7516DF,
            -0x29e94eb,
            0x2F501EC8,
            -0x52faad55,
            0x323DB5FA,
            -0x2dc78a0,
            0x53317B48,
            0x3E00DF82,
            -0x61a3a845,
            -0x35907360,
            0x1A87562E,
            -0x20e89625,
            -0x2abd570a,
            0x287EFFC3,
            -0x5398cd3a,
            -0x73b0aa8d,
            0x695B27B0,
            -0x4435a738,
            -0x1e005ca3,
            -0x470fee60,
            0x10FA3D98,
            -0x2de7c48,
            0x4AFCB56C,
            0x2DD1D35B,
            -0x65ac1b87,
            -0x4907ba9b,
            -0x2d71b644,
            0x4BFB9790,
            -0x1e220d26,
            -0x5b3481cd,
            0x62FB1341,
            -0x311b3918,
            -0x10df3526,
            0x36774C01,
            -0x2f816102,
            0x2BF11FB4,
            -0x6a2425b3,
            -0x516f6e68,
            -0x1552718f,
            0x6B93D5A0,
            -0x2f712e30,
            -0x5038da20,
            -0x71c3a4d1,
            -0x718a6b49,
            -0x70091d05,
            -0xdedd49c,
            -0x777747ee,
            -0x6ff20fe4,
            0x4FAD5EA0,
            0x688FC31C,
            -0x2e300e6f,
            -0x4c573e53,
            0x2F2F2218,
            -0x41f1e889,
            -0x158ad202,
            -0x74fde05f,
            -0x1a5f33f1,
            -0x4a908b18,
            0x18ACF3D6,
            -0x31761d67,
            -0x4b57b020,
            -0x2ec1f49,
            0x7CC43B81,
            -0x2d525727,
            0x165FA266,
            -0x7f6a88fb,
            -0x6c338cec,
            0x211A1477,
            -0x1952df9b,
            0x77B5FA86,
            -0x38abbd0b,
            -0x462ca31,
            -0x143250f4,
            0x7B3E89A0,
            -0x29bee42d,
            -0x51e181b7,
            0x00250E2D,
            0x2071B35E,
            0x226800BB,
            0x57B8E0AF,
            0x2464369B,
            -0xff646e2,
            0x5563911D,
            0x59DFA6AA,
            0x78C14389,
            -0x26a5ac81,
            0x207D5BA2,
            0x02E5B9C5,
            -0x7cd9fc8a,
            0x6295CFA9,
            0x11C81968,
            0x4E734A41,
            -0x4cb8d236,
            0x7B14A94A,
            0x1B510052,
            -0x65acd6eb,
            -0x29f0a8c1,
            -0x4364391c,
            0x2B60A476,
            -0x7e198c00,
            0x08BA6FB5,
            0x571BE91F,
            -0xd691395,
            0x2A0DD915,
            -0x499c9adf,
            -0x1846064a,
            -0xcbfad2,
            -0x3a7aa99c,
            0x53B02D5D,
            -0x5660705f,
            0x08BA4799,
            0x6E85076A
        )
        /**
         * Constants - Pi value
         */
        private val SBOX_INIT_1 = intArrayOf(
            0x4B7A70E9,
            -0x4a4cd6bc,
            -0x248af6d2,
            -0x3be6d9dd,
            -0x52915950,
            0x49A7DF7D,
            -0x63119f48,
            -0x70124d9a,
            -0x1355738f,
            0x699A17FF,
            0x5664526C,
            -0x3d4e611f,
            0x193602A5,
            0x75094C29,
            -0x5fa6ecc0,
            -0x1be7c5c2,
            0x3F54989A,
            0x5B429D65,
            0x6B8FE4D6,
            -0x6608c02a,
            -0x5e2d63f9,
            -0x1017cf0b,
            0x4D2D38E6,
            -0xfdaa23f,
            0x4CDD2086,
            -0x7b8f14da,
            0x6382E9C6,
            0x021ECC5E,
            0x09686B3F,
            0x3EBAEFC9,
            0x3C971814,
            0x6B6A70A1,
            0x687F3584,
            0x52A0E286,
            -0x4863acfb,
            -0x55aff8c9,
            0x3E07841C,
            0x7FDEAE5C,
            -0x7182bb14,
            0x5716F2B8,
            -0x4fc525c9,
            -0xfaff3f3,
            -0xfe3e0fc,
            0x0200B3FF,
            -0x51f30ae6,
            0x3CB574B2,
            0x25837A58,
            -0x23f6de43,
            -0x2e6eec07,
            0x7CA92FF6,
            -0x6bcdb88d,
            0x22F54701,
            0x3AE5E581,
            0x37C2DADC,
            -0x374a89cc,
            -0x650c2259,
            -0x56bb9eba,
            0x0FD0030E,
            -0x133738c2,
            -0x5b8ae1bf,
            -0x1dc73267,
            0x3BEA0E2F,
            0x3280BBA1,
            0x183EB331,
            0x4E548B38,
            0x4F6DB908,
            0x6F420D03,
            -0x9f5fb41,
            0x2CB81290,
            0x24977C79,
            0x5679B072,
            -0x43507651,
            -0x216588e1,
            -0x266cf7f0,
            -0x4c7451ee,
            -0x2330c0d2,
            0x5512721F,
            0x2E6B7124,
            0x501ADDE6,
            -0x607b3279,
            0x7A584718,
            0x7408DA17,
            -0x43606544,
            -0x16b48274,
            -0x138513c6,
            -0x247ae206,
            0x63094366,
            -0x3b9b3c2e,
            -0x10e3e7b9,
            0x3215D908,
            -0x22bcc4c9,
            0x24C2BA16,
            0x12A14D43,
            0x2A65C451,
            0x50940002,
            0x133AE4DD,
            0x71DFF89E,
            0x10314E55,
            -0x7e53882a,
            0x5F11199B,
            0x043556F1,
            -0x285c3895,
            0x3C11183B,
            0x5924A509,
            -0xd701913,
            -0x680e0406,
            -0x614540d4,
            0x1E153C6E,
            -0x791cba90,
            -0x1516904f,
            -0x79f1a1f6,
            0x5A3E2AB3,
            0x771FE71C,
            0x4E3D06FA,
            0x2965DCB9,
            -0x6618e2f1,
            -0x7fc1762a,
            0x5266C825,
            0x2E4CC978,
            -0x63ef4c96,
            -0x39eaf146,
            -0x6b1d1588,
            -0x5a03c3ad,
            0x1E0A2DF4,
            -0xd08b159,
            0x361D2B3D,
            0x1939260F,
            0x19C27960,
            0x5223A708,
            -0x8eced4a,
            -0x14520192,
            -0x153ce09a,
            -0x1c43ba6b,
            -0x5984377d,
            -0x4e80c82f,
            0x018CFF28,
            -0x3ccd2211,
            -0x4193a55b,
            0x65582185,
            0x68AB9802,
            -0x11315af1,
            -0x24d06ac5,
            0x2AEF7DAD,
            0x5B6E2F84,
            0x1521B628,
            0x29076170,
            -0x1322b88b,
            0x619F1510,
            0x13CCA830,
            -0x149e426a,
            0x0334FE1E,
            -0x55fc9c31,
            -0x4a8ca370,
            0x4C70A239,
            -0x2a6161f5,
            -0x345521ec,
            -0x11337944,
            0x60622CA7,
            -0x6354a355,
            -0x4d0c7b92,
            0x648B1EAF,
            0x19BDF0CA,
            -0x5fdc9647,
            0x655ABB50,
            0x40685A32,
            0x3C2AB4B3,
            0x319EE9D5,
            -0x3fde4709,
            -0x64abf4e7,
            -0x78a05f67,
            -0x6a086682,
            0x623D7DA8,
            -0x7c87766,
            -0x681cd289,
            0x11ED935F,
            0x16681281,
            0x0E358829,
            -0x3819e02a,
            -0x6921205f,
            0x7858BA99,
            0x57F584A5,
            0x1B227263,
            -0x647c3c01,
            0x1AC24696,
            -0x324cf515,
            0x532E3054,
            -0x7026b71c,
            0x6DBC3128,
            0x58EBF2EF,
            0x34C6FFEA,
            -0x1d7129f,
            -0x1183c38d,
            0x5D4A14D9,
            -0x179b481d,
            0x42105D14,
            0x203E13E0,
            0x45EEE2B6,
            -0x5c555416,
            -0x2493b0eb,
            -0x534b030,
            -0x38bd0bbe,
            -0x1095444b,
            0x654F3B1D,
            0x41CD2105,
            -0x27e18662,
            -0x797ab239,
            -0x1bb4b896,
            0x3D816250,
            -0x309d5e0e,
            0x5B8D2646,
            -0x3777c60,
            -0x3e38495d,
            0x7F1524C3,
            0x69CB7492,
            0x47848A0B,
            0x5692B285,
            0x095BBF00,
            -0x52e6b763,
            0x1462B174,
            0x23820E00,
            0x58428D2A,
            0x0C55F5EA,
            0x1DADF43E,
            0x233F7061,
            0x3372F092,
            -0x726c81bf,
            -0x29a0130f,
            0x6C223BDB,
            0x7CDE3759,
            -0x34118ba0,
            0x4085F2A7,
            -0x3188cd92,
            -0x59f87f7c,
            0x19F8509E,
            -0x171027ab,
            0x61D99735,
            -0x56965856,
            -0x3af3f93e,
            0x5A04ABFC,
            -0x7ff43524,
            -0x61bb85d2,
            -0x3cbacb7c,
            -0x22a98fb,
            0x0E1E9EC9,
            -0x248c242d,
            0x105588CD,
            0x675FDA79,
            -0x1c98bcc0,
            -0x3a3bcb9b,
            0x713E38D8,
            0x3D28F89E,
            -0xe9200e0,
            0x153E21E7,
            -0x704fc2b6,
            -0x191c60d5,
            -0x247c5209
        )
        /**
         * Constants - Pi value
         */
        private val SBOX_INIT_2 = intArrayOf(
            -0x16c2a598,
            -0x6b7ebf09,
            -0x9b3d9e4,
            -0x6b96d6cc,
            0x411520F7,
            0x7602D4F7,
            -0x430b94d2,
            -0x2b5dff98,
            -0x2bf7db8f,
            0x3320F46A,
            0x43B7D4B7,
            0x500061AF,
            0x1E39F62E,
            -0x68dbbaba,
            0x14214F74,
            -0x407477c0,
            0x4D95FC1D,
            -0x694a6e51,
            0x70F4DDD3,
            0x66A02F45,
            -0x4043f614,
            0x03BD9785,
            0x7FAC6DD0,
            0x31CB8504,
            -0x6914d84d,
            0x55FD3941,
            -0x25dab81a,
            -0x5435f566,
            0x28507825,
            0x530429F4,
            0x0A2C86DA,
            -0x16499205,
            0x68DC1462,
            -0x28b79700,
            0x680EC0A4,
            0x27A18DEE,
            0x4F3FFEA2,
            -0x17785274,
            -0x4a731ffa,
            0x7AF4D6B6,
            -0x5531e184,
            -0x2cc8a014,
            -0x31875c67,
            0x406B2A42,
            0x20FE9E35,
            -0x260c7a47,
            -0x11c62855,
            0x3B124E8B,
            0x1DC9FAF7,
            0x4B6D1856,
            0x26A36631,
            -0x151c684e,
            0x3A6EFA74,
            -0x22a4bcce,
            0x6841E7F7,
            -0x3587df05,
            -0x4f50ab2,
            -0x27014c69,
            0x454056AC,
            -0x45b76ad9,
            0x55533A3A,
            0x20838D87,
            -0x1945649,
            -0x2f696ab5,
            0x55A867BC,
            -0x5eea65a8,
            -0x3356d69d,
            -0x661e24cd,
            -0x59d5b5aa,
            0x3F3125F9,
            0x5EF47E1C,
            -0x6fd6ce84,
            -0x20717fe,
            0x04272F70,
            -0x7f44eaa4,
            0x05282CE3,
            -0x6a3eeab8,
            -0x1b3992de,
            0x48C1133F,
            -0x38f07924,
            0x07F9C9EE,
            0x41041F0F,
            0x404779A4,
            0x5D886E17,
            0x325F51EB,
            -0x2a643f2f,
            -0xd433e71,
            0x41113564,
            0x257B7834,
            0x602A9C60,
            -0x2007175d,
            0x1F636C1B,
            0x0E12B4C2,
            0x02E1329E,
            -0x5099b02f,
            -0x352e7eeb,
            0x6B2395E0,
            0x333E92E1,
            0x3B240B62,
            -0x114146de,
            -0x7a4d5df2,
            -0x1945f267,
            -0x218df374,
            0x2DA2F728,
            -0x2fed87bb,
            -0x6a486b03,
            0x647D0862,
            -0x18330a10,
            0x5449A36F,
            -0x7882b706,
            -0x3c6202d9,
            -0xcc172e2,
            0x0A476341,
            -0x66d1008c,
            0x3A6F6EAB,
            -0xb0702c9,
            -0x57ed23a0,
            -0x5e142208,
            -0x66e41eb4,
            -0x249194f3,
            -0x3984aaf0,
            0x6D672C37,
            0x2765D43B,
            -0x232f17fc,
            -0xed6f239,
            -0x33ff005d,
            -0x4ac6f06e,
            0x690FED0B,
            0x667B9FFB,
            -0x31248264,
            -0x5f6e30f5,
            -0x26eaa15d,
            -0x44ecd078,
            0x515BAD24,
            0x7B9479BF,
            0x763BD6EB,
            0x37392EB3,
            -0x33eea687,
            -0x7fd91d69,
            -0xbd1ced3,
            0x6842ADA7,
            -0x3995d4c5,
            0x12754CCC,
            0x782EF11C,
            0x6A124237,
            -0x486dae19,
            0x06A1BBE6,
            0x4BFB6350,
            0x1A6B1018,
            0x11CAEDFA,
            0x3D25BDD8,
            -0x1d1e3c37,
            0x44421659,
            0x0A121386,
            -0x26f31392,
            -0x2a5415d6,
            0x64AF674E,
            -0x257957a1,
            -0x41401678,
            0x64E4C3FE,
            -0x62437fa9,
            -0xf083f7a,
            0x60787BF8,
            0x6003604D,
            -0x2e027cba,
            -0x9c7e050,
            0x7745AE04,
            -0x28c90334,
            -0x7cbd94cd,
            -0xfe1548f,
            -0x4f7fbe79,
            0x3C005E5F,
            0x77A057BE,
            -0x421751dc,
            0x55464299,
            -0x40a7d19f,
            0x4E58F48F,
            -0xd22025e,
            -0xb8b10c8,
            -0x7876423e,
            0x5366F9C3,
            -0x374c718c,
            -0x4b8a0dab,
            0x46FCD9B9,
            0x7AEB2661,
            -0x74e2207c,
            -0x7b95f187,
            -0x6ea06a1e,
            0x466E598E,
            0x20B45770,
            -0x732aaa6f,
            -0x36fd21b4,
            -0x46f4531f,
            -0x447dfa30,
            0x11A86248,
            0x7574A99E,
            -0x4880e64a,
            -0x1f5623f7,
            0x662D09A1,
            -0x3bcdb9cd,
            -0x17a5e0fe,
            0x09F0BE8C,
            0x4A99A025,
            0x1D6EFE10,
            0x1AB93D1D,
            0x0BA5A4DF,
            -0x5e790df1,
            0x2868F169,
            -0x2348257d,
            0x573906FE,
            -0x5e1d3165,
            0x4FCD7F52,
            0x50115E01,
            -0x58f97c06,
            -0x5ffd4a3c,
            0x0DE6D027,
            -0x650773d9,
            0x773F8641,
            -0x3c9fb3fa,
            0x61A806B5,
            -0xfe885d8,
            -0x3f0a7920,
            0x006058AA,
            0x30DC7D62,
            0x11E69ED7,
            0x2338EA63,
            0x53C2DD94,
            -0x3d3de9cc,
            -0x443411aa,
            -0x6f434922,
            -0x1403825f,
            -0x31a6e28a,
            0x6F05E409,
            0x4B7C0188,
            0x39720A3D,
            0x7C927C24,
            -0x791c8da1,
            0x724D9DB9,
            0x1AC15BB4,
            -0x2c614704,
            -0x12abaa88,
            0x08FCA5B5,
            -0x27c2832d,
            0x4DAD0FC4,
            0x1E50EF5E,
            -0x4e9e1908,
            -0x5d7aeb27,
            0x6C51133C,
            0x6FD5C7E7,
            0x56E14EC4,
            0x362ABFCE,
            -0x223937c9,
            -0x2865cdcc,
            -0x6d9c7dee,
            0x670EFA8E,
            0x406000E0
        )
        /**
         * Constants - Pi value
         */
        private val SBOX_INIT_3 = intArrayOf(
            0x3A39CE37,
            -0x2c050a31,
            -0x543d88c9,
            0x5AC52D1B,
            0x5CB0679E,
            0x4FA33742,
            -0x2c7dd8c0,
            -0x66436442,
            -0x2aee7163,
            -0x40f08ceb,
            -0x29d2e382,
            -0x38ff3b85,
            -0x4873e495,
            0x21A19045,
            -0x4d914e42,
            0x6A366EB4,
            0x5748AB2F,
            -0x436b9187,
            -0x395c892e,
            0x6549C2C8,
            0x530FF8EE,
            0x468DDE7D,
            -0x2a8cf5e3,
            0x4CD04DC6,
            0x2939BBDB,
            -0x5645b9b0,
            -0x536ad918,
            -0x41a11cfc,
            -0x5e052a10,
            0x6A2D519A,
            0x63EF8CE2,
            -0x657911de,
            -0x3f763d48,
            0x43242EF6,
            -0x5ae1fc56,
            -0x630d2f5c,
            -0x7c3f9e46,
            -0x641695b3,
            -0x701aeab0,
            -0x459ba42a,
            0x2826A2F9,
            -0x58c5c51f,
            0x4BA99586,
            -0x10aa9d17,
            -0x38d0102d,
            -0x8ad0826,
            0x3F046F69,
            0x77FA0A59,
            -0x7f1b56eb,
            -0x784f79ff,
            -0x64f61953,
            0x3B3EE593,
            -0x166f02a6,
            -0x61cb2869,
            0x2CF0B7D9,
            0x022B8B51,
            -0x692a53c6,
            0x017DA67D,
            -0x2e30c12a,
            0x7C7D2D28,
            0x1F9F25CF,
            -0x520d4765,
            0x5AD6B472,
            0x5A88F54C,
            -0x1fd6538f,
            -0x1fe65a1a,
            0x47B0ACFD,
            -0x126c0565,
            -0x172c3b73,
            0x283B57CC,
            -0x72a99d7,
            0x79132E28,
            0x785F0191,
            -0x128a9fab,
            -0x869f1bc,
            -0x1c2ca174,
            0x15056DD4,
            -0x770b9246,
            0x03A16125,
            0x0564F0BD,
            -0x3c1461eb,
            0x3C9057A2,
            -0x68d8e514,
            -0x56c5f8d6,
            0x1B3F6D9B,
            0x1E6321F5,
            -0xa639905,
            0x26DCF319,
            0x7533D928,
            -0x4eaa020b,
            0x03563482,
            -0x7545c345,
            0x28517711,
            -0x3df52608,
            -0x5433ae99,
            -0x33526da1,
            0x4DE81751,
            0x3830DC8E,
            0x379D5862,
            -0x6cdf066f,
            -0x15856f3e,
            -0x4c18432,
            0x5121CE64,
            0x774FBE32,
            -0x57491c82,
            -0x3cd6c2ba,
            0x48DE5369,
            0x6413E680,
            -0x5d51f7f0,
            -0x22924ddc,
            0x69852DFD,
            0x09072166,
            -0x4c65b9f6,
            0x6445C0DD,
            0x586CDECF,
            0x1C20C8AE,
            0x5BBEF7DD,
            0x1B588D40,
            -0x332dfe81,
            0x6BB4E3BB,
            -0x225d9582,
            0x3A59FF45,
            0x3E350A44,
            -0x434b322b,
            0x72EACEA8,
            -0x59b7b45,
            -0x7299ed52,
            -0x40c390b9,
            -0x2d641b9d,
            0x542F5D9E,
            -0x513d88e5,
            -0x9b19c90,
            0x740E0D8D,
            -0x18a4eca9,
            -0x78de98f,
            -0x50ac82a3,
            0x4040CB08,
            0x4EB4E2CC,
            0x34D2466A,
            0x0115AF84,
            -0x1e4ffbd8,
            -0x6a67c5e3,
            0x06B89FB4,
            -0x31915fb8,
            0x6F3F3B82,
            0x3520AB82,
            0x011A1D4B,
            0x277227F8,
            0x611560B1,
            -0x186cc024,
            -0x44c586d5,
            0x344525BD,
            -0x5f77c61f,
            0x51CE794B,
            0x2F32C9B7,
            -0x5fe04537,
            -0x1fe33782,
            -0x43382e0a,
            -0x30feee3d,
            -0x5e175539,
            0x1A908749,
            -0x2bb04266,
            -0x2f252135,
            -0x2af525c8,
            0x0339C32A,
            -0x396ec999,
            -0x7206ce84,
            -0x1f4ed4b1,
            -0x861a649,
            0x43F5BB3A,
            -0xd2ae601,
            0x27D9459C,
            -0x4068ddd4,
            0x15E6FC2A,
            0x0F91FC71,
            -0x646beadb,
            -0x51a6c9f,
            -0x31496315,
            -0x3d579ba7,
            0x12BAA8D1,
            -0x493ef8a2,
            -0x1cfa95f4,
            0x10D25065,
            -0x34fc5bbe,
            -0x1f1391f2,
            0x1698DB3B,
            0x4C98A0BE,
            0x3278E964,
            -0x60e06ace,
            -0x1f2c6d21,
            -0x2c5fcbd5,
            -0x768e0de2,
            0x1B0A7441,
            0x4BA3348C,
            -0x3a418ee0,
            -0x3c89cd28,
            -0x20ca6073,
            -0x6466d0d2,
            -0x19f490b9,
            0x0FE3F11D,
            -0x1ab325ac,
            0x1EDAD891,
            -0x319d8631,
            -0x32c18191,
            0x1618B166,
            -0x2d3e2fb,
            -0x7b702d3b,
            -0x904dd67,
            -0xadc0ca9,
            -0x59cd89dd,
            -0x6c57cacf,
            0x56CCCD02,
            -0x530f7e9e,
            0x5A75EBB5,
            0x6E163697,
            -0x772d8c34,
            -0x21699d6e,
            -0x7e46b630,
            0x4C50901B,
            0x71C65614,
            -0x19393843,
            0x327A140A,
            0x45E1D006,
            -0x3c0d8466,
            -0x3655ac03,
            0x62A80F00,
            -0x44da401e,
            0x35BDD2F6,
            0x71126905,
            -0x4dfbfdde,
            -0x49343084,
            -0x328963d5,
            0x53113EC0,
            0x1640E3D3,
            0x38ABBD60,
            0x2547ADF0,
            -0x45c7df64,
            -0x8b9318a,
            0x77AFA1C5,
            0x20756060,
            -0x7a3401b2,
            -0x75177228,
            0x7AAAF9B0,
            0x4CF9AA7E,
            0x1948C25C,
            0x02FB8A8C,
            0x01C36AE4,
            -0x29141e07,
            -0x6f2b0797,
            -0x59a32160,
            0x3F09252D,
            -0x3df71961,
            -0x48b19ece,
            -0x31881da5,
            0x578FDFE3,
            0x3AC372E6
        )
    }
}
/**
 * Cipher the given byte-array with Blowfish cipher
 * @param data byte array to be ciphered
 */
/**
 * Decipher the given byte-array with Blowfish cipher
 * @param data byte array to be deciphered
 */
