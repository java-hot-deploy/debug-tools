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
package io.github.future0923.debug.tools.hotswap.core.plugin.mybatis.reload;

import io.github.future0923.debug.tools.base.logging.Logger;
import io.github.future0923.debug.tools.hotswap.core.config.PluginConfiguration;
import io.github.future0923.debug.tools.hotswap.core.config.PluginManager;
import javassist.ClassPool;
import javassist.CtClass;
import io.github.future0923.debug.tools.hotswap.core.plugin.mybatis.patch.IBatisPatcher;
import io.github.future0923.debug.tools.hotswap.core.plugin.mybatis.patch.MyBatisSpringPatcher;
import org.apache.ibatis.session.Configuration;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.ClassPathMapperScanner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * MyBatis环境资源管理
 *
 * @author future0923
 */
public class MyBatisSpringResourceManager {

    private static final Logger logger = Logger.getLogger(MyBatisSpringResourceManager.class);

    /**
     * mybatis Configuration 集合
     */
    private static final Set<Configuration> configurationList = new HashSet<>();

    /**
     * MyBatis Spring MapperScanner
     */
    private static ClassPathMapperScanner mapperScanner;

    /**
     * Mapper.xml文件扫描路径
     */
    private static final Set<String> mapperLocations = new HashSet<>();

    /**
     * 用于解析Mapper.xml文件的位置
     */
    private static final PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    /**
     * <ul>
     *  <li>当{@link Configuration}实例化的时候{@link IBatisPatcher#patchConfiguration}会注册进来。</li>
     *  <li>当{@link SqlSessionFactoryBean}实例化完成时会获取到{@link Configuration}对象注入到集合中，在{@link MyBatisSpringPatcher#patchSqlSessionFactoryBean(CtClass, ClassPool)}插桩。</li>
     * </ul>
     */
    public static void registerConfiguration(Configuration configuration) {
        if (configuration != null) {
            configurationList.add(configuration);
        }
    }

    /**
     * {@link MyBatisSpringPatcher#patchClassPathMapperScanner}注入对象
     */
    public static void loadScanner(ClassPathMapperScanner scanner) {
        if(null != mapperScanner) {
            return;
        }
        mapperScanner = scanner;

    }

    /**
     * {@link MyBatisSpringPatcher#patchMapperLocations(CtClass, ClassPool)}注入对象
     */
    public static void addMapperLocations(String[] mapperLocations) {
        if (mapperLocations == null) {
            return;
        }
        MyBatisSpringResourceManager.mapperLocations.addAll(Arrays.asList(mapperLocations));
    }

    /**
     * 获取 url 的真实地址，因为可能在 watchResources 和 extraClasspath 中
     */
    public static String getRelativePath(URL changedUrl) {
        PluginConfiguration pluginConfiguration = PluginManager.getInstance().getPluginConfiguration(MyBatisSpringResourceManager.class.getClassLoader());
        String changePath = changedUrl.getPath();
        URL[] watchResources = pluginConfiguration.getWatchResources();
        if (watchResources != null) {
            for (URL watchResource : watchResources) {
                if (changePath.contains(watchResource.getPath())) {
                    return changePath.replace(watchResource.getPath(), "");
                }
            }
        }

        URL[] extraClasspath = pluginConfiguration.getExtraClasspath();
        if (extraClasspath != null) {
            for (URL extraUrl : extraClasspath) {
                if (changePath.contains(extraUrl.getPath())) {
                    return changePath.replace(extraUrl.getPath(), "");
                }
            }
        }

        return changePath;
    }

    public static ClassPathMapperScanner getMapperScanner() {
        return mapperScanner;
    }

    public static Set<Configuration> getConfigurationList() {
        return configurationList;
    }

    public static boolean isInMapperLocations(String absolutePath) {
        if (mapperLocations.isEmpty()) {
            logger.debug("mapperLocations未配置，所有mapper xml文件都会加载");
            return true;
        }
        for (String mapperLocation : mapperLocations) {
            try {
                Resource[] resources = resourcePatternResolver.getResources(mapperLocation);
                for (Resource resource : resources) {
                    if (resource.getFile().getAbsolutePath().equals(absolutePath)) {
                        return true;
                    }
                }
            } catch (Exception e) {
                logger.error("获取mapperLocations失败", e);
            }
        }
        return false;
    }
}
