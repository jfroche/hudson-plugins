package hudson.plugins.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.remoting.Callable;
import hudson.tasks.Builder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 *  A Builder which executes system Groovy script in Hudson JVM (similar to HUDSON_URL/script).
 * 
 * @author dvrzalik
 */
public class SystemGroovy extends AbstractGroovy {

    //initial variable bindings
    String bindings;
    
    @DataBoundConstructor
    public SystemGroovy(ScriptSource scriptSource, String bindings) {
        super(scriptSource);
        this.bindings = bindings;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        Hudson.getInstance().checkPermission(Hudson.ADMINISTER);
        
        GroovyShell shell = new GroovyShell(new Binding(parseProperties(bindings)));

        shell.setVariable("out", listener.getLogger());
        Object output = shell.evaluate(getScriptSource().getScriptStream(build.getProject().getWorkspace()));
        if (output instanceof Boolean) {
            return (Boolean) output;
        } else {
            if (output != null) {
                listener.getLogger().println("Script returned: " + output);
            }
            
            if (output instanceof Number) {
                return ((Number) output).intValue() == 0;
            }
        }
        //No output. Suppose success.
        return true;
    }
    
    public Descriptor<Builder> getDescriptor() {
        return DESCRIPTOR;
    }   
    
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static final class DescriptorImpl extends AbstractGroovyDescriptor {

        DescriptorImpl() {
            super(SystemGroovy.class);
            load();
        }
        
        @Override
        public String getDisplayName() {
            return "Execute system Groovy script";
        }
        
         @Override
        public Builder newInstance(StaplerRequest req, JSONObject data) throws FormException {
            ScriptSource source = getScriptSource(req, data);
            String binds = data.getString("bindings");
            return new SystemGroovy(source, binds);
         }

        @Override
        public String getHelpFile() {
            return "/plugin/groovy/systemscript-projectconfig.html";
        }
    }

    //---- Backward compatibility -------- //
    
    public enum BuilderType { COMMAND,FILE }
    
    private String command;
    
    private Object readResolve() {
        if(command != null) {
            scriptSource = new StringScriptSource(command);
            command = null;
        }

        return this;
    }
    
    public String getCommand() {
        return command;
    }

    public String getBindings() {
        return bindings;
    }
}
