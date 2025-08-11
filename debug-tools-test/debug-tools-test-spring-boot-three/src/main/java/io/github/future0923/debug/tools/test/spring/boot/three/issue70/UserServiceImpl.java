package io.github.future0923.debug.tools.test.spring.boot.three.issue70;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author future0923
 */
@Service("userService")
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    @Override
    public List<User> list() {
        return userDao.list();
    }
}
