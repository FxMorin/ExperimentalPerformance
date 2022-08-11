package ca.fxco.experimentalperformance.memoryDensity.mixinHacks;

import java.lang.instrument.Instrumentation;

class InstrumentationAgent {
    private static Instrumentation instrumentation;

    public static void agentmain(final String argument, final Instrumentation instrumentation) {
        InstrumentationAgent.instrumentation = instrumentation;
    }

    static {
        System.out.println("Agent has been Attached");
    }
}
