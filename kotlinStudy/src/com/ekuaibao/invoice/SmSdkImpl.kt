package com.ekuaibao.invoice

import com.tenpay.TencentSM.SM2Algo
import com.tenpay.TencentSM.SM3Algo
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.binary.Hex
import org.xerial.snappy.Snappy
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.zip.GZIPInputStream

class SmSdkImpl {
    /* 服务商通讯请求签名
	 * IN PARAM:
	 * sHttpMethod : http请求的method, 例如：POST
	 * sUrlPath : 请求url的path，例如: /v2/ReceiptApply
	 * iTimestamp : http请求头Authorization中的timestamp字段，表示当前utc时间戳，单位:秒
	 * sHttpBody: http请求的原始body
	 * sSpPubkeyBase64: 服务商的sm2公钥(base64编码)
	 * sSpPrikeyBase64: 服务商的sm2私钥(base64编码)
	 * RETURN:
	 * 成功返回 保存签名后的内容(作base16编码)
	 * 失败抛出异常
	 */
    @Throws(Exception::class)
    fun Sm2SignForSp(sHttpMethod: String, sUrlPath: String, lTimestamp: Long, sHttpBody: String, sSpPubkeyBase64: String, sSpPrikeyBase64: String): String {
        val toBeSignStr = """
            $sHttpMethod
            $sUrlPath
            $lTimestamp
            $sHttpBody
            
            """.trimIndent()
        println("toBeSignStr: $toBeSignStr")
        return _Sm2Sign(toBeSignStr, false, sSpPubkeyBase64, sSpPrikeyBase64)
    }

    /* 服务商通讯应答验签
	 * IN PARAM:
	 * sTimestamp: http应答头中的"BERS-Timestamp"对应的内容, 表示服务端应答的utc时间搓，单位:秒
	 * sSignatureBase16: http应答头中的"BERS-Signature"对应的内容, 表示服务端应答的签名信息
	 * sHttpBody: http应答的原始body
	 * sSpvPubkeyBase64: 服务端用于验签的sm2公钥(线下跟服务端沟通获取)
	 * RETURN:
	 * 验签成功返回true，失败返回false
	 */
    fun Sm2CheckSignForSpv(sTimestamp: String, sHttpBody: String, sSignatureBase16: String, sSpvPubkeyBase64: String): Boolean {
        val sRawString = """
            $sTimestamp
            $sHttpBody
            
            """.trimIndent()
        return _Sm2Verify(sRawString, false, sSignatureBase16, sSpvPubkeyBase64)
    }

    /* 企业上链签名接口
	 * IN PARAM:
	 * sToBeSignedBase16: Apply请求返回的to_be_signed字段
	 * sEpPubkeyBase64: 企业的sm2公钥(base64编码)
	 * sEpPrikeyBase64: 企业的sm2私钥(base64编码)
	 * OUT PARAM:
	 * RETURN:
	 * 成功返回签名后的内容(作base16编码)，
	 * 失败抛出异常
	 */
    @Throws(Exception::class)
    fun Sm2SignForEp(sToBeSignedBase16: String?, sEpPubkeyBase64: String?, sEpPrikeyBase64: String?): String? {
        return _Sm2Sign(sToBeSignedBase16!!, true, sEpPubkeyBase64!!, sEpPrikeyBase64!!)
    }

    @Throws(Exception::class)
    private fun _uncompressData(compType: Int, compData: String): String {
        return try {
            var strUncompData: String? = null
            var uncompData: ByteArray? = byteArrayOf()
            uncompData = if (compType == 1) // gzip
            {
                decompress(Base64.decodeBase64(compData.toByteArray()))
            } else if (compType == 2) // snappy
            {
                Snappy.uncompress(Base64.decodeBase64(compData.toByteArray()))
            } else {
                throw Exception("unknow compress type")
            }
            strUncompData = String(uncompData!!)
            strUncompData
        } catch (e: Exception) {
            throw Exception("uncompress data fail")
        }
    }

    /**
     * 加签
     */
    @Throws(Exception::class)
    fun _Sm2Sign(toBeSign: String, bNotUseSm3: Boolean, sSpPubkeyBase64: String, sSpPrikeyBase64: String): String {
        var hash = byteArrayOf()
        var sign = byteArrayOf()
        try {
            val priKeyHex = Hex.encodeHexString(Base64.decodeBase64(sSpPrikeyBase64))
            val priKeyByte = Base64.encodeBase64(priKeyHex.toByteArray(Charset.defaultCharset()))
            val pubKeyHex = Hex.encodeHexString(Base64.decodeBase64(sSpPubkeyBase64))
            val pubKeyByte = Base64.encodeBase64(pubKeyHex.toByteArray(Charset.defaultCharset()))
            val sm2algo = SM2Algo()
            sm2algo.initCtx()
            if (!bNotUseSm3) {
                val sm3 = SM3Algo()
                sm3.update(toBeSign.toByteArray(Charset.defaultCharset()))
                hash = sm3.digest()
                sign = sm2algo.sign(hash, id.toByteArray(Charset.defaultCharset()), Base64.decodeBase64(pubKeyByte), Base64.decodeBase64(priKeyByte))
            } else {
                sign = sm2algo.sign(Hex.decodeHex(toBeSign.toCharArray()), id.toByteArray(Charset.defaultCharset()), Base64.decodeBase64(pubKeyByte), Base64.decodeBase64(priKeyByte))
            }
            sm2algo.freeCtx()
        } catch (e: Exception) {
            throw Exception("sm sign fail")
        }
        return Hex.encodeHexString(sign)
    }

    /**
     * 解签
     */
    fun _Sm2Verify(msg: String, isHash: Boolean, sSignatureBase16: String, pubKeyBase64: String): Boolean {
        var result = false
        try {
            var hashMsg = byteArrayOf()
            val sm2algo = SM2Algo()
            sm2algo.initCtx()
            val pubKeyHex = Hex.encodeHexString(Base64.decodeBase64(pubKeyBase64))
            val pubKeyByte = Base64.encodeBase64(pubKeyHex.toByteArray())
            if (!isHash) {
                val sm3 = SM3Algo()
                sm3.update(msg.toByteArray())
                hashMsg = sm3.digest()
                result = sm2algo.verify(hashMsg, id.toByteArray(), Hex.decodeHex(sSignatureBase16.toCharArray()), Base64.decodeBase64(pubKeyByte))
            } else {
                result = sm2algo.verify(msg.toByteArray(), id.toByteArray(), Hex.decodeHex(sSignatureBase16.toCharArray()), Base64.decodeBase64(pubKeyByte))
            }
            sm2algo.freeCtx()
        } catch (e: Exception) {
            return false
        }
        return result
    }

    /**
     *
     * 数据解压缩
     *
     *
     *
     * @param is
     *
     * @param os
     *
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun decompress(`is`: InputStream, os: OutputStream) {
        val gis = GZIPInputStream(`is`)
        var count: Int
        val data = ByteArray(BUFFER)
        while (gis.read(data, 0, BUFFER).also { count = it } != -1) {
            os.write(data, 0, count)
        }
        gis.close()
    }

    /**
     *
     * 数据解压缩
     *
     *
     *
     * @param data
     *
     * @return
     *
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun decompress(data: ByteArray): ByteArray {
        var data = data
        val bais = ByteArrayInputStream(data)
        val baos = ByteArrayOutputStream()

        // 解压缩
        decompress(bais, baos)
        data = baos.toByteArray()
        baos.flush()
        baos.close()
        bais.close()
        return data
    }

    companion object {
        private const val BUFFER = 4096
        private const val id = "trustsql.qq.com"
    }
}