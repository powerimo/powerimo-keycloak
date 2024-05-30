package org.powerimo.keycloak.provider.config;

import org.jboss.logging.Logger;
import org.powerimo.keycloak.provider.PowerimoKeycloakProviderException;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;

public class YamlConfigReader {
    private static final Logger log = Logger.getLogger(YamlConfigReader.class);
    public static final String DEFAULT_PATH = "powerimo-mq-listener-config.json";
    private String configFilePath = DEFAULT_PATH;

    public YamlConfigReader() {
        this.configFilePath = DEFAULT_PATH;
    }

    public YamlConfigReader(String configFilePath) {
        this.configFilePath = configFilePath;
    }

    public YamlConfig readConfig() {
        Yaml yaml = new Yaml();
        try {
            var is = new FileInputStream(configFilePath);
            var yamlData = yaml.loadAs(is, YamlConfig.class);
            yamlData.setConfigFilePath(configFilePath);
            log.trace(yamlData);
            return yamlData;
        } catch (Exception ex) {
            throw new PowerimoKeycloakProviderException("Exception on read config file: " + configFilePath, ex);
        }
    }
}
