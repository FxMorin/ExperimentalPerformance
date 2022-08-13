package ca.fxco.experimentalperformance.memoryDensity.analysis;

import ca.fxco.experimentalperformance.memoryDensity.mixinHacks.HacktasticClassLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.openjdk.jol.info.ClassData;
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.info.FieldData;
import org.openjdk.jol.layouters.CurrentLayouter;
import org.openjdk.jol.layouters.Layouter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;

public class ClassAnalysis {

    private final String className;
    private final Layouter layouter;

    public ClassAnalysis(String className) {
        this(className, new CurrentLayouter());
    }

    public ClassAnalysis(String className, Layouter layouter) {
        this.className = className;
        this.layouter = layouter;
    }

    private static ClassData createClassData(@NotNull Class<?> clazz, boolean privateOnly, Set<String> validFields) {
        String clazzName = clazz.getName();
        ClassData classData = new ClassData(clazzName);
        Class<?> superClazz = clazz.getSuperclass();
        if (superClazz != null && !clazz.equals(superClazz)) //Recursively build classData
            classData.addSuperClassData(createClassData(superClazz, privateOnly, validFields));
        do {
            for (Field field : clazz.getDeclaredFields()) {
                if (validFields != null && validFields.contains(field.getName())) continue;
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers)) continue;
                if (privateOnly && !Modifier.isPrivate(modifiers)) continue;
                classData.addField(FieldData.parse(field));
            }
            classData.addSuperClass(clazz.getName());
        } while ((clazz = clazz.getSuperclass()) != null);
        return classData;
    }

    private static ClassData createClassData(@NotNull ClassNode classNode, byte[] classBytes, boolean privateOnly, Set<String> validFields) {
        String className = classNode.name;
        ClassData classData = new ClassData(className);

        // Get class without loading it with the normal classLoaders
        HacktasticClassLoader hackyClassLoader = new HacktasticClassLoader();
        Class clazz = hackyClassLoader.defineClass(className, classBytes);

        for (FieldNode field : classNode.fields) {
            if (validFields != null && validFields.contains(field.name)) continue;
            if ((field.access & Opcodes.ACC_STATIC) != 0) continue; // If isStatic
            if (privateOnly && (field.access & Opcodes.ACC_PRIVATE) == 0) continue; // If not private and privateOnly
            try {
                classData.addField(FieldData.parse(clazz.getDeclaredField(field.name)));
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        return classData;
    }

    public AnalysisResults runAnalysis(@NotNull Class<?> clazz, @Nullable Set<String> validFields) {
        ClassLayout classLayout = layouter.layout(createClassData(clazz, false, validFields));
        ClassLayout privateClassLayout = layouter.layout(createClassData(clazz, true, validFields));
        return new AnalysisResults(classLayout, privateClassLayout);
    }

    public AnalysisResults runAnalysis(@NotNull ClassNode classNode, byte[] classBytes, @Nullable Set<String> validFields) {
        if (classBytes == null || classBytes.length == 0) return null;
        ClassLayout classLayout = layouter.layout(createClassData(classNode, classBytes, false, validFields));
        ClassLayout privateClassLayout = layouter.layout(createClassData(classNode, classBytes, true, validFields));
        return new AnalysisResults(classLayout, privateClassLayout);
    }

    class AnalysisResults {

        private final ClassLayout classLayout;
        private final ClassLayout privateClassLayout;
        private final boolean canOptimize;

        public AnalysisResults(final ClassLayout classLayout, final ClassLayout privateClassLayout) {
            this.classLayout = classLayout;
            this.privateClassLayout = privateClassLayout;
            long totalSize = classLayout.instanceSize();
            this.canOptimize = totalSize > 64 && (privateClassLayout.instanceSize() >= totalSize % 64);
        }

        public long getSize() {
            return this.classLayout.instanceSize();
        }

        public long getPrivateSize() {
            return this.privateClassLayout.instanceSize();
        }

        public long getHeaderSize() {
            return this.classLayout.headerSize();
        }

        public long getPrivateHeaderSize() {
            return this.privateClassLayout.headerSize();
        }

        public int getFieldCount() {
            return this.classLayout.fields().size();
        }

        public int getPrivateFieldCount() {
            return this.privateClassLayout.fields().size();
        }

        public boolean canOptimize() {
            return this.canOptimize;
        }

        public String getClassName() {
            return ClassAnalysis.this.className;
        }

        @Override
        public String toString() { //TODO: Change this once private size is fixed
            return String.format("Size: %4d - %s", (getSize() - getHeaderSize()), ClassAnalysis.this);
            /*return "Size: " + (getSize() - getHeaderSize()) +
                    " - PrivateSize: " + (getPrivateSize() - getPrivateHeaderSize()) +
                    " - canOptimize: " + canOptimize + ClassAnalysis.this;*/
        }
    }
}
