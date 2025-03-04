package xyz.lidaning.mymysql.controllers;

import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import xyz.lidaning.common.JsonResult;
import xyz.lidaning.mymysql.domains.User;
import xyz.lidaning.random.RandomObjectUtil;
@Slf4j
@RestController
public class DemoController {

  @Autowired
  JdbcTemplate jdbcTemplate;

  @Transactional
  @PostMapping("/initTables")
  public JsonResult initTables(){
    jdbcTemplate.execute("DROP TABLE IF EXISTS users");
    jdbcTemplate.execute("CREATE TABLE users (\n" +
      "  id INT AUTO_INCREMENT PRIMARY KEY,\n" +
      "  username VARCHAR(100) NOT NULL,\n" +
      "  gender VARCHAR(2)," +
      "  birthday date ,\n" +
      "  age INT NOT NULL\n" +
      ")  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ;");
    return JsonResult.success();
  }

  @PostMapping("/insert1millionUsersv1")
  public JsonResult insert1millionUsersv1() throws Exception{
    List<User> users = new ArrayList<>();
    for (int i = 0; i < 10000000; i++) {
      users.add(RandomObjectUtil.randomInstance(User.class));
    }
    jdbcTemplate.batchUpdate("INSERT INTO users (username, gender, birthday, age) " +
        "VALUES (?, ?, ?, ?)",
        users,
        1000,
        (PreparedStatement ps, User u) -> {
          log.debug("user: {}", u);
          ps.setString(1, u.getUsername());
          ps.setString(2, u.getGender());
          ps.setDate(3, new java.sql.Date(u.getBirthday().getTime()));
          ps.setInt(4, u.getAge());
        });
    return JsonResult.success();
  }

  @PostMapping("/insert1millionUsersv2")
  public JsonResult insert1millionUsersv2() throws Exception{
    ExecutorService executorService = Executors.newFixedThreadPool(64);
    for(int i=0;i<1000;i++){
      executorService.execute(new Runnable() {
        @Override
        public void run() {
          List<User> users = new ArrayList<>();
          for (int i = 0; i < 10000; i++) {
            try {
              users.add(RandomObjectUtil.randomInstance(User.class));
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
          jdbcTemplate.batchUpdate("INSERT INTO users (username, gender, birthday, age) " +
          "VALUES (?, ?, ?, ?)",
          users,
          10000,
          (PreparedStatement ps, User u) -> {
            log.debug("user: {}", u);
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getGender());
            ps.setDate(3, new java.sql.Date(u.getBirthday().getTime()));
            ps.setInt(4, u.getAge());
          });
        }
      });
    }
    
    return JsonResult.success();
  }


}
