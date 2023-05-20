package com.michaelcozzolino.mockmate.intentions.mockgenerator.file;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.NewExpression;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.michaelcozzolino.mockmate.intentions.mockgenerator.exception.ElementCreationFailedException;
import com.michaelcozzolino.mockmate.intentions.mockgenerator.utils.PhpUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class InstanceCreationFactory {

    public static @NotNull PhpPsiElement create(
            Project project,
            String instanceClassType,
            List<String> parameterNames
    ) throws ElementCreationFailedException {
        PhpPsiElement instanceParameters = getInstanceParameters(project, parameterNames);
        String instanceString   = String.format("new %s(%s)", instanceClassType, instanceParameters != null ? instanceParameters.getText() : "");

        PhpPsiElement instance = PhpPsiElementFactory.createFromText(project, NewExpression.class, instanceString);

        if (instance == null) {
            throw new ElementCreationFailedException(String.format("The instance of class %s::class cannot be created.",
                                                                   instanceClassType
            ));
        }

        instance.add(PhpUtil.createSemiColon(project));
        instance.add(PhpPsiElementFactory.createNewLine(project));

        return instance;
    }

    @Nullable
    protected static PhpPsiElement getInstanceParameters(Project project, @NotNull List<String> parameterNames) {
        PhpPsiElement parameters = null;

       for (String name : parameterNames) {
            PhpPsiElement parameter = PhpPsiElementFactory.createVariable(project, name, true);
            parameter.add(PhpPsiElementFactory.createComma(project));
            if (parameters == null) {
                parameters = parameter;
            } else {
                parameters.add(parameter);
            }
        }

        return parameters;
    }
}
