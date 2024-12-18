package com.yscoco.lib.util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class SecretUtil {
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    /**
     * 使用 HMAC-SHA1 签名方法对data进行签名
     *
     * @param data 被签名的字符串
     * @param key 密钥
     * @return 加密后的字符串
     */
    public static String genHmacSha1(String data, String key) {
        String result = null;
        try {
            //根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
            SecretKeySpec signKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
            //生成一个指定 Mac 算法 的 Mac 对象
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            //用给定密钥初始化 Mac 对象
            mac.init(signKey);
            //完成 Mac 操作
            byte[] rawHmac = mac.doFinal(data.getBytes());
            result = ByteUtil.bytesToClearHexString(rawHmac).toLowerCase();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return Objects.requireNonNullElse(result, StringUtil.EMPTY);
    }
}
