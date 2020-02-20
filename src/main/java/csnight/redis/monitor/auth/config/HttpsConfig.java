package csnight.redis.monitor.auth.config;

import csnight.redis.monitor.utils.BaseUtils;
import csnight.redis.monitor.utils.YamlUtils;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.net.ssl.KeyManagerFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Properties;

@Configuration
public class HttpsConfig {

    @Bean
    public TomcatServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                SecurityConstraint constraint = new SecurityConstraint();
                constraint.setUserConstraint("CONFIDENTIAL");
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");
                constraint.addCollection(collection);
                context.addConstraint(constraint);
            }
        };
        tomcat.addAdditionalTomcatConnectors(httpConnector());
        return tomcat;
    }

    @Bean
    public Connector httpConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setScheme("http");
        connector.setPort(8020);
        connector.setSecure(false);
        connector.setRedirectPort(443);
        return connector;
    }

    @Bean(value = "yaml")
    public YamlUtils ymlConfigurerUtil() {
        //1:加载配置文件
        Resource app = new ClassPathResource("application.yml");
        YamlPropertiesFactoryBean yamlPropertiesFactoryBean = new YamlPropertiesFactoryBean();
        // 2:将加载的配置文件交给 YamlPropertiesFactoryBean
        yamlPropertiesFactoryBean.setResources(app);
        // 3：将yml转换成 key：val
        Properties properties = yamlPropertiesFactoryBean.getObject();
        // 4: 将Properties 通过构造方法交给我们写的工具类
        return new YamlUtils(properties);
    }

    @Bean
    @DependsOn(value = "yaml")
    public KeyManagerFactory initKeyManager() {
        KeyManagerFactory kmf = null;
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            InputStream stream;
            ClassPathResource resource = new ClassPathResource(YamlUtils.getStrYmlVal("server.ssl.key-store"));
            if (resource.exists()) {
                stream = resource.getInputStream();
            } else {
                stream = new FileInputStream(BaseUtils.getResourceDir() + "www.csnight.xyz.pfx");
            }
            keyStore.load(stream, YamlUtils.getStrYmlVal("server.ssl.key-store-password").toCharArray());
            kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, YamlUtils.getStrYmlVal("server.ssl.key-store-password").toCharArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return kmf;
    }
}
