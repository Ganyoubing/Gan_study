package com.ekuaibao.invoice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

//http end

/**
 * Hello world!
 */
public class InvoiceTest {   //这里填写测试用的秘钥和服务商id
    static String epPubKeyBase64 = ""; // 开票企业公钥
    static String epPriKeyBase64 = ""; // 开票企业私钥
    static String spPubKeyBase64 = "BMOgG0DA9orCPjY05MDdnfcOItyGvyFSbbL5vTCvm1RhPoYyRfmzyVZRQT31AwTqCeVk6bS5ijITPRDV9fE4pw0="; // 服务商公钥
    static String spPriKeyBase64 = "BA3Gc1omajZRqaJeKDXtIlmO/P46XD0q4Da+qNyPHKY="; // 服务商私钥
    static String spvPubKeyBase64 = "";  // SPV公钥
    static String sp_id = "sp2019120616253571704";

    public static void main(String[] args) throws Exception {

        try {
            test_QueryBillInfo();	// 发票查询

            //test_DigitalEnvelopeDecode();
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public static void test_QueryBillInfo() throws Exception {
        String cgi = "/bers_ep_api/v2/BatchVerifyBill";
//    val body1 = "{\"tx_hash\":\"0196b971609a02e8b8a4f2afb55f02717a182f2f396305d6bf372d4581f12f6999\"}"
        String body = "{\n" +
                "    \"query_bill_size\": 1,\n" +
                "    \"query_bill_list\": [\n" +
                "    {\n" +
                "        \"bill_code\":\"845032009110\",\n" +
                "        \"bill_num\":\"03539845\",\n" +
                "        \"issue_date\":\"20200619\",\n" +
                "        \"ch_code\":\"df0e3\",\n" +
                "        \"bill_net_amout\":26549\n" +
                "    }\n" +
                "    ]\n" +
                "}";
        test_http_post(cgi, body);
    }


    public static String test_http_post(String cgi, String body) throws Exception {
        HttpURLConnection connection = null;
        InputStream is = null;
        OutputStream os = null;
        BufferedReader br = null;
        String result = null;

        try {
            String httpUrl = "https://bcfp.baas.qq.com" + cgi;
            URL url = new URL(httpUrl);
            // 通过远程url连接对象打开连接
            connection = (HttpURLConnection) url.openConnection();
            // 设置连接请求方式
            connection.setRequestMethod("POST");
            // 设置连接主机服务器超时时间：15000毫秒
            connection.setConnectTimeout(15000);
            // 设置读取主机服务器返回数据超时时间：60000毫秒
            connection.setReadTimeout(60000);

            // 默认值为：false，当向远程服务器传送数据/写数据时，需要设置为true
            connection.setDoOutput(true);
            // 默认值为：true，当前向远程服务读取数据时，设置为true，该参数可有可无
            connection.setDoInput(true);
            // 设置传入参数的格式:请求参数应该是 name1=value1&name2=value2 的形式。
            connection.setRequestProperty("Content-Type", "application/json");
            // 设置鉴权信息：Authorization: Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0

            long timestamp = System.currentTimeMillis() / 1000;
            Sm2SDK sdk = new Sm2SDK();
            String spSign = sdk.Sm2SignForSp("POST", cgi, timestamp, body, spPubKeyBase64, spPriKeyBase64);
            String sAuthorization = "BERS-SM3-SM2 sp_id=\"" + sp_id
                    + "\", timestamp=\"" + String.valueOf(timestamp)
                    + "\",signature=\"" + spSign
                    + "\"";
            System.out.println("Authorization: "+sAuthorization);
            connection.setRequestProperty("Authorization", sAuthorization);

            // set body end
            // 通过连接对象获取一个输出流
            os = connection.getOutputStream();
            String param = null;
            // 通过输出流对象将参数写出去/传输出去,它是通过字节数组写出的
            //os.write(param.getBytes());
            os.write(body.getBytes());
            // 通过连接对象获取一个输入流，向远程读取
            if (connection.getResponseCode() == 200) {
                String rspTimestamp = connection.getHeaderField("BERS-Timestamp");
                String rspSignature = connection.getHeaderField("BERS-Signature");

                is = connection.getInputStream();
                // 对输入流对象进行包装:charset根据工作项目组的要求来设置
                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                StringBuffer sbf = new StringBuffer();
                String temp = null;
                // 循环遍历一行一行读取数据
                while ((temp = br.readLine()) != null) {
                    sbf.append(temp);
                }
                result = sbf.toString();
                System.out.println("result: " + result);


            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != os) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // 断开与远程地址url的连接
            connection.disconnect();
        }
        return null;
    }

}

