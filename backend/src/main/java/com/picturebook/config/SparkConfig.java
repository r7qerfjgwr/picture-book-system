package com.picturebook.config;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "spark.enabled", havingValue = "true", matchIfMissing = false)
public class SparkConfig {

    private static final Logger log = LoggerFactory.getLogger(SparkConfig.class);

    @Value("${spark.app-name:PictureBookAnalysis}")
    private String appName;

    @Value("${spark.master:local[*]}")
    private String master;

    @Value("${spark.eventLog.enabled:false}")
    private String eventLogEnabled;

    @Value("${spark.sql.warehouse.dir:${java.io.tmpdir}/spark-warehouse}")
    private String warehouseDir;

    @Bean
    public SparkSession sparkSession() {
        log.info("Initializing SparkSession: appName={}, master={}", appName, master);

        SparkConf conf = new SparkConf()
                .setAppName(appName)
                .setMaster(master)
                .set("spark.sql.warehouse.dir", warehouseDir)
                .set("spark.eventLog.enabled", eventLogEnabled)
                .set("spark.driver.host", "localhost")
                .set("spark.ui.enabled", "false")
                .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");

        SparkSession session = SparkSession.builder()
                .config(conf)
                .getOrCreate();

        log.info("SparkSession initialized successfully");
        return session;
    }
}
