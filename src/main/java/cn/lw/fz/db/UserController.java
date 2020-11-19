package cn.lw.fz.db;

import cn.lw.fz.busi.BusiUtils;
import cn.lw.fz.busi.LoginCookie;
import cn.lw.fz.busi.LoginResult;
import cn.lw.fz.busi.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Controller
@RequestMapping("/fz")
public class UserController {

    @Autowired
    UserDao userDao;

    private User curUser;
    private Map<Integer, String> map = new HashMap<>();

    private ExecutorService pool = Executors.newFixedThreadPool(2);

    private boolean isEnough(User user) {
        return user.getRest().contains("GB");
    }

    private boolean isAlmostGone(User user) {
        if (!this.isEnough(user)) return true;
        BigDecimal bd = new BigDecimal(user.getRest().replace("GB", ""));
        return bd.compareTo(new BigDecimal("2")) < 0;
    }

    @GetMapping("/auto")
    @ResponseBody
    public String auto(HttpServletResponse response) throws Exception {
        String targetUrl = null;

        if (curUser != null) {
            targetUrl = curUser.getUrl();
            if (isAlmostGone(curUser)) {
                // 余量小于2G时，每次请求后更新余量，如果不足1G，则下次更换地址。
                pool.submit(() -> {
                    User user = this.refreshUser(curUser);
                    if (!this.isEnough(user)) {
                        this.del(curUser.getId());
                    }
                });
            }
        } else {
            List<User> all = userDao.findAll();
            for (User u : all) {
                if (this.isEnough(u)) {
                    targetUrl = u.getUrl();
                    curUser = u;
                }
            }
            if (all.size() < 5) {
                pool.submit(() -> {
                    this.create();
                });
            }
        }

        response.setHeader("Content-Disposition", "attachment; filename=sFrU7rgsRso38v2g.txt");
        String rs;
        if (map.containsKey(curUser.getId())) {
            rs = map.get(curUser.getId());
        } else {
            rs = BusiUtils.getRs(targetUrl);
            map.put(curUser.getId(), rs);
        }
        System.out.println(rs);
        return rs;
    }

    @RequestMapping("")
    public String list(Model model) {
        List<User> all = userDao.findAll();
        model.addAttribute("all", all);
        model.addAttribute("curId", curUser==null?-1:curUser.getId());
        return "list";
    }

    @RequestMapping("/create")
    @ResponseBody
    public String create() {
        if (userDao.count() >= 20) {
            return "已超过20条";
        }
        boolean register = false;
        LoginUser loginUser = new LoginUser(LoginUser.genStr(8) + "@126.com");
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
        User user = this.refreshUser(id);
        if (user != null) {
            return "ok";
        }
        return null;
    }

    private User refreshUser(User one) {
        try {
            LoginUser loginUser = new LoginUser(one.getEmail());
            LoginCookie cookie = BusiUtils.login(loginUser);
            LoginResult user = BusiUtils.user(cookie);
            one.setRest(user.getRest());
            one.setUrl(user.getUrl());
            userDao.save(one);
            return one;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private User refreshUser(Integer id) {
        User one = userDao.getOne(id);
        return this.refreshUser(one);
    }

    @RequestMapping("/bak/{id}")
    @ResponseBody
    public String del(@PathVariable("id") Integer id) {
        try {
            int bak = userDao.bak(id);
            if (bak > 0) {
                userDao.deleteById(id);
            }
            if (curUser != null && curUser.getId() == id) {
                map.remove(curUser.getId());
                curUser = null;
            }
            return "ok";
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
    }
}
