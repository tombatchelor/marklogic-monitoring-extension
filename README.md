# AppDynamics Monitoring Extension for use with MarkLogic

This extension works only with the standalone machine agent.

## Use Case
MarkLogic is an operational and transactional Enterprise NoSQL database that is designed to integrate, store, manage, and search data.

##Installation
1. Build the project with maven `mvn clean install` and unzip MarkLogicMonitor-<version>.zip
2. Copy the "MarkLogicMonitor" directory to `<MACHINE_AGENT_HOME>/monitors`
3. Configure the extension by following the below section and restart the Machine Agent.

## Configuration ##
Note : Please make sure to not use tab (\t) while editing yaml files. You may want to validate the yaml file using a [yaml validator](http://yamllint.com/)

1. Configure the MarkLogic instances by editing the config.yml file in `<MACHINE_AGENT_HOME>/monitors/MarkLogicMonitor/`.

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
   
2. Configure the metrics you are interested in by editing the metrics.xml file in `<MACHINE_AGENT_HOME>/monitors/MarkLogicMonitor/`.

## Troubleshooting & WorkBench

Workbench is a feature that lets you preview the metrics before registering it with the controller. This is useful if you want to fine tune the configurations. Workbench is embedded into the extension jar.

To use the workbench
1. Follow all the installation steps
2. Start the workbench with the command
java -jar /path/to/MachineAgent/monitors/MarkLogicMonitor/marklogic-monitoring-extension.jar
This starts an http server at http://host:9090/. This can be accessed from the browser.
If the server is not accessible from outside/browser, you can use the following end points to see the list of registered metrics and errors.
#Get the stats
curl http://localhost:9090/api/stats
#Get the registered metrics
curl http://localhost:9090/api/metric-paths
You can make the changes to config.yml and validate it from the browser or the API
Once the configuration is complete, you can kill the workbench and start the Machine Agent


## Metrics
To view complete set of metrics, start the extension in WorkBench mode.

Note : By default, a Machine agent or a AppServer agent can send a fixed number of metrics to the controller. To change this limit, please follow the instructions mentioned [here](http://docs.appdynamics.com/display/PRO14S/Metrics+Limits).
For eg.  
```    
    java -Dappdynamics.agent.maxMetrics=2500 -jar machineagent.jar
```


##Support
For any questions or feature request, please contact [AppDynamics Support](mailto:help@appdynamics.com).
