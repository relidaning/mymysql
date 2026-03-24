package xyz.lidaning.mymysql.controllers;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import ch.qos.logback.core.model.Model;
import lombok.extern.slf4j.Slf4j;
import xyz.lidaning.common.JsonResult;
import xyz.lidaning.mymysql.domains.User;
import xyz.lidaning.random.RandomObjectUtil;
@Slf4j
@Controller
public class DemoController {

  @Autowired
  JdbcTemplate jdbcTemplate;

  @GetMapping("/")
  public ModelAndView index(){
    return new ModelAndView("index");
  }

  @Transactional
  @GetMapping("/initTables")
  public ModelAndView initTables(ModelAndView model){
    jdbcTemplate.execute("DROP TABLE IF EXISTS users");
    jdbcTemplate.execute("CREATE TABLE users (\n" +
      "  id INT AUTO_INCREMENT PRIMARY KEY,\n" +
      "  username VARCHAR(100) NOT NULL,\n" +
      "  gender VARCHAR(2)," +
      "  birthday date ,\n" +
      "  age INT NOT NULL\n" +
      ")  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ;");
    model.addObject("message", "Initialized users table.");
    return index();
  }

  @GetMapping("/insert1millionUsersv1")
  public ModelAndView insert1millionUsersv1(ModelAndView model) throws Exception{
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
    model.addObject("message", "Inserted 10 million users.");
    return index();
  }

  @GetMapping("/insert1millionUsersv2")
  public ModelAndView insert1millionUsersv2(ModelAndView model) throws Exception{
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
    model.addObject("message", "Inserted 10 million users.");
    return index();
  }

  @GetMapping("/count")
  public ModelAndView count(ModelAndView model){
    long start = System.currentTimeMillis();
    List result = jdbcTemplate.queryForList("SELECT gender, COUNT(1) nums FROM users group by gender");
    Map map=new HashMap();
    long end = System.currentTimeMillis();
    long total=(end-start);
    model.addObject("message", "Costs: "+total/1000+"s, result: "+result.toString());
    return index();
  }

  /* @Autowired
  UserRepository userRepository; */
  @GetMapping("/insert2ES")
  public JsonResult insert2ES(){

    /* int pageSize = 10000; // Adjust batch size as needed
    long totalRecords = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
    
    for (int offset = 0; offset < totalRecords; offset += pageSize) {
        String sql = "SELECT * FROM users LIMIT ? OFFSET ?";
        List<User> users = jdbcTemplate.query(sql, new Object[]{pageSize, offset}, new BeanPropertyRowMapper<>(User.class));
        userRepository.saveAll(users);
        log.info("Inserted {} records from offset {}", users.size(), offset);
    }*/
    return JsonResult.success(); 
  }

}
