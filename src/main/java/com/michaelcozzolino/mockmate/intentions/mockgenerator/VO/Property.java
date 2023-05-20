package com.michaelcozzolino.mockmate.intentions.mockgenerator.VO;

import com.jetbrains.php.lang.psi.elements.PhpModifier;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;

public class Property {
    public final PhpModifier modifier;
    public final PhpType type;

    public final String name;

    public final String defaultValue;

    public Property(PhpType type, String name) {
        this.modifier = this.getDefaultModifier();
        this.type = type;
        this.name = name;
        this.defaultValue = null;
    }

    public PhpModifier getDefaultModifier() {
        return PhpModifier.instance(
                PhpModifier.Access.PROTECTED,
                PhpModifier.Abstractness.IMPLEMENTED,
                PhpModifier.State.DYNAMIC
        );
    }

    public PhpType getType() {
        return type;
    }
}
