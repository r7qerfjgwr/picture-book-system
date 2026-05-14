package com.picturebook.config;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
@ConditionalOnProperty(name = "hadoop.hdfs.enabled", havingValue = "true", matchIfMissing = false)
public class HadoopConfig {

    private static final Logger log = LoggerFactory.getLogger(HadoopConfig.class);

    @Value("${hadoop.hdfs.namenode:hdfs://localhost:9000}")
    private String nameNode;

    @Value("${hadoop.hdfs.user:root}")
    private String hdfsUser;

    @Bean
    public Configuration hadoopConfiguration() {
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", nameNode);
        conf.set("dfs.replication", "1");
        conf.set("dfs.client.use.datanode.hostname", "true");
        log.info("Hadoop HDFS configured: namenode={}", nameNode);
        return conf;
    }

    @Bean
    public FileSystem fileSystem(Configuration hadoopConfiguration) throws Exception {
        System.setProperty("HADOOP_USER_NAME", hdfsUser);
        FileSystem fs = FileSystem.get(hadoopConfiguration);
        log.info("HDFS FileSystem initialized: {}", fs.getUri());
        return fs;
    }
}
