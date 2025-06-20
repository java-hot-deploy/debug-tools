/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.future0923.debug.tools.test.solon.aop;

import org.noear.solon.core.aspect.Interceptor;
import org.noear.solon.core.aspect.Invocation;


/**
 * @author future0923
 */
public class TestAopInterceptor implements Interceptor {

    @Override
    public Object doIntercept(Invocation inv) throws Throwable {
        TestAop anno = inv.getMethodAnnotation(TestAop.class);
        if (anno == null) {
            anno = inv.getTargetAnnotation(TestAop.class);
        }
        if (anno == null) {
            return inv.invoke();
        }
        System.out.println("before");
        Object invoke = inv.invoke();
        System.out.println("after");
        return invoke;
    }
}
