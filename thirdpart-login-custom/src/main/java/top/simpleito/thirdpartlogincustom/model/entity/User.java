package top.simpleito.thirdpartlogincustom.model.entity;

public class User {
    private Integer id;
    private String name;
    private String email;
    private Long giteeId;

    public Integer getId() {
        return id;
    }

    public User setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }

    public Long getGiteeId() {
        return giteeId;
    }

    public User setGiteeId(Long giteeId) {
        this.giteeId = giteeId;
        return this;
    }
}
