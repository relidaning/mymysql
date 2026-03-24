package xyz.lidaning.mymysql.controllers;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

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
  public ModelAndView index() {
    return new ModelAndView("index");
  }

  @Transactional
  @GetMapping("/initTables")
  @ResponseBody
  public JsonResult initTables(ModelAndView model) {
    jdbcTemplate.execute("DROP TABLE IF EXISTS users");
    jdbcTemplate.execute("CREATE TABLE users (\n" +
        "  id INT AUTO_INCREMENT PRIMARY KEY,\n" +
        "  username VARCHAR(100) NOT NULL,\n" +
        "  gender VARCHAR(2)," +
        "  birthday date ,\n" +
        "  age INT NOT NULL\n" +
        ")  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ;");
    return JsonResult.success("Initialized users table.");
  }
  
  @GetMapping("/insert1millionUsersv1")
  @ResponseBody
  public JsonResult insert1millionUsersv1(ModelAndView model) throws Exception {
    long start = System.currentTimeMillis();
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
    long end = System.currentTimeMillis();
    log.info("Time taken to insert 10 million users: {} s", (end - start)/1000);
    return JsonResult.success("Inserted 10 million users in " + (end - start) / 1000 + "s.");
  }

  @GetMapping("/insert1millionUsersv2/{table}")
  @ResponseBody
  public JsonResult insert1millionUsersv2(@PathVariable("table") String table) throws Exception {
    long start = System.currentTimeMillis();
    ExecutorService executorService = Executors.newFixedThreadPool(64);
    for (int i = 0; i < 1000; i++) {
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
          jdbcTemplate.batchUpdate("INSERT INTO " + table + " (username, gender, birthday, age) " +
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
    long end = System.currentTimeMillis();
    log.info("Time taken to insert 10 million users: {} s", (end - start)/1000);
    return JsonResult.success("Inserted 10 million users in " + (end - start) / 1000 + "s.");
  }

  @GetMapping("/count")
  @ResponseBody
  public JsonResult count(ModelAndView model) {
    long start = System.currentTimeMillis();
    List result = jdbcTemplate.queryForList("SELECT case gender when '0' then 'female' else 'male' end as gender, "+
      " COUNT(1) nums FROM users group by gender");
    long end = System.currentTimeMillis();
    long total = (end - start);
    log.info("Time taken to count users: {} s", total / 1000);
    return JsonResult.success("Costs: " + total / 1000 + "s, result: " + result.toString());
  }

  @GetMapping("/addIdxGender/{table}")
  @ResponseBody
  public JsonResult addIdxGender(@PathVariable("table") String table){
    jdbcTemplate.execute("ALTER TABLE " + table + " ADD INDEX idx_gender (gender)");
    return JsonResult.success("Added index on gender.");
  }
  
  @GetMapping("/initPortionedUserTbl")
  @ResponseBody
  public JsonResult initPortionedUserTbl(){
    jdbcTemplate.execute("  CREATE TABLE usersv2 ( " + //
            "  id INT AUTO_INCREMENT, \n" +
            "  username VARCHAR(100) NOT NULL,\n" +
            "  gender VARCHAR(2)," +
            "  birthday date ,\n" +
            "  age INT NOT NULL ,\n" +
            "  PRIMARY KEY(id, birthday) "+
            " ) " +
            " ENGINE=InnoDB \n" + 
            " DEFAULT CHARSET=utf8mb4\n" +
            " COLLATE=utf8mb4_bin "+
            "   PARTITION BY RANGE (YEAR(birthday)) ( " + //
            "   PARTITION p90 VALUES LESS THAN (1991), " + //
            "   PARTITION p91 VALUES LESS THAN (1992), " + //
            "   PARTITION p92 VALUES LESS THAN (1993), " + //
            "   PARTITION p93 VALUES LESS THAN (1994), " + //
            "   PARTITION p94 VALUES LESS THAN (1995), " + //
            "   PARTITION p95 VALUES LESS THAN (1996), " + //
            "   PARTITION p96 VALUES LESS THAN (1997), " + //
            "   PARTITION p97 VALUES LESS THAN (1998), " + //
            "   PARTITION p98 VALUES LESS THAN (1999), " + //
            "   PARTITION p99 VALUES LESS THAN (2000), " + //
            "   PARTITION pmax  VALUES LESS THAN MAXVALUE " + //
            " ) "
          );
            
    return JsonResult.success("Initialized portioned users table.");
  }

  @GetMapping("/queryPortionedUsersInaSpecificDate")
  @ResponseBody
  public JsonResult queryPortionedUsersInaSpecificDate() {
    long start = System.currentTimeMillis();
    List<Map<String, Object>> results = jdbcTemplate.queryForList("SELECT * FROM usersv2 WHERE birthday >= '1995-01-01' AND birthday < '1996-01-01'");
    long end = System.currentTimeMillis();
    return JsonResult.success("Querying portioned users born in 1995, total: " + results.size() + ", time taken: " + (end - start) / 1000 + " s");
  }

  @GetMapping("/queryPortionedUsersAggregatedByGender")
  @ResponseBody
  public JsonResult queryPortionedUsersAggregatedByGender() {
    long start = System.currentTimeMillis();
    List<Map<String, Object>> results = jdbcTemplate.queryForList("SELECT case gender when '0' then 'female' else 'male' end as gender "+
      " , COUNT(*) as count FROM usersv2 GROUP BY gender");
    long end = System.currentTimeMillis();
    return JsonResult.success("Querying portioned users aggregated by gender, time taken: " + (end - start) / 1000 + " s", results);
  }

  /*
   * @Autowired
   * UserRepository userRepository;
   */
  @GetMapping("/insert2ES")
  @ResponseBody
  public JsonResult insert2ES() {

    /*
     * int pageSize = 10000; // Adjust batch size as needed
     * long totalRecords = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users",
     * Long.class);
     * 
     * for (int offset = 0; offset < totalRecords; offset += pageSize) {
     * String sql = "SELECT * FROM users LIMIT ? OFFSET ?";
     * List<User> users = jdbcTemplate.query(sql, new Object[]{pageSize, offset},
     * new BeanPropertyRowMapper<>(User.class));
     * userRepository.saveAll(users);
     * log.info("Inserted {} records from offset {}", users.size(), offset);
     * }
     */
    return JsonResult.success();
  }

}
