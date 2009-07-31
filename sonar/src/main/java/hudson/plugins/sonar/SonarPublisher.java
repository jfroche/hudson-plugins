package hudson.plugins.sonar;

import hudson.CopyOnWrite;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.maven.MavenModuleSet;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Hudson;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.model.Cause.UpstreamCause;
import hudson.plugins.sonar.template.SimpleTemplate;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Maven;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.tasks.Maven.MavenInstallation;
import hudson.triggers.SCMTrigger.SCMTriggerCause;
import hudson.triggers.TimerTrigger.TimerTriggerCause;
import hudson.util.ArgumentListBuilder;
import hudson.util.FormValidation;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

public class SonarPublisher extends Notifier {
  private final String jobAdditionalProperties;
  private final String installationName;
  private final String mavenInstallationName;
  private final boolean useSonarLight;
  private final String groupId;
  private final String artifactId;
  private final String projectName;
  private final String projectVersion;
  private final String projectDescription;
  private final String javaVersion;
  private final String projectSrcDir;
  private final String projectSrcEncoding;
  private final String projectBinDir;
  private final String mavenOpts;
  private final boolean reuseReports;
  private final String surefireReportsPath;
  private final String coberturaReportPath;
  private final String cloverReportPath;
  private final boolean scmBuilds;
  private boolean timerBuilds = true;
  private final boolean snapshotDependencyBuilds;
  private final boolean skipIfBuildFails;
  
  @Deprecated
  private Boolean skipOnScm;

  @DataBoundConstructor
  public SonarPublisher(String installationName, String jobAdditionalProperties, boolean useSonarLight,
                        String groupId, String artifactId, String projectName, String projectVersion, String projectSrcDir, String javaVersion,
                        String projectDescription, String mavenOpts, String mavenInstallationName, boolean snapshotDependencyBuilds, boolean scmBuilds, boolean timerBuilds, boolean skipIfBuildFails, String projectBinDir,
                        boolean reuseReports, String coberturaReportPath, String surefireReportsPath, String cloverReportPath, String projectSrcEncoding) {
    this.jobAdditionalProperties = jobAdditionalProperties;
    this.installationName = installationName;
    this.useSonarLight = useSonarLight;
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.projectName = projectName;
    this.projectVersion = projectVersion;
    this.javaVersion = javaVersion;
    this.projectSrcDir = projectSrcDir;
    this.projectDescription = projectDescription;
    this.mavenOpts = mavenOpts;
    this.scmBuilds = scmBuilds;
    this.timerBuilds = timerBuilds;
    this.snapshotDependencyBuilds = snapshotDependencyBuilds;
    this.mavenInstallationName = mavenInstallationName;
    this.skipIfBuildFails = skipIfBuildFails;
    this.projectBinDir = projectBinDir;
    this.reuseReports = reuseReports;
    this.surefireReportsPath = surefireReportsPath;
    this.coberturaReportPath = coberturaReportPath;
    this.cloverReportPath = cloverReportPath;
    this.projectSrcEncoding = projectSrcEncoding;
  }
  
  @Deprecated
  public Boolean getSkipOnScm() {
    return skipOnScm;
  }

  public String getJobAdditionalProperties() {
    return StringUtils.trimToEmpty(jobAdditionalProperties);
  }

  public String getInstallationName() {
    return installationName;
  }

  public boolean isUseSonarLight() {
    return useSonarLight;
  }

  public boolean isSkipIfBuildFails() {
    return skipIfBuildFails;
  }

  public boolean isTimerBuilds() {
    return timerBuilds;
  }
  
  public boolean isScmBuilds() {
    return scmBuilds;
  }
  
  public boolean isSnapshotDependencyBuilds() {
    return snapshotDependencyBuilds;
  }

  public String getGroupId() {
    return groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public String getProjectName() {
    return projectName;
  }

  public String getProjectVersion() {
    return StringUtils.trimToEmpty(projectVersion);
  }

  public String getJavaVersion() {
    return StringUtils.trimToEmpty(javaVersion);
  }

  public String getProjectSrcDir() {
    return StringUtils.trimToEmpty(projectSrcDir);
  }
  
  public String getProjectSrcEncoding() {
    return StringUtils.trimToEmpty(projectSrcEncoding);
  }

  public String getProjectBinDir() {
    return StringUtils.trimToEmpty(projectBinDir);
  }

  public String getProjectDescription() {
    return StringUtils.trimToEmpty(projectDescription);
  }

  public String getMavenOpts() {
    return mavenOpts;
  }

  public boolean isReuseReports() {
    return reuseReports;
  }

  public String getSurefireReportsPath() {
    return StringUtils.trimToEmpty(surefireReportsPath);
  }

  public String getCoberturaReportPath() {
    return StringUtils.trimToEmpty(coberturaReportPath);
  }

  public String getCloverReportPath() {
    return StringUtils.trimToEmpty(cloverReportPath);
  }

  public static boolean isMavenBuilder(AbstractProject currentProject) {
    return (currentProject instanceof MavenModuleSet);
  }

  public List<MavenInstallation> getMavenInstallations() {
    return Arrays.asList(Hudson.getInstance().getDescriptorByType(Maven.DescriptorImpl.class).getInstallations());
  }

  public MavenInstallation getMavenInstallation() {
    List<MavenInstallation> installations = getMavenInstallations();
    if (StringUtils.isEmpty(mavenInstallationName) && !installations.isEmpty()) {
      return installations.get(0);
    }
    for (MavenInstallation install : installations) {
      if (StringUtils.equals(mavenInstallationName, install.getName())) {
        return install;
      }
    }
    return null;
  }

  public SonarInstallation getInstallation() {
    DescriptorImpl sonarDescriptor = Hudson.getInstance().getDescriptorByType(DescriptorImpl.class);
    if (StringUtils.isEmpty(installationName) && sonarDescriptor.getInstallations().length > 0) {
      return sonarDescriptor.getInstallations()[0];
    }
    for (SonarInstallation si : sonarDescriptor.getInstallations()) {
      if (StringUtils.equals(installationName, si.getName())) {
        return si;
      }
    }
    return null;
  }

  @Override
  public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
    SonarInstallation sonarInstallation = getInstallation();
    String skipLaunchMsg = null;
    if (skipIfBuildFails && build.getResult().isWorseThan(Result.SUCCESS)) {
      skipLaunchMsg = Messages.SonarPublisher_BadBuildStatus(build.getResult().toString());
    } else if (sonarInstallation == null) {
      skipLaunchMsg = Messages.SonarPublisher_NoInstallation(installationName, Hudson.getInstance().getDescriptorByType(Maven.DescriptorImpl.class).getInstallations().length);
    } else if (sonarInstallation.isDisabled()) {
      skipLaunchMsg = Messages.SonarPublisher_InstallDisabled(sonarInstallation.getName());
    } else if (!isScmBuilds() && isTrigger(build, SCMTriggerCause.class)) {
      skipLaunchMsg = Messages.SonarPublisher_SCMBuild();
    } else if (!isTimerBuilds() && isTrigger(build, TimerTriggerCause.class)) {
      skipLaunchMsg = Messages.SonarPublisher_TimerBuild();
    } else if (!isSnapshotDependencyBuilds() && isTrigger(build, UpstreamCause.class)) {
      skipLaunchMsg = Messages.SonarPublisher_SnapshotDepBuild();
    }

    if (skipLaunchMsg != null) {
      listener.getLogger().println(skipLaunchMsg);
      return true;
    }
    boolean sonarSuccess = executeSonar(build, launcher, listener, sonarInstallation);
    if (!sonarSuccess) {
      // returning false has no effect on the global build status so need to do it manually
      build.setResult(Result.FAILURE);
    }
    return sonarSuccess;
  }

  private boolean isTrigger(AbstractBuild<?, ?> build, Class<? extends hudson.model.Cause> trigger) {
    CauseAction buildCause = build.getAction(CauseAction.class);
    List<Cause> buildCauses = buildCause.getCauses();
    for (Cause cause : buildCauses) {
      if (cause.getClass().equals(trigger)) {
        return true;
      }
    }
    return false;
  }

  private Maven.MavenInstallation getMavenInstallationForSonar(AbstractBuild<?, ?> build, TaskListener listener) throws IOException, InterruptedException {
    Maven.MavenInstallation mavenInstallation = null;
    if (build.getProject() instanceof Maven.ProjectWithMaven) {
      mavenInstallation = ((Maven.ProjectWithMaven) build.getProject()).inferMavenInstallation();
    }
    if (mavenInstallation == null) {
      mavenInstallation = getMavenInstallation();
    }
    return mavenInstallation != null ? mavenInstallation.forNode(build.getBuiltOn(), listener) : mavenInstallation;
  }

  private MavenModuleSet getMavenProject(AbstractBuild build) {
    return (build.getProject() instanceof MavenModuleSet) ? (MavenModuleSet) build.getProject() : null;
  }

  private boolean executeSonar(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener, SonarInstallation sonarInstallation) {
    try {
      Maven.MavenInstallation mavenInstallation = getMavenInstallationForSonar(build, listener);
      FilePath root = build.getProject().getModuleRoot();
      MavenModuleSet mavenModuleProject = getMavenProject(build);
      String pomName = mavenModuleProject != null ? mavenModuleProject.getRootPOM() : "pom.xml";
      if (useSonarLight) {
        generatePomForNonMavenProject(root);
        pomName = "sonar-pom.xml";
      }

      String executable = buildExecName(launcher, mavenInstallation, listener.getLogger());

      Launcher.ProcStarter starter = launcher.launch();
      starter.cmds(buildCommand(launcher, listener, build, sonarInstallation, executable, pomName, mavenModuleProject));
      starter.envs(getMavenEnvironmentVars(listener, build, mavenInstallation, sonarInstallation));
      starter.pwd(root);
      starter.stderr(listener.getLogger());
      starter.stdout(listener.getLogger());
      return starter.join() == 0;
    }
    catch (IOException e) {
      Util.displayIOException(e, listener);
      e.printStackTrace(listener.fatalError("command execution failed"));
      return false;
    }
    catch (InterruptedException e) {
      return false;
    }
  }

  private void generatePomForNonMavenProject(FilePath root) throws IOException, InterruptedException {
    SimpleTemplate pomTemplate = new SimpleTemplate("hudson/plugins/sonar/sonar-light-pom.template");
    pomTemplate.setAttribute("groupId", getGroupId());
    pomTemplate.setAttribute("artifactId", getArtifactId());
    pomTemplate.setAttribute("projectName", getProjectName());
    pomTemplate.setAttribute("projectVersion", StringUtils.isEmpty(getProjectVersion()) ? "1.0" : getProjectVersion());
    pomTemplate.setAttribute("javaVersion", StringUtils.isEmpty(getJavaVersion()) ? "1.5" : getJavaVersion());
   
    List<String> srcDirs = getProjectSrcDirsList();
    boolean multiSources = srcDirs.size() > 1;
    setPomElement("sourceDirectory", srcDirs.get(0), true, pomTemplate);
    pomTemplate.setAttribute("srcDirsPlugin", multiSources ? generateSrcDirsPluginTemplate(srcDirs).toString() : "");

    setPomElement("project.build.sourceEncoding", getProjectSrcEncoding(), true, pomTemplate);
    setPomElement("encoding", getProjectSrcEncoding(), true, pomTemplate);
    setPomElement("description", getProjectDescription(), true, pomTemplate);
    setPomElement("sonar.phase", multiSources ? "generate-sources" : "", true, pomTemplate);
    setPomElement("outputDirectory", getProjectBinDir(), StringUtils.isNotBlank(getProjectBinDir()), pomTemplate);
    setPomElement("sonar.dynamicAnalysis", isReuseReports() ? "reuseReports" : "false", true, pomTemplate);
    setPomElement("sonar.surefire.reportsPath", getSurefireReportsPath(), isReuseReports(), pomTemplate);
    setPomElement("sonar.cobertura.reportPath", getCoberturaReportPath(), isReuseReports(), pomTemplate);
    setPomElement("sonar.clover.reportPath", getCloverReportPath(), isReuseReports(), pomTemplate);

    pomTemplate.write(root);
  }
  
  private SimpleTemplate generateSrcDirsPluginTemplate(List<String> srcDirs) throws IOException, InterruptedException {
    SimpleTemplate srcTemplate = new SimpleTemplate("hudson/plugins/sonar/sonar-multi-sources.template");
    StringBuffer sourcesXml = new StringBuffer();
    for (int i = 1; i < srcDirs.size(); i++) {
      sourcesXml.append("<source><![CDATA[").append(StringUtils.trim(srcDirs.get(i))).append("]]></source>\n");
    }
    srcTemplate.setAttribute("sources", sourcesXml.toString());
    return srcTemplate;
  }
  
  private List<String> getProjectSrcDirsList() {
    String[] dirs = StringUtils.split(getProjectSrcDir(), ',');
    return Arrays.asList(dirs);
  }
  
  private void setPomElement(String tagName, String tagValue, boolean enabled, SimpleTemplate template) {
    String tagContent = enabled && StringUtils.isNotBlank(tagValue) ? "<" + tagName + "><![CDATA[" + tagValue + "]]></" + tagName + ">" : "";
    template.setAttribute(tagName, tagContent);
  }

  private EnvVars getMavenEnvironmentVars(BuildListener listener, AbstractBuild<?, ?> build, Maven.MavenInstallation mavenInstallation, SonarInstallation sonarInstallation) throws IOException, InterruptedException {
    EnvVars environmentVars = build.getEnvironment(listener);
    if (mavenInstallation != null) {
      environmentVars.put("M2_HOME", mavenInstallation.getHome());
    }
    String envMavenOpts = getMavenOpts();
    MavenModuleSet mavenModuleProject = getMavenProject(build);
    if (StringUtils.isEmpty(envMavenOpts) && mavenModuleProject != null && StringUtils.isNotEmpty(mavenModuleProject.getMavenOpts())) {
      envMavenOpts = mavenModuleProject.getMavenOpts();
    }
    if (StringUtils.isNotEmpty(envMavenOpts)) {
      environmentVars.put("MAVEN_OPTS", envMavenOpts);
    }
    return environmentVars;
  }

  private String buildExecName(Launcher launcher, Maven.MavenInstallation mavenInstallation, PrintStream logger) {
    String execName = launcher.isUnix() ? "mvn" : "mvn.bat";
    String separator = launcher.isUnix() ? "/" : "\\";

    String executable = execName;
    if (mavenInstallation != null) {
      String mavenHome = mavenInstallation.getHome();
      executable = mavenHome + separator + "bin" + separator + execName;
    } else {
      logger.println(Messages.SonarPublisher_NoMavenInstallation());
    }
    return executable;
  }

  private ArgumentListBuilder buildCommand(Launcher launcher, BuildListener listener, AbstractBuild<?, ?> build, SonarInstallation sonarInstallation, String executable, String pomName, MavenModuleSet mms) throws IOException, InterruptedException {
    EnvVars envVars = build.getEnvironment(listener);
    ArgumentListBuilder args = new ArgumentListBuilder();
    args.add(executable).add("-e").add("-B");
    args.addTokenized("-f " + pomName);
    args.addKeyValuePairs("-D", build.getBuildVariables());
    addTokenizedAndQuoted(launcher.isUnix(), args, sonarInstallation.getPluginCallArgs(envVars));
    addTokenizedAndQuoted(launcher.isUnix(), args, envVars.expand(sonarInstallation.getAdditionalProperties()));
    addTokenizedAndQuoted(launcher.isUnix(), args, envVars.expand(getJobAdditionalProperties()));
    if (mms != null && mms.usesPrivateRepository()) {
      args.add("-Dmaven.repo.local=" + mms.getWorkspace().child(".repository").getRemote());
    }
    args.add("sonar:sonar");
    return args;
  }


  private void addTokenizedAndQuoted(boolean isUnix, ArgumentListBuilder args, String argsString) {
    if (StringUtils.isNotBlank(argsString)) {
      for (String argToken : Util.tokenize(argsString)) {
        // see SONARPLUGINS-123 amperstand bug with windows..
        if (!isUnix && argToken.contains("&")) {
          args.addQuoted(argToken);
        } else {
          args.add(argToken);
        }
      }
    }
  }

  @Extension
  public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

    @CopyOnWrite
    private volatile SonarInstallation[] installations = new SonarInstallation[0];

    public DescriptorImpl() {
      super(SonarPublisher.class);
      load();
    }

    @Override
    public String getDisplayName() {
      return "Sonar";
    }

    @Override
    public String getHelpFile() {
      return "/plugin/sonar/help.html";
    }

    public SonarInstallation[] getInstallations() {
      return installations;
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) {
      installations = req.bindParametersToList(SonarInstallation.class, "sonar.").toArray(new SonarInstallation[0]);
      save();
      return true;
    }

    @Override
    public Notifier newInstance(StaplerRequest req, JSONObject json) {
      return req.bindParameters(SonarPublisher.class, "sonar.");
    }

    public FormValidation doCheckMandatory(@QueryParameter String value) {
      return StringUtils.isBlank(value) ?
          FormValidation.error(Messages.SonarPublisher_MandatoryProperty()) : FormValidation.ok();
    }

    public FormValidation doCheckMandatoryAndNoSpaces(@QueryParameter String value) {
      return (StringUtils.isBlank(value) || value.contains(" ")) ?
          FormValidation.error(Messages.SonarPublisher_MandatoryPropertySpaces()) : FormValidation.ok();
    }

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> jobType) {
      // eventually check if job type of FreeStyleProject.class || MavenModuleSet.class
      return true;
    }
  }
}
