package dejain.lang.ast;

import dejain.lang.ASMCompiler;
import dejain.lang.ASMCompiler.Region;

public abstract class AbstractContext implements Context {
    private Region region;

    protected AbstractContext(Region region) {
        this.region = region;
    }
    
    @Override
    public ASMCompiler.Region getRegion() {
        return region;
    }
}
