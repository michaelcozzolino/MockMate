package com.michaelcozzolino.mockmate.intentions.mockgenerator;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.PhpUnitMockMethodReferenceContributor;
import com.jetbrains.php.lang.psi.PhpCodeEditUtil;
import com.jetbrains.php.lang.psi.PhpGroupUseElement;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.phpunit.PhpUnitUtil;
import com.jetbrains.php.refactoring.PhpAliasImporter;
import com.michaelcozzolino.mockmate.intentions.mockgenerator.VO.InitializableTestElement;
import com.michaelcozzolino.mockmate.intentions.mockgenerator.VO.Property;
import com.michaelcozzolino.mockmate.intentions.mockgenerator.classfile.PropertyFactory;
import com.michaelcozzolino.mockmate.intentions.mockgenerator.exception.ClassNotFoundException;
import com.michaelcozzolino.mockmate.intentions.mockgenerator.exception.ConstructorNotFoundException;
import com.michaelcozzolino.mockmate.intentions.mockgenerator.exception.ElementCreationFailedException;
import com.michaelcozzolino.mockmate.intentions.mockgenerator.exception.MockGeneratorException;
import com.michaelcozzolino.mockmate.intentions.mockgenerator.file.InstanceAssignmentFactory;
import com.michaelcozzolino.mockmate.intentions.mockgenerator.phpunit.testcase.CoveredClassFinder;
import com.michaelcozzolino.mockmate.intentions.mockgenerator.phpunit.testcase.MockAssignmentFactory;
import com.michaelcozzolino.mockmate.intentions.mockgenerator.phpunit.testcase.SetUpMethodFactory;
import com.michaelcozzolino.mockmate.intentions.mockgenerator.utils.PhpUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MockGenerator extends PsiElementBaseIntentionAction {
    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws
            IncorrectOperationException {
        try {
            PhpClass          testClass               = getTestClass(psiElement);
            PhpClass          coveredClass            = CoveredClassFinder.find(project, testClass);
            Method            coveredClassConstructor = getClassConstructor(coveredClass);
            List<Parameter>   constructorParameters   = Arrays.stream(coveredClassConstructor.getParameters()).toList();
            ArrayList<String> namesToImport           = getNamesToImport(constructorParameters, coveredClass);
            ArrayList<InitializableTestElement> testElements = getInitializableTestElements(project,
                                                                                            constructorParameters,
                                                                                            coveredClass
            );
            PhpPsiElement scopeForUseOperator = PhpCodeInsightUtil.findScopeForUseOperator(testClass);

            WriteCommandAction.runWriteCommandAction(project, () -> {
                use(scopeForUseOperator, namesToImport);

                testElements.forEach((InitializableTestElement element) -> {
                    PhpCodeEditUtil.insertClassMember(testClass, element.property);
                });

                addSetUpMethod(project, testElements, testClass);

                CodeStyleManager.getInstance(project).reformat(psiElement);
            });
        } catch (MockGeneratorException e) {
            return; //todo: log
        }
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) {
        PhpClass phpClass = PhpPsiUtil.getParentByCondition(psiElement, true, PhpClass.INSTANCEOF, null);

        return phpClass != null && PhpUnitUtil.isTestClass(phpClass) && PhpUtil.hasCoversTag(phpClass);
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "MockMate";
    }

    @NotNull
    @Override
    public String getText() {
        return "Generate phpUnit mocks";
    }

    protected PhpClass getTestClass(PsiElement psiElement) throws ClassNotFoundException {
        PhpClass testClass = PhpPsiUtil.getParentByCondition(psiElement, true, PhpClass.INSTANCEOF, null);

        if (testClass == null) {
            throw new ClassNotFoundException("The Php Unit test class has not been found");
        }

        return testClass;
    }

    protected Method getClassConstructor(PhpClass phpClass) throws ConstructorNotFoundException {
        Method constructor = phpClass.getConstructor();

        if (constructor == null) {
            throw new ConstructorNotFoundException(String.format("Constructor for class %s not found.",
                                                                 phpClass.getType()
            ));
        }

        return constructor;
    }

    protected ArrayList<InitializableTestElement> getInitializableTestElements(
            Project project, List<Parameter> constructorParameters, PhpClass coveredClass
    ) throws ElementCreationFailedException {
        ArrayList<InitializableTestElement> mocks = getMocks(project, constructorParameters);

        String className = coveredClass.getName();
        Property coveredClassProperty = new Property(PhpType.createParametrized(PhpUtil.getClassNameFromFQCN(coveredClass.getType().toString())),
                                                     uncapitalize(className)
        );

        PhpPsiElement property = PropertyFactory.create(project, coveredClassProperty);

        PsiElement initializer = InstanceAssignmentFactory.create(project,
                                                                  coveredClassProperty.name,
                                                                  coveredClassProperty.type,
                                                                  constructorParameters.stream().map(Parameter::getName).toList()
        );

        InitializableTestElement coveredClassTestElement = new InitializableTestElement(property, initializer);

        ArrayList<InitializableTestElement> properties = new ArrayList<>(mocks);
        properties.add(coveredClassTestElement);

        return properties;
    }

    protected ArrayList<InitializableTestElement> getMocks(Project project, @NotNull List<Parameter> mockableElements) {
        ArrayList<InitializableTestElement> mocks = new ArrayList<>();

        mockableElements.forEach((Parameter mockableElement) -> {
            PhpType  mockableType = mockableElement.getType();
            Property property     = new Property(PhpUtil.typeToMockType(mockableType), mockableElement.getName());

            InitializableTestElement mock = new InitializableTestElement(PropertyFactory.create(project, property),
                                                                         MockAssignmentFactory.create(project,
                                                                                                      property.name,
                                                                                                      PhpUtil.typeToShortType(
                                                                                                              mockableType).toString()
                                                                         )
            );

            mocks.add(mock);

        });

        return mocks;
    }

    protected String uncapitalize(String str) {
        return str.substring(0, 1) + str.substring(1);
    }

    protected ArrayList<String> getNamesToImport(List<Parameter> parameters, PhpClass coveredClass) {
        ArrayList<String> namesToImport = new ArrayList<>();
        parameters.forEach((Parameter p) -> namesToImport.add(p.getType().toString()));
        namesToImport.add(coveredClass.getType().toString());
        namesToImport.add(PhpUnitMockMethodReferenceContributor.MOCK_OBJECT);

        return namesToImport;
    }


    protected void use(PhpPsiElement scopeForUseOperator, ArrayList<String> namesToImport) {
        if (scopeForUseOperator == null) {
            return;
        }

        namesToImport.forEach((String nameToImport) -> {
            boolean isImported = PhpCodeInsightUtil.findImportedName(scopeForUseOperator,
                                                                     nameToImport,
                                                                     PhpGroupUseElement.PhpUseKeyword.CLASS
            ) != null;

            if (isImported == false) {
                PhpAliasImporter.insertUseStatement(nameToImport, scopeForUseOperator);
            }
        });
    }

    protected void addSetUpMethod(Project project, ArrayList<InitializableTestElement> properties, PhpClass testClass) {
        Method setUp = SetUpMethodFactory.create(project, properties);
        PhpCodeEditUtil.insertClassMember(testClass, setUp);
    }
}