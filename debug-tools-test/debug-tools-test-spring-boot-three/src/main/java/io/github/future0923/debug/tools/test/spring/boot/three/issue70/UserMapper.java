package io.github.future0923.debug.tools.test.spring.boot.three.issue70;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author future0923
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
