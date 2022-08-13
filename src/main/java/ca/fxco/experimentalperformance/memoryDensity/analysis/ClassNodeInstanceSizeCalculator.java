package ca.fxco.experimentalperformance.memoryDensity.analysis;

import net.minecraft.util.Pair;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.openjdk.jol.util.MathUtil;
import org.openjdk.jol.vm.VM;
import org.openjdk.jol.vm.VirtualMachine;

import java.util.function.Predicate;

import static org.objectweb.asm.Type.*;

// Uses ClassNode & FieldNode instead of Class & Field like JOL
// *this may result in some inconsistencies* Although seems to work perfectly from my testing
public class ClassNodeInstanceSizeCalculator {

    private static final int INTERNAL = 12;
    private static final VirtualMachine vm = VM.current();

    public static final int OBJECT_HEADER_SIZE = vm.objectHeaderSize();
    public static final int OBJECT_ALIGNMENT = vm.objectAlignment();
    public static final int BYTE_SIZE = (int) vm.sizeOfField("byte");
    public static final int BOOLEAN_SIZE = (int) vm.sizeOfField("boolean");
    public static final int SHORT_SIZE = (int) vm.sizeOfField("short");
    public static final int CHAR_SIZE = (int) vm.sizeOfField("char");
    public static final int FLOAT_SIZE = (int) vm.sizeOfField("float");
    public static final int INT_SIZE = (int) vm.sizeOfField("int");
    public static final int LONG_SIZE = (int) vm.sizeOfField("long");
    public static final int DOUBLE_SIZE = (int) vm.sizeOfField("double");
    public static final int REF_SIZE = (int) vm.sizeOfField("ref");

    public static int calculateFieldCost(FieldNode fieldNode) {
        return getTypeSortSize(Type.getType(fieldNode.desc).getSort());
    }

    /**
     This does not account for @Contended fields - Contended is not supported for MC mods anyways
     **/
    public static long calculateClassNodeSize(ClassNode classNode) {
        long instanceSize = OBJECT_HEADER_SIZE;
        for (FieldNode field : classNode.fields) {
            if ((field.access & Opcodes.ACC_STATIC) != 0) continue; // Don't include static fields
            instanceSize += calculateFieldCost(field);
        }
        return MathUtil.align(instanceSize, OBJECT_ALIGNMENT);
    }

    /**
     This does not account for @Contended fields - Contended is not supported for MC mods anyways
     **/
    public static long calculateClassNodeSize(ClassNode classNode, Predicate<FieldNode> shouldInclude) {
        long instanceSize = OBJECT_HEADER_SIZE;
        for (FieldNode field : classNode.fields) {
            if ((field.access & Opcodes.ACC_STATIC) != 0) continue; // Don't include static fields
            if (shouldInclude.test(field))
                instanceSize += calculateFieldCost(field);
        }
        return MathUtil.align(instanceSize, OBJECT_ALIGNMENT);
    }

    /**
     This does not account for @Contended fields - Contended is not supported for MC mods anyways
     Calculates two separate long values for different conditions
     **/
    public static Pair<Long, Long> calculateDualClassNodeSize(ClassNode classNode, Predicate<FieldNode> firstPredicate,
                                                            Predicate<FieldNode> secondPredicate) {
        long firstInstanceSize = OBJECT_HEADER_SIZE;
        long secondInstanceSize = firstInstanceSize;
        for (FieldNode field : classNode.fields) {
            if ((field.access & Opcodes.ACC_STATIC) != 0) continue; // Don't include static fields
            if (firstPredicate.test(field))
                firstInstanceSize += calculateFieldCost(field);
            if (firstPredicate.test(field))
                secondInstanceSize += calculateFieldCost(field);
        }
        return new Pair<>(
                MathUtil.align(firstInstanceSize, OBJECT_ALIGNMENT),
                MathUtil.align(secondInstanceSize, OBJECT_ALIGNMENT)
        );
    }

    private static int getTypeSortSize(int sort) {
        return switch (sort) {
            case ARRAY, OBJECT ,INTERNAL -> REF_SIZE;
            case BOOLEAN -> BOOLEAN_SIZE;
            case INT -> INT_SIZE;
            case FLOAT -> FLOAT_SIZE;
            case LONG -> LONG_SIZE;
            case DOUBLE -> DOUBLE_SIZE;
            case BYTE -> BYTE_SIZE;
            case SHORT -> SHORT_SIZE;
            case CHAR -> CHAR_SIZE;
            case VOID -> 0;
            default -> throw new AssertionError();
        };
    }
}
