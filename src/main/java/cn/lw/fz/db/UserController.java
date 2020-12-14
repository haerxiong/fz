package cn.lw.fz.db;

import cn.lw.fz.aop.AutoClassLog;
import cn.lw.fz.busi.BusiUtils;
import cn.lw.fz.busi.LoginCookie;
import cn.lw.fz.busi.LoginResult;
import cn.lw.fz.busi.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Controller
@RequestMapping("/fz")
@AutoClassLog
@Slf4j
public class UserController {

    @Autowired
    UserDao userDao;

    private User curUser;
    /**
     * 订阅信息缓存
     */
    private Map<Integer, String> cacheMap = new HashMap<>();

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
        // 从库中返回另一个url，然后去更新检测当前url
        List<User> all = userDao.findAll(Sort.by(Sort.Direction.ASC, "createTime"));
        for (User u : all) {
            if (curUser == null || !curUser.getEmail().equals(u.getEmail())) {
                log.warn(String.format("更换了订阅账号：[%s->%s]", (curUser==null)?"":curUser.getEmail(), u.getEmail()));
                User check = curUser;
                curUser = u;
                if (check != null) {
                    pool.submit(() -> {
                        User rs = this.refreshUser(check);
                        if (!isEnough(rs)) {
                            // 更新检查后，如果不足，删除
                            this.del(rs.getId());
                        }
                    });
                }
                break;
            }
        }

        String targetUrl = curUser.getUrl();
        if (all.size() < 5) {
            pool.submit(() -> {
                this.create();
            });
        }

        response.setHeader("Content-Disposition", "attachment; filename=sFrU7rgsRso38v2g.txt");
        String rs;
        if (cacheMap.containsKey(curUser.getId())) {
            rs = cacheMap.get(curUser.getId());
        } else {
            rs = BusiUtils.getRs(targetUrl);
            cacheMap.put(curUser.getId(), rs);
        }
        return rs;
    }

    @RequestMapping("")
    public String list(Model model) {
        List<User> all = userDao.findAll(Sort.by(Sort.Direction.ASC, "createTime"));
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
                log.warn("申请了新的账号：" + newUser.getEmail());
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
            log.warn("更新账号流量：" + one.getEmail());
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
                cacheMap.remove(curUser.getId());
                curUser = null;
            }
            return "ok";
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
    }
}
