package mdn.jh.automation.webserver;

import java.io.File;
import java.util.logging.Level;

import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import mdn.jh.automation.Main;

public class TomcatServer {

	Tomcat tomcat = null;

	public TomcatServer() {
		tomcat = new Tomcat();
	}

	public Tomcat getTomcat() {
		return tomcat;
	}

	public void start() throws Exception {
		Main.getLogger().log(Level.INFO, "Starting Tomcat Webserver");
		
		String base = new File("tomcat").getAbsolutePath() + "/";

		String webappDirLocation = "tomcat/webapps";
		tomcat.setBaseDir(base);
		tomcat.setSilent(false);
		tomcat.setPort(Main.getServerPort());
		tomcat.getConnector().setProperty("address", Main.getServerAddress());
		tomcat.getHost().setAppBase(base);
		tomcat.getHost().setAutoDeploy(true);
		tomcat.getHost().setCreateDirs(true);
		tomcat.getHost().setDeployOnStartup(true);

		String abspath = new File(webappDirLocation).getAbsolutePath();
		// System.out.println("Starting webserver. AppDir= " + abspath);
		StandardContext ctx = (StandardContext) tomcat.addWebapp("", abspath);
		Tomcat.addServlet(ctx, "outputApi", new OutputApiServlet());
		ctx.addServletMappingDecoded("/api/output", "outputApi");
		// Only expose web endpoint classes to the web-app class loader. Loading all
		// of target/classes here would create a second copy of Main and its static state.
		File additionWebInfClasses = new File("target/webapp-classes");
		WebResourceRoot resources = new StandardRoot(ctx);
		resources.addPreResources(
				new DirResourceSet(resources, "/WEB-INF/classes", additionWebInfClasses.getAbsolutePath(), "/"));

		ctx.setResources(resources);
		// System.out.println("State:" + tomcat.getServer().getStateName());
		
		
		try {
			tomcat.start();

			Main.getLogger().log(Level.INFO, "Tomcat Webserver Started - Status: " + tomcat.getServer().getStateName()
					+ " on " + Main.getServerAddress() + ":" + Main.getServerPort());
//			org.apache.catalina.util.ServerInfo.main(null);
			tomcat.getServer().await();
			
		} catch (Exception e) {
			Main.getLogger().log(Level.SEVERE, "Tomcat Webserver Failed to Start: " + e.getLocalizedMessage());

		}
		

	}

}
