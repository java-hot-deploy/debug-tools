package io.github.future0923.debug.tools.test.spring.boot.three.issue70;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * @author future0923
 */
@Data
@TableName("dp_user")
public class User {

    private Integer id;

    private String name;

    private Integer age;

    private Integer version;

}