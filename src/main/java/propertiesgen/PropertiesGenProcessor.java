package propertiesgen;

import org.apache.maven.model.Model;
import org.apache.maven.model.building.*;
import org.apache.maven.model.interpolation.StringSearchModelInterpolator;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.*;
import java.io.*;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static javax.tools.Diagnostic.Kind.*;

@SupportedAnnotationTypes("propertiesgen.ProjectProperties")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class PropertiesGenProcessor extends AbstractProcessor {


    private static final String OPTION_GENERATED_CLASS = "generatedClass";
    private static final String OPTION_PACKAGE = "generatedPackage";

    private static final Map<Class<?>,String> TYPE_PARSERS = Map.ofEntries(
        Map.entry(String.class, "%s"),
        Map.entry(byte.class, "Byte.parseByte(%s)"),
        Map.entry(Byte.class, "Byte.valueOf(%s)"),
        Map.entry(short.class, "Short.parseShort(%s)"),
        Map.entry(Short.class, "Short.valueOf(%s)"),
        Map.entry(int.class, "Integer.parseInt(%s)"),
        Map.entry(Integer.class, "Integer.valueOf(%s)"),
        Map.entry(long.class, "Long.parseLong(%s)"),
        Map.entry(Long.class, "Long.valueOf(%s)"),
        Map.entry(float.class, "Float.parseFloat(%s)"),
        Map.entry(Float.class, "Float.valueOf(%s)"),
        Map.entry(double.class, "Double.parseDouble(%s)"),
        Map.entry(Double.class, "Double.valueOf(%s)")
    );


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            for (Element element : roundEnv.getElementsAnnotatedWith(ProjectProperties.class)) {
                log(NOTE,element,"Processing Maven Properties");
                if (element.getKind() != ElementKind.INTERFACE) {
                    log(ERROR,element,"annotation {} only accepted on interfaces", ProjectProperties.class);
                    continue;
                }
                Map<String, String> properties = collectProperties(element);
                if (properties.isEmpty()) {
                    continue;
                }
                log(NOTE,element,"properties :: "+properties);
                Map<String, Class<?>> methodTypes = collectMethodTypes(element);
                Map<String, String> effectiveProperties = interpolateMavenProperties(properties);
                log(NOTE,element,"interpolation :: "+effectiveProperties);
                createStaticClass((TypeElement) element,properties,effectiveProperties,methodTypes);
            }
        } catch (ModelBuildingException | IOException e) {
            log(ERROR, e.toString());
            e.printStackTrace();
            return false;
        }
        return true;
    }


    private Map<String, String> collectProperties(Element element) {
        Map<String,String> properties = new LinkedHashMap<>();
        for (Element child : element.getEnclosedElements()) {
            var property = child.getAnnotation(ProjectProperty.class);
            if (property == null) {
                continue;
            }
            // ProjectProperty only can be used in methods
            ExecutableElement method = (ExecutableElement) child;
            if (!validateMethod(method)) {
                continue;
            }
            properties.put(method.getSimpleName().toString(),property.value());
        }
        return properties;
    }


    private Map<String, Class<?>> collectMethodTypes(Element element) {
        Map<String,Class<?>> methodTypes = new LinkedHashMap<>();
        for (Element child : element.getEnclosedElements()) {
            var property = child.getAnnotation(ProjectProperty.class);
            if (property == null) {
                continue;
            }
            ExecutableElement method = (ExecutableElement) child;
            String methodName = normalize(method.getSimpleName().toString());
            Class<?> methodType = getClass(method);
            if (methodType != null) {
                methodTypes.put(methodName, methodType);
            }
        }
        return methodTypes;
    }


    private boolean validateMethod(ExecutableElement method) {
        boolean valid = true;
        if (method.isDefault() || method.getModifiers().contains(Modifier.STATIC)) {
            log(
                WARNING,
                method,
                "annotation {} ignored: only usable on non-default, non-static methods",
                ProjectProperty.class
            );
            valid = false;
        }
        Class<?> returnType = getClass(method);
        if (returnType == null || !TYPE_PARSERS.containsKey(returnType)) {
            log(
                WARNING,
                method,
                "annotation {} ignored: method return {} by only usable on methods returning one of the following: {}",
                ProjectProperty.class,
                method.getReturnType().toString(),
                TYPE_PARSERS.keySet().stream().map(Class::getSimpleName).collect(toList())
            );
            valid = false;
        }
        if (!method.getParameters().isEmpty()) {
            log(
                WARNING,
                method,
                "annotation {} ignored: only usable on methods with zero arguments",
                ProjectProperty.class
            );
            valid = false;
        }
        return valid;
    }


    public Map<String,String> interpolateMavenProperties(Map<String,String> properties) throws ModelBuildingException {

        File pomFile = new File("pom.xml");
        DefaultModelBuilder modelBuilder = new DefaultModelBuilderFactory().newInstance();
        var result = modelBuilder.buildRawModel(pomFile, ModelBuildingRequest.VALIDATION_LEVEL_STRICT,false);
        if (result.hasErrors()) {
            result.getProblems().forEach(problem -> log(ERROR, problem.getMessage()));
            return Map.of();
        }
        Model rawModel = result.get();
        properties.forEach(rawModel.getProperties()::putIfAbsent);

        ModelBuildingRequest request = new DefaultModelBuildingRequest()
            .setRawModel(rawModel)
            .setProcessPlugins(false)
            .setTwoPhaseBuilding(false)
        ;

        var effectiveProperties = new StringSearchModelInterpolator()
            .interpolateModel(rawModel, new File(""), request, null)
            .getProperties();


        Map<String,String> interpolatedProperties = new LinkedHashMap<>();
        properties.keySet().forEach(property ->
            interpolatedProperties.put(property,effectiveProperties.getProperty(property, property))
        );
        return interpolatedProperties;
    }


    private void createStaticClass(
        TypeElement element,
        Map<String, String> methods,
        Map<String, String> effectiveProperties,
        Map<String, Class<?>> methodTypes
    ) throws IOException {

        TypeMirror type = element.asType();
        String namingPattern = processingEnv.getOptions().getOrDefault(OPTION_GENERATED_CLASS, "Static%s");
        String packageName = processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();
        packageName = processingEnv.getOptions().getOrDefault(OPTION_PACKAGE, packageName);
        String interfaceName = element.getSimpleName().toString();
        String className = String.format(namingPattern, interfaceName);
        String qualifiedClassName = packageName + "." + className;
        log(NOTE,element,"Creating implementation "+qualifiedClassName);
        JavaFileObject file = this.processingEnv.getFiler().createSourceFile(qualifiedClassName, element);

        try (var writer = file.openWriter()) {
            writer.append(String.format("package %s;\n", packageName));
            writer.append("\n");
            writer.append(String.format(
                "public final class %s implements %s {\n",
                className, interfaceName
            ));
            writer.append("\n");
            for (var property : effectiveProperties.entrySet()) {
                String propertyConstant = normalize(property.getKey());
                Class<?> methodType = methodTypes.get(propertyConstant);
                String mavenValue = "\""+property.getValue()+"\"";
                String parser = String.format(TYPE_PARSERS.get(methodType), mavenValue);
                writer.append(String.format(
                    "    public static final %s %s = %s;\n",
                    methodType.getSimpleName(),
                    propertyConstant,
                    parser
                ));
            }
            writer.append("\n\n");
            for (var property : methods.entrySet()) {
                String javaMethod = property.getKey();
                String propertyConstant = normalize(javaMethod);
                Class<?> methodType = methodTypes.get(propertyConstant);
                writer.append("    @Override\n");
                writer.append(String.format(
                    "    public %s %s() {\n",
                    methodType.getSimpleName(),
                    javaMethod
                ));
                writer.append(String.format("        return %s;\n", propertyConstant));
                writer.append("    }\n");
                writer.append("\n");
            }
            writer.append("}\n");
        }
    }


    private String normalize(String mavenProperty) {
        return mavenProperty.replace('.','_').replaceAll("[A-Z]","_$0").toUpperCase();
    }


    private Class<?> getClass(ExecutableElement method) {
        String type = method.getReturnType().toString();
        Class<?> methodType;
        try {
            methodType = Class.forName(type);
        } catch (ClassNotFoundException e) {
            switch (type) {
                case "byte" : methodType = byte.class; break;
                case "short" : methodType = short.class; break;
                case "int" : methodType = int.class; break;
                case "long" : methodType = long.class; break;
                case "float" : methodType = float.class; break;
                case "double" : methodType = double.class; break;
                default: methodType = null;
            }
        }
        return methodType;
    }


    private void log(Diagnostic.Kind kind, String message, Object... messageArgs) {
        processingEnv.getMessager().printMessage(
            kind,
            "[propertiesgen] :: " + String.format(message.replace("{}", "%s"), messageArgs)
        );
    }


    private void log(Diagnostic.Kind kind, Element element, String message, Object... messageArgs) {
        processingEnv.getMessager().printMessage(
            kind,
            "[propertiesgen] at " + element.getSimpleName() + " :: " +
                String.format(message.replace("{}", "%s"), messageArgs)
        );
    }


    

}
