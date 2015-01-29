package dejain.lang.ast;

import dejain.lang.ASMCompiler;
import dejain.lang.ClassResolver;
import java.util.List;

public class ThisTypeContext extends AbstractContext implements TypeContext {
    private ClassContext classInfo;

    public ThisTypeContext(ASMCompiler.Region region) {
        super(region);
    }

    @Override
    public void resolve(ClassContext thisClass, ClassResolver resolver, List<ASMCompiler.Message> errorMessages) {
        classInfo = thisClass;
    }

    @Override
    public String getDescriptor(String thisClassName) {
        return thisClassName != null ? "L" + thisClassName + ";" : "this";
    }

    @Override
    public String getName(String thisClassName) {
        return thisClassName.replace("/", ".");
    }

    @Override
    public String getSimpleName(String thisClassName) {
        String name = getName(thisClassName);
        return name.substring(name.lastIndexOf("."));
    }

    @Override
    public boolean isCompatibleWith(TypeContext other) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public TypeContext getFieldType(String fieldName) {
        return classInfo.getFieldType(fieldName);
    }
}