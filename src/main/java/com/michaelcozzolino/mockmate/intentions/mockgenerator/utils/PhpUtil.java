package com.michaelcozzolino.mockmate.intentions.mockgenerator.utils;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.PhpUnitMockMethodReferenceContributor;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;

import java.util.Objects;

public class PhpUtil {
    public static final String COVERS_TAG_NAME = "@covers";

    public static String getClassNameFromFQCN(String fqcn) {
        String[] splitFQCN   = fqcn.split("\\\\");
        int      splitLength = splitFQCN.length;

        return splitLength > 0 ? splitFQCN[splitFQCN.length - 1] : "";
    }

    public static PsiElement createSemiColon(Project project) {
        return PhpPsiElementFactory.createFromText(project, PhpTokenTypes.opSEMICOLON, ";");
    }

    public static boolean hasCoversTag(PhpClass phpClass) {
        PhpDocComment docComment = phpClass.getDocComment();
        try {
            PhpDocTag[] tags = Objects.requireNonNull(docComment).getTagElementsByName(COVERS_TAG_NAME);

            return tags.length > 0;
        } catch (NullPointerException e) {
            return false;
        }
    }

    /**
     * @return The type containing the string "ObjectType & MockObject"
     */
    public static PhpType typeToMockType(PhpType propertyType) {
        String type = String.format(
                "%s %s %s",
                PhpUtil.getClassNameFromFQCN(propertyType.toString()),
                PhpType.INTERSECTION_TYPE_DELIMITER,
                PhpUtil.getClassNameFromFQCN(PhpUnitMockMethodReferenceContributor.MOCK_OBJECT)
        );

        return PhpType.createParametrized(type);
    }

    public static PhpType typeToShortType(PhpType type) {
        return PhpType.createParametrized(PhpUtil.getClassNameFromFQCN(type.toString()));
    }
}
