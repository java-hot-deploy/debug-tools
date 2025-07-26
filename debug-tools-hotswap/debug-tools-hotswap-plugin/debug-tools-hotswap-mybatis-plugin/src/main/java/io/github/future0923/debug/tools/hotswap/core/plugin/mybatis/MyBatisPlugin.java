/*
 * Copyright (C) 2024-2025 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package io.github.future0923.debug.tools.hotswap.core.plugin.mybatis;

import io.github.future0923.debug.tools.base.logging.Logger;
import io.github.future0923.debug.tools.hotswap.core.annotation.FileEvent;
import io.github.future0923.debug.tools.hotswap.core.annotation.Init;
import io.github.future0923.debug.tools.hotswap.core.annotation.OnResourceFileEvent;
import io.github.future0923.debug.tools.hotswap.core.annotation.Plugin;
import io.github.future0923.debug.tools.hotswap.core.command.Scheduler;
import io.github.future0923.debug.tools.hotswap.core.plugin.mybatis.command.MyBatisSpringXmlReloadCommand;
import io.github.future0923.debug.tools.hotswap.core.plugin.mybatis.patch.DynamicPatcher;
import io.github.future0923.debug.tools.hotswap.core.plugin.mybatis.patch.IBatisPatcher;
import io.github.future0923.debug.tools.hotswap.core.plugin.mybatis.patch.MyBatisPlusPatcher;
import io.github.future0923.debug.tools.hotswap.core.plugin.mybatis.patch.MyBatisSpringPatcher;
import io.github.future0923.debug.tools.hotswap.core.plugin.mybatis.reload.MyBatisSpringResourceManager;
import io.github.future0923.debug.tools.hotswap.core.plugin.mybatis.utils.MyBatisUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Mybatis热重载插件
 */
@Plugin(name = "MyBatis",
        description = "Reload MyBatis configuration after configuration create/change.",
        testedVersions = {"All between 5.3.2"},
        expectedVersions = {"5.3.2"},
        supportClass = {
                IBatisPatcher.class,
                MyBatisPlusPatcher.class,
                MyBatisSpringPatcher.class,
                DynamicPatcher.class
        }
)
public class MyBatisPlugin {

    private static final Logger logger = Logger.getLogger(MyBatisPlugin.class);

    @Init
    Scheduler scheduler;

    @Init
    static ClassLoader appClassLoader;

    /**
     * 不能使用注解，因为注解只能获取AppClassLoader
     */
    private static ClassLoader userClassLoader;

    private final Map<String, Object> configurationMap = new HashMap<>();

    /**
     * 打印Solon信息
     */
    public void init(ClassLoader classLoader) {
        MyBatisPlugin.userClassLoader = classLoader;
    }

    public static ClassLoader getUserClassLoader() {
        return userClassLoader == null ? appClassLoader : userClassLoader;
    }

    /**
     * 在{@link IBatisPatcher#patchXMLMapperBuilder}处调用时生成mapper文件信息
     */
    public void registerConfigurationFile(String configFile, Object configObject) {
        if (configFile != null && !configurationMap.containsKey(configFile)) {
            logger.debug("MyBatisPlugin - configuration file registered : {}", configFile);
            configurationMap.put(configFile, configObject);
        }
    }

    /**
     * OnResourceFileEvent只能在主插件作用与实例对象，所以放在这里
     */
    @OnResourceFileEvent(path = "/", filter = ".*.xml", events = {FileEvent.CREATE, FileEvent.MODIFY})
    public void watchResource(final URL url, final FileEvent fileEvent) throws ParserConfigurationException, IOException, SAXException {
        logger.debug("registerResourceListeners, url:{}", url.getPath());
        if (!MyBatisUtils.isMyBatisSpring(appClassLoader) && !MyBatisUtils.isMyBatisPlus(appClassLoader)) {
            return;
        }
        if ((FileEvent.CREATE.equals(fileEvent) && MyBatisUtils.isMapperXml(url.getPath()) && isInMapperLocations(url))
                || (FileEvent.MODIFY.equals(fileEvent) && configurationMap.containsKey(url.getPath()))) {
            scheduler.scheduleCommand(new MyBatisSpringXmlReloadCommand(appClassLoader, url), 1000);
        }
    }

    private static boolean isInMapperLocations(URL url) {
        if (MyBatisSpringResourceManager.isInMapperLocations(url.getPath())) {
            return true;
        }
        logger.info("{} is not in mybatis.mapper-locations or mybatis-plus.mapper-locations", url.getPath());
        return false;
    }
}