package com.michaelcozzolino.mockmate.intentions.mockgenerator.file;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.michaelcozzolino.mockmate.intentions.mockgenerator.exception.ElementCreationFailedException;

import java.util.List;

public class InstanceAssignmentFactory {
    public static PsiElement create(Project project, String name, PhpType type, List<String> parameters) throws
            ElementCreationFailedException {
        return BasicAssignmentFactory.create(
                project,
                PhpPsiElementFactory.createFieldReferenceUsingThis(project, name),
                InstanceCreationFactory.create(project, type.toString(), parameters)
        );
    }
}
