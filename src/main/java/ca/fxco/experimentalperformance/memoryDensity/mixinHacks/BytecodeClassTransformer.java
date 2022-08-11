package ca.fxco.experimentalperformance.memoryDensity.mixinHacks;

public interface BytecodeClassTransformer {
    byte[] transform(String className, byte[] buffer);

    default BytecodeClassTransformer andThen(BytecodeClassTransformer transformer) {
        return (s, d) -> transformer.transform(s, this.transform(s, d));
    }
}
