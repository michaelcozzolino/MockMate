package com.michaelcozzolino.mockmate.intentions.mockgenerator.phpunit.testcase;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.michaelcozzolino.mockmate.intentions.mockgenerator.file.BasicAssignmentFactory;
import com.michaelcozzolino.mockmate.intentions.mockgenerator.utils.PhpUtil;

public class MockAssignmentFactory {
    protected static final String MOCK_PROPERTY_SUFFIX = "Mock";

    /**
     * Creates the element "$this->classNameMock = $this->createMock(ClassName::class);"
     */
    public static PsiElement create(Project project, String mockName, String originalClassName) {
        return BasicAssignmentFactory.create(
                project,
                PhpPsiElementFactory.createFieldReferenceUsingThis(project, createMockName(mockName)), //$this->name
                createRightHandOperand(project, originalClassName)
        );
    }

    /**
     * Creates the element "createMock(ClassName::class)"
     */
    protected static MethodReference createRightHandOperand(Project project, String originalClassName) {
        MethodReference createMockCall = PhpPsiElementFactory.createMethodReference(project, getCreateMockCall(originalClassName));
        createMockCall.add(PhpUtil.createSemiColon(project));
        createMockCall.add(PhpPsiElementFactory.createNewLine(project));

        return createMockCall;
    }

    protected static String getCreateMockCall(String originalClassName) {
        return String.format("$this->createMock(%s::class)", originalClassName);
    }

    protected static String createMockName(String propertyName) {
        return propertyName + MOCK_PROPERTY_SUFFIX;
    }
}
