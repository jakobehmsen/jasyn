package jasy.lang.ast;

import jasy.lang.ASMCompiler;
import jasy.lang.ClassResolver;
import jasy.runtime.asm.IfAllTransformer;
import java.util.List;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

public class MethodSelectorAST {
    public Integer accessModifier;
    public Boolean isStatic;
    public TypeAST returnType;
    public String name;
    public List<Parameter> parameters;

    public MethodSelectorAST(Integer accessModifier, Boolean isStatic, TypeAST returnType, String name, List<Parameter> parameters) {
        this.accessModifier = accessModifier;
        this.isStatic = isStatic;
        this.returnType = returnType;
        this.name = name;
        this.parameters = parameters;
    }

    public void resolve(Scope thisClass, TypeAST expectedResultType, ClassResolver resolver, ClassLoader classLoader, List<ASMCompiler.Message> errorMessages) {
        returnType.resolve(thisClass, expectedResultType, resolver, classLoader, errorMessages);
        parameters.forEach(pt -> pt.type.resolve(thisClass, expectedResultType, resolver, classLoader, errorMessages));
    }

    public void populate(IfAllTransformer<Transformation<MethodNode>> transformer) {
        if(accessModifier != null)
            transformer.addPredicate(m -> (m.getTarget().access & accessModifier) != 0);
        if(isStatic != null)
            transformer.addPredicate(m -> (m.getTarget().access & Opcodes.ACC_STATIC) != 0);
        if(returnType != null)
            transformer.addPredicate(m -> Type.getType(m.getTarget().desc).getClassName().equals(returnType.getDescriptor()));
        if(name != null)
            transformer.addPredicate(m -> m.getTarget().name.equals(name));
        
        if(parameters != null) {
            transformer.addPredicate(m -> {
                Type[] argumentTypes = Type.getArgumentTypes(m.getTarget().desc);
                
                if(argumentTypes.length != parameters.size())
                    return false;
                
                for(int i = 0; i < parameters.size(); i++) {
                    if(!argumentTypes[i].getClassName().equals(parameters.get(0).type.getDescriptor()))
                        return false;
                }
                
                return true;
            });
        }
    }
}
