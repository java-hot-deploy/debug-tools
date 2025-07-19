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
package io.github.future0923.debug.tools.server.scoket.handler;

import io.github.future0923.debug.tools.base.hutool.core.exceptions.ExceptionUtil;
import io.github.future0923.debug.tools.base.exception.DefaultClassLoaderException;
import io.github.future0923.debug.tools.base.logging.Logger;
import io.github.future0923.debug.tools.base.utils.DebugToolsFileUtils;
import io.github.future0923.debug.tools.base.utils.DebugToolsOSUtils;
import io.github.future0923.debug.tools.common.handler.BasePacketHandler;
import io.github.future0923.debug.tools.common.protocal.packet.Packet;
import io.github.future0923.debug.tools.common.protocal.packet.response.HotDeployResponsePacket;
import io.github.future0923.debug.tools.hotswap.core.config.PluginConfiguration;
import io.github.future0923.debug.tools.hotswap.core.config.PluginManager;
import io.github.future0923.debug.tools.hotswap.core.util.classloader.ClassLoaderHelper;
import io.github.future0923.debug.tools.server.DebugToolsBootstrap;

import java.io.File;
import java.io.OutputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author future0923
 */
public abstract class AbstractHotDeployRequestHandler<T extends Packet> extends BasePacketHandler<T> {

    private static final Logger logger = Logger.getLogger(AbstractHotDeployRequestHandler.class);

    /**
     * instrumentation的redefineClasses锁
     */
    protected final Object hotswapLock = new Object();

    protected abstract Map<String, byte[]> getByteCodes(T packet) throws DefaultClassLoaderException;

    protected abstract ClassLoader getClassLoader(T packet) throws DefaultClassLoaderException;

    @Override
    public void handle(OutputStream outputStream, T packet) throws Exception {
        long start = System.currentTimeMillis();
        Map<String, byte[]> byteCodesMap;
        try {
            byteCodesMap = getByteCodes(packet);
        } catch (Exception e) {
            writeAndFlushNotException(outputStream, HotDeployResponsePacket.of(false, "Hot deploy error\n" + ExceptionUtil.stacktraceToString(e, -1), DebugToolsBootstrap.serverConfig.getApplicationName()));
            return;
        }
        String reloadClass = String.join(", ", byteCodesMap.keySet());
        ClassLoader defaultClassLoader;
        try {
            defaultClassLoader = getClassLoader(packet);
        } catch (DefaultClassLoaderException e) {
            logger.error("Fail to reload classes {}, msg is {}", reloadClass, e);
            writeAndFlushNotException(outputStream, HotDeployResponsePacket.of(false, "Hot deploy error, file [" + reloadClass + "]\n" + ExceptionUtil.stacktraceToString(e, -1), DebugToolsBootstrap.serverConfig.getApplicationName()));
            return;
        }
        writeFile(defaultClassLoader, byteCodesMap);
        List<ClassDefinition> definitions = new ArrayList<>();
        for (Map.Entry<String, byte[]> entry : byteCodesMap.entrySet()) {
            if (ClassLoaderHelper.isClassLoaded(defaultClassLoader, entry.getKey())) {
                definitions.add(new ClassDefinition(defaultClassLoader.loadClass(entry.getKey()), entry.getValue()));
            }
        }
        if (definitions.isEmpty()) {
            logger.warning("There are no classes that need to be redefined. {}", reloadClass);
            writeAndFlushNotException(outputStream, HotDeployResponsePacket.of(true, "Hot deploy success, file [" + reloadClass + "]", DebugToolsBootstrap.serverConfig.getApplicationName()));
            return;
        }
        try {
            logger.reload("Reloading classes {}", reloadClass);
            synchronized (hotswapLock) {
                Instrumentation instrumentation = DebugToolsBootstrap.INSTANCE.getInstrumentation();
                instrumentation.redefineClasses(definitions.toArray(new ClassDefinition[0]));
            }
            long end = System.currentTimeMillis();
            writeAndFlushNotException(outputStream, HotDeployResponsePacket.of(true, "Hot deploy success. cost " + (end - start) +" ms. file [" + reloadClass + "]", DebugToolsBootstrap.serverConfig.getApplicationName()));
        } catch (Exception e) {
            logger.error("Fail to reload classes {}, msg is {}", reloadClass, e);
            writeAndFlushNotException(outputStream, HotDeployResponsePacket.of(false, "Hot deploy error, file [" + reloadClass + "]\n" + ExceptionUtil.stacktraceToString(e, -1), DebugToolsBootstrap.serverConfig.getApplicationName()));
        }
    }

    protected void writeFile(ClassLoader defaultClassLoader, Map<String, byte[]> byteCodesMap) {
        PluginConfiguration pluginConfiguration = PluginManager.getInstance().getPluginConfiguration(defaultClassLoader);
        if (pluginConfiguration == null) {
            logger.error("Failure to retrieve PluginConfiguration. Please ensure that the project is started in hot reload mode.");
            return;
        }
        URL[] classpath = pluginConfiguration.getExtraClasspath();
        if (classpath == null || classpath.length == 0) {
            logger.error("cache is null");
            return;
        }
        String extraClasspath = classpath[0].getPath();
        if (!extraClasspath.endsWith(File.separator)) {
            extraClasspath += File.separator;
        }
        for (Map.Entry<String, byte[]> entry : byteCodesMap.entrySet()) {
            String clasFilePath = entry.getKey().replace(".", File.separator).concat(".class");
            DebugToolsFileUtils.writeBytes(entry.getValue(), extraClasspath + clasFilePath);
        }
    }
}
