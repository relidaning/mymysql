package xyz.lidaning.mymysql.domains;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;
import xyz.lidaning.random.Random;

@Data
public class User {
  private int id;
  @Random({"张三","李四","王五","赵六","钱七","孙八","周九","吴十"})
  private String username;
  private transient String password;
  @Random({"0","1"}) 
  private String gender;
  @Random(dates={"1990-01-01","1991-01-01","1992-01-01","1993-01-01","1994-01-01",
    "1995-01-01","1996-01-01","1997-01-01","1998-01-01","1999-01-01"})
  private Date birthday;
  @Random(integers = {18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35})
  private int age;
  @Random({"18512345678","18512345679","18512345680","18512345681","18512345682",
    "18512345683","18512345684","18512345685"})
  private String phone;
  @Random({"18512345678@qq.com", "18512345679@qq.com", "18512345680@qq.com", 
    "18512345681@qq.com", "18512345682@qq.com", "18512345683@qq.com", "18512345684@qq.com"})
  private String email;
  private BigDecimal balance;
  
  private String nativePalce;
  private String residentPlace;
  
  private String degree;
  
  private String job;
  
  private String hobby;

}
