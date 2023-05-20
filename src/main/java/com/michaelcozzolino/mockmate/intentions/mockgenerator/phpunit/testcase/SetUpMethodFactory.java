package com.michaelcozzolino.mockmate.intentions.mockgenerator.phpunit.testcase;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.PhpCodeUtil;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.Method;
import com.michaelcozzolino.mockmate.intentions.mockgenerator.VO.InitializableTestElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class SetUpMethodFactory {

    public static Method create(Project project, ArrayList<InitializableTestElement> properties) {
        Method setUp = generateSetUp(project);
        initializeMocks(setUp, properties);

        return setUp;
    }

    protected static void initializeMocks(Method setUp, ArrayList<InitializableTestElement> mocks) {
        PsiElement setUpParentCall = getSetUpParentCall(setUp);

        if (setUpParentCall == null) {
            return;
        }

        mocks.forEach((InitializableTestElement mock) ->
            setUp.addAfter(
                    mock.initializer,
                    setUpParentCall
            )
        );
    }

    protected static Method generateSetUp(Project project) {
        String setup = PhpCodeUtil.getCodeTemplate("PHPUnit SetUp Method", null, project);

        return PhpPsiElementFactory.createMethod(project, setup);
    }

    /**
     * Returns the element "parent::setUp()" from the overridden setUp, null if setUp is not overridden
     */
    protected static @Nullable PsiElement getSetUpParentCall(@NotNull Method setUp) {
        try {
            return setUp.getChildren()[2].getChildren()[0];
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }
}
