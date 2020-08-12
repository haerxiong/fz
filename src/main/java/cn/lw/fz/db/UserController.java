package cn.lw.fz.db;

import cn.lw.fz.busi.BusiUtils;
import cn.lw.fz.busi.LoginCookie;
import cn.lw.fz.busi.LoginResult;
import cn.lw.fz.busi.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/fz")
public class UserController {

    @Autowired
    UserDao userDao;

    @RequestMapping("/")
    public String list(Model model) {
        List<User> all = userDao.findAll(Sort.by(Sort.Direction.DESC, "createTime"));
        model.addAttribute("all", all);
        return "list";
    }

    @RequestMapping("/create")
    @ResponseBody
    public String create() {
        if (userDao.count() >= 20) {
            return "已超过20条";
        }
        boolean register = false;
        LoginUser loginUser = new LoginUser( LoginUser.genStr(8) + "@126.com");
        try {
            register = BusiUtils.register(loginUser);
            if (register) {
                LoginCookie cookie = BusiUtils.login(loginUser);
                LoginResult user = BusiUtils.user(cookie);
                User newUser = new User();
                newUser.setCreateTime(new Date());
                newUser.setEmail(loginUser.getEmail());
                newUser.setRest(user.getRest());
                newUser.setUrl(user.getUrl());
                userDao.save(newUser);
                return "ok";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
        return loginUser.getEmail() + (register ? "注册成功" : "注册失败");
    }

    @RequestMapping("/refresh/{id}")
    @ResponseBody
    public String refresh(@PathVariable("id") Integer id) {
        try {
            User one = userDao.getOne(id);
            LoginUser loginUser = new LoginUser(one.getEmail());
            LoginCookie cookie = BusiUtils.login(loginUser);
            LoginResult user = BusiUtils.user(cookie);
            one.setRest(user.getRest());
            one.setUrl(user.getUrl());
            userDao.save(one);
            return "ok";
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    @RequestMapping("/bak/{id}")
    @ResponseBody
    public String del(@PathVariable("id") Integer id) {
        try {
            int bak = userDao.bak(id);
            if (bak > 0) {
                userDao.deleteById(id);
            }
            return "ok";
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
    }
}
