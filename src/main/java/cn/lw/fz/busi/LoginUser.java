package cn.lw.fz.busi;

import com.sun.org.apache.bcel.internal.generic.LUSHR;
import lombok.Data;

import java.util.Random;

@Data
public class LoginUser {

    public LoginUser(String email) {
        this.email = email;
    }

    String email;

    LoginCookie cookie;

    String getPwd() {
        return email.replace("@", "").replace(".", "");
    }

    public static String genStr(int len) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            int offset = random.nextInt('z' - 'a' + 1 + 9);
            if ('a' + offset > 'z') {
                sb.append('a' + offset - 'z' - 1);
            } else {
                sb.append((char) ('a' + offset));
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println(LoginUser.genStr(8));
    }
}
