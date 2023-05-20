package com.michaelcozzolino.mockmate.intentions.mockgenerator.VO;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;

public class InitializableTestElement {
    public final PhpPsiElement property;

    public final PsiElement initializer;

    public InitializableTestElement(PhpPsiElement property, PsiElement initializer) {
        this.property = property;
        this.initializer = initializer;
    }
}
