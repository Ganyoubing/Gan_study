package com.ekuaibao.invoice;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import com.google.gson.Gson;

import com.tenpay.TencentSM.SM2Algo;
import com.tenpay.TencentSM.SM3Algo;
import com.tenpay.TencentSM.SM4Algo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

import org.xerial.snappy.Snappy;

import java.util.List;

public class Sm2SDK {
    private static final int BUFFER = 4096;

    private static String id = "trustsql.qq.com";

    public String[] GenerateBase64KeyPair() throws Exception
    {
        String[] keyPairs={"",""};
        try
        {
            SM2Algo sm2algo = new SM2Algo();
            sm2algo.initCtx();
            String[] tmpKeyPairs = sm2algo.generateBase64KeyPair();
            sm2algo.freeCtx();
            String strHex = new String(Base64.decodeBase64(tmpKeyPairs[0]));
            strHex = strHex.substring(0,64);

            keyPairs[0] = Base64.encodeBase64String(Hex.decodeHex(strHex.toCharArray()));

            strHex = new String(Base64.decodeBase64(tmpKeyPairs[1]));
            strHex = strHex.substring(0,130);

            keyPairs[1] = Base64.encodeBase64String(Hex.decodeHex(strHex.toCharArray()));

        }catch(Exception e)
        {
            throw new Exception("generate base64 key pair fail");
        }

        return keyPairs;
    }

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
    public String Sm2SignForSp(String sHttpMethod, String sUrlPath, long lTimestamp, String sHttpBody, String sSpPubkeyBase64, String sSpPrikeyBase64) throws Exception {
        String toBeSignStr = sHttpMethod + "\n" + sUrlPath + "\n" + String.valueOf(lTimestamp) + "\n" + sHttpBody + "\n";

        return _Sm2Sign(toBeSignStr, false, sSpPubkeyBase64, sSpPrikeyBase64);
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
    public boolean Sm2CheckSignForSpv(String sTimestamp,  String sHttpBody, String sSignatureBase16, String sSpvPubkeyBase64)
    {
        String sRawString = sTimestamp + "\n" + sHttpBody + "\n";
        return _Sm2Verify(sRawString, false, sSignatureBase16, sSpvPubkeyBase64);
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
    public String Sm2SignForEp(String sToBeSignedBase16, String sEpPubkeyBase64, String sEpPrikeyBase64)  throws Exception
    {
        return _Sm2Sign(sToBeSignedBase16, true, sEpPubkeyBase64, sEpPrikeyBase64);
    }
    /* 构造请求中dst_addr.sp_sign字段
     * IN PARAM:
     * sPubkeyBase64: dst_addr.pubkey字段
     * sSpId: dst_addr.sp_id字段
     * sSpPubkeyBase64: sp_id对应服务商的sm2公钥(base64编码)
     * sSpPrikeyBase64: sp_id对应服务商的sm2私钥(base64编码)
     * OUT PARAM:
     * RETURN:
     * 成功返回签名后的内容(作base16编码), 应该赋值给dst_addr.sp_sign字段
     * 失败抛出异常
     */
    public String Sm2SignForDstAddr(String sPubkeyBase64, String sSpId, String sSpPubkeyBase64, String sSpPrikeyBase64) throws Exception
    {
        String sRawString = sPubkeyBase64 + sSpId;
        String sToBeSignBase16 = String.valueOf(Hex.encodeHex(sRawString.getBytes()));
        return _Sm2Sign(sToBeSignBase16, true,sSpPubkeyBase64, sSpPrikeyBase64);
    }

    private String _uncompressData(int compType, String compData) throws Exception
    {
        try
        {
            String strUncompData=null;
            byte[] uncompData={};
            if (compType == 1)	// gzip
            {
                uncompData = decompress(Base64.decodeBase64(compData.getBytes()));

            }else if (compType == 2)	// snappy
            {
                uncompData = Snappy.uncompress(Base64.decodeBase64(compData.getBytes()));
            }else
            {
                throw new Exception("unknow compress type");
            }
            strUncompData = new String(uncompData,"utf-8");
            return strUncompData;
        }catch(Exception e)
        {
            throw new Exception("uncompress data fail");
        }
    }

    private String _DecodeData( String encodeData, String key) throws Exception
    {
        String decodeData;
        try
        {
            byte[] plaintext = SM4Algo.decrypt_ecb(Base64.decodeBase64(encodeData),Base64.decodeBase64(key));
            decodeData = Base64.encodeBase64String(plaintext);
            return decodeData;
        }catch(Exception e)
        {
            throw new Exception("decode data fail");
        }
    }


    public String _Sm2Sign(String toBeSign, boolean bNotUseSm3,String sSpPubkeyBase64, String sSpPrikeyBase64) throws Exception
    {
        byte[] hash={};
        byte[] sign={};
        try
        {
            String priKeyHex = Hex.encodeHexString(Base64.decodeBase64(sSpPrikeyBase64));
            byte[] priKeyByte = Base64.encodeBase64(priKeyHex.getBytes());

            String pubKeyHex = Hex.encodeHexString(Base64.decodeBase64(sSpPubkeyBase64));
            byte[] pubKeyByte = Base64.encodeBase64(pubKeyHex.getBytes());

            SM2Algo sm2algo = new SM2Algo();
            sm2algo.initCtx();
            if (!bNotUseSm3)
            {
                SM3Algo sm3 = new SM3Algo();
                sm3.update(toBeSign.getBytes());
                hash = sm3.digest();
                sign = sm2algo.sign(hash, id.getBytes(), Base64.decodeBase64(pubKeyByte),Base64.decodeBase64(priKeyByte));
            }else
            {
                sign = sm2algo.sign(Hex.decodeHex(toBeSign.toCharArray()), id.getBytes(), Base64.decodeBase64(pubKeyByte),Base64.decodeBase64(priKeyByte));
            }

            sm2algo.freeCtx();
        }catch(Exception e)
        {
            throw new Exception("sm sign fail");
        }

        return Hex.encodeHexString(sign);
    }

    public boolean _Sm2Verify(String msg, boolean isHash, String sSignatureBase16,  String pubKeyBase64)
    {
        boolean result=false;
        try
        {
            byte[] hashMsg={};
            SM2Algo sm2algo = new SM2Algo();
            sm2algo.initCtx();
            String pubKeyHex = Hex.encodeHexString(Base64.decodeBase64(pubKeyBase64));
            byte[] pubKeyByte = Base64.encodeBase64(pubKeyHex.getBytes());
            if (!isHash)
            {
                SM3Algo sm3 = new SM3Algo();
                sm3.update(msg.getBytes());
                hashMsg = sm3.digest();
                result =  sm2algo.verify(hashMsg, id.getBytes(), Hex.decodeHex(sSignatureBase16.toCharArray()), Base64.decodeBase64(pubKeyByte));
            }else
            {
                result =  sm2algo.verify(msg.getBytes(), id.getBytes(), Hex.decodeHex(sSignatureBase16.toCharArray()), Base64.decodeBase64(pubKeyByte));
            }
            sm2algo.freeCtx();
        }catch(Exception e)
        {
            return false;
        }
        return result;
    }

    /**

     * 数据解压缩

     *

     * @param is

     * @param os

     * @throws Exception

     */

    private void decompress(InputStream is, OutputStream os)

            throws Exception {

        GZIPInputStream gis = new GZIPInputStream(is);

        int count;

        byte data[] = new byte[BUFFER];

        while ((count = gis.read(data, 0, BUFFER)) != -1) {

            os.write(data, 0, count);

        }

        gis.close();

    }
    /**

     * 数据解压缩

     *

     * @param data

     * @return

     * @throws Exception

     */

    private byte[] decompress(byte[] data) throws Exception {

        ByteArrayInputStream bais = new ByteArrayInputStream(data);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // 解压缩

        decompress(bais, baos);

        data = baos.toByteArray();

        baos.flush();

        baos.close();

        bais.close();

        return data;

    }

}
