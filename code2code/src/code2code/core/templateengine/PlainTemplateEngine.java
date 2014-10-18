package code2code.core.templateengine;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;

import code2code.core.generator.Generator;
import code2code.utils.FileUtils;


public class PlainTemplateEngine extends AbstractTemplateEngine implements TemplateEngine {

	
	@Override
	public List<String> getKnownExtensions() {
		return Arrays.asList(new String[]{"txt", "properties", "params"});
	}

	@Override
	public Map<String, String> processMap(Generator generator, Map<String, String> params, Map<String, String> context) {
		return params;
	}

	@Override
	public String processString(Generator generator, String content, Map<String, String> context) throws Exception {
		return content;
	}

	public String processTemplate(Generator generator, String templateName, Map<String, String> context, IProject project) throws Exception {
		return FileUtils.read(generator.getGeneratorFolder().getFile(templateName).getContents());
	}

	public String escape(String contents) {
		return contents;
	}


}
