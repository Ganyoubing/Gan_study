package com.ekuaibao.invoice
//http
import java.io.*
import java.net.ContentHandler
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL


//http end
/**
 * Hello world!
 *
 */
class App {
}

@Throws(Exception::class)
fun testBatchVerifyBill() {
    val cgi = "/bers_ep_api/v2/BatchVerifyBill"
//    val body1 = "{\"tx_hash\":\"0196b971609a02e8b8a4f2afb55f02717a182f2f396305d6bf372d4581f12f6999\"}"
    val body = "{\n" +
            "    \"query_bill_size\":1,\n" +
            "    \"query_bill_list\":[\n" +
            "    {\n" +
            "        \"bill_code\":\"144032009110\",\n" +
            "        \"bill_num\":\"03818701\",\n" +
            "        \"issue_date\":\"20200401\",\n" +
            "        \"ch_code\":\"9da13\",\n" +
            "        \"bill_net_amout\":5861\n" +
            "    }\n" +
            "    ]\n" +
            "}"
    testHttpPost(cgi, body)
}

@Throws(Exception::class)
fun testHttpPost(cgi: String, body: String): String? {
    //这里填写测试用的秘钥和服务商id
    var spPubKeyBase64 = "BMOgG0DA9orCPjY05MDdnfcOItyGvyFSbbL5vTCvm1RhPoYyRfmzyVZRQT31AwTqCeVk6bS5ijITPRDV9fE4pw0=" // 服务商公钥
    var spPriKeyBase64 = "BA3Gc1omajZRqaJeKDXtIlmO/P46XD0q4Da+qNyPHKY=" // 服务商私钥
    //    var spvPubKeyBase64 = "" // SPV公钥
    var sp_id = "sp2019120616253571704"
    val httpUrl = "https://bcfp.baas.qq.com$cgi"
    val url = URL(httpUrl)
    // 通过远程url连接对象打开连接
    val connection = url.openConnection() as HttpURLConnection
    val timestamp = System.currentTimeMillis() / 1000
    val sdk = SmSdkImpl()
    val spSign: String = sdk.Sm2SignForSp("POST", cgi, timestamp, body, spPubKeyBase64, spPriKeyBase64)
    val sAuthorization = "BERS-SM3-SM2 sp_id=\"$sp_id\", timestamp=\"$timestamp\",signature=\"$spSign\""
    println("Authorization： $sAuthorization")
    connection.let {
        // 设置连接请求方式
        it.requestMethod = "POST"
        // 设置连接主机服务器超时时间：15000毫秒
        it.connectTimeout = 15000
        // 设置读取主机服务器返回数据超时时间：60000毫秒
        it.readTimeout = 60000
        // 默认值为：false，当向远程服务器传送数据/写数据时，需要设置为true
        it.doOutput = true
        // 默认值为：false，当向远程服务器传送数据/写数据时，需要设置为true
        it.doOutput = true
        // 默认值为：true，当前向远程服务读取数据时，设置为true，该参数可有可无
        it.doInput = true
        it.setRequestProperty("Content-Type", "application/json")

        it.setRequestProperty("Authorization", sAuthorization)

    }
    connection.outputStream.write(body.toByteArray(Charsets.UTF_8))
    val inStream = if (connection.responseCode == 200)
        connection.inputStream
    else
        connection.errorStream
    // 输入流转换成字符串
    val result = inStream.bufferedReader().lineSequence().joinToString()
    println("访问返回结果： $result")
    return result
}


@Throws(Exception::class)
fun main(args: Array<String>) {
    try {
        testBatchVerifyBill();    // 发票查询

    } catch (e: Exception) {
        println(e)
    }
}



