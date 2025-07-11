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
package io.github.future0923.debug.tools.server.compiler;

import io.github.future0923.debug.tools.base.hutool.core.io.FileUtil;
import io.github.future0923.debug.tools.base.utils.DebugToolsExecUtils;
import lombok.Data;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author future0923
 */
class DynamicCompilerTest {

    public static void main(String[] args) throws Exception{
        String jarPath = Data.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        File file = new File(jarPath);

        String javaHome = DebugToolsExecUtils.findJavaHome();
        File toolsJar = DebugToolsExecUtils.findToolsJar(javaHome);
        List<URL> urls = new LinkedList<>();
        urls.add(file.toURI().toURL());
        try {
            Class.forName("com.sun.tools.javac.processing.JavacProcessingEnvironment");
        } catch (ClassNotFoundException e) {
            urls.add(toolsJar.toURI().toURL());
        }

        URL toolsJarUrl = toolsJar.toURI().toURL();

        //// 获取 AppClassLoader
        //ClassLoader appClassLoader = ClassLoader.getSystemClassLoader();
        //
        //// 反射调用 URLClassLoader 的 addURL 方法（JDK 8 可用）
        //Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        //addURL.setAccessible(true);
        //addURL.invoke(appClassLoader, toolsJarUrl);

        URLClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[0]),
                ClassLoader.getSystemClassLoader());
        Thread.currentThread().setContextClassLoader(classLoader);
        //String jarPath = LoggerFactory.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        //DynamicCompiler dynamicCompiler = new DynamicCompiler(DynamicCompilerTest.class.getClassLoader());

        DynamicCompiler dynamicCompiler = new DynamicCompiler(classLoader);

        InputStream logger1Stream = DynamicCompilerTest.class.getClassLoader().getResourceAsStream("Test.java");

        dynamicCompiler.addSource("Test", toString(logger1Stream));

        Map<String, byte[]> byteCodes = dynamicCompiler.buildByteCodes();

        System.out.println(byteCodes.containsKey("com.test.Test"));

        FileUtil.writeBytes(byteCodes.get("com.test.Test"), new File("Test.class"));
    }

    /**
     * Get the contents of an <code>InputStream</code> as a String
     * using the default character encoding of the platform.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedInputStream</code>.
     *
     * @param input  the <code>InputStream</code> to read from
     * @return the requested String
     * @throws NullPointerException if the input is null
     * @throws IOException if an I/O error occurs
     */
    public static String toString(InputStream input) throws IOException {
        BufferedReader br = null;
        try {
            StringBuilder sb = new StringBuilder();
            br = new BufferedReader(new InputStreamReader(input));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }
}