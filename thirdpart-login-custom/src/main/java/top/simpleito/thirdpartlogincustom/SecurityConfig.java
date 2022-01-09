package top.simpleito.thirdpartlogincustom;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.impl.HMAC;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 默认配置下tokenResolver不会从参数中获取，此处方便测试取参数"access_token"
        var tokenResolver = new DefaultBearerTokenResolver();
        tokenResolver.setAllowUriQueryParameter(true);

        defaultConfig(http)
                // 确实没理解好 OAuth2AuthorizationCodeGrantFilter 作用，但又是oauth2Client所必须配置的
                // 因为它在该业务场景下，会带来额外开销（1.带code访问回调地址时对access_token的访问，包装Authentication又不用 2.完了进行client的保存也是不需要的 3.又对回调地址去参数后再次重定向）
                // 此处修改后还会连带WebClient出问题，所以最后WebClient也没用，详细参考 WebClientConfiguration 注释
                .oauth2Client(oc ->
                    oc.authorizationCodeGrant().authorizationRequestRepository(new AuthorizationRequestRepository<>() {
                        @Override
                        public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {return null;}
                        @Override
                        public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {}
                        @Override
                        public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request) {return null;}
                    })
                )
                .authorizeRequests(authorize -> authorize
                        .antMatchers("/favicon.ico").permitAll()
                        .antMatchers("/login/**", "/oauth2/**").permitAll() // 放行授权请求以及回调
                        .antMatchers("/public/**").permitAll()
                        .anyRequest().fullyAuthenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                        .accessDeniedHandler(new BearerTokenAccessDeniedHandler())
                )
                .oauth2ResourceServer(ors -> ors
                        .bearerTokenResolver(tokenResolver)
                        .jwt() // 可通过 jwtAuthenticationConverter() 定制我们自己的业务需要 Authentication，包括token失效判断，是否读取数据库，权限怎么拿等等。
                );

    }

    private HttpSecurity defaultConfig(HttpSecurity http) throws Exception {
        // 调整部分默认配置，具体可参考WebSecurityConfigurerAdapter::applyDefaultConfiguration
        return http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().formLogin().disable()
                .logout().disable()
                .requestCache().disable();
//                .anonymous().disable(); //不要禁了，否则在Filter鉴权时会因为没有Authentication对象而直接抛异常
    }

    @Bean
    public JdbcTemplate jdbcTemplate(@Autowired DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }


    @Value("${jwt.public.key}")
    RSAPublicKey key;

    @Value("${jwt.private.key}")
    RSAPrivateKey priv;

    @Bean
    JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(this.key).build();
    }

    @Bean
    JwtEncoder jwtEncoder() {
        JWK jwk = new RSAKey.Builder(this.key).privateKey(this.priv).build();
        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwks);
    }

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
