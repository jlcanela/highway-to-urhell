package io.highway.to.urhell;

import io.highway.to.urhell.agent.AgentService;
import io.highway.to.urhell.domain.BreakerData;
import io.highway.to.urhell.domain.H2hConfig;
import io.highway.to.urhell.exception.H2HException;
import io.highway.to.urhell.service.AbstractLeechService;
import io.highway.to.urhell.service.LeechService;
import io.highway.to.urhell.service.ReporterService;
import io.highway.to.urhell.service.GatherService;
import io.highway.to.urhell.service.ThunderService;
import io.highway.to.urhell.service.TransformerService;
import io.highway.to.urhell.transformer.GenericTransformer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.UnmodifiableClassException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoreEngine {

    private static CoreEngine instance;

    private Map<String, LeechService> leechPluginRegistry = new HashMap<String, LeechService>();
    private Set<ReporterService> reporterPluginRegistry = new HashSet<ReporterService>();
    protected final transient Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final static String H2H_CONFIG = "H2H_CONFIG";
    private H2hConfig config;

    private CoreEngine() {
        // nothing
    }

    public static CoreEngine getInstance() {
        if (instance == null) {
            synchronized (CoreEngine.class) {
                if (instance == null) {
                    instance = new CoreEngine();
                    instance.registerPlugins();
                    instance.runPluginsTriggeredAtStartup();
                    instance.initEnv();
                }
            }
        }
        return instance;
    }

    
    	
    public void launchTransformerGeneric() throws ClassNotFoundException, UnmodifiableClassException{
    	LOGGER.info("launchTransformerGeneric !");
    	if(AgentService.getInstance().getInst() !=null){
    		TransformerService ts = new TransformerService();
    		Map<String, List<BreakerData>> mapConvert = ts.transformDataH2h(leechPluginRegistry.values());
    		ThunderService.getInstance().sendH2hPath();
    		AgentService.getInstance().getInst().addTransformer(new GenericTransformer(mapConvert), true);
	    	ts.transformAllClassScanByH2h(AgentService.getInstance().getInst(),mapConvert.keySet());
    	}else{
    		LOGGER.error("Instrumentation fail because internal inst is null");
    	}
    }

    public void leech() {
        for (ReporterService reporterService : reporterPluginRegistry) {
            for (LeechService leechService : leechPluginRegistry.values()) {
                reporterService.report(leechService.getFrameworkInformations());
            }
        }
    }

    public Collection<LeechService> getLeechServiceRegistered() {
        return leechPluginRegistry.values();
    }


    public LeechService getFramework(String frameworkName) {
        return leechPluginRegistry.get(frameworkName);
    }


    private void registerPlugins() {
        autoDiscoverLeechPlugins();
        autoDiscoverReporterPlugins();
    }

    private void autoDiscoverLeechPlugins() {
        Set<AbstractLeechService> leechServices = PluginUtils.autodiscoverPlugin(AbstractLeechService.class);
        for (AbstractLeechService leechService : leechServices) {
            leechPluginRegistry.put(leechService.getFrameworkInformations().getFrameworkName(), leechService);
        }
    }

    private void autoDiscoverReporterPlugins() {
        reporterPluginRegistry = PluginUtils.autodiscoverPlugin(ReporterService.class);
    }

    private void runPluginsTriggeredAtStartup() {
        for (LeechService leechService : leechPluginRegistry.values()) {
            if (leechService.isTriggeredAtStartup()) {
                leechService.receiveData(null);
            }
        }
    }
    
    private void initEnv() {
        // Grab Env
        String rootH2h = System.getProperty(H2H_CONFIG);
        try {
            if (rootH2h == null) {
                throw new H2HException(
                        "Unknow Variable H2H_CONFIG. Please Set H2H_CONFIG to location application deployment.");
            }
            if ("".equals(rootH2h)) {
                throw new H2HException(
                        "Variable Path H2H_CONFIG. Please Set H2H_CONFIG to location application deployment.");
            }
            parseConfig(rootH2h);

        } catch (H2HException e) {
            LOGGER.error("H2H is broken ", e);
        }
        GatherService.getInstance().initEnv();
        
    }
    
    public void parseConfig(String pathFile) {
    	 config = new H2hConfig();
         Properties prop = new Properties();
         InputStream input = null;
         try {
             input = new FileInputStream(pathFile);
             prop.load(input);
             config.setUrlApplication(prop.getProperty("urlapplication"));
             config.setNameApplication(prop.getProperty("nameapplication"));
             config.setUrlH2hWeb(prop.getProperty("urlh2hweb"));
             config.setPathH2h(prop.getProperty("pathH2h"));
             config.setPathSource(prop.getProperty("pathSource"));
             config.setDescription(prop.getProperty("description"));
             config.setVersionApp(prop.getProperty("versionApp"));
             config.setOutputSystem(io.highway.to.urhell.domain.OutputSystem.valueOf(prop.getProperty("outputSystem")));

         } catch (IOException ex) {
             LOGGER.error("Error while reading H2hConfigFile " + pathFile, ex);
             config = null;
         } finally {
             if (input != null) {
                 try {
                     input.close();
                 } catch (IOException e) {
                     //Don't care
                 }
             }
         }
    }

	public H2hConfig getConfig() {
		return config;
	}
    
   
}

