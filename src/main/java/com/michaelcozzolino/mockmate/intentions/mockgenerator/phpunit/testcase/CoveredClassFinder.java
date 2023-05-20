package com.michaelcozzolino.mockmate.intentions.mockgenerator.phpunit.testcase;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.michaelcozzolino.mockmate.intentions.mockgenerator.exception.ClassNotFoundException;
import com.michaelcozzolino.mockmate.intentions.mockgenerator.utils.PhpUtil;

import java.util.Objects;
import java.util.Optional;

public class CoveredClassFinder {
    public static PhpClass find(Project project, PhpClass testClass) throws ClassNotFoundException {
        PhpDocComment docComment = testClass.getDocComment();

        PhpDocTag[] coversTags = Objects.requireNonNull(docComment).getTagElementsByName(PhpUtil.COVERS_TAG_NAME);

        String coveredClassFQCN = coversTags.length > 0 ? getCoversTagValue(coversTags[0]) : null;

        return getCoveredClass(project, coveredClassFQCN);
    }

    protected static PhpClass getCoveredClass(Project project, String FQCN) throws ClassNotFoundException {
        Optional<PhpClass> coveredClass = PhpIndex.getInstance(project)
                .getClassesByFQN(FQCN)
                .stream()
                .findFirst();

        if (coveredClass.isEmpty()) {
            throw new ClassNotFoundException(String.format("The covered class `%s` has not been found", FQCN));
        }

        return coveredClass.get();
    }

    protected static String getCoversTagValue(PhpDocTag tag) {
        return tag.getText().replace(PhpUtil.COVERS_TAG_NAME, "").trim();
    }
}
