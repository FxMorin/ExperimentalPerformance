package ca.fxco.experimentalperformance.utils;

import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Textifier;

import java.util.List;
import java.util.Objects;

import static org.objectweb.asm.tree.AbstractInsnNode.*;

public class NodeUtils {

    public static void printMethod(List<MethodNode> methods) {
        for (int i = 0; i < methods.size(); i++) {
            MethodNode methodNode = methods.get(i);
            System.out.println(
                    i + ") name: " + methodNode.name + " - parameterCount: " +
                            (methodNode.parameters == null ? 0 : methodNode.parameters.size()) +
                            " - instructionCount: " + methodNode.instructions.size()
            );
        }
    }

    public static void printInstructions(InsnList instructions, boolean numbered) {
        for (int i = 0; i < instructions.size(); i++) {
            AbstractInsnNode instructionNode = instructions.get(i);
            if (numbered) {
                System.out.printf("%3d) %s%n", i, instructionToString(instructionNode));
            } else {
                System.out.println(instructionToString(instructionNode));
            }
        }
    }

    public static String getOpcodeString(int opcode) {
        return opcode == -1 ? "" : Textifier.OPCODES[opcode];
    }

    public static String getTypeString(int type) {
        return switch(type) {
            case INSN -> "INSN";
            case INT_INSN -> "INT_INSN";
            case VAR_INSN -> "VAR_INSN";
            case TYPE_INSN -> "TYPE_INSN";
            case FIELD_INSN -> "FIELD_INSN";
            case METHOD_INSN -> "METHOD_INSN";
            case INVOKE_DYNAMIC_INSN -> "INVOKE_DYNAMIC_INSN";
            case JUMP_INSN -> "JUMP_INSN";
            case LABEL -> "LABEL";
            case LDC_INSN -> "LDC_INSN";
            case IINC_INSN -> "IINC_INSN";
            case TABLESWITCH_INSN -> "TABLESWITCH_INSN";
            case LOOKUPSWITCH_INSN -> "LOOKUPSWITCH_INSN";
            case MULTIANEWARRAY_INSN -> "MULTIANEWARRAY_INSN";
            case FRAME -> "FRAME";
            case LINE -> "LINE";
            default -> throw new IllegalStateException("Unexpected type: " + type);
        };
    }

    public static String instructionToString(AbstractInsnNode insn) {
        int type = insn.getType();
        String data = switch(type) {
            case INSN -> "";
            case INT_INSN -> String.valueOf(((IntInsnNode)insn).operand);
            case VAR_INSN -> String.valueOf(((VarInsnNode)insn).var);
            case TYPE_INSN -> ((TypeInsnNode)insn).desc;
            case FIELD_INSN -> ((FieldInsnNode)insn).owner + " " + ((FieldInsnNode)insn).name + " " + ((FieldInsnNode)insn).desc;
            case METHOD_INSN -> ((MethodInsnNode)insn).owner + " " + ((MethodInsnNode)insn).name + " " + ((MethodInsnNode)insn).desc + " " + ((MethodInsnNode)insn).itf;
            case INVOKE_DYNAMIC_INSN -> ((InvokeDynamicInsnNode)insn).name + " " + ((InvokeDynamicInsnNode)insn).desc;
            case JUMP_INSN -> ((JumpInsnNode)insn).label.getLabel().toString();
            case LABEL -> ((LabelNode)insn).getLabel().toString();
            case LDC_INSN -> "<stuff>"; // Object
            case IINC_INSN -> ((IincInsnNode)insn).var + " " + ((IincInsnNode)insn).incr;
            case TABLESWITCH_INSN -> ((TableSwitchInsnNode)insn).min + " " + ((TableSwitchInsnNode)insn).max + " " + ((TableSwitchInsnNode)insn).dflt.getLabel().toString(); // missing some
            case LOOKUPSWITCH_INSN -> ""; // lazy
            case MULTIANEWARRAY_INSN -> ((MultiANewArrayInsnNode)insn).desc + " " + ((MultiANewArrayInsnNode)insn).dims;
            case FRAME -> "FRAME"; // lazy
            case LINE -> ((LineNumberNode)insn).start.getLabel().toString();
            default -> throw new IllegalStateException("Unexpected type: " + type);
        };
        String opcode = getOpcodeString(insn.getOpcode());
        return (type == LINE ? "" : "  ") + (Objects.equals(opcode, "") ? "" : opcode + " ") +
                (Objects.equals(data, "") ? "" : data + " ");// + getTypeString(type);
    }
}
