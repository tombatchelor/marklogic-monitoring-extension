# AppDynamics Monitoring Extension for use with MarkLogic

This extension requires a Java Machine agent.

## Use Case
MarkLogic is an operational and transactional Enterprise NoSQL database that is designed to integrate, store, manage, and search data. This extension fetches performance metrics from MarkLogic and reports to AppDynamics Controller.

##Installation
1. Please start the Machine Agent before installing the extension and make sure that it reports data. Verify that the machine-agent status is UP and it is reporting Hardware Metrics
2. To build from source, clone this repository and run `mvn clean install`. This will produce a MarkLogicMonitor-VERSION.zip in target directory. Alternatively download the latest release archive from [here](https://github.com/Appdynamics/marklogic-monitoring-extension/releases).
3. Unzip as "MarkLogicMonitor" and copy it to `<MACHINE_AGENT_HOME>/monitors` directory.
4. Configure the extension by following the below section and restart the Machine Agent.
5. In the AppDynamics Metric Browser, look for: Application Infrastructure Performance | \<Tier\> | Individual Nodes | \<Node\> | Custom Metrics | MarkLogic

## Configuration
Note : Please make sure to not use tab (\t) while editing yaml files. You may want to validate the yaml file using a [yaml validator](http://yamllint.com/)

1. Configure the MarkLogic instances by editing the config.yml file in `<MACHINE_AGENT_HOME>/monitors/MarkLogicMonitor/`by specifying the Component_ID, displayName, uri, username and password

   For eg.
   ```
           
        #This will create it in specific Tier/Component. Make sure to replace <COMPONENT_ID> with the appropriate one
        #from your environment. To find the <COMPONENT_ID> in your environment, please follow the screenshot in
        #https://docs.appdynamics.com/display/PRO42/Build+a+Monitoring+Extension+Using+Java
        
        metricPrefix: Server|Component:<COMPONENT_ID>|Custom Metrics|MarkLogic
        
        servers:
          - displayName: Server1
            uri: "http://localhost:8002"
            username: "admin"
            password: "password123"
        
        
        connection:
          socketTimeout: 2000
          connectTimeout: 2000
        
        
        # number of concurrent tasks.
        # This doesn't need to be changed unless many instances are configured
        numberOfThreads: 5

   ```
   
2. Configure the metrics you are interested in by commenting/uncommenting the metrics in  metrics.xml file in `<MACHINE_AGENT_HOME>/monitors/MarkLogicMonitor/`.

## WorkBench
Workbench is a feature that lets you preview the metrics before registering it with the controller. This is useful if you want to fine tune the configurations. Workbench is embedded into the extension jar.
To use the workbench

1. Follow all the installation steps
2. Start the workbench with the command
`java -jar /path/to/MachineAgent/monitors/MarkLogicMonitor/marklogic-monitoring-extension.jar`
This starts an http server at `http://host:9090/`. This can be accessed from the browser.
3. If the server is not accessible from outside/browser, you can use the following end points to see the list of registered metrics and errors.
#Get the stats
`curl http://localhost:9090/api/stats`
#Get the registered metrics
`curl http://localhost:9090/api/metric-paths`
4. You can make the changes to config.yml and validate it from the browser or the API
5. Once the configuration is complete, you can kill the workbench and start the Machine Agent

## Metrics
This extension uses REST API to fetch metrics from MarkLogic server. Please refer to this [screenshot](https://github.com/Appdynamics/marklogic-monitoring-extension/blob/master/MetricsSnapShot.png) for a view of metrics. Some of the metrics are listed below:

1. Total Hosts - number of hosts in this cluster
2. Disks Performance
 - Query Read Rate - The moving average of reading query data from disk
 - Journal Write Rate - The moving average of data writes to the journal.
 - Save Write Rate - The moving average of data writes to in-memory stands.
 - Merge Read Rate - The moving average of reading merge data from disk
 - Merge Write Rate - The moving average of writing data for merges
 - Backup Rate - The moving average of reading and writing backup data to disk.
 - Restore Rate - The moving average of reading and writing restore data from disk.
 - Large Read Rate - The moving average of reading large documents from disk.
 - Large Write Rate - The moving average of writing data for large documents to disk.
3. Memory
 - System Page-In Rate - The page-in rate (from Linux /proc/vmstat) for the cluster in pages/sec.
 - System Page-Out Rate - The page-out rate (from Linux /proc/vmstat) for the cluster in pages/sec.
 - System Swap-In Rate - The swap-in rate (from Linux /proc/vmstat) for the cluster in pages/sec.
 - System Swap-Out Rate - The swap-out rate (from Linux /proc/vmstat) for the cluster in pages/sec.
4. Server Performance
 - Request Rate - The total number of queries being processed per second, across all of the App Servers.
 - Expanded Tree Cache Hits/Misses - The number of times per second that queries could use (Hits) and could not use (Misses) the expanded tree cache.
 - Request Count
5. Network Performance
 - XDQP Client Receive/Send Rate
 - XDQP Server Receive/Send Rate
 - XDQP Foreign Client Receive/Send Rate
 - XDQP Foreign Server Receive/Send Rate
6. Database Performance
 - Read Lock Rate - The number of read locks set per second on each database.
 - Write Lock Rate - The number of write locks set per second on each database.
 - Deadlock Rate - The number of deadlocks per second on each database.
7. Requests Summary
 - Total Requests
 - Update Count
 - Query Count
8. Transactions Summary
 - Total Transactions

## Troubleshooting 
1. Verify Machine Agent Data:Please start the Machine Agent without the extension and make sure that it reports data. Verify that the machine agent status is UP and it is reporting Hardware Metrics.
2. Metric Limit: Please start the machine agent with the argument -Dappdynamics.agent.maxMetrics=2000, if there is a metric limit reached error in the logs.
3. Collect Debug Logs: Edit the file, `<MachineAgent>/conf/logging/log4j.xml` and update the level of the appender "com.appdynamics" and "com.singularity" to debug.

##Support
For any questions or feature request, please contact [AppDynamics Support](mailto:help@appdynamics.com).
