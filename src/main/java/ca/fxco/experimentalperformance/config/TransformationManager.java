package ca.fxco.experimentalperformance.config;

import ca.fxco.experimentalperformance.ExperimentalPerformance;
import ca.fxco.experimentalperformance.utils.asm.FakeMixinStreamHandler;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.ext.Extensions;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;
import org.spongepowered.asm.mixin.transformer.ext.extensions.ExtensionClassExporter;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

import static ca.fxco.experimentalperformance.utils.CommonConst.CLASS_EXT;
import static ca.fxco.experimentalperformance.utils.CommonConst.OBJECT_PATH;
import static ca.fxco.experimentalperformance.utils.GeneralUtils.*;

public class TransformationManager {

    private static final String MIXIN_ANNOTATION_DESCRIPTOR = "Lorg/spongepowered/asm/mixin/Mixin;";

    private final Map<String, Set<Consumer<ClassNode>>> preTransformers = new HashMap<>();
    private final Map<String, Set<Consumer<ClassNode>>> postTransformers = new HashMap<>();
    private final Map<String, byte[]> mixinClassModifiers = new HashMap<>();
    private final Set<String> loadAsMixins = new HashSet<>();

    private final String mixinPackage;
    private final Consumer<URL> urlParser; // Will add the url to the classLoader

    public TransformationManager(String mixinPackage) {
        mixinPackage = formatPathSlash(mixinPackage);
        if (!mixinPackage.endsWith("/")) mixinPackage += "/";
        this.mixinPackage = mixinPackage;
        this.urlParser = getAddURLMethod();
    }

    public void onLoad() {
        try {
            this.urlParser.accept(FakeMixinStreamHandler.createURL(this.mixinClassModifiers));
        } catch (MalformedURLException e) {
            ExperimentalPerformance.LOGGER.error("MalformedURL used for Mixin UrlParser", e);
        }

        ExtensionClassExporter extensionClassExporter = getExtension(ExtensionClassExporter.class);
        FakeMixinStreamHandler.sign = (name, bytes) -> {
            ClassNode node = new ClassNode();
            new ClassReader(bytes).accept(node, ClassReader.EXPAND_FRAMES);
            extensionClassExporter.export(MixinEnvironment.getCurrentEnvironment(), name, false, node);
        };
    }

    public String getMixinPackage() {
        return this.mixinPackage;
    }

    public void addPreTransformer(String className, Consumer<ClassNode> consumer) {
        className = formatPathSlash(className);
        addMixinClass(className);
        this.preTransformers.computeIfAbsent(className, (n) -> new HashSet<>()).add(consumer);
    }

    public void addPostTransformer(String className, Consumer<ClassNode> consumer) {
        className = formatPathSlash(className);
        addMixinClass(className);
        this.postTransformers.computeIfAbsent(className, (n) -> new HashSet<>()).add(consumer);
    }

    private void addMixinClass(String className) {
        this.loadAsMixins.add(formatPathDot(className));
        String fullName = this.mixinPackage + className;
        if (ExperimentalPerformance.VERBOSE)
            ExperimentalPerformance.LOGGER.info("Generating " + fullName + " with target " + className);
        this.mixinClassModifiers.put('/' + fullName + CLASS_EXT, makeMixinBlob(fullName, className));
    }

    public List<String> onGetMixins() {
        return new ArrayList<>(this.loadAsMixins);
    }

    // Runs on MixinPlugin `preApply()`
    public void onPreApply(String targetClassName, ClassNode targetClass, String mixinClassName) {
        Set<Consumer<ClassNode>> classChanges = this.preTransformers.get(formatPathSlash(targetClassName));
        if (classChanges == null) return;
        for (Consumer<ClassNode> changes : classChanges)
            changes.accept(targetClass);
    }

    // Runs on MixinPlugin `postApply()`
    public void onPostApply(String targetClassName, ClassNode targetClass, String mixinClassName) {
        Set<Consumer<ClassNode>> classChanges = this.postTransformers.get(formatPathSlash(targetClassName));
        if (classChanges == null) return;
        for (Consumer<ClassNode> changes : classChanges)
            changes.accept(targetClass);
        targetClass.interfaces.remove(formatPathSlash(mixinClassName));
    }

    private Consumer<URL> getAddURLMethod() {
        ClassLoader classLoader = TransformationManager.class.getClassLoader();
        for (Method addUrlMethod : classLoader.getClass().getDeclaredMethods()) {
            if (addUrlMethod.getReturnType() == Void.TYPE && addUrlMethod.getParameterCount() == 1 &&
                    addUrlMethod.getParameterTypes()[0] == URL.class) {
                try {
                    addUrlMethod.setAccessible(true);
                    MethodHandle handle = MethodHandles.lookup().unreflect(addUrlMethod);
                    return url -> {
                        try {
                            handle.invoke(classLoader, url);
                        } catch (Throwable t) {
                            throw new RuntimeException("Unknown error while attempting to invoke Url handle",t);
                        }
                    };
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Unable to find handle for " + addUrlMethod, e);
                }
            }
        }
        throw new IllegalStateException("Unable to find addUrl method in " + classLoader);
    }

    private static byte[] makeMixinBlob(String name, String targetClassName) {
        ClassWriter writer = new ClassWriter(0);
        writer.visit(
                Opcodes.V17,
                Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT | Opcodes.ACC_INTERFACE,
                name,
                null,
                OBJECT_PATH,
                null
        );
        AnnotationVisitor mixinAnnotation = writer.visitAnnotation(MIXIN_ANNOTATION_DESCRIPTOR, false);
        AnnotationVisitor valueAnnotation = mixinAnnotation.visitArray("value");
        valueAnnotation.visit(null, asType(targetClassName));
        valueAnnotation.visitEnd();
        mixinAnnotation.visitEnd();
        writer.visitEnd();
        return writer.toByteArray();
    }

    private static <T extends IExtension> T getExtension(Class<T> extensionClass) {
        Object transformer = MixinEnvironment.getCurrentEnvironment().getActiveTransformer();
        if (transformer == null)
            throw new IllegalStateException("No transformer was found for current Mixin environment");
        try {
            for (Field field : transformer.getClass().getDeclaredFields()) {
                if (field.getType().equals(Extensions.class)) { // Find extensions, then access it to get extension
                    field.setAccessible(true);
                    return ((Extensions) field.get(transformer)).getExtension(extensionClass);
                }
            }
            throw new NoSuchFieldError("Unable to find extensions field for current Mixin environment transformer!");
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Critical error happened while attempting to get extension", e);
        }
    }
}
