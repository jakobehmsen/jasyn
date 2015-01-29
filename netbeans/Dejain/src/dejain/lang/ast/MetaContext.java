package dejain.lang.ast;

import dejain.lang.ASMCompiler;
import dejain.lang.ClassResolver;
import java.util.List;

public class MetaContext extends AbstractContext implements ExpressionContext {
    public List<CodeContext> body;

    public MetaContext(ASMCompiler.Region region, List<CodeContext> body) {
        super(region);
        
        this.body = body;
    }

    @Override
    public void resolve(ClassContext thisClass, ClassResolver resolver, List<ASMCompiler.Message> errorMessages) {
        body.forEach(s -> s.resolve(thisClass, resolver, errorMessages));
    }

    @Override
    public TypeContext resultType() {
        return new NameTypeContext(getRegion(), String.class);
    }

    @Override
    public void accept(CodeVisitor visitor) {
        visitor.visitMeta(this);
    }
}