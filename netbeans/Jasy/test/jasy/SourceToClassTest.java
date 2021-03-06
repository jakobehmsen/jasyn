/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jasy;

import jasy.lang.ProxyClassLoader;
import jasy.lang.ClassBytesTransformer;
import jasy.lang.ClassBytesSourcePredicate;
import jasy.lang.ClassBytesSource;
import jasy.lang.ClassBytesFromFile;
import jasy.lang.ModuleClassBytesTransformer;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;
import static jasy.Assertion.*;
import static jasy.TemplateSource.*;
import jasy.lang.ASMCompiler;
import jasy.lang.ASMCompiler.Message;
import jasy.lang.ClassResolver;
import jasy.lang.CommonClassMap;
import jasy.lang.CommonClassResolver;
import jasy.lang.ExhaustiveClassTransformer;
import jasy.lang.ast.ModuleAST;
import jasy.lang.ast.Reduction;
import jasy.lang.ast.Transformation;
import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

/**
 *
 * @author Jakob
 */
public class SourceToClassTest {
    @Test
    public void testAllClassesAdd1PublicPrimitiveField() throws IOException {
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            "class {+public float someField2;}", 
            forClass("jasy.TestClass1", chasFieldWhere(
                fname(is("someField2"))
                .and(ftype(is(float.class)))
                .and(fmodifiers(isPublic()))
                .and(fmodifiers(isStatic().negate()))
            ))
        );
    }
    
    @Test
    public void testAllClassesAdd1ProtectedPrimitiveField() throws IOException {
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            "class {+protected float someField2;}", 
            forClass("jasy.TestClass1", chasFieldWhere(
                fname(is("someField2"))
                .and(ftype(is(float.class)))
                .and(fmodifiers(isProtected()))
                .and(fmodifiers(isStatic().negate()))
            ))
        );
    }
    
    @Test
    public void testAllClassesAdd1PrivatePrimitiveField() throws IOException {
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            "class {+private float someField2;}", 
            forClass("jasy.TestClass1", chasFieldWhere(
                fname(is("someField2"))
                .and(ftype(is(float.class)))
                .and(fmodifiers(isPrivate()))
                .and(fmodifiers(isStatic().negate()))
            ))
        );
    }
    
    @Test
    public void testAllClassesAdd1PublicStaticPrimitiveField() throws IOException {
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            "class {+public static float someField2;}", 
            forClass("jasy.TestClass1", chasFieldWhere(
                fname(is("someField2"))
                .and(ftype(is(float.class)))
                .and(fmodifiers(isPublic()))
                .and(fmodifiers(isStatic()))
            ))
        );
    }
    
    @Test
    public void testAllClassesAdd1PublicObjectField() throws IOException {
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            "class {+public String someField2;}", 
            forClass("jasy.TestClass1", chasFieldWhere(
                fname(is("someField2"))
                .and(ftype(is(String.class)))
                .and(fmodifiers(isPublic()))
                .and(fmodifiers(isStatic().negate()))
            ))
        );
    }
    
    @Test
    public void testAllClassesAdd1PublicMethodReturningStringLiteral() throws IOException {
        String expectedResult = "Hi";
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            "class {+public String toString() {return \"" + expectedResult + "\";}}", 
            forClass("jasy.TestClass1", 
                chasMethodWhere(
                    mname(is("toString"))
                    .and(rreturnType(is(String.class)))
                    .and(rmodifiers(isPublic()))
                    .and(rmodifiers(isStatic().negate()))
                ).and(
                    forInstance(imethod("toString", invocationResult(is(expectedResult))))
                )
            )
        );
    }
    
    @Test
    public void testAllClassesAdd1PublicMethodReturningGeneratedStringLiteral() throws IOException {
        String expectedResult = "Hi";
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            "class {+public String toString() {return $\"" + expectedResult + "\";}}", 
            forClass("jasy.TestClass1", 
                chasMethodWhere(
                    mname(is("toString"))
                    .and(rreturnType(is(String.class)))
                    .and(rmodifiers(isPublic()))
                    .and(rmodifiers(isStatic().negate()))
                ).and(
                    forInstance(imethod("toString", invocationResult(is(expectedResult))))
                )
            )
        );
    }
    
    @Test
    public void testAllClassesAdd1PublicMethodReturningXLessThanY() throws IOException {
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            "class {+public boolean compare(int x, int y) {return x < y;}}", 
            forClass("jasy.TestClass1", 
                forInstance(imethod("compare", new Class<?>[]{int.class, int.class}, 
                    invocationResult(new Object[]{8, 9}, is(true))
                    .and(
                        invocationResult(new Object[]{9, 9}, is(false))
                    ).and(
                        invocationResult(new Object[]{9, 8}, is(false))
                    )
                ))
            )
        );
    }
    
    @Test
    public void testAllClassesAdd1PublicMethodReturningXLessThanOrEqualsY() throws IOException {
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            "class {+public boolean compare(int x, int y) {return x <= y;}}", 
            forClass("jasy.TestClass1", 
                forInstance(imethod("compare", new Class<?>[]{int.class, int.class}, 
                    invocationResult(new Object[]{8, 9}, is(true))
                    .and(
                        invocationResult(new Object[]{9, 9}, is(true))
                    ).and(
                        invocationResult(new Object[]{9, 8}, is(false))
                    )
                ))
            )
        );
    }
    
    @Test
    public void testAllClassesAdd1PublicMethodReturningXGreaterThanY() throws IOException {
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            "class {+public boolean compare(int x, int y) {return x > y;}}", 
            forClass("jasy.TestClass1", 
                forInstance(imethod("compare", new Class<?>[]{int.class, int.class}, 
                    invocationResult(new Object[]{8, 9}, is(false))
                    .and(
                        invocationResult(new Object[]{9, 9}, is(false))
                    ).and(
                        invocationResult(new Object[]{9, 8}, is(true))
                    )
                ))
            )
        );
    }
    
    @Test
    public void testAllClassesAdd1PublicMethodReturningXGreaterThanOrEqualsY() throws IOException {
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            "class {+public boolean compare(int x, int y) {return x >= y;}}", 
            forClass("jasy.TestClass1", 
                forInstance(imethod("compare", new Class<?>[]{int.class, int.class}, 
                    invocationResult(new Object[]{8, 9}, is(false))
                    .and(
                        invocationResult(new Object[]{9, 9}, is(true))
                    ).and(
                        invocationResult(new Object[]{9, 8}, is(true))
                    )
                ))
            )
        );
    }
    
    @Test
    public void testAllClassesAdd1PublicMethodReturningXEqualsY() throws IOException {
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            "class {+public boolean compare(int x, int y) {return x == y;}}", 
            forClass("jasy.TestClass1", 
                forInstance(imethod("compare", new Class<?>[]{int.class, int.class}, 
                    invocationResult(new Object[]{8, 9}, is(false))
                    .and(
                        invocationResult(new Object[]{9, 9}, is(true))
                    ).and(
                        invocationResult(new Object[]{9, 8}, is(false))
                    )
                ))
            )
        );
    }
    
    @Test
    public void testAllClassesAdd1PublicMethodReturningXNotEqualsY() throws IOException {
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            "class {+public boolean compare(int x, int y) {return x != y;}}", 
            forClass("jasy.TestClass1", 
                forInstance(imethod("compare", new Class<?>[]{int.class, int.class}, 
                    invocationResult(new Object[]{8, 9}, is(true))
                    .and(
                        invocationResult(new Object[]{9, 9}, is(false))
                    ).and(
                        invocationResult(new Object[]{9, 8}, is(true))
                    )
                ))
            )
        );
    }
    
    @Test
    public void testAllClassesAdd1PublicMethodReturningGeneratedStringConcatenation() throws IOException {
        String str1 = "H";
        String str2 = "i";
        String expectedResult = str1 + str2;
        
        String strConcSrc = "\"" + str1 + "\" + \"" + str2 + "\"";
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            "class {+public String toString() {return $" + strConcSrc + ";}}", 
            forClass("jasy.TestClass1", 
                chasMethodWhere(
                    mname(is("toString"))
                    .and(rreturnType(is(String.class)))
                    .and(rmodifiers(isPublic()))
                    .and(rmodifiers(isStatic().negate()))
                ).and(
                    forInstance(imethod("toString", invocationResult(is(expectedResult))))
                )
            )
        );
    }
    
    @Test
    public void testAllClassesAdd1PublicMethodReturningIntPlusInterpolatedInt() throws IOException {
        int i1 = 5;
        int i2 = 7;
        int expectedResult = i1 + i2;
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            "class {+public int toInt() {return " + i1 + " + $" + i2 + ";}}", 
            forClass("jasy.TestClass1", 
                chasMethodWhere(
                    mname(is("toInt"))
                    .and(rreturnType(is(int.class)))
                    .and(rmodifiers(isPublic()))
                    .and(rmodifiers(isStatic().negate()))
                ).and(
                    forInstance(imethod("toInt", invocationResult(is(expectedResult))))
                )
            )
        );
    }
    
    @Test
    public void testAllClassesAdd1PublicMethodReturningStringConcatenation() throws IOException {
        String str1 = "H";
        String str2 = "i";
        String expectedResult = str1 + str2;
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            "class {+public String toString() {return \"" + str1 + "\" + \"" + str2 + "\";}}", 
            forClass("jasy.TestClass1", 
                chasMethodWhere(
                    mname(is("toString"))
                    .and(rreturnType(is(String.class)))
                    .and(rmodifiers(isPublic()))
                    .and(rmodifiers(isStatic().negate()))
                ).and(
                    forInstance(imethod("toString", invocationResult(is(expectedResult))))
                )
            )
        );
    }
    
    @Test
    public void testAllClassesAdd1PublicMethodReturningIntPlusString() throws IOException {
        int i1 = 5;
        String str2 = "i";
        String expectedResult = i1 + str2;
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            "class {+public String toString() {return " + i1 + " + \"" + str2 + "\";}}", 
            forClass("jasy.TestClass1", 
                chasMethodWhere(
                    mname(is("toString"))
                    .and(rreturnType(is(String.class)))
                    .and(rmodifiers(isPublic()))
                    .and(rmodifiers(isStatic().negate()))
                ).and(
                    forInstance(imethod("toString", invocationResult(is(expectedResult))))
                )
            )
        );
    }
    
    @Test
    public void testAllClassesAdd1PublicMethodReturningClassName() throws IOException {
        String expectedResult = jasy.TestClass1.class.getName().replace(".", "/");
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            "c=class {+public String getClassName() {return $c.name;}}", 
            forClass("jasy.TestClass1", 
                chasMethodWhere(
                    mname(is("getClassName"))
                    .and(rreturnType(is(String.class)))
                    .and(rmodifiers(isPublic()))
                    .and(rmodifiers(isStatic().negate()))
                ).and(
                    forInstance(imethod("getClassName", invocationResult(is(expectedResult))))
                )
            )
        );
    }
    
    @Test
    public void testAllClassesAdd1PublicMethodReturningIntPlusIntPlusString() throws IOException {
        int i1 = 1;
        int i2 = 5;
        String str3 = "i";
        String expectedResult = i1 + i2 + str3;
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            "class {+public String toString() {return " + i1 + " + " + i2 + " + \"" + str3 + "\";}}", 
            forClass("jasy.TestClass1", 
                chasMethodWhere(
                    mname(is("toString"))
                    .and(rreturnType(is(String.class)))
                    .and(rmodifiers(isPublic()))
                    .and(rmodifiers(isStatic().negate()))
                ).and(
                    forInstance(imethod("toString", invocationResult(is(expectedResult))))
                )
            )
        );
    }
    
    @Test
    public void testAllClassesAdd1PublicMethodReturningIntPlusIntPlusStringPlusIntPlusInt() throws IOException {
        int i1 = 1;
        int i2 = 4;
        String str3 = "i";
        int i4 = 5;
        int i5 = 7;
        String expectedResult = i1 + i2 + str3 + i4 + i5;
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            "class {+public String toString() {return " + i1 + " + " + i2 + " + \"" + str3 + "\" + " + i4 + " + " + i5 + ";}}", 
            forClass("jasy.TestClass1", 
                chasMethodWhere(
                    mname(is("toString"))
                    .and(rreturnType(is(String.class)))
                    .and(rmodifiers(isPublic()))
                    .and(rmodifiers(isStatic().negate()))
                ).and(
                    forInstance(imethod("toString", invocationResult(is(expectedResult))))
                )
            )
        );
    }
    
    @Test
    public void testAllClassesAdd1PublicMethodReturningIntPlusInt() throws IOException {
        int i1 = 1;
        int i2 = 4;
        int expectedResult = i1 + i2;
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            "class {+public int toInt() {return " + i1 + " + " + i2 + ";}}", 
            forClass("jasy.TestClass1", 
                chasMethodWhere(
                    mname(is("toInt"))
                    .and(rreturnType(is(int.class)))
                    .and(rmodifiers(isPublic()))
                    .and(rmodifiers(isStatic().negate()))
                ).and(
                    forInstance(imethod("toInt", invocationResult(is(expectedResult))))
                )
            )
        );
    }
    
    @Test
    public void testAllClassesAdd1StringFieldWithValue() throws IOException {
        String str = "myValue";
        int i = 7;
        String expectedResult = str + i;
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            "class {+private String myField = \"" + str + "\" + " + i + ";}", 
            forClass("jasy.TestClass1", 
                chasFieldWhere(
                    fname(is("myField"))
                    .and(ftype(is(String.class)))
                    .and(fmodifiers(isPrivate()))
                    .and(fmodifiers(isStatic().negate()))
                ).and(
                    forInstance(ifield("myField", ifget(is(expectedResult))))
                )
            )
        );
    }
    
    @Test
    public void testAllClassesAdd1IntFieldWithValue() throws IOException {
        int expectedResult = 7;
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            "class {+private int myField = " + expectedResult + ";}", 
            forClass("jasy.TestClass1", 
                chasFieldWhere(
                    fname(is("myField"))
                    .and(ftype(is(int.class)))
                    .and(fmodifiers(isPrivate()))
                    .and(fmodifiers(isStatic().negate()))
                ).and(
                    forInstance(ifield("myField", ifget(is(expectedResult))))
                )
            )
        );
    }
    
    @Test
    public void testAllClassesAdd1ShortFieldWithValue() throws IOException {
        short expectedResult = 7;
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            "class {+private short myField = " + expectedResult + ";}", 
            forClass("jasy.TestClass1", 
                chasFieldWhere(
                    fname(is("myField"))
                    .and(ftype(is(short.class)))
                    .and(fmodifiers(isPrivate()))
                    .and(fmodifiers(isStatic().negate()))
                ).and(
                    forInstance(ifield("myField", ifget(is(expectedResult))))
                )
            )
        );
    }
    
    @Test
    public void testAllClassesAdd1LongFieldWithValue() throws IOException {
        long expectedResult = 3000000000L;
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            "class {+private long myField = " + expectedResult + "L;}", 
            forClass("jasy.TestClass1", 
                chasFieldWhere(
                    fname(is("myField"))
                    .and(ftype(is(long.class)))
                    .and(fmodifiers(isPrivate()))
                    .and(fmodifiers(isStatic().negate()))
                ).and(
                    forInstance(ifield("myField", ifget(is(expectedResult))))
                )
            )
        );
    }
    
    @Test
    public void testAllClassesAddFieldReturnFieldIntMethod() throws IOException {
        String myFieldName = "myField";
        String myFieldValue = "Hi";
        String expectedResult = myFieldName + "=" + myFieldValue;
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            "class {+private String " + myFieldName + " = \"" + myFieldValue + "\"; +public String toString() {return \"" + myFieldName+ "=\" + " + myFieldName + ";} }", 
            forClass("jasy.TestClass1", 
                chasFieldWhere(
                    fname(is("myField"))
                    .and(ftype(is(String.class)))
                    .and(fmodifiers(isPrivate()))
                    .and(fmodifiers(isStatic().negate()))
                ).and(
                    forInstance(imethod("toString", invocationResult(is(expectedResult))))
                )
            )
        );
    }
    
    @Test
    public void testAllClassesAddMethodReturnNameOfSingleField() throws IOException {
        Field singleField = TestClass1.class.getDeclaredFields()[0];
        String expectedResult = singleField.getName();
        
        String src =
            "class {\n" +
            "    fields=;\n" +
            "    \n" +
            "    +public String getDescription() {\n" +
            "        return $fields.get(0).name;\n" +
            "    }\n" +
            "}\n";
        
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            src, 
            forClass("jasy.TestClass1", 
                forInstance(imethod("getDescription", invocationResult(is(expectedResult))))
            )
        );
    }
    
    @Test
    public void testAllClassesAddMethodWithNew() throws IOException {
        String src =
            "class {\n" +
            "    +public StringBuilder createStringBuilder() {\n" +
            "        return new StringBuilder();\n" +
            "    }\n" +
            "}\n";
        
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            src, 
            forClass("jasy.TestClass1", 
                forInstance(imethod("createStringBuilder", invocationResult(instanceOf(StringBuilder.class))))
            )
        );
    }
    
    @Test
    public void testAllClassesAddMethodWithVariableAssignAddInt() throws IOException {
        String src =
            "class {\n" +
            "    +public int getInt() {\n" +
            "        int i = 0;\n" +
            "        i += 1;\n" +
            "        return 1;\n" +
            "    }\n" +
            "}\n";
        
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            src, 
            forClass("jasy.TestClass1", 
                forInstance(imethod("getInt", invocationResult(is(1))))
            )
        );
    }
    
    @Test
    public void testAllClassesAddMethodWithVariableAssignAddString() throws IOException {
        String src =
            "class {\n" +
            "    +public String getString() {\n" +
            "        String str = \"\";\n" +
            "        str += \"Hi\";\n" +
            "        return str;\n" +
            "    }\n" +
            "}\n";
        
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            src, 
            forClass("jasy.TestClass1", 
                forInstance(imethod("getString", invocationResult(is("Hi"))))
            )
        );
    }
    
    private static Number toNumber(long value, String type) {
        switch(type) {
            case "byte":
                return (byte)value;
            case "short":
                return (short)value;
            case "int":
                return (int)value;
            case "long":
                return value;
            case "float":
                return (float)value;
            case "double":
                return (double)value;
        }
        
        return null;
    }
    
    @Test
    public void testAllClassesAddMethodWithPrefixInc2() {
        long start = 0;
        
        long i = start;
        long j = ++i;
        long expectedResultBase = i + j;
        
        testAllClassesAddMethodWithIncDec(start, expectedResultBase, "++i");
    }
    
    @Test
    public void testAllClassesAddMethodWithPostfixInc2() {
        long start = 0;
        
        long i = start;
        long j = i++;
        long expectedResultBase = i + j;
        
        testAllClassesAddMethodWithIncDec(start, expectedResultBase, "i++");
    }
    
    @Test
    public void testAllClassesAddMethodWithPrefixDec2() {
        long start = 0;
        
        long i = start;
        long j = --i;
        long expectedResultBase = i + j;
        
        testAllClassesAddMethodWithIncDec(start, expectedResultBase, "--i");
    }
    
    @Test
    public void testAllClassesAddMethodWithPostfixDec2() {
        long start = 0;
        
        long i = start;
        long j = i--;
        long expectedResultBase = i + j;
        
        testAllClassesAddMethodWithIncDec(start, expectedResultBase, "i--");
    }
    
    private static final String incDecTemplateSrcMethodName = "incDec";
    private static final String incDecTemplateSrc =
        "class {\n" +
        "    +public <<type>> " + incDecTemplateSrcMethodName + "() {\n" +
        "        <<type>> i = <<start>>;\n" +
        "        <<type>> j = <<incDec>>;\n" +
        "        return i + j;\n" +
        "    }\n" +
        "}\n";
    
    public void testAllClassesAddMethodWithIncDec(long start, long expectedResultBase, String incDec) {
        SourceCode sourceCodeBase = 
            TemplateSource.expand(incDecTemplateSrc, map(entry("incDec", incDec))).get(0);
        
        primitiveNumberTypes.forEach(type -> {
            String resultType = type;
            String value = toSourceCode(start, type);
            
            SourceCode sourceCode = TemplateSource.expand(sourceCodeBase.src, 
                map(entry("type", type), entry("start", value))
            ).get(0);
            
            Number expectedValue = toNumber(expectedResultBase, resultType);
            try {
                testSourceToClasses(new String[]{"jasy.TestClass1"},
                    sourceCode.src,
                    forClass("jasy.TestClass1",
                        forInstance(imethod(incDecTemplateSrcMethodName, invocationResult(is(expectedValue))))
                    )
                );
            } catch (IOException ex) {
                Logger.getLogger(SourceToClassTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
    
    @Test
    public void testAllClassesAddMethodWithNewWithArguments() throws IOException {
        String expectedResult = "Some text";
        
        String src =
            "class {\n" +
            "    +public String createStringBuilder() {\n" +
            "        return new StringBuilder(\"" + expectedResult + "\").toString();\n" +
            "    }\n" +
            "}\n";
        
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            src, 
            forClass("jasy.TestClass1", 
                forInstance(imethod("createStringBuilder", invocationResult(is(expectedResult))))
            )
        );
    }
    
    @Test
    public void testAllClassesAddMethodReturnNamesOfAllFields() throws IOException, InstantiationException, IllegalAccessException {
        Object instance = TestClass2.class.newInstance();
        Field[] fields = TestClass2.class.getDeclaredFields();
        String expectedResult = "";
        for(int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            f.setAccessible(true);
            if(i > 0)
                expectedResult += ", ";
            expectedResult += f.getName() + " = " + f.get(instance);
        }
        
        String src =
            "class {\n" +
            "    fields=;\n" +
            "    \n" +
            "    +public String getDescription() ${\n" +
            "        CodeAST statements = #{};\n" +
            "        for(int i = 0; i < fields.size(); i++) {\n" +
            "            FieldNode f = fields.get(i);\n" +
            "            if(i > 0)\n" +
            "                statements += #sb.append(\", \");\n" +
            "            statements += #sb.append(($f.name) + \" = \" + (:$f.name));\n" +
            "        }\n" +
            "        return #{\n" +
            "            StringBuilder sb = new StringBuilder();\n" +
            "            $statements;\n" +
            "            return sb.toString();\n" +
            "        };\n" +
            "    }\n" +
            "}\n";
        
        testSourceToClasses(
            new String[]{"jasy.TestClass2"}, 
            src, 
            forClass("jasy.TestClass2", 
                forInstance(imethod("getDescription", invocationResult(is(expectedResult))))
            )
        );
    }
    
    @Test
    public void testAllClassesAddMethodReturnNameAndValueOfFirstField() throws IOException {
        Field singleField = TestClass1.class.getDeclaredFields()[0];
        int expectedValue = 0;
        String expectedResult = singleField.getName() + " = " + expectedValue;
        
        String src =
            "class {\n" +
            "    fields=;\n" +
            "    +public Object getDescription() {\n" +
            "        return ($fields.get(0).name) + \" = \" + (:$fields.get(0).name);\n" +
            "    }\n" +
            "}\n";                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  
        
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            src, 
            forClass("jasy.TestClass1", 
                forInstance(imethod("getDescription", invocationResult(is(expectedResult))))
            )
        );
    }
    
//    @Test
//    public void testAllClassesAddMethodReturnValueOfMetaVariable() throws IOException {
//        int expectedResult = 5;
//        
//        String src =
//            "class {\n" +
//            "    +public int getValue() {\n" +
//            "        ${int i = " + expectedResult + ";}\n" + 
//            "        return $i;\n" +
//            "    }\n" +
//            "}\n";
//        
//        testSourceToClasses(
//            new String[]{"jasy.TestClass1"}, 
//            src, 
//            forClass("jasy.TestClass1", 
//                forInstance(imethod("getValue", invocationResult(is(expectedResult))))
//            )
//        );
//    }
    
    @Test
    public void testAllClassesAddMethodReturnValueOfVariable() throws IOException {
        int expectedResult = 5;
        
        String src =
            "class {\n" +
            "    +public int getValue() {\n" +
            "        int i = " + expectedResult + ";\n" +
            "        return i;\n" +
            "    }\n" +
            "}\n";
        
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            src, 
            forClass("jasy.TestClass1", 
                forInstance(imethod("getValue", invocationResult(is(expectedResult))))
            )
        );
    }
    
    @Test
    public void testAllClassesAddMethodReturnValueOfVariableWithSeparateDeclaration() throws IOException {
        int expectedResult = 5;
        
        String src =
            "class {\n" +
            "    +public int getValue() {\n" +
            "        int i;\n" +
            "        i = " + expectedResult + ";\n" +
            "        return i;\n" +
            "    }\n" +
            "}\n";
        
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            src, 
            forClass("jasy.TestClass1", 
                forInstance(imethod("getValue", invocationResult(is(expectedResult))))
            )
        );
    }
    
    @Test
    public void testAllClassesAddMethodReturnValueOfSharedMetaVariable() throws IOException {
        int expectedResult = 5;
        
        String src =
            "class {\n" +
            "    +public int getValue() ${\n" +
            "        int i = " + expectedResult + ";\n" +
            "        return #return $i;\n" +
            "    }\n" +
            "}\n";
        
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            src, 
            forClass("jasy.TestClass1", 
                forInstance(imethod("getValue", invocationResult(is(expectedResult))))
            )
        );
    }
    
    @Test
    public void testAllClassesAddMethodGenerateFromRootQuote() throws IOException {
        int expectedResult = 5;
        
        String src =
            "class {\n" +
            "    +public int getValue() {\n" +
            "        int i = " + expectedResult + ";\n" +
            "        $#return i;\n" +
            "    }\n" +
            "}\n";
        
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            src, 
            forClass("jasy.TestClass1", 
                forInstance(imethod("getValue", invocationResult(is(expectedResult))))
            )
        );
    }
    
    @Test
    public void testAllClassesAddMethodWhichGenerateQuotedBlock() throws IOException {
        int expectedResult = 5;
        
        String src =
            "class {\n" +
            "    +public int getValue() ${\n" +
            "        return #{\n" +
            "            int i = " + expectedResult + ";\n" +
            "            return i;\n" +
            "        };\n" +
            "    }\n" +
            "}\n";
        
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            src, 
            forClass("jasy.TestClass1", 
                forInstance(imethod("getValue", invocationResult(is(expectedResult))))
            )
        );
    }
    
    @Test
    public void testAllClassesAddMethodWhichGenerateQuotedBlockWithInjection() throws IOException {
        int expectedResult = 5;
        
        String src =
            "class {\n" +
            "    +public int getValue() ${\n" +
            "        return #{\n" +
            "            int i;\n" +
            "            $#i = " + expectedResult + ";\n" +
            "            return i;\n" +
            "        };\n" +
            "    }\n" +
            "}\n";
        
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            src, 
            forClass("jasy.TestClass1", 
                forInstance(imethod("getValue", invocationResult(is(expectedResult))))
            )
        );
    }
    
    @Test
    public void testAllClassesAddMethodWhichGeneratesAndInterpolateQuotedBlock() throws IOException {
        int i1 = 5;
        int i2 = 7;
        int expectedResult = i1 + i2;
        
//        String src =
//            "class {\n" +
//            "    +public int getValue() ${\n" +
//            "        ArrayList<CodeAST> statements = new ArrayList<CodeAST>();\n" +
//            "        statements.add(#i1 = " + i1 + ");\n" +
//            "        statements.add(#i2 = " + i2 + ");\n" +
//            "        return #{\n" +
//            "            int i1;\n" +
//            "            int i2;\n" +
//            "            $statements\n" +
//            "            return i1 + i2;\n" +
//            "        };\n" +
//            "    }\n" +
//            "}\n";
        
        String src =
            "class {\n" +
            "    +public int getValue() ${\n" +
            "        jasy.lang.ast.CodeAST statements = \n" +
            "            (#i1 = " + i1 + ") +\n" +
            "            (#i2 = " + i2 + ");\n" +
            "        return #{\n" +
            "            int i1;\n" +
            "            int i2;\n" +
            "            $statements;\n" +
            "            return i1 + i2;\n" +
            "        };\n" +
            "    }\n" +
            "}\n";
        
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            src, 
            forClass("jasy.TestClass1", 
                forInstance(imethod("getValue", invocationResult(is(expectedResult))))
            )
        );
    }
    
    @Test
    public void testAllClassesAddMethodReturnValueOfSumOfSharedMetaVariables() throws IOException {
        int i1 = 5;
        int i2 = 7;
        int expectedResult = i1 + i2;
        
        String src =
            "class {\n" +
            "    +public int getValue() ${\n" +
            "        int i1 = " + i1 + ";\n" +
            "        int i2 = " + i2 + ";\n" +
            "        return #return $i1 + i2;\n" +
            "    }\n" +
            "}\n";
        
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            src, 
            forClass("jasy.TestClass1", 
                forInstance(imethod("getValue", invocationResult(is(expectedResult))))
            )
        );
    }
    
    @Test
    public void testAllClassesAddMethodReturnSumOfVariable() throws IOException {
        int i1 = 5;
        int i2 = 7;
        int expectedResult = i1 + i2;
        
        String src =
            "class {\n" +
            "    +public int getValue() {\n" +
            "        int i1 = " + i1 + ";\n" +
            "        int i2 = " + i2 + ";\n" +
            "        return i1 + i2;\n" +
            "    }\n" +
            "}\n";
        
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            src, 
            forClass("jasy.TestClass1", 
                forInstance(imethod("getValue", invocationResult(is(expectedResult))))
            )
        );
    }
    
    @Test
    public void testAllClassesAddMethodWithInjection() throws IOException {
        int expectedResult = 5;
        
        String src =
            "class {\n" +
            "    +public int getValue() {\n" +
            "        int i;\n" +
            "        $#i = " + expectedResult + ";\n" +
            "        return i;\n" +
            "    }\n" +
            "}\n";
        
        testSourceToClasses(
            new String[]{"jasy.TestClass1"}, 
            src, 
            forClass("jasy.TestClass1", 
                forInstance(imethod("getValue", invocationResult(is(expectedResult))))
            )
        );
    }
    
    @Test
    public void testAllClassesAddMethodWithWhileCounting() throws IOException {
        int counterStart = 0;
        int counterEnd = 10;
        int valueStart = 0;
        int valueIncrement = 6;
        int expectedResult = valueStart + (counterEnd - counterStart) * valueIncrement;
        
        String templaceSrc =
            "class {\n" +
            "    +public int getValue() {\n" +
            "        int i = <<init>>;\n" +
            "        int value = " + valueStart + ";\n" +
            "        while(<<cond>>) {\n" +
            "           value += " + valueIncrement + ";\n" +
            "           <<inc>>;\n" +
            "        }\n" +
            "        return value;\n" +
            "    }\n" +
            "}\n";
        
        expand(templaceSrc, 
            map(entry("init", "" + counterStart), entry("cond", "i < " + counterEnd), entry("inc", "i++")),
            map(entry("init", "" + counterStart), entry("cond", "i < " + counterEnd), entry("inc", "++i")),
            map(entry("init", "" + counterEnd), entry("cond", "i > " + counterStart), entry("inc", "i--")),
            map(entry("init", "" + counterEnd), entry("cond", "i > " + counterStart), entry("inc", "--i"))
        ).forEach(combination -> {
            try {
                testSourceToClasses(
                    new String[]{"jasy.TestClass1"},
                    combination.src,
                    forClass("jasy.TestClass1",
                        forInstance(imethod("getValue", invocationResult(is(expectedResult))))
                    )
                );
            } catch (IOException ex) {
                Logger.getLogger(SourceToClassTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
    
    @Test
    public void testAllClassesAddMethodWithForCounting() throws IOException {
        int counterStart = 0;
        int counterEnd = 10;
        int valueStart = 0;
        int valueIncrement = 6;
        int expectedResult = valueStart + (counterEnd - counterStart) * valueIncrement;
        
        String templaceSrc =
            "class {\n" +
            "    +public int getValue() {\n" +
            "        int value = " + valueStart + ";\n" +
            "        for(int i = <<init>>; <<cond>>; <<inc>>) {\n" +
            "           value += " + valueIncrement + ";\n" +
            "        }\n" +
            "        return value;\n" +
            "    }\n" +
            "}\n";
        
        expand(templaceSrc, 
            map(entry("init", "" + counterStart), entry("cond", "i < " + counterEnd), entry("inc", "i++")),
            map(entry("init", "" + counterStart), entry("cond", "i < " + counterEnd), entry("inc", "++i")),
            map(entry("init", "" + counterEnd), entry("cond", "i > " + counterStart), entry("inc", "i--")),
            map(entry("init", "" + counterEnd), entry("cond", "i > " + counterStart), entry("inc", "--i"))
        ).forEach(combination -> {
            try {
                testSourceToClasses(
                    new String[]{"jasy.TestClass1"},
                    combination.src,
                    forClass("jasy.TestClass1",
                        forInstance(imethod("getValue", invocationResult(is(expectedResult))))
                    )
                );
            } catch (IOException ex) {
                Logger.getLogger(SourceToClassTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
    
    @Test
    public void testAllClassesAddMethodAmbigousName() throws IOException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException {
        Class<?> c = TestClassStaticField.class;
        Field field = c.getDeclaredField("myField");
        
        String expectedResultField = (String)field.get(null);
        
        String templaceSrc =
            "class {\n" +
            "    +private String " + field.getName() + " = \"" + expectedResultField + "\";\n" +
            "    +private " + TestClass2.class.getName() + " tc = new " + TestClass2.class.getName() + "();\n" +
            "    +private " + TestComplexClass.class.getName() + " tcc = new " + TestComplexClass.class.getName() + "();\n" +
            "    +public Object getValue() {\n" +
            "        <<ambiguousClassName>>\n" +
            "    }\n" +
            "}\n";
                
        expand(templaceSrc,
            // As expression tests
            new Configuration(
                map(entry("ambiguousClassName", "return " + c.getSimpleName() + "." + field.getName() + ";")), 
                map(entry("expectedResult", expectedResultField))
            ),
            new Configuration(
                map(entry("ambiguousClassName", "return " + c.getName() + "." + field.getName() + ";")), 
                map(entry("expectedResult", expectedResultField))
            ),
            new Configuration(
                map(entry("ambiguousClassName", "return " + field.getName() + ";")), 
                map(entry("expectedResult", expectedResultField))
            ),
            new Configuration(
                map(entry("ambiguousClassName", "return tc.field2;")), 
                map(entry("expectedResult", new TestClass2().field2))
            ),
            new Configuration(
                map(entry("ambiguousClassName", "return tcc.testClass2.field2;")), 
                map(entry("expectedResult", new TestComplexClass().testClass2.field2))
            ),
            new Configuration(
                map(entry("ambiguousClassName", "return new jasy.TestComplexClass().testClass2.get(\"Hi\");")), 
                map(entry("expectedResult", new jasy.TestComplexClass().testClass2.get("Hi")))
            ),
            // As statement tests
            new Configuration(
                map(entry("ambiguousClassName", "new jasy.TestComplexClass().testClass2.toString(); return \"Hi\";")), 
                map(entry("expectedResult", "Hi"))
            ),
            new Configuration(
                map(entry("ambiguousClassName", "jasy.TestComplexClass.testClass2Static.toString(); return \"Hi\";")), 
                map(entry("expectedResult", "Hi"))
            )
        ).forEach(combination -> {
            Object expectedResult = combination.customMap.get("expectedResult");
            try {
                testSourceToClasses(
                    new String[]{"jasy.TestClass1"},
                    combination.src,
                    forClass("jasy.TestClass1",
                        forInstance(imethod("getValue", invocationResult(is(expectedResult))))
                    )
                );
            } catch (IOException ex) {
                Logger.getLogger(SourceToClassTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
    
    @Test
    public void testAllClassesAddMethodWithReturnInIfElse() throws IOException {
        int trueIfGT = 10;
        
        String src =
            "class {\n" +
            "    +public boolean gt(int x) {\n" +
            "        if(x > " + trueIfGT + ")\n" +
            "            return true;\n" +
            "        else\n" +
            "            return false;\n" +
            "    }\n" +
            "}\n";
        
        testSourceToClasses(new String[]{"jasy.TestClass1"}, 
            src, 
            forClass("jasy.TestClass1", 
                forInstance(imethod("gt", new Class<?>[]{int.class}, invocationResult(new Object[]{trueIfGT + 1}, is(true))))
                .and(
                    forInstance(imethod("gt", new Class<?>[]{int.class}, invocationResult(new Object[]{trueIfGT}, is(false))))
                )
            )
        );
    }
    
    @Test
    public void testAllClassesAddMethodWithReturnInIf() throws IOException {
        int trueIfGT = 10;
        
        String src =
            "class {\n" +
            "    +public boolean gt(int x) {\n" +
            "        if(x > " + trueIfGT + ")\n" +
            "            return true;\n" +
            "        \n" +
            "        return false;\n" +
            "    }\n" +
            "}\n";
        
        testSourceToClasses(new String[]{"jasy.TestClass1"}, 
            src, 
            forClass("jasy.TestClass1", 
                forInstance(imethod("gt", new Class<?>[]{int.class}, invocationResult(new Object[]{trueIfGT + 1}, is(true))))
                .and(
                    forInstance(imethod("gt", new Class<?>[]{int.class}, invocationResult(new Object[]{trueIfGT}, is(false))))
                )
            )
        );
    }
    
    @Test
    public void testAllClassesAddMethodWithParametersAndVariables() throws IOException {
        int x = 10;
        int a = 15;
        int y = 5;
        int b = 56;
        int expectedValue = x + a + y + b;
        
        String src =
            "class {\n" +
            "    +public int gt(int x, int a) {\n" +
            "        int y = " + y + ";\n" +
            "        int b = " + b + ";\n" +
            "        return x + a + y + b;\n" +
            "    }\n" +
            "}\n";
        
        testSourceToClasses(new String[]{"jasy.TestClass1"}, 
            src, 
            forClass("jasy.TestClass1", 
                forInstance(imethod("gt", new Class<?>[]{int.class, int.class}, invocationResult(new Object[]{x, a}, is(expectedValue))))
            )
        );
    }
    
    private static class Combination<T> {
        public final T first;
        public final T second;

        public Combination(T first, T second) {
            this.first = first;
            this.second = second;
        }
    }
    
    private static <T> List<Combination<T>> combine(List<T> elements) {
        ArrayList<Combination<T>> combinations = new ArrayList<>();
        
        elements.forEach(x -> 
            elements.forEach(y -> 
                combinations.add(new Combination<>(x, y))
            )
        );
        
        return combinations;
    }
    
    private static final String reductionTemplateSrcMethodName = "reduce";
    private static final String reductionTemplateSrc =
        "class {\n" +
        "    +public <<type>> " + reductionTemplateSrcMethodName + "() {\n" +
        "        <<lhsType>> x = <<lhs>>;\n" +
        "        <<rhsType>> y = <<rhs>>;\n" +
        "        return x <<op>> y;\n" +
        "    }\n" +
        "}\n";
    private static final List<String> primitiveNumberTypes = 
        Arrays.asList("byte", "short", "int", "long", "float", "double");
    
    private static String toSourceCode(long value, String type) {
        switch(type) {
            case "byte":
            case "short":
            case "int":
                return "" + value;
            case "long":
                return "" + value + "L";
            case "float":
                return "" + value + ".0F";
            case "double":
                return "" + value + ".0";
        }
        
        throw new IllegalArgumentException("Cannot convert '" + type + "' into source code.");
    }
    
    @Test
    public void testAllClassesAddMethodWithAddExpression() {
        long lhs = 5;
        long rhs = 7;
        long expectedValueRaw = lhs + rhs;
        
        testAllClassesAddMethodWithReduction(lhs, rhs, expectedValueRaw, "+");
    }
    
    @Test
    public void testAllClassesAddMethodWithSubExpression() {
        long lhs = 5;
        long rhs = 7;
        long expectedValueRaw = lhs - rhs;
        
        testAllClassesAddMethodWithReduction(lhs, rhs, expectedValueRaw, "-");
    }
    
    @Test
    public void testAllClassesAddMethodWithMultExpression() {
        long lhs = 5;
        long rhs = 7;
        long expectedValueRaw = lhs * rhs;
        
        testAllClassesAddMethodWithReduction(lhs, rhs, expectedValueRaw, "*");
    }
    
    @Test
    public void testAllClassesAddMethodWithDivExpression() {
        long lhs = 10;
        long rhs = 5;
        long expectedValueRaw = lhs / rhs;
        
        testAllClassesAddMethodWithReduction(lhs, rhs, expectedValueRaw, "/");
    }
    
    @Test
    public void testAllClassesAddMethodWithRemExpression() {
        long lhs = 11;
        long rhs = 5;
        long expectedValueRaw = lhs % rhs;
        
        testAllClassesAddMethodWithReduction(lhs, rhs, expectedValueRaw, "%");
    }
    
    public void testAllClassesAddMethodWithReduction(long lhs, long rhs, long expectedValueRaw, String op) {
        SourceCode addSourceCode = TemplateSource.expand(reductionTemplateSrc, 
            map(entry("op", op))
        ).get(0);
        
        combine(primitiveNumberTypes).forEach(typeCombination -> {
            String lhsType = typeCombination.first;
            String rhsType = typeCombination.second;
            String resultType = Reduction.typeOf(lhsType, rhsType).getSimpleName();
            String lhsValue = toSourceCode(lhs, lhsType);
            String rhsValue = toSourceCode(rhs, rhsType);
            
            SourceCode sourceCode = TemplateSource.expand(addSourceCode.src, 
                map(entry("type", resultType), entry("lhsType", lhsType), entry("rhsType", rhsType), entry("lhs", "" + lhsValue), entry("rhs", "" + rhsValue))
            ).get(0);
            
            Number expectedValue = toNumber(expectedValueRaw, resultType);
            try {
                testSourceToClasses(new String[]{"jasy.TestClass1"},
                    sourceCode.src,
                    forClass("jasy.TestClass1",
                        forInstance(imethod(reductionTemplateSrcMethodName, invocationResult(is(expectedValue))))
                    )
                );
            } catch (IOException ex) {
                Logger.getLogger(SourceToClassTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
    
    private static ClassBytesTransformer transformClass(ClassResolver resolver, String source) {
        return new ModuleClassBytesTransformer(source, resolver);
        
//        ASMCompiler compiler = new ASMCompiler(resolver);
//        return (classLoader, bytes) -> {
//            try {
//                ModuleAST module = compiler.compile(new ByteArrayInputStream(source.getBytes("UTF-8")));
//                ArrayList<Message> errorMessages = new ArrayList<>();
//                module.resolve(null, null, resolver, classLoader, errorMessages);
//                
//                if(errorMessages.size() > 0) {
//                    String msg = errorMessages.stream().map(m -> m.toString()).collect(Collectors.joining("\n"));
//                    throw new RuntimeException(msg);
//                } else {
//                    Function<Transformation<ClassNode>, Runnable> classTransformer = module.toClassTransformer(resolver, classLoader);
//                    ExhaustiveClassTransformer eTransformer = new ExhaustiveClassTransformer(classTransformer);
//                    byte[] newBytes = eTransformer.transform(bytes);
//                    
//                    InputStream classStream = new ByteArrayInputStream(newBytes);
//                    ClassReader classReader = new ClassReader(classStream);
////                    classReader.accept(new TraceClassVisitor(new PrintWriter(System.out)), 0);
//                    CheckClassAdapter.verify(classReader, false, new PrintWriter(System.out));
////                    ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
////                    classReader.accept(classWriter, 0);
////                    Textifier asmifier = new Textifier();
////                    classWriter.
//                    
//                    return newBytes;
//                }
//            } catch (IOException ex) {
//                Logger.getLogger(SourceToClassTest.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            return bytes;
//        };
    }
    
    private static void testSourceToClasses(String[] classNames, String source, Predicate<Class<?>[]> assertion) throws IOException {
        CommonClassMap classMap = new CommonClassMap();
        
        for(String className: classNames)
            classMap.addClassName(className);
        
        classMap.addClassName("java.lang.System");
        classMap.addClassName("java.lang.String");
        classMap.addClassName("java.lang.Object");
        classMap.addClassName("java.lang.StringBuilder");
        classMap.addClassName("jasy.lang.ast.CodeAST");
        classMap.addClassName("org.objectweb.asm.tree.FieldNode");
        classMap.addClassName(TestClassStaticField.class.getName());
        
        CommonClassResolver resolver = new CommonClassResolver(classMap);
        
        resolver.importPackage("java.lang");
        resolver.importPackage("jasy.lang.ast");
        resolver.importPackage("org.objectweb.asm.tree");
        resolver.importPackage("jasy");
        
        ClassLoader cl = new ProxyClassLoader(ifIn(classNames).ifTrue(classBytesFromName()).andThen(transformClass(resolver, source)));
        
        Class<?>[] classes = Arrays.asList(classNames).stream()
            .map(className -> {
                try {
//                    return cl.loadClass(className);
                    return Class.forName(className, true, cl);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(SourceToAstTest.class.getName()).log(Level.SEVERE, null, ex);
                    return null;
                }
            })
            .toArray(size -> new Class<?>[size]);
        
        try {
            assertTrue(assertion.test(classes));
        } catch(Error e) {
            throw e;
        }
        
        // Read all classes
        // Replace interusages with derived classes
        // Derived classes using module
        // Assert derived classes
//        assertTrue(modulePredicate.test(module));
    }
    
    private static Predicate<Class<?>[]> forClass(String name, Predicate<Class<?>> predicate) {
        return classes -> {
            Class<?> c = Arrays.asList(classes).stream().filter(x -> x.getName().equals(name)).findFirst().get();
            return predicate.test(c);
        };
    }
    
    private static Predicate<Class<?>> chasFieldWhere(Predicate<Field> predicate) {
        return c -> Arrays.asList(c.getDeclaredFields()).stream().anyMatch(predicate);
    }
    
    private static Predicate<Field> fname(Predicate<String> predicate) {
        return f -> predicate.test(f.getName());
    }
    
    private static Predicate<Field> ftype(Predicate<Class<?>> predicate) {
        return f -> predicate.test(f.getType());
    }
    
    private static Predicate<Field> fmodifiers(Predicate<Integer> predicate) {
        return f -> predicate.test(f.getModifiers());
    }
    
    private static Predicate<Class<?>> chasMethodWhere(Predicate<Method> predicate) {
        return c -> Arrays.asList(c.getDeclaredMethods()).stream().anyMatch(predicate);
    }
    
    private static Predicate<Class<?>> forInstance(Predicate<Object> predicate) {
        return c -> {
            try {
                Object instance = c.newInstance();
                return predicate.test(instance);
            } catch (InstantiationException | IllegalAccessException ex) {
                Logger.getLogger(SourceToClassTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            return false;
        };
    }
    
    private static Predicate<Object> imethod(String name, BiPredicate<Object, Method> predicate) {
        return imethod(name, new Class<?>[0], predicate);
    }
    
    private static Predicate<Object> imethod(String name, Class<?>[] parameterTypes, BiPredicate<Object, Method> predicate) {
        return i -> {
            try {
                Method m = i.getClass().getDeclaredMethod(name, parameterTypes);
                return predicate.test(i, m);
            } catch (NoSuchMethodException | SecurityException ex) {
                Logger.getLogger(SourceToClassTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            return false;
        };
    }
    
    private static Predicate<Object> ifield(String name, BiPredicate<Object, Field> predicate) {
        return i -> {
            try {
                Field m = i.getClass().getDeclaredField(name);
                m.setAccessible(true);
                return predicate.test(i, m);
            } catch (NoSuchFieldException | SecurityException ex) {
                Logger.getLogger(SourceToClassTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            return false;
        };
    }
    
    private static BiPredicate<Object, Method> invocationResult(Predicate<Object> predicate) {
        return invocationResult(new Object[0], predicate);
    }
    
    private static BiPredicate<Object, Method> invocationResult(Object[] args, Predicate<Object> predicate) {
        return (i, m) -> {
            try {
                Object result = m.invoke(i, args);
                System.out.println("Invocation result:\n" + result);
                return predicate.test(result);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(SourceToClassTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            return false;
        };
    }
    
    private static BiPredicate<Object, Field> ifget(Predicate<Object> predicate) {
        return (i, f) -> {
            try {
                Object value = f.get(i);
                return predicate.test(value);
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(SourceToClassTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            return false;
        };
    }
    
    private static Predicate<Integer> isPublic() {
        return m -> Modifier.isPublic(m);
    }
    
    private static Predicate<Integer> isProtected() {
        return m -> Modifier.isProtected(m);
    }
    
    private static Predicate<Integer> isPrivate() {
        return m -> Modifier.isPrivate(m);
    }
    
    private static Predicate<Integer> isStatic() {
        return m -> Modifier.isStatic(m);
    }
    
    private static ClassBytesSourcePredicate ifIn(String[] names) {
        return (cl, name) -> Arrays.asList(names).contains(name);
    }
    
    private static ThreadLocal<Hashtable<String, byte[]>> classBytesCacheMap = new ThreadLocal<Hashtable<String, byte[]>>() {
        @Override
        protected Hashtable<String, byte[]> initialValue() {
            return new Hashtable<String, byte[]>();
        }   
    };
    
    private static ClassBytesSource classBytesFromName() {
        return new ClassBytesFromFile(Arrays.asList("build/test/classes"));
    }

    private static Predicate<Method> mname(Predicate<String> predicate) {
        return m -> predicate.test(m.getName());
    }

    private static Predicate<? super Method> rreturnType(Predicate<Class<?>> predicate) {
        return m -> predicate.test(m.getReturnType());
    }

    private static Predicate<? super Method> rmodifiers(Predicate<Integer> predicate) {
        return m -> predicate.test(m.getModifiers());
    }
}
