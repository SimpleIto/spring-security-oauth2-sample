package top.simpleito.thirdpartlogincustom.controller.template;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import top.simpleito.thirdpartlogincustom.controller.LoginController;
import top.simpleito.thirdpartlogincustom.model.entity.User;

@Controller
public class PageController {

    @GetMapping("login")
    public String login() {
        return "login";
    }

    @GetMapping("user")
    public String user(Model model) {
        var token = (JwtAuthenticationToken)SecurityContextHolder.getContext().getAuthentication();
        // 因为我们并未定义自己的Authentication，也没去自定义jwt->authentication转换逻辑，没有实际查询user放入身份中。
        // 因此这里只能拿到token中携带信息，而且token失效完全依赖token中的expire声明，依赖默认解析时的validate逻辑。至于能不能满足 就根据业务而定了。
        User user = null;
        model.addAttribute("token", token.getToken().getTokenValue());
        model.addAttribute("atts", token.getTokenAttributes());
        return "user";
    }
}
