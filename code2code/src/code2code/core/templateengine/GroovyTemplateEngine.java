package code2code.core.templateengine;

import groovy.lang.Writable;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;

import java.io.File;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;

import code2code.core.generator.Generator;
import code2code.utils.Console;
import code2code.utils.FileUtils;

public class GroovyTemplateEngine extends AbstractTemplateEngine implements
	TemplateEngine {
    private static final String CLASSPATH_PARAM_NAME = "classpath";

    @Override
    public List<String> getKnownExtensions() {
	return Arrays.asList(new String[] { "groovy" });
    }

    public String processTemplate(Generator generator, String templateName,
	    Map<String, String> context, IProject project) throws Exception {

	String[] generatorClasspath = generator.getGeneratorClassPath();
	String[] projectClasspath = JavaRuntime
		.computeDefaultRuntimeClassPath(JavaCore.create(project));

	String[] classpath = new String[generatorClasspath.length
		+ projectClasspath.length];
	System.arraycopy(generatorClasspath, 0, classpath, 0,
		generatorClasspath.length);
	System.arraycopy(projectClasspath, 0, classpath,
		generatorClasspath.length, projectClasspath.length);

	URL[] urls = new URL[classpath.length];
	StringBuilder classpathParam = new StringBuilder();
	for (int i = 0; i < classpath.length; i++) {
	    File file = new File(classpath[i]);
	    urls[i] = file.toURI().toURL();

	    if (classpathParam.length() > 0) {
		classpathParam.append(File.pathSeparatorChar);
	    }
	    classpathParam.append(file.getAbsolutePath());
	}

	SimpleTemplateEngine engine = new SimpleTemplateEngine(
		new URLClassLoader(urls, getClass().getClassLoader()));

	Template template = engine.createTemplate(FileUtils.read(generator
		.getGeneratorFolder().getFile(templateName).getContents()));

	Map<String, String> engineContext = new HashMap<String, String>(context);
	String oldValue = engineContext.put(CLASSPATH_PARAM_NAME,
		classpathParam.toString());
	if (oldValue != null) {
	    Console.write("Warning: override parameter " + CLASSPATH_PARAM_NAME
		    + oldValue);
	}

	Writable writable = template.make(engineContext);

	StringWriter stringWriter = new StringWriter();
	writable.writeTo(stringWriter);

	return stringWriter.toString();
    }

    @Override
    public String processString(Generator generator, String templateContent,
	    Map<String, String> context) throws Exception {

	SimpleTemplateEngine engine = new SimpleTemplateEngine();

	Template template = engine.createTemplate(templateContent);

	Writable writable = template.make(context);

	StringWriter stringWriter = new StringWriter();
	writable.writeTo(stringWriter);

	return stringWriter.toString();
    }

    public String escape(String contents) {

	String escaped = contents.replace("\\", "\\\\").replace("$", "\\$")
		.replaceAll("(?<=<)%|%(?=>)", "<%=\"%\"%>");

	return escaped;
    }

}
