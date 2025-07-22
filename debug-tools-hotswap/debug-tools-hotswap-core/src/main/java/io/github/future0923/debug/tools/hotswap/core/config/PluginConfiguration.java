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
package io.github.future0923.debug.tools.hotswap.core.config;


import io.github.future0923.debug.tools.base.hutool.core.io.FileUtil;
import io.github.future0923.debug.tools.base.logging.Logger;
import io.github.future0923.debug.tools.base.utils.DebugToolsLibUtils;
import io.github.future0923.debug.tools.base.utils.DebugToolsProperties;
import io.github.future0923.debug.tools.base.utils.DebugToolsStringUtils;
import io.github.future0923.debug.tools.hotswap.core.HotswapAgent;
import io.github.future0923.debug.tools.hotswap.core.annotation.OnResourceFileEvent;
import io.github.future0923.debug.tools.hotswap.core.annotation.Plugin;
import io.github.future0923.debug.tools.hotswap.core.plugin.watchResources.WatchResourcesPlugin;
import io.github.future0923.debug.tools.hotswap.core.util.classloader.HotswapAgentClassLoaderExt;
import io.github.future0923.debug.tools.hotswap.core.util.classloader.URLClassLoaderPathHelper;
import io.github.future0923.debug.tools.hotswap.core.util.spring.util.StringUtils;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * 插件配置，每个ClassLoader只存在一个实例
 */
public class PluginConfiguration {

    private static final Logger LOGGER = Logger.getLogger(PluginConfiguration.class);

    /**
     * 配置文件
     */
    private static final String PLUGIN_CONFIGURATION = "debug-tools-agent.properties";

    /**
     * 要初始化的ClassLoader
     */
    private static final String INCLUDED_CLASS_LOADERS_KEY = "includedClassLoaderPatterns";

    /**
     * 不初始化的ClassLoader
     */
    private static final String EXCLUDED_CLASS_LOADERS_KEY = "excludedClassLoaderPatterns";

    /**
     * 热部署文件是所有类加载所有插件公用的，所以只需要初始化一次，如果多次初始化，则之前的watch就会被后面的清理掉从而失效
     */
    private static volatile boolean clean = false;

    /**
     * 配置（key小驼峰）
     */
    Properties properties = new DebugToolsProperties();

    /**
     * 如果ClassLoader中未定义，从父类加载器中查找
     */
    PluginConfiguration parent;

    /**
     * 配置所在的类加载器
     */
    @Getter
    final ClassLoader classLoader;

    /**
     * 配置文件URL
     */
    URL configurationURL;

    /**
     * 配置文件是否在当前类加载器中
     */
    boolean containsPropertyFileDirectly = false;


    public PluginConfiguration(ClassLoader classLoader) {
        this(null, classLoader);
    }

    public PluginConfiguration(PluginConfiguration parent, ClassLoader classLoader) {
        this(parent, classLoader, true);
    }

    public PluginConfiguration(PluginConfiguration parent, ClassLoader classLoader, boolean init) {
        this.parent = parent;
        this.classLoader = classLoader;
        // 载入配置文件
        loadConfigurationFile();
        if (init) {
            init();
        }
    }

    /**
     * 载入配置文件
     * 有外部使用外部配置文件
     */
    private void loadConfigurationFile() {
        try {
            String externalPropertiesFile = HotswapAgent.getExternalPropertiesFile();
            if (DebugToolsStringUtils.isNotBlank(externalPropertiesFile)) {
                configurationURL = DebugToolsStringUtils.resourceNameToURL(externalPropertiesFile);
                properties.load(configurationURL.openStream());
                return;
            }
        } catch (Exception e) {
            LOGGER.error("Error while loading external properties file " + configurationURL, e);
        }

        if (parent == null) {
            configurationURL = classLoader == null ? ClassLoader.getSystemResource(PLUGIN_CONFIGURATION) : classLoader.getResource(PLUGIN_CONFIGURATION);
            try {
                if (configurationURL != null) {
                    containsPropertyFileDirectly = true;
                    properties.load(configurationURL.openStream());
                }
                properties.putAll(System.getProperties());
            } catch (Exception e) {
                LOGGER.error("Error while loading 'debug-tools-agent.properties' from base URL " + configurationURL, e);
            }

        } else {
            // 在父类加载器中找
            try {
                Enumeration<URL> urls = null;
                if (classLoader != null) {
                    urls = classLoader.getResources(PLUGIN_CONFIGURATION);
                }
                if (urls == null) {
                    urls = ClassLoader.getSystemResources(PLUGIN_CONFIGURATION);
                }
                while (urls.hasMoreElements()) {
                    URL url = urls.nextElement();
                    boolean found = false;
                    ClassLoader parentClassLoader = parent.getClassLoader();
                    Enumeration<URL> parentUrls = parentClassLoader == null ? ClassLoader.getSystemResources(PLUGIN_CONFIGURATION) : parentClassLoader.getResources(PLUGIN_CONFIGURATION);
                    while (parentUrls.hasMoreElements()) {
                        if (url.equals(parentUrls.nextElement()))
                            found = true;
                    }
                    if (!found) {
                        configurationURL = url;
                        break;
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Error while loading 'debug-tools-agent.properties' from URL " + configurationURL, e);
            }
            if (configurationURL == null) {
                configurationURL = parent.configurationURL;
                LOGGER.debug("Classloader does not contain 'debug-tools-agent.properties', using parent file '{}'", parent.configurationURL);

            } else {
                LOGGER.debug("Classloader contains 'debug-tools-agent.properties' at location '{}'", configurationURL);
                containsPropertyFileDirectly = true;
            }
            try {
                if (configurationURL != null) {
                    properties.load(configurationURL.openStream());
                }
            } catch (Exception e) {
                LOGGER.error("Error while loading 'debug-tools-agent.properties' from URL " + configurationURL, e);
            }
        }
    }

    protected void init() {
        LogConfigurationHelper.configureLog(properties);
        initPluginPackage();
        checkProperties();
        checkExtraPath();
        initIncludedClassLoaderPatterns();
        initExcludedClassLoaderPatterns();
        //initExtraClassPath()
        ;
    }

    private void checkExtraPath() {
        if (!clean) {
            cleanExtraPath(getExtraClasspath());
            cleanExtraPath(getWatchResources());
            clean = true;
        }
    }

    private void cleanExtraPath(URL[] extraPath) {
        for (URL url : extraPath) {
            String path = url.getPath();
            FileUtil.mkdir(path);
            FileUtil.clean(path);
        }
    }

    /**
     * 扫描其它路径的Plugin
     */
    private void initPluginPackage() {
        String pluginPackages = properties.getProperty("pluginPackages");
        if (StringUtils.hasText(pluginPackages)) {
            for (String pluginPackage : pluginPackages.split(",")) {
                PluginManager.getInstance().getPluginRegistry().scanPlugins(getClassLoader(), pluginPackage);
            }
        }
    }

    private void checkProperties() {
        if (properties != null && DebugToolsStringUtils.isNotBlank(properties.getProperty(INCLUDED_CLASS_LOADERS_KEY)) && DebugToolsStringUtils.isNotBlank(properties.getProperty(EXCLUDED_CLASS_LOADERS_KEY))) {
            throw new IllegalArgumentException("includedClassLoaderPatterns, excludedClassLoaderPatterns in debug-tools-agent.properties are exclusive to each other. You cannot configure both options");
        }
    }

    private void initExtraClassPath() {
        URL[] extraClassPath = getExtraClasspath();
        if (extraClassPath.length > 0 && !checkExcluded()) {
            if (classLoader instanceof HotswapAgentClassLoaderExt) {
                ((HotswapAgentClassLoaderExt) classLoader).$$ha$setExtraClassPath(extraClassPath);
            } else if (URLClassLoaderPathHelper.isApplicable(classLoader)) {
                URLClassLoaderPathHelper.prependClassPath(classLoader, extraClassPath);
            } else {
                LOGGER.debug("Unable to set extraClasspath to {} on classLoader {}. Only classLoader with 'ucp' " +
                        "field present is supported.\n*** extraClasspath configuration property will not be " +
                        "handled on JVM level ***", Arrays.toString(extraClassPath), classLoader);
            }
        }
    }

    private void initIncludedClassLoaderPatterns() {
        if (properties != null && DebugToolsStringUtils.isNotBlank(properties.getProperty(INCLUDED_CLASS_LOADERS_KEY))) {
            List<Pattern> includedClassLoaderPatterns = new ArrayList<>();
            for (String pattern : properties.getProperty(INCLUDED_CLASS_LOADERS_KEY).split(",")) {
                includedClassLoaderPatterns.add(Pattern.compile(pattern));
            }
            PluginManager.getInstance().getHotswapTransformer()
                    .setIncludedClassLoaderPatterns(includedClassLoaderPatterns);
        }
    }

    private void initExcludedClassLoaderPatterns() {
        if (properties != null && DebugToolsStringUtils.isNotBlank(properties.getProperty(EXCLUDED_CLASS_LOADERS_KEY))) {
            List<Pattern> excludedClassLoaderPatterns = new ArrayList<>();
            for (String pattern : properties.getProperty(EXCLUDED_CLASS_LOADERS_KEY).split(",")) {
                excludedClassLoaderPatterns.add(Pattern.compile(pattern));
            }
            PluginManager.getInstance().getHotswapTransformer()
                    .setExcludedClassLoaderPatterns(excludedClassLoaderPatterns);
        }
    }

    private boolean checkExcluded() {
        if (PluginManager.getInstance().getHotswapTransformer().getIncludedClassLoaderPatterns() != null) {
            for (Pattern pattern : PluginManager.getInstance().getHotswapTransformer().getIncludedClassLoaderPatterns()) {
                if (pattern.matcher(classLoader.getClass().getName()).matches()) {
                    return false;
                }
            }
            return true;
        }

        if (PluginManager.getInstance().getHotswapTransformer().getExcludedClassLoaderPatterns() != null) {
            for (Pattern pattern : PluginManager.getInstance().getHotswapTransformer().getExcludedClassLoaderPatterns()) {
                if (pattern.matcher(classLoader.getClass().getName()).matches()) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getProperty(String property) {
        String value = properties.getProperty(property);
        if (DebugToolsStringUtils.isNotBlank(value))
            return value;
        else if (parent != null) {
            return parent.getProperty(property);
        }
        return null;
    }

    public String getProperty(String property, String defaultValue) {
        String value = getProperty(property);
        return value != null ? value : defaultValue;
    }

    public boolean getPropertyBoolean(String property) {
        String value = properties.getProperty(property);
        if (DebugToolsStringUtils.isNotBlank(value)) {
            return Boolean.parseBoolean(value);
        } else if (parent != null) {
            return parent.getPropertyBoolean(property);
        }
        return false;
    }

    /**
     * 获取额外的ClassPath
     */
    public URL[] getExtraClasspath() {
       //  String path = DebugToolsOSUtils.isWindows() ? getProperty("extraClasspathWin") : getProperty("extraClasspath");
        String path = FileUtil.mkdir(DebugToolsLibUtils.getDebugToolsCacheDir() + File.separator + "classes").getPath();
        return convertToURL(path);
    }

    /**
     * 获取需要watch的资源路径
     *
     * @see WatchResourcesPlugin
     * @see OnResourceFileEvent
     */
    public URL[] getWatchResources() {
        String path = FileUtil.mkdir(DebugToolsLibUtils.getDebugToolsCacheDir() + File.separator + "resources").getPath();
        return convertToURL(path);
    }

    /**
     * 获取lombok.jar路径
     */
    public String getLombokJarPath() {
        return getProperty("lombokJarPath");
    }

    /**
     * Spring基础package前缀
     */
    public String[] getBasePackagePrefixes() {
        String basePackagePrefix = getProperty("spring.basePackagePrefix");
        if (basePackagePrefix != null) {
            return basePackagePrefix.split(",");
        }
        return null;
    }

    /**
     * 禁用的插件名集合
     */
    public List<String> getDisabledPlugins() {
        List<String> ret = new ArrayList<>();
        for (String disabledPlugin : getProperty("disabledPlugins", "").split(",")) {
            ret.add(disabledPlugin.trim());
        }
        return ret;
    }

    /**
     * 插件在当前ClassLoader是否被禁用
     */
    public boolean isDisabledPlugin(String pluginName) {
        return HotswapAgent.isPluginDisabled(pluginName) || getDisabledPlugins().contains(pluginName);
    }

    /**
     * 插件在当前ClassLoader是否被禁用
     */
    public boolean isDisabledPlugin(Class<?> pluginClass) {
        Plugin pluginAnnotation = pluginClass.getAnnotation(Plugin.class);
        return isDisabledPlugin(pluginAnnotation.name());
    }


    private URL[] convertToURL(String resources) {
        List<URL> ret = new ArrayList<>();
        if (resources != null) {
            StringTokenizer tokenizer = new StringTokenizer(resources, ",;");
            while (tokenizer.hasMoreTokens()) {
                String name = tokenizer.nextToken().trim();
                try {
                    ret.add(DebugToolsStringUtils.resourceNameToURL(name));
                } catch (Exception e) {
                    LOGGER.error("Invalid configuration value: '{}' is not a valid URL or path and will be skipped.", name, e);
                }
            }
        }
        return ret.toArray(new URL[0]);
    }

    /**
     * 是否存在配置文件
     */
    public boolean containsPropertyFile() {
        return containsPropertyFileDirectly;
    }
}
