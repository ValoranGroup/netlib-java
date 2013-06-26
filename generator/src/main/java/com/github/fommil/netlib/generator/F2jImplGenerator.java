package com.github.fommil.netlib.generator;

import com.google.common.collect.Lists;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.stringtemplate.v4.ST;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Generates the F2J implementation of a netlib-java interface for the given methods.
 */
@Mojo(
        name = "f2j",
        defaultPhase = LifecyclePhase.GENERATE_SOURCES,
        requiresDependencyResolution = ResolutionScope.COMPILE
)
public class F2jImplGenerator extends AbstractJavaGenerator {

    /**
     * The interface that we are implementing.
     */
    @Parameter(required = true)
    protected String implementing;

    @Override
    protected String generate(List<Method> methods) throws Exception {
        List<String> members = Lists.newArrayList();
        for (Method method : methods) {
            ST m = jTemplates.getInstanceOf("f2jImplMethod");
            m.add("returns", method.getReturnType());
            m.add("method", method.getName());
            m.add("paramTypes", getNetlibJavaParameterTypes(method));
            m.add("paramNames", getNetlibJavaParameterNames(method));
            m.add("impl", method.getDeclaringClass().getCanonicalName() + "." + method.getName());
            m.add("calls", getF2jJavaParameters(method));
            if (method.getReturnType().equals(Void.TYPE))
                m.add("return", "");
            members.add(m.render());
        }

        ST t = jTemplates.getInstanceOf("implClass");
        t.add("package", getTargetPackage());
        t.add("name", getTargetClassName());
        t.add("members", members);
        t.add("docs", "Generated by {@code " + getClass().getSimpleName() + "} from {@code " + scan + "}");
        t.add("parent", implementing);

        return t.render();
    }

}