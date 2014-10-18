package code2code.core.templateengine;

import groovy.lang.Writable;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;

import java.io.File;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;

import code2code.core.generator.Generator;
import code2code.utils.FileUtils;

public class GroovyTemplateEngine extends AbstractTemplateEngine  implements TemplateEngine {

	@Override
	public List<String> getKnownExtensions() {
		return Arrays.asList(new String[]{"groovy"});
	}

	public String processTemplate(Generator generator, String templateName, Map<String, String> context, IProject project) throws Exception {
	    	
	    	String[] defaultRuntimeClasspath = JavaRuntime.computeDefaultRuntimeClassPath(JavaCore.create(project));
	        URL[] urls = new URL[defaultRuntimeClasspath.length];
	        for (int i = 0; i < defaultRuntimeClasspath.length; i++)
	        {
	          File file = new File(defaultRuntimeClasspath[i]);
	          urls[i] = file.toURI().toURL();
	        }
	    
		SimpleTemplateEngine engine = new SimpleTemplateEngine(new URLClassLoader(urls, getClass().getClassLoader()));
		
		Template template = engine.createTemplate(FileUtils.read(generator.getGeneratorFolder().getFile(templateName).getContents()));

		Writable writable = template.make(context);
		
		StringWriter stringWriter = new StringWriter();
		writable.writeTo(stringWriter);
		
		return stringWriter.toString();
	}
	

	@Override
	public String processString(Generator generator, String templateContent, Map<String, String> context) throws Exception {

		SimpleTemplateEngine engine = new SimpleTemplateEngine();
		
		Template template = engine.createTemplate(templateContent);

		Writable writable = template.make(context);
		
		StringWriter stringWriter = new StringWriter();
		writable.writeTo(stringWriter);
		
		return stringWriter.toString();
	}


	public String escape(String contents) {
		
		String escaped = contents.replace("\\", "\\\\").
			replace("$", "\\$")
			.replaceAll("(?<=<)%|%(?=>)", "<%=\"%\"%>");
		
		return escaped;
	}

	

}
