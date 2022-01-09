package top.simpleito.thirdpartlogincustom.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import top.simpleito.thirdpartlogincustom.model.dto.LoginDTO;
import top.simpleito.thirdpartlogincustom.model.dto.LoginWithOAuth2InputDTO;
import top.simpleito.thirdpartlogincustom.model.entity.User;
import top.simpleito.thirdpartlogincustom.model.utils.BaseDTO;
import top.simpleito.thirdpartlogincustom.model.utils.Response;
import top.simpleito.thirdpartlogincustom.service.UserService;

import javax.servlet.http.HttpServletRequest;

import java.time.Instant;
import java.util.Map;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@RestController
public class LoginController {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());
    static final String SESSION_KEY_TEMP_OAUTH_INFO = "TEMP_OAUTH_INFO";
    static final String SESSION_KEY_TEMP_OAUTH_CLIENT = "TEMP_OAUTH_CLIENT";

    /**
     * @see top.simpleito.thirdpartlogincustom.WebClientConfiguration
     */
    private final WebClient webClient;
    private final JdbcTemplate jdbcTemplate;
    private final UserService userService;
    private final JwtEncoder jwtEncoder;
    private final RestTemplate restTemplate;
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    public LoginController(JdbcTemplate jdbcTemplate, WebClient webClient, UserService userService, JwtEncoder jwtEncoder, RestTemplate restTemplate, ClientRegistrationRepository clientRegistrationRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.webClient = webClient;
        this.userService = userService;
        this.jwtEncoder = jwtEncoder;
        this.restTemplate = restTemplate;
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    /**
     * （在我们模型中，回调地址为前端页面。在demo中直接用回调地址，模拟前端请求）
     * 1. 以Oauth2流程，根据传入的授权码，进行一系列请求获取第三方用户数据
     * 2. 拿到三方用户数据，查询是否已绑定该系统用户，若有绑定则直接登录；若未绑定则告诉前端需补全信息注册，并临时将信息存入Session（不信任前端提交）
     */
    @GetMapping("/login/oauth2/code/{clientId}")
    public BaseDTO<Object> loginWithOauth2(LoginWithOAuth2InputDTO inputDTO,
                                           @PathVariable("clientId") String clientId,
                                           HttpServletRequest request) throws JsonProcessingException {
        var client = clientRegistrationRepository.findByRegistrationId(clientId);
        String body = null;
        try {
            body = queryForUserInfo(clientRegistrationRepository.findByRegistrationId(clientId), inputDTO.getCode());
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error(-1, e.toString());
        }
        var atts = OBJECT_MAPPER.readTree(body);
//        String body = this.webClient
//                .get()
//                .attributes(clientRegistrationId(clientId))
//                .retrieve()
//                .bodyToMono(String.class)
//                .block();

        Object id = null;
        String name = null;
        if (clientId.equals("gitee")) {
            id = atts.get("id").asLong();
            name = atts.get("name").asText();
        }

        User existedUser = userService.queryUserByOAuthId(clientId, id);
        if (existedUser == null){
            request.getSession().setAttribute(SESSION_KEY_TEMP_OAUTH_CLIENT, clientId);
            request.getSession().setAttribute(SESSION_KEY_TEMP_OAUTH_INFO, body);
            return Response.error(name,2, "无绑定用户，请补全信息注册");
        }

        return Response.ok(new LoginDTO(genToken(existedUser), existedUser));
    }

    @PostMapping("/public/sign/oauth2")
    public BaseDTO signupWithOAuth2Info(@RequestBody Map<String, String> params,
                                        HttpServletRequest request) throws JsonProcessingException {
        var session = request.getSession();

        var clientId = (String) session.getAttribute(SESSION_KEY_TEMP_OAUTH_CLIENT);
        User user = null;
        if ("gitee".equals(clientId)) {
            var atts = OBJECT_MAPPER.readTree((String)session.getAttribute(SESSION_KEY_TEMP_OAUTH_INFO));
            user = new User()
                    .setEmail(params.get("email"))
                    .setName(atts.get("name").asText())
                    .setGiteeId(atts.get("id").asLong());
        }

        if (user == null){
            return Response.error(-1,null);
        }
        user = userService.registerUser(user);
        return Response.ok(new LoginDTO(genToken(user), user));
    }

    public String queryForUserInfo(ClientRegistration clientRegistration, String code) throws Exception{
        var param1 = Map.of("grant_type", "authorization_code",
                "code", code,
                "client_id", clientRegistration.getClientId(),
                "redirect_uri", clientRegistration.getRedirectUri(),
                "client_secret", clientRegistration.getClientSecret());
        var access_token = restTemplate.postForEntity(clientRegistration.getProviderDetails().getTokenUri(), param1, Map.class).getBody().get("access_token");

        var userInfoUri = clientRegistration.getProviderDetails().getUserInfoEndpoint().getUri() + "?access_token={1}";
        var result = restTemplate.getForEntity(userInfoUri, String.class, access_token).getBody();
        if ("".equals(result) || result == null)
            throw new RuntimeException("用户信息获取异常");
        return result;
    }

    public String genToken(User user) {
        var now = Instant.now();
        var claims = JwtClaimsSet.builder()
                .issuer("ASystem")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(86400))
                .subject(user.getId()+"")
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
