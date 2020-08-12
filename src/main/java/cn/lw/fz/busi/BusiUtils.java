package cn.lw.fz.busi;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.InputStream;
import java.net.URI;
import java.util.Scanner;

public class BusiUtils {

    public static boolean register(LoginUser user) throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost();
        httpPost.setURI(URI.create("https://f1zz.club/auth/register"));
        httpPost.addHeader("content-type", "application/json");
        JSONObject jo = new JSONObject();
        jo.put("email", user.getEmail());
        jo.put("name", user.getPwd());
        jo.put("passwd", user.getPwd());
        jo.put("repasswd", user.getPwd());
        jo.put("wechat", user.getPwd());
        jo.put("imtype", "2");
        jo.put("code", "0");
        StringEntity stringEntity = new StringEntity(jo.toJSONString(), "UTF-8");

        httpPost.setEntity(stringEntity);
        return client.execute(httpPost, response -> {
            HttpEntity rs = response.getEntity();
            InputStream is = rs.getContent();
            Scanner scanner = new Scanner(is).useDelimiter("\\A");
            String str = scanner.hasNext() ? scanner.next() : "";
            System.out.println("--------");
            System.out.println(str);
            return str.contains("\"ret\":1");
        });
    }

    public static LoginResult user(LoginCookie cookie) throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet();
        httpGet.setURI(URI.create("https://f1zz.club/user"));
        httpGet.addHeader("content-type", "text/html; charset=UTF-8");
        httpGet.addHeader("cookie", cookie.getCookieStr());
        return client.execute(httpGet, response -> {
            LoginResult result = null;
            HttpEntity rs = response.getEntity();
            InputStream is = rs.getContent();
            Scanner scanner = new Scanner(is).useDelimiter("\\A");
            String str = scanner.hasNext() ? scanner.next() : "";
            System.out.println("--------");
            try {
                String url = str.split("重置订阅地址")[1].split("name=\"input1\" value=\"")[2].split("\" readonly=\"true\"")[0];
                String rest = str.split("id=\"remain\">")[1].split("</code>")[0];
                result = new LoginResult();
                result.setUrl(url);
                result.setRest(rest);
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return result;
            }
        });
    }

    public static LoginCookie login(LoginUser user) throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost();
        httpPost.setURI(URI.create("https://f1zz.club/auth/login"));
        httpPost.addHeader("content-type", "application/json");
        JSONObject jo = new JSONObject();
        jo.put("email", user.getEmail());
        jo.put("passwd", user.getPwd());
        StringEntity stringEntity = new StringEntity(jo.toJSONString(), "UTF-8");
        httpPost.setEntity(stringEntity);
        return client.execute(httpPost, response -> {
            System.out.println("--------");
            LoginCookie loginCookie = new LoginCookie();
            for (Header header : response.getHeaders("Set-cookie")) {
                for (HeaderElement element : header.getElements()) {
                    switch (element.getName()) {
                        case "__cfduid":
                            loginCookie.setCfduid(element.getValue());
                            break;
                        case "email":
                            loginCookie.setEmail(element.getValue());
                            break;
                        case "expire_in":
                            loginCookie.setExpireIn(element.getValue());
                            break;
                        case "uid":
                            loginCookie.setUid(element.getValue());
                            break;
                        case "key":
                            loginCookie.setKey(element.getValue());
                            break;
                        case "ip":
                            loginCookie.setIp(element.getValue());
                            break;
                        default:
                    }
                }
            }
            return loginCookie;
        });
    }
}
