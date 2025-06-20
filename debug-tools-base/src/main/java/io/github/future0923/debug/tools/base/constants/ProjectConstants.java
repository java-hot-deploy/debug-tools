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
package io.github.future0923.debug.tools.base.constants;

/**
 * @author future0923
 */
public interface ProjectConstants {

    Boolean DEBUG = true;

    String NAME = "DebugTools";

    String VERSION = "4.1.0-SNAPSHOT";

    String SPRING_EXTENSION_JAR_NAME = "debug-tools-extension-spring";

    String SOLON_EXTENSION_JAR_NAME = "debug-tools-extension-solon";

    String XXMLJOB_EXTENSION_JAR_NAME = "debug-tools-extension-xxljob";

    String CONFIG_FILE = "debug-tools.properties";

    String AUTO_ATTACH_FLAG_FILE = NAME + "/auto_attach.txt";
}
