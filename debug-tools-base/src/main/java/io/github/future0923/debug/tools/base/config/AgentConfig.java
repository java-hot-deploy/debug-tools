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
package io.github.future0923.debug.tools.base.config;

import io.github.future0923.debug.tools.base.SpyAPI;
import io.github.future0923.debug.tools.base.constants.ProjectConstants;
import io.github.future0923.debug.tools.base.hutool.core.io.FileUtil;
import io.github.future0923.debug.tools.base.logging.Logger;
import io.github.future0923.debug.tools.base.utils.DebugToolsFileUtils;
import io.github.future0923.debug.tools.base.utils.DebugToolsLibUtils;
import io.github.future0923.debug.tools.base.utils.DebugToolsStringUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Properties;

/**
 * debug-tools-cache.properties配置类
 *
 * @author future0923
 */
public class AgentConfig {

    private static final Logger logger = Logger.getLogger(AgentConfig.class);

    private static final String FILE_NAME = "debug-tools-cache.properties";

    private static final String VERSION = "debug.tools.version";

    private static final String SPRING_EXTENSION_PATH = "debug.tools.extension.spring.path";

    private static final String SOLON_EXTENSION_PATH = "debug.tools.extension.solon.path";

    private static final String XXLJOB_EXTENSION_PATH = "debug.tools.extension.xxljob.path";

    private static final String JNI_LIBRARY_PATH = "debug.tools.jni.library.path";

    public static final AgentConfig INSTANCE = new AgentConfig();

    private final Properties properties = new Properties();

    private final File propertiesFile;

    private boolean isUpgrade;

    private AgentConfig() {
        propertiesFile = FileUtil.touch(DebugToolsLibUtils.getDebugToolsConfigDir() + File.separator + FILE_NAME);
        if (!propertiesFile.exists()) {
            DebugToolsFileUtils.touch(propertiesFile);
        }
        try {
            properties.load(propertiesFile.toURI().toURL().openStream());
            processUpgrade();
            createExtensionJar();
        } catch (IOException e) {
            logger.error("load properties error", e);
        }

    }

    private void processUpgrade() {
        String version = getVersion();
        isUpgrade = !ProjectConstants.VERSION.equals(version);
        if (isUpgrade) {
            setVersion(ProjectConstants.VERSION);
        }
    }

    private void createExtensionJar() {
        createSpringJar();
        createSolonJar();
        createXxlJobJar();
        store();
    }

    private void createSpringJar() {
        File jarFile = loadJarFile(getSpringExtensionPath(), ProjectConstants.SPRING_EXTENSION_JAR_NAME);
        setSpringExtensionPath(jarFile.getAbsolutePath());
    }

    private void createSolonJar() {
        File jarFile = loadJarFile(getSolonExtensionPath(), ProjectConstants.SOLON_EXTENSION_JAR_NAME);
        setSolonExtensionPath(jarFile.getAbsolutePath());
    }

    private void createXxlJobJar() {
        File jarFile = loadJarFile(getXxlJobExtensionPath(), ProjectConstants.XXMLJOB_EXTENSION_JAR_NAME);
        setXxlJobExtensionPath(jarFile.getAbsolutePath());
    }

    private File loadJarFile(String jarPath, String jarName) {
        File jarFile;
        if (ProjectConstants.DEBUG || jarPath == null || jarPath.isEmpty() || isUpgrade) {
            jarFile = DebugToolsFileUtils.getLibResourceJar(SpyAPI.class.getClassLoader(), jarName);
        } else {
            File file = new File(jarPath);
            if (file.exists()) {
                jarFile = file;
            } else {
                jarFile = DebugToolsFileUtils.getLibResourceJar(SpyAPI.class.getClassLoader(), jarName);
            }
        }
        return jarFile;
    }

    public String getVersion() {
        return properties.getProperty(VERSION);
    }

    public void setVersion(String version) {
        properties.setProperty(VERSION, version);
    }

    public void setVersionAndStore(String version) {
        setVersion(version);
        store();
    }

    public boolean isUpgrade() {
        return isUpgrade;
    }

    public void setUpgrade(boolean upgrade) {
        isUpgrade = upgrade;
    }

    public String getSpringExtensionPath() {
        return properties.getProperty(SPRING_EXTENSION_PATH);
    }

    public URL getSpringExtensionURL() {
        try {
            return DebugToolsStringUtils.resourceNameToURL(getSpringExtensionPath());
        } catch (Exception e) {
            if (ProjectConstants.DEBUG) {
                logger.warning("load spring extension error", e);
            }
            return null;
        }
    }


    public void setSpringExtensionPath(String path) {
        properties.setProperty(SPRING_EXTENSION_PATH, path);
    }

    public void setSpringExtensionPathAndStore(String corePath) {
        setSpringExtensionPath(corePath);
        store();
    }

    public String getSolonExtensionPath() {
        return properties.getProperty(SOLON_EXTENSION_PATH);
    }

    public URL getSolonExtensionURL() {
        try {
            return DebugToolsStringUtils.resourceNameToURL(getSolonExtensionPath());
        } catch (Exception e) {
            if (ProjectConstants.DEBUG) {
                logger.warning("load solon extension error", e);
            }
            return null;
        }
    }


    public void setSolonExtensionPath(String path) {
        properties.setProperty(SOLON_EXTENSION_PATH, path);
    }

    public void setSolonExtensionPathAndStore(String corePath) {
        setSolonExtensionPath(corePath);
        store();
    }

    public String getXxlJobExtensionPath() {
        return properties.getProperty(XXLJOB_EXTENSION_PATH);
    }

    public URL getXxlJobExtensionURL() {
        try {
            return DebugToolsStringUtils.resourceNameToURL(getXxlJobExtensionPath());
        } catch (Exception e) {
            if (ProjectConstants.DEBUG) {
                logger.warning("load xxl-job extension error", e);
            }
            return null;
        }
    }

    public void setXxlJobExtensionPath(String path) {
        properties.setProperty(XXLJOB_EXTENSION_PATH, path);
    }

    public void setXxlJobExtensionPathAndStore(String corePath) {
        setXxlJobExtensionPath(corePath);
        store();
    }

    public String getJniLibraryPath() {
        return properties.getProperty(JNI_LIBRARY_PATH);
    }

    public void setJniLibraryPath(String jniLibraryPath) {
        properties.setProperty(JNI_LIBRARY_PATH, jniLibraryPath);
    }

    public void setJniLibraryPathAndStore(String jniLibraryPath) {
        setJniLibraryPath(jniLibraryPath);
        store();
    }

    public void store() {
        try {
            OutputStream outputStream = Files.newOutputStream(propertiesFile.toPath());
            properties.store(outputStream, "config debug tools core path");
            outputStream.close();
        } catch (IOException e) {
            logger.error("store properties error", e);
        }
    }
}
