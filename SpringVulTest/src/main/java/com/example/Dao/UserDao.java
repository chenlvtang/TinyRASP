package com.example.Dao;

import com.example.Model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Repository;

@Repository
public class UserDao {
    private JdbcTemplate jdbcTemplate;

    public UserDao() {
        // 因不明原因，只能这样硬编码了
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/spring");
        dataSource.setUsername("spring");
        dataSource.setPassword("spring");

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public User getUserById(String id) {
        // 存在SQL注入的代码
        try{
            String sql = "SELECT id, name, number FROM user WHERE id = " + id;
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setName(rs.getString("name"));
                user.setNumber(rs.getString("number"));
                return user;
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
