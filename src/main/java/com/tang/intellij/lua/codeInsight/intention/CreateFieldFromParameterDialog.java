package com.tang.intellij.lua.codeInsight.intention;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 *
 * Created by tangzx on 2017/2/11.
 */
public class CreateFieldFromParameterDialog extends DialogWrapper {
    private String fieldName;
    private JTextField nameField;
    private JCheckBox docCheckbox;

    CreateFieldFromParameterDialog(@Nullable Project project, String defaultName) {
        super(project);
        this.fieldName = defaultName;
        init();
        setTitle("Create Field");
    }

    @Nullable
    @Override
    protected JComponent createNorthPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbConstraints = new GridBagConstraints();
        gbConstraints.insets = JBUI.insets(4);
        gbConstraints.anchor = GridBagConstraints.EAST;
        gbConstraints.fill = GridBagConstraints.BOTH;

        gbConstraints.gridwidth = 1;
        gbConstraints.weightx = 1;
        gbConstraints.weighty = 1;

        //------------------------
        gbConstraints.weightx = 0;
        gbConstraints.gridx = 0;
        gbConstraints.gridy = 0;
        JLabel nameLabel = new JLabel("Name:");
        panel.add(nameLabel, gbConstraints);

        gbConstraints.weightx = 1;
        gbConstraints.gridx = 1;
        nameField = new JTextField(fieldName) {
            @Override
            public Dimension getPreferredSize() {
                Dimension dimension = super.getPreferredSize();
                dimension.setSize(200, dimension.getHeight());
                return dimension;
            }
        };
        panel.add(nameField, gbConstraints);

        //------------------------
        gbConstraints.weightx = 1;
        gbConstraints.gridx = 0;
        gbConstraints.gridy = 1;
        docCheckbox = new JCheckBox("Type annotation");
        panel.add(docCheckbox, gbConstraints);

        return panel;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return null;
    }

    public String getFieldName() {
        return nameField.getText();
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return nameField;
    }

    boolean isCreateDoc() {
        return docCheckbox.isSelected();
    }
}
