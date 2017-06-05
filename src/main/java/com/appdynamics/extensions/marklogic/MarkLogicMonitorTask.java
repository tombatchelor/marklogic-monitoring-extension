/**
 * Copyright 2017 AppDynamics
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.appdynamics.extensions.marklogic;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.http.HttpClientUtils;
import com.appdynamics.extensions.http.UrlBuilder;
import com.appdynamics.extensions.marklogic.input.Metric;
import com.appdynamics.extensions.marklogic.input.MetricConfig;
import com.appdynamics.extensions.marklogic.input.MetricGroup;
import com.appdynamics.extensions.marklogic.input.Stat;
import com.appdynamics.extensions.util.JsonUtils;
import com.google.common.base.Strings;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Iterator;
import java.util.Map;

public class MarkLogicMonitorTask implements Runnable {

    private static final Logger logger = Logger.getLogger(MarkLogicMonitorTask.class);

    private Map server;
    private MonitorConfiguration configuration;

    public MarkLogicMonitorTask(MonitorConfiguration configuration, Map server) {
        this.configuration = configuration;
        this.server = server;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        String uri = (String) server.get("uri");
        try {
            if (!Strings.isNullOrEmpty(uri)) {
                String displayName = (String) server.get("displayName");
                String serverPrefix;
                if (!Strings.isNullOrEmpty(displayName)) {
                    serverPrefix = configuration.getMetricPrefix() + "|" + displayName + "|";
                } else {
                    serverPrefix = configuration.getMetricPrefix() + "|";
                }
                logger.debug("Fetching metrics for the server uri=" + uri + ",metricPrefix =" + serverPrefix);
                fetchMetrics(serverPrefix);
            }
        } catch (Exception e) {
            String msg = "Exception while running the MarkLogic task in the server " + uri;
            logger.error(msg, e);
            configuration.getMetricWriter().registerError(msg, e);
        } finally {
            long endTime = System.currentTimeMillis() - startTime;
            logger.debug("MarkLogic monitor thread for server " + uri + " ended. Time taken is " + endTime);
        }

    }

    private void fetchMetrics(String serverPrefix) {
        UrlBuilder urlBuilder;
        String url;
        Stat[] stats = getStats();
        if (stats != null && stats.length != 0) {
            for (Stat stat : getStats()) {
                urlBuilder = UrlBuilder.fromYmlServerConfig(server).path(stat.getEntityURL() + "&format=json");
                url = urlBuilder.build();
                JsonNode entityList = HttpClientUtils.getResponseAsJson(configuration.getHttpClient(), url, JsonNode.class);
                Iterator<JsonNode> itr = entityList.getElements().next().get("list-items").get("list-item").iterator();
                while (itr.hasNext()) {
                    JsonNode entity = itr.next();
                    String targetUrl = entity.get("uriref").getTextValue();
                    String entityName = entity.get("nameref").getTextValue();
                    if (!Strings.isNullOrEmpty(stat.getUrlSuffix())) {
                        urlBuilder = UrlBuilder.fromYmlServerConfig(server).path(targetUrl + stat.getUrlSuffix() + "&format=json");
                        url = urlBuilder.build();
                        JsonNode node = HttpClientUtils.getResponseAsJson(configuration.getHttpClient(), url, JsonNode.class);
                        MetricGroup[] metricGroups = stat.getMetricGroups();
                        for (MetricGroup metricGroup : metricGroups) {
                            String xpath = metricGroup.getXpath();
                            String metricGroupPrefix = metricGroup.getPrefix();
                            JsonNode jsonNode = JsonUtils.getNestedObject(node.path(stat.getEntryNode()), xpath.split("\\|"));
                            Metric[] metrics = metricGroup.getMetrics();
                            for (Metric metric : metrics) {
                                if (jsonNode != null) {
                                    String metricName = getMetricPath(metricGroupPrefix, metric, jsonNode, entityName, stat.getDisplayName());
                                    printMetric(serverPrefix + metricName, extractMetricValueFromNode(jsonNode, metric.getXpath()), stat);
                                }
                            }
                        }

                    } else {
                        logger.debug("uri-suffix for stat in metrics.xml is not configured");
                    }
                }
            }
        } else {
            logger.debug("Stat in metrics.xml is empty");
        }
    }

    private String getMetricPath(String metricGroupPrefix, Metric metric, JsonNode jsonNode, String entityName, String displayName) {
        String metricName = metric.getLabel() + "(" + jsonNode.path(metric.getXpath()).path("units").asText() + ")";
        if (!Strings.isNullOrEmpty(metricGroupPrefix)) {
            return displayName + "|" + entityName + "|" + metricGroupPrefix + "|" + metricName;
        } else {
            return metricName;
        }
    }

    private Stat[] getStats() {
        MetricConfig statConf = (MetricConfig) configuration.getMetricsXmlConfiguration();
        return statConf.getStats();
    }

    private BigDecimal extractMetricValueFromNode(JsonNode node, String metric) {
        try {
            String value = node.path(metric).path("value").asText();
            String type = node.path(metric).path("units").asText();
            if (type.contentEquals("bool")) {
                if (value.contentEquals("true")) {
                    return new BigDecimal(1);
                } else {
                    return new BigDecimal(0);
                }
            } else {
                return new BigDecimal(value).setScale(0, RoundingMode.HALF_UP);
            }
        } catch (NumberFormatException ex) {
            logger.error("Number exception for metric: " + metric, ex);
            return new BigDecimal(0);
        }
    }

    public void printMetric(String metricPath, BigDecimal metricValue, Stat stat) {
        if (metricValue != null) {
            //logger.debug("Metric:" + metricPath + ", Raw Value:" + metricValue);
            configuration.getMetricWriter().printMetric(metricPath, metricValue, getMetricType(stat));
        }
    }

    private String getMetricType(Stat stat) {
        if (stat.getMetricType() != null) {
            return stat.getMetricType();
        } else {
            return "AVG.AVG.COL";
        }
    }
}
