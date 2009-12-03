package hudson.plugins.collabnet.browser;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Class to represent the override_auth data.  
 * Needed for the DataBoundConstructor to work properly.
 */
public class OverrideAuth {
    public String collabneturl;
    public String username;
    public String password;
    
    @DataBoundConstructor
    public OverrideAuth(String collabneturl, String username, 
                        String password) {
        this.collabneturl = collabneturl;
        this.username = username;
        this.password = password;
    }
}
