package io.github.future0923.debug.tools.test.spring.boot.three.issue70;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author future0923
 */
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    public void list() {
        userService.list();
    }
}
