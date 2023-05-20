package com.michaelcozzolino.mockmate.intentions.mockgenerator.classfile;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.michaelcozzolino.mockmate.intentions.mockgenerator.VO.Property;
import org.jetbrains.annotations.NotNull;

public class PropertyFactory {
    public static PhpPsiElement create(@NotNull Project project, @NotNull Property property) {
        return PhpPsiElementFactory.createClassField(
                project,
                property.modifier,
                property.name,
                property.defaultValue,
                property.type.toString()
        );
    }
}
