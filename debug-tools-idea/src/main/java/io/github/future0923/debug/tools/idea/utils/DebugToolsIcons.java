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
package io.github.future0923.debug.tools.idea.utils;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * @author future0923
 */
public class DebugToolsIcons {

    public static final Icon DebugTools = IconLoader.getIcon("/icon/plugin_icon.svg", DebugToolsIcons.class);
    public static final Icon ExampleSimple = IconLoader.getIcon("/icon/example_simple.svg", DebugToolsIcons.class);
    public static final Icon ExampleCurrent = IconLoader.getIcon("/icon/example_current.svg", DebugToolsIcons.class);
    public static final Icon ExampleAll = IconLoader.getIcon("/icon/example_all.svg", DebugToolsIcons.class);
    public static final Icon Pretty = IconLoader.getIcon("/icon/pretty.svg", DebugToolsIcons.class);
    public static final Icon Compress = IconLoader.getIcon("/icon/compress.svg", DebugToolsIcons.class);
    public static final Icon Import = IconLoader.getIcon("/icon/import.svg", DebugToolsIcons.class);
    public static final Icon Export = IconLoader.getIcon("/icon/export.svg", DebugToolsIcons.class);
    public static final Icon Add = IconLoader.getIcon("/icon/add.svg", DebugToolsIcons.class);
    public static final Icon Clear = IconLoader.getIcon("/icon/clear.svg", DebugToolsIcons.class);
    public static final Icon Groovy = IconLoader.getIcon("/icon/groovy.svg", DebugToolsIcons.class);
    public static final Icon Request = IconLoader.getIcon("/icon/request.svg", DebugToolsIcons.class);
    public static final Icon Last = IconLoader.getIcon("/icon/last.svg", DebugToolsIcons.class);
    public static final Icon Last_ClassLoader = IconLoader.getIcon("/icon/last_classloader.svg", DebugToolsIcons.class);
    public static final Icon Connect = IconLoader.getIcon("/icon/connect.svg", DebugToolsIcons.class);
    public static final Icon Search = IconLoader.getIcon("/icon/search.svg", DebugToolsIcons.class);
    public static final Icon Setting = IconLoader.getIcon("/icon/setting.svg", DebugToolsIcons.class);
    public static final Icon Help = IconLoader.getIcon("/icon/help.svg", DebugToolsIcons.class);
    public static final Icon Json = IconLoader.getIcon("/icon/json.svg", DebugToolsIcons.class);

    public static final class HttpMethod {
        public static final Icon Delete = IconLoader.getIcon("/icon/request/delete.png", DebugToolsIcons.class);
        public static final Icon Get = IconLoader.getIcon("/icon/request/get.png", DebugToolsIcons.class);
        public static final Icon Head = IconLoader.getIcon("/icon/request/head.png", DebugToolsIcons.class);
        public static final Icon Options = IconLoader.getIcon("/icon/request/options.png", DebugToolsIcons.class);
        public static final Icon Patch = IconLoader.getIcon("/icon/request/patch.png", DebugToolsIcons.class);
        public static final Icon Post = IconLoader.getIcon("/icon/request/post.png", DebugToolsIcons.class);
        public static final Icon Put = IconLoader.getIcon("/icon/request/put.png", DebugToolsIcons.class);
        public static final Icon Request = IconLoader.getIcon("/icon/request/request.png", DebugToolsIcons.class);
        public static final Icon Trace = IconLoader.getIcon("/icon/request/trace.png", DebugToolsIcons.class);
    }

    public static final class Hotswap {
        public static final Icon Off = IconLoader.getIcon("/icon/hotswap/hotswap.svg", DebugToolsIcons.class);
        public static final Icon On = IconLoader.getIcon("/icon/hotswap/hotswap_on.svg", DebugToolsIcons.class);
        public static final Icon Compile = IconLoader.getIcon("/icon/hotswap/compile.svg", DebugToolsIcons.class);
        public static final Icon Deploy = IconLoader.getIcon("/icon/hotswap/hot_deployment.svg", DebugToolsIcons.class);
        public static final Icon RemoteCompiler = IconLoader.getIcon("/icon/hotswap/remote_compiler.svg", DebugToolsIcons.class);
    }

    public static final class Action {
        public static final Icon Add = IconLoader.getIcon("/icon/action/add.svg", DebugToolsIcons.class);
        public static final Icon Delete = IconLoader.getIcon("/icon/action/delete.svg", DebugToolsIcons.class);
        public static final Icon Clear = IconLoader.getIcon("/icon/action/clear.svg", DebugToolsIcons.class);
        public static final Icon Reset = IconLoader.getIcon("/icon/action/reset.svg", DebugToolsIcons.class);
    }
}
