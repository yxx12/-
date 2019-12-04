package com.changgou.token;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName CreateJwt73Test
 * @Description
 * @Author 传智播客
 * @Date 18:40 2019/8/21
 * @Version 2.1
 **/
public class CreateJwt73Test {

    /***
     * 创建令牌测试
     */
    @Test
    public void testCreateToken(){
        //证书文件路径
        String key_location="changgou73.jks";
        //秘钥库密码
        String key_password="changgou73";
        //秘钥密码
        String keypwd = "changgou73";
        //秘钥别名
        String alias = "changgou73";

        //访问证书路径
        ClassPathResource resource = new ClassPathResource(key_location);

        //创建秘钥工厂
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(resource,key_password.toCharArray());

        //读取秘钥对(公钥、私钥)
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair(alias,keypwd.toCharArray());

        //获取私钥
        RSAPrivateKey rsaPrivate = (RSAPrivateKey) keyPair.getPrivate();

        //定义Payload(载体)
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("id", "1");
        tokenMap.put("name", "yangxin");
        tokenMap.put("roles", "ROLE_VIP,ROLE_USER");

        //生成Jwt令牌
        Jwt jwt = JwtHelper.encode(JSON.toJSONString(tokenMap), new RsaSigner(rsaPrivate));

        //取出令牌
        String encoded = jwt.getEncoded();
        System.out.println(encoded);
    }


    /***
     * 校验令牌
     */
    @Test
    public void testParseToken(){
        //令牌
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlcyI6IlJPTEVfVklQLFJPTEVfVVNFUiIsIm5hbWUiOiJ5YW5neGluIiwiaWQiOiIxIn0.Omvxa-8RjolhT7FUKU_bk0XRviBhhqbn1XsXsa3OCTMTVddU4GGRNsufecLAr9ALcbc1A3agUlSmNFrYrLuh1KFJLfVigcEmhwDvU2eR5fFOEkM5MAXku50s-KJV8EA5o__A1Pp8MnSScJXzVCBo02IuDfW4UEbRh985Os0K-m4HnmG7duvvkeQmVKPNPVzMuUKy6ZQ2xus1CKV57qbd4jGV8RLEJZCqqvKyKO-E2xSzzpFkq-y9rNssUIe0wlkhf0pv09SxpKPv5nd3W2TE5faaB4jo2HLz6ld_UkmYGUlL0iEuZZcAMDdmIFetlOWTHaJpCyDe0Yhq1Vb3fRhG7w";

        //公钥
        String publickey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqhfQANGjYPjkAjR3Uwv+qm2ybAJnM2ZTcr4Mom1ZCxMdxa78iwhVQ5VdHStzRNygNYcLebu9xq1mOe14jFW0W+kccQ9kNrl5n+HjWrR6G/gUORq0yPLkJJ5mA63ha0XAzj5GXc1MlmvWH4CG31/d5HMpS45VUjL4jrhIpMT8CvdFcATpPEjxRDmOjlJyYNHOhafgSPsjsATyktxYw2/guaHHpYMhbNrcSBIyeskGY43c+JFNN/CHFTGcMlm8izSYf/rFAFbOheJD5onnp9NwYVu4dBGn4or2He5Wo7ZUbx1Y/Z7tJMfyLbTutuXDegR7r96qZjkT77WMbQqKTROCyQIDAQAB-----END PUBLIC KEY-----";
        //校验Jwt
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(publickey));

        //获取Jwt原始内容
        String claims = jwt.getClaims();
        System.out.println(claims);
        //jwt令牌
        String encoded = jwt.getEncoded();
        System.out.println(encoded);
    }

}
