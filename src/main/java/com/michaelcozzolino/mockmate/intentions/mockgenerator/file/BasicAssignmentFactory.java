package com.michaelcozzolino.mockmate.intentions.mockgenerator.file;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;

public class BasicAssignmentFactory {
    public static PsiElement create(Project project, PhpPsiElement leftHandOperand, PhpPsiElement rightHandOperand) {
        leftHandOperand.add(createAssignmentSymbol(project));
        leftHandOperand.add(rightHandOperand);

        return leftHandOperand;
    }

    /**
     * Creates the element "="
     */
    protected static PsiElement createAssignmentSymbol(Project project) {
        return PhpPsiElementFactory.createFromText(project, PhpTokenTypes.opASGN, "'1' = '1'");
    }
}
