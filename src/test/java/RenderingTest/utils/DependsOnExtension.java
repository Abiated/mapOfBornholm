//package RenderingTest.utils;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Optional;
//
//import org.junit.jupiter.api.Assumptions;
//import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
//import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
//import org.junit.jupiter.api.extension.ExtensionContext;
//
//public class DependsOnExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {
//
//    private static final Map<String, Boolean> results = new HashMap<>();
//
//    @Override
//    public void beforeTestExecution(ExtensionContext context) {
//        DependsOn dependsOn = context.getTestMethod()
//                .flatMap(m -> Optional.ofNullable(m.getAnnotation(DependsOn.class)))
//                .orElse(null);
//
//        if (dependsOn != null) {
//            //Determine if prerequisites are satisfied
//            boolean dependenciesPassed = results.getOrDefault(dependsOn.value(), false);
//            Assumptions.assumeTrue(dependenciesPassed, () -> "Skipping " + context.getDisplayName() + " because " + dependsOn.value() + "failed");
//        }
//    }
//
//    @Override
//    public void afterTestExecution(ExtensionContext context) {
//        boolean testPassed = context.getExecutionException().isEmpty();
//        results.put(context.getTestMethod().get().getName(), testPassed);
//    }
//}
