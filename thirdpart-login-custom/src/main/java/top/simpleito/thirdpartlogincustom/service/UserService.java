package top.simpleito.thirdpartlogincustom.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import top.simpleito.thirdpartlogincustom.model.entity.User;

import java.sql.PreparedStatement;
import java.sql.Statement;

@Service
public class UserService {
    private final JdbcTemplate jdbcTemplate;
    @Autowired
    public UserService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public User queryUserByOAuthId(String clientId, Object id) {
        var keyName = clientId + "_id";
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM user WHERE " + keyName + " = ?",
                    new BeanPropertyRowMapper<>(User.class), id);
        } catch (EmptyResultDataAccessException e){
            return null;
        }
    }

    public User registerUser(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        PreparedStatementCreator preparedStatementCreator = con -> {
            PreparedStatement ps = con.prepareStatement("INSERT INTO user(name, email, gitee_id) VALUES(?,?,?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setLong(3, user.getGiteeId());
            return ps;
        };

        if (jdbcTemplate.update(preparedStatementCreator, keyHolder) <= 0)
            throw new RuntimeException();
        user.setId(keyHolder.getKey().intValue());
        return user;
    }
}
