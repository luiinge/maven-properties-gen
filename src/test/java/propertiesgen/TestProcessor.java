package propertiesgen;

import com.google.testing.compile.*;
import com.google.testing.compile.Compiler;
import org.junit.Test;

import java.io.IOException;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class TestProcessor {

    @Test
    public void testProcessor() throws IOException {
        Compilation compilation =
            Compiler.javac()
            .withProcessors(new PropertiesGenProcessor())
            .compile(JavaFileObjects.forResource("TestProperties.java"));
        System.out.println(compilation.status());
        System.out.println(compilation.diagnostics().stream().map(Object::toString).collect(Collectors.joining("\n")));
        assertTrue(compilation.status() == Compilation.Status.SUCCESS);
        var generated = compilation.generatedSourceFile("propertiesgen.StaticTestProperties");
        assertTrue(generated.isPresent());
        assertEquals(
            generated.get().getCharContent(true),
            JavaFileObjects.forResource("StaticTestProperties.java").getCharContent(true)
        );
    }
}
