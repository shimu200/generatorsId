# GeneratorsId



GeneratorsId 是一款基于雪花算法生成唯一ID的Java库，是一个用于Java开发的工具类。它依赖于Spring Boot框架，并使用Redis来解决多服务所在机器的MAC地址相同可能导致ID重复的问题。

具体而言，GeneratorsId 引入了Redis来分发雪花算法中的dataCenterId和workId，有效地解决了ID重复的风险。这使得GeneratorsId在分布式系统中能够生成高效且唯一的ID，是一个适用于各种Java应用程序的可靠工具。

## 快速入门

### 1. 将项目下载到本地

```
git clone https://gitee.com/pick-up-wood/generatorsid.git
```



### 2. Maven install

通过 Maven install 将项目打包生成的构件安装到本地 Maven 仓库中



### 3.引入相关依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>com.generatorsid</groupId>
    <artifactId>generatorsid</artifactId>
    <version>1.0.0</version>
</dependency>
```



### 4.application.yml 配置

```
spring:
  data:
    redis:
      host:	###.###.###.###
      port: 6379
      password: 123456
```



### 5.编写redis配置类

```
@Configuration
public class RedisConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory();
        lettuceConnectionFactory.setHostName("###.###.###.###");
        lettuceConnectionFactory.setPort(6379);
        lettuceConnectionFactory.setPassword("123456");
        return lettuceConnectionFactory;
    }
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }
}
```



### 6.编写测试接口

```
@RestController
public class IdController {
    @Autowired
    SnowflakeIdUtil snowflakeIdUtil;

    @RequestMapping("/hello")
    public long hello() {
        Snowflake snowflake1 = SnowflakeIdUtil.getInstance();
        return snowflake1.nextId();
    }
}
```



### 7.结果

日志中看到  Snowflake type: LocalRedisWorkIdChoose 便是获取成功

```
c.g.c.s.AbstractWorkIdChooseTemplate     : Snowflake type: LocalRedisWorkIdChoose, workId: 0, dataCenterId: 0
```

测试接口

```
http://localhost:8080/hello
```

