package hudson.plugins.checkstyle.dashboard;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.plugins.analysis.core.AbstractProjectAction;
import hudson.plugins.analysis.dashboard.AbstractWarningsGraphPortlet;
import hudson.plugins.checkstyle.CheckStyleProjectAction;
import hudson.plugins.checkstyle.Messages;
import hudson.plugins.view.dashboard.DashboardPortlet;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * A dashboard that shows a table with the number of warnings in a job.
 *
 * @author Ulli Hafner
 */
public class WarningsGraphPortlet extends AbstractWarningsGraphPortlet {
    /**
     * Creates a new instance of {@link WarningsGraphPortlet}.
     *
     * @param name
     *            the name of the portlet
     */
    @DataBoundConstructor
    public WarningsGraphPortlet(final String name) {
        super(name);
    }

    /** {@inheritDoc} */
    @Override
    protected Class<? extends AbstractProjectAction<?>> getAction() {
        return CheckStyleProjectAction.class;
    }

    /** {@inheritDoc} */
    @Override
    protected String getPluginName() {
        return "checkstyle";
    }

    /**
     * Extension point registration.
     *
     * @author Ulli Hafner
     */
    public static class WarningsGraphDescriptor extends Descriptor<DashboardPortlet> {
        /**
         * Creates a new descriptor if the dashboard-view plug-in is installed.
         *
         * @return the descriptor or <code>null</code> if the dashboard view is not installed
         */
        @Extension
        public static WarningsGraphDescriptor newInstance() {
            if (Hudson.getInstance().getPlugin("dashboard-view") != null) {
                return new WarningsGraphDescriptor();
            }
            return null;
        }

        @Override
        public String getDisplayName() {
            return Messages.Portlet_WarningsTable();
        }
    }
}
