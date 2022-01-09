package top.simpleito.thirdpartlogincustom.model.dto;

import top.simpleito.thirdpartlogincustom.model.entity.User;

public class LoginDTO {
    private String token;
    private User user;

    public LoginDTO(String token, User user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public LoginDTO setToken(String token) {
        this.token = token;
        return this;
    }
}
