package com.michaelcozzolino.plugins.intentions.testmockgenerator;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;
import com.michaelcozzolino.plugins.intentions.testmockgenerator.VO.Dependency;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


public class TestMockGenerator extends PsiElementBaseIntentionAction {
    private static final Logger LOG = Logger.getInstance(TestMockGenerator.class);


    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {

        PhpClass testClass = PhpPsiUtil.getParentByCondition(psiElement, true, PhpClass.INSTANCEOF, null);

        if (testClass == null) {
            return;
        }

        PhpDocComment testClassDocComment = testClass.getDocComment();

        String coversTagName = "@covers";
        PhpDocTag[] coversTags = testClassDocComment.getTagElementsByName(coversTagName);

        if (isDocTagUnique(coversTags) == false) {
            return;
        }

        String fqcnToTest = getDocTagValue(coversTags[0], coversTagName);

        Collection<PhpClass> foundClasses = PhpIndex.getInstance(project).getAnyByFQN(fqcnToTest);

        if (foundClasses.size() != 1) {
            return;
        }

        PhpClass classToTest = foundClasses.stream().findFirst().get();

        Method constructor = classToTest.getConstructor();

        if (constructor == null) {
            return; //temporary
        }

        List<Parameter> parameters = Arrays.stream(constructor.getParameters()).toList();

        ArrayList<Dependency> dependencies = new ArrayList<>();
        parameters.forEach((Parameter p) -> {
            dependencies.add(new Dependency(p.getType(), p.getName()));
        });

        WriteCommandAction.runWriteCommandAction(project, () -> {
            dependencies.forEach((Dependency d) -> {
                this.addClassProperty(project, testClass, d);
            });

            this.addSetUpMethod(project, testClass, dependencies);
            CodeStyleManager.getInstance(project).reformat(psiElement);

        });

    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) {
        PhpClass testClass = PhpPsiUtil.getParentByCondition(psiElement, true, PhpClass.INSTANCEOF, null);

        if (testClass == null) {
            return false;
        }

        String namespaceName = testClass.getNamespaceName();

        return namespaceName.startsWith("\\Tests");
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "Symfony2MethodCreateService";
    }

    @NotNull
    @Override
    public String getText() {
        return "Generate phpunit mocks";
    }

    protected boolean isDocTagUnique(PhpDocTag[] tags) {
        return tags.length == 1;
    }

    protected String getDocTagValue(PhpDocTag tag, String tagName) {
        return tag.getText().replace(tagName, "").trim();
    }

    protected void addClassProperty(Project project, PhpClass phpClass, Dependency dependency) {
        PhpModifier modifier = PhpModifier.instance(
                PhpModifier.Access.PROTECTED,
                PhpModifier.Abstractness.IMPLEMENTED,
                PhpModifier.State.DYNAMIC
        );

        PhpPsiElement property = PhpPsiElementFactory.createClassField(
                project,
                modifier,
                dependency.name,
                null,
                dependency.type.toString() + " & \\PHPUnit\\Framework\\MockObject\\MockObject"
        );

        phpClass.addBefore(property, phpClass.getLastChild());
    }

    protected void addSetUpMethod(Project project, PhpClass phpClass, ArrayList<Dependency> dependencies) {
        AtomicReference<String> body = new AtomicReference<>("");

        dependencies.forEach((Dependency dependency) -> {
            body.set(body.get() + this.buildMock(dependency));
        });

        String signature = "protected function setUp(): void\n{\n parent::setUp();\n" + body.get() + "}";

        Method setUp = PhpPsiElementFactory.createMethod(project, signature);

        phpClass.addBefore(setUp, phpClass.getLastChild());
    }

    protected String buildMock(Dependency dependency) {
        String mockName = dependency.name + "Mock";
        String typeClassString = dependency.type.toString() + "::class";

        return "$this->" + mockName + "=" + "$this->createMock(" + typeClassString + ");\n";
    }
}