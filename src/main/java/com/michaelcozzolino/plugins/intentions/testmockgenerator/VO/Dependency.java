package com.michaelcozzolino.plugins.intentions.testmockgenerator.VO;

import com.jetbrains.php.lang.psi.resolve.types.PhpType;

public class Dependency {
    public final PhpType type;

    public final String name;

    public Dependency(PhpType type, String name) {
        this.type = type;
        this.name = name;
    }

    public PhpType getType() {
        return type;
    }
}
