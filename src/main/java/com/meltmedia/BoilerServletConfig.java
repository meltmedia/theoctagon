package com.meltmedia;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.UnitOfWork;
import com.google.inject.persist.jpa.JpaPersistModule;
import com.google.inject.servlet.GuiceServletContextListener;
import com.meltmedia.dao.UserDAO;
import com.meltmedia.data.User;
import com.meltmedia.util.UserUtil;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.guice.web.GuiceShiroFilter;
import org.apache.shiro.guice.web.ShiroWebModule;
import org.apache.shiro.mgt.SecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.util.HashMap;
import java.util.Map;


public class BoilerServletConfig extends GuiceServletContextListener {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  private ServletContext servletContext;
  
  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    servletContext = servletContextEvent.getServletContext();
    super.contextInitialized(servletContextEvent);

    for (int i = 1; i <=50; i++) {
      // Create a default user
      User user = new User();
      user.setEmail("lonestarr"+i);
      UserUtil.setupNewPassword(user, "vespa".toCharArray());

      Injector injector = (Injector)servletContext.getAttribute(Injector.class.getName());
      UnitOfWork unitOfWork = injector.getInstance(UnitOfWork.class);
      unitOfWork.begin();
      UserDAO userDAO = injector.getInstance(UserDAO.class);
      userDAO.create(user);
      unitOfWork.end();

      log.info("Created user");
    }
//    // Create a default user
//    User user = new User();
//    user.setEmail("lonestarr");
//    UserUtil.setupNewPassword(user, "vespa".toCharArray());
//
//    Injector injector = (Injector)servletContext.getAttribute(Injector.class.getName());
//    UnitOfWork unitOfWork = injector.getInstance(UnitOfWork.class);
//    unitOfWork.begin();
//    UserDAO userDAO = injector.getInstance(UserDAO.class);
//    userDAO.create(user);
//    unitOfWork.end();
//
//    log.info("Created user");
  }
  
  @Override
  protected Injector getInjector() {
    Injector injector = Guice.createInjector(new JerseyServletModule() {
      @Override
      protected void configureServlets() {
        install(new JpaPersistModule("boilerJPAUnit"));        
        
//        filter("/*").through(PersistFilter.class);
        // Guice/Shiro compatibility filter
        filter("/*").through(GuiceShiroFilter.class);
        
        Map<String, String> params = new HashMap<String, String>();
        // Turn on pojo mapping for json
        params.put("com.sun.jersey.api.json.POJOMappingFeature", "true");
        // Scan packages for Jersey resource endpoints
        params.put(PackagesResourceConfig.PROPERTY_PACKAGES, "com.meltmedia.resource");
//        params.put("javax.ws.rs.Application", "com.meltmedia.MainJerseyApplication");
        serve("/api/*").with(GuiceContainer.class, params);
      }
    }, new ShiroWebModule(servletContext) {
      @Override
      protected void configureShiroWeb() {
        bindRealm().to(BoilerRealm.class);
      }
    });
    
    PersistService service = injector.getInstance(PersistService.class);
    service.start();
    
    SecurityManager securityManager = injector.getInstance(SecurityManager.class);
    SecurityUtils.setSecurityManager(securityManager);
    
    return injector;
  }
}