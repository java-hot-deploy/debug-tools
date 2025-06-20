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
package io.github.future0923.debug.tools.idea.ui.setting;

import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.FormBuilder;
import io.github.future0923.debug.tools.base.enums.PrintSqlType;
import io.github.future0923.debug.tools.idea.setting.DebugToolsSettingState;
import io.github.future0923.debug.tools.idea.setting.GenParamType;
import lombok.Getter;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * @author future0923
 */
public class SettingPanel {

    private final DebugToolsSettingState settingState;
    @Getter
    private JPanel settingPanel;

    @Getter
    private final JBRadioButton defaultGenParamTypeSimple = new JBRadioButton(GenParamType.SIMPLE.getType());
    @Getter
    private final JBRadioButton defaultGenParamTypeCurrent = new JBRadioButton(GenParamType.CURRENT.getType());
    @Getter
    private final JBRadioButton defaultGenParamTypeAll = new JBRadioButton(GenParamType.ALL.getType());

    @Getter
    private final JBRadioButton printPrettySql = new JBRadioButton(PrintSqlType.PRETTY.getType());
    @Getter
    private final JBRadioButton printCompressSql = new JBRadioButton(PrintSqlType.COMPRESS.getType());
    @Getter
    private final JBRadioButton printNoSql = new JBRadioButton(PrintSqlType.NO.getType());

    @Getter
    private final JBRadioButton autoAttachYes = new JBRadioButton("Yes");
    @Getter
    private final JBRadioButton autoAttachNo = new JBRadioButton("No");

    @Getter
    private final JBTextArea removeContextPath = new JBTextArea();

    public SettingPanel(Project project) {
        this.settingState = DebugToolsSettingState.getInstance(project);
        initLayout();
    }

    private void initLayout() {
        JPanel defaultGenParamType = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        defaultGenParamType.add(defaultGenParamTypeSimple);
        defaultGenParamType.add(defaultGenParamTypeCurrent);
        defaultGenParamType.add(defaultGenParamTypeAll);
        ButtonGroup defaultGenParamTypeButtonGroup = new ButtonGroup();
        defaultGenParamTypeButtonGroup.add(defaultGenParamTypeSimple);
        defaultGenParamTypeButtonGroup.add(defaultGenParamTypeCurrent);
        defaultGenParamTypeButtonGroup.add(defaultGenParamTypeAll);
        if (GenParamType.SIMPLE.equals(settingState.getDefaultGenParamType())) {
            defaultGenParamTypeSimple.setSelected(true);
        } else if (GenParamType.CURRENT.equals(settingState.getDefaultGenParamType())) {
            defaultGenParamTypeCurrent.setSelected(true);
        } else if (GenParamType.ALL.equals(settingState.getDefaultGenParamType())) {
            defaultGenParamTypeAll.setSelected(true);
        }

        JPanel printSqlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        printSqlPanel.add(printPrettySql);
        printSqlPanel.add(printCompressSql);
        printSqlPanel.add(printNoSql);
        ButtonGroup printSqlButtonGroup = new ButtonGroup();
        printSqlButtonGroup.add(printPrettySql);
        printSqlButtonGroup.add(printCompressSql);
        printSqlButtonGroup.add(printNoSql);
        if (PrintSqlType.PRETTY.equals(settingState.getPrintSql()) || PrintSqlType.YES.equals(settingState.getPrintSql())) {
            printPrettySql.setSelected(true);
        } else if (PrintSqlType.COMPRESS.equals(settingState.getPrintSql())) {
            printCompressSql.setSelected(true);
        } else {
            printNoSql.setSelected(true);
        }

        JPanel autoAttachPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        autoAttachPanel.add(autoAttachYes);
        autoAttachPanel.add(autoAttachNo);
        ButtonGroup autoAttachButtonGroup = new ButtonGroup();
        autoAttachButtonGroup.add(autoAttachYes);
        autoAttachButtonGroup.add(autoAttachNo);
        if (settingState.getAutoAttach()) {
            autoAttachYes.setSelected(true);
        } else {
            autoAttachNo.setSelected(true);
        }

        removeContextPath.setText(settingState.getRemoveContextPath());
        // 添加边框
        Border border = BorderFactory.createLineBorder(JBColor.GRAY); // 创建灰色线条边框
        removeContextPath.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(5, 5, 5, 5))); // 内外边框组合
        // 自动换行
        removeContextPath.setLineWrap(true);
        // 按单词边界换行
        removeContextPath.setWrapStyleWord(true);
        settingPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(
                        new JBLabel("Entity class default param:"),
                        defaultGenParamType
                )
                .addLabeledComponent(
                        new JBLabel("Print sql:"),
                        printSqlPanel
                )
                .addLabeledComponent(
                        new JBLabel("Auto attach start application:"),
                        autoAttachPanel
                )
                .addLabeledComponent(
                        new JBLabel("Remove context path:"),
                        removeContextPath
                )
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

}
