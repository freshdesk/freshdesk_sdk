package com.freshdesk.sdk.plug;

import com.freshdesk.sdk.FAException;
import com.freshdesk.sdk.ManifestContents;
import com.freshdesk.sdk.TemplateRendererSdk;
import com.freshdesk.sdk.plug.run.ScssCompiler;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import org.wiztools.commons.FileUtil;

/**
 *
 * @author raghav
 */
public class PlugContentUnifier {
    
    private final File htmlFile;
    private final File cssFile;
    private final File jsFile;
    private final ManifestContents manifest;
    private final File prjDir;
    private final File workDir;
    private final Map<String, Object> renderParams;
    
    public PlugContentUnifier(File appDir, ManifestContents mf, Map<String, Object> renderParams) {
        if(appDir.isDirectory() && appDir.canRead()) {
            htmlFile = new File(appDir, PlugFile.toString(PlugFile.HTML));
            cssFile = new File(appDir, PlugFile.toString(PlugFile.CSS));
            jsFile = new File(appDir, PlugFile.toString(PlugFile.JS));
            if(!(htmlFile.isFile() && htmlFile.canRead()
                && cssFile.isFile() && cssFile.canRead()
                && jsFile.isFile() && jsFile.canRead())) {
                throw new FAException("Files missing");
            }
            prjDir = new File(".");
            workDir = new File(prjDir, "work");
            this.manifest = mf;
            this.renderParams = renderParams;
        }
        else {
            throw new FAException("Lib Directory corrupt/ not readable.");
        }
    }
    
    private String getFileContent(File f) throws IOException {
        return FileUtil.getContentAsString(f, manifest.getCharset());
    }
    
    public String getPlugResponse() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(getCssContent())
                .append("\n")
                .append(getHtmlContent())
                .append("\n")
                .append(getJsContent());
        return sb.toString();
    }
    
    private String getCssContent() throws IOException {
        
        StringBuilder out = new StringBuilder();
        
        if(!workDir.isDirectory()) {
            workDir.mkdirs();
        }
        
        // Read from scss file for liquid parsing
        File tmpFile = File.createTempFile("fa_", "_app.scss", workDir);
        OutputStream os = new FileOutputStream(tmpFile);
        String scssContent = getFileContent(cssFile);
        
        // Write to a temporary file
        TemplateRendererSdk renderer = new TemplateRendererSdk().registerFilter(new FilterAssetURLPlug(prjDir));
        String liquidParsedScss = renderer.renderString(scssContent, renderParams);
        os.write(liquidParsedScss.getBytes());
        os.close();
        
        // Read from temporary file for Scss Compilation
        File outputFile = new File(workDir, "app.css");
        ScssCompiler compiler = new ScssCompiler(tmpFile, outputFile);
        compiler.compile();

        // Delete temporary file
        tmpFile.delete();
        
        // Read from app.css in workDir for final output
        String finalCss = getFileContent(outputFile);
        
        // Append style tag and return
        out.append("<style>\n").append(finalCss).append("</style>\n");
        
        return out.toString();
    }
    
    private String getHtmlContent() throws IOException {
        return getFileContent(htmlFile);
    }
    
    private String getJsContent() throws IOException {
        StringBuilder sb = new StringBuilder();
        final String jsContents = getFileContent(jsFile);
        sb.append("<script type='text/javascript'>\n")
                .append("Freshapp.run(function() { \n var fa_prefix = ")
                .append(jsContents)
                .append("\nfa_prefix.initialize(); \n});\n")
                .append("</script>");
        return sb.toString();
    }
}