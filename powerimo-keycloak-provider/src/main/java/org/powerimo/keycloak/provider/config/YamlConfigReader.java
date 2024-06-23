package org.powerimo.keycloak.provider.config;

import org.jboss.logging.Logger;
import org.powerimo.keycloak.provider.PowerimoKeycloakProviderException;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;

public class YamlConfigReader {
    private static final Logger log = Logger.getLogger(YamlConfigReader.class);
    public static final String DEFAULT_PATH = "/etc/keycloak-mq-sender/config.yaml";
    private String configFilePath = DEFAULT_PATH;

    public YamlConfigReader() {
        this.configFilePath = DEFAULT_PATH;
    }

    public YamlConfigReader(String configFilePath) {
        this.configFilePath = configFilePath;
    }

    public YamlConfig readConfig() {
        if (!isConfigFileExist()) {
            log.error("Config file does not exist: " + configFilePath);
            return new YamlConfig();
        }

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

    public boolean isConfigFileExist() {
        File file = new File(configFilePath);
        return file.exists();
    }

}
