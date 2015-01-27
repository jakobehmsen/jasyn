package dejain.lang;

import dejain.runtime.asm.ClassAction;
import dejain.runtime.asm.ClassTransformer;
import java.util.function.Function;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

public class ExhaustiveClassTransformer {
    private ClassTransformer transformer;

    public ExhaustiveClassTransformer(Function<ClassNode, Runnable> transformer) {
        this((ClassTransformer)(c -> {
            Runnable t = transformer.apply(c);
            return t != null ? c1 -> t.run() : null;
        }));
    }

    public ExhaustiveClassTransformer(ClassTransformer transformer) {
        this.transformer = transformer;
    }

    public byte[] transform(byte[] classfileBuffer) {
        while(true) {
            ClassReader classReader = new ClassReader(classfileBuffer);
            ClassNode classNode = new ClassNode();
            classReader.accept(classNode, 0);
            ClassAction action = transformer.resolve(classNode);

            if(action == null)
                break;

            action.perform(classNode);
            
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS|ClassWriter.COMPUTE_FRAMES);
            classNode.accept(classWriter);

            classfileBuffer = classWriter.toByteArray();
        }
        
        return classfileBuffer;
    }
}