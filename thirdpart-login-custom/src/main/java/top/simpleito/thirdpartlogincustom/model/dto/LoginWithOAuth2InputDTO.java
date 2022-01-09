package top.simpleito.thirdpartlogincustom.model.dto;

public class LoginWithOAuth2InputDTO {
    private String code;
    private String state;

    public String getCode() {
        return code;
    }

    public LoginWithOAuth2InputDTO setCode(String code) {
        this.code = code;
        return this;
    }

    public String getState() {
        return state;
    }

    public LoginWithOAuth2InputDTO setState(String state) {
        this.state = state;
        return this;
    }
}
