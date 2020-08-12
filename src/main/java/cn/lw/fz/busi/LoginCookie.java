package cn.lw.fz.busi;

import lombok.Data;

@Data
public class LoginCookie {
    String cfduid = "ddc6af25c8950b70fce74d68925dca1dc1596106881";
    String uid = "22542";
    String email = "xa%40126.com";
    String key = "7cae2ff33ade3d34585bf9dc134677c6d60e6bb86ca22";
    String ip = "ab31b5064f58b08cf2198f8438786306";
    String expireIn = "1597127528";

    String getCookieStr() {
        return String.format("__cfduid=%s; uid=%s; email=%s; key=%s; ip=%s; expire_in=%s"
                , cfduid, uid, email, key, ip, expireIn);
    }
}
