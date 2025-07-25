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
package io.github.future0923.debug.tools.idea.ui.editor;

import com.intellij.json.JsonFileType;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import io.github.future0923.debug.tools.common.utils.DebugToolsJsonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author future0923
 */
public class JsonEditor extends BaseEditor {

    public static final String FILE_NAME = "DebugToolsEditFile.json";

    /**
     * json格式
     */
    public static final FileType JSON_FILE_TYPE = JsonFileType.INSTANCE;

    public JsonEditor(@NotNull Project project) {
        this(project, null);
    }

    public JsonEditor(Project project, String text) {
        super(project, JSON_FILE_TYPE, text);
    }

    @Override
    protected String fileName() {
        return FILE_NAME;
    }

    @Override
    public void setText(@Nullable String text) {
        setText(DebugToolsJsonUtils.toJsonPrettyStr(text), JSON_FILE_TYPE);
    }
}
