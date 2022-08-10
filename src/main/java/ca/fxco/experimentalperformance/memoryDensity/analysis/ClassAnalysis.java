package ca.fxco.experimentalperformance.memoryDensity.analysis;

import org.jetbrains.annotations.NotNull;
import org.openjdk.jol.info.ClassData;
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.info.FieldData;
import org.openjdk.jol.layouters.Layouter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class ClassAnalysis {

    private final Class<?> clazz;
    private final Layouter layouter;


    public ClassAnalysis(Class<?> clazz, Layouter layouter) {
        this.clazz = clazz;
        this.layouter = layouter;
    }

    private static ClassData createClassDataFromClass(@NotNull Class<?> clazz, boolean privateOnly) {
        String clazzName = clazz.getName();
        ClassData classData = new ClassData(clazzName);
        Class<?> superClazz = clazz.getSuperclass();
        if (superClazz != null && !clazz.equals(superClazz)) //Recursively build classData
            classData.addSuperClassData(createClassDataFromClass(superClazz, privateOnly));
        do {
            for (Field field : clazz.getFields()) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers)) continue;
                if (privateOnly && !Modifier.isPrivate(modifiers)) continue;
                classData.addField(FieldData.parse(field));
            }
            classData.addSuperClass(clazz.getName());
        } while ((clazz = clazz.getSuperclass()) != null);
        return classData;
    }

    public Future<AnalysisResults> runAnalysis() {
        return CompletableFuture.supplyAsync(() -> {
            ClassLayout classLayout = layouter.layout(createClassDataFromClass(clazz, false));
            ClassLayout privateClassLayout = layouter.layout(createClassDataFromClass(clazz, true));
            return new AnalysisResults(classLayout, privateClassLayout);
        });
    }

    @Override
    public String toString() {
        return clazz.getName();
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

        @Override
        public String toString() { //TODO: Change this once private size is fixed
            return String.format("Size: %4d - %s", (getSize() - getHeaderSize()), ClassAnalysis.this);
            /*return "Size: " + (getSize() - getHeaderSize()) +
                    " - PrivateSize: " + (getPrivateSize() - getPrivateHeaderSize()) +
                    " - canOptimize: " + canOptimize + ClassAnalysis.this;*/
        }
    }
}
