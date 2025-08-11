package io.github.future0923.debug.tools.test.spring.boot.three.issue70;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;

/**
 * @author future0923
 */
@Repository
public class UserDao extends ServiceImpl<UserMapper, User> {

}
