# MOH DAP API Layer.

<!-- TABLE OF CONTENTS -->
<details open="open">
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
	<li><a href="#installation">Test unit & API documentation</a></li>
      </ul>
    </li>
    <li><a href="#license">License</a></li>
  </ol>
</details>


<!-- ABOUT THE PROJECT -->
## About The Project

DAP API Endpoints provides RESTful access to the integrated health data in the Data Analytics Platform Warehouse. The endpoints are organised around the different available data sources and the data mining funtionalities.


<!-- GETTING STARTED -->
## Getting Started

This is a java project built using the maven tool( [follow for maven overview](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html) ). 

### Prerequisites

* Install JDK (jdk1.8+  -  [Oracle jdk1.8](https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html),  [Open jdk1.8](https://openjdk.java.net/install/)).

* Install maven V3.6.3+ [Maven](https://maven.apache.org/install.html)

* Some unit tests in the project depend on the [dslminer project](https://github.com/uonafya/dslminer) to complete. Please see the project set up docs to run it before building your DAP API sources. Edit the file **dslapi/dsl_service_parent_project/dsl_api_implementation/src/main/resources/settings.properties** to set up the current host and port of the DAP miner app to allow unit tests to run against those APIs during the DAP API build process.

* The project uses the [ehcache library for cacheing](https://www.ehcache.org/). If in the course of development you get the following error:
```
   unknow request net.sf.ehcache.config.InvalidConfigurationException: There is one error in your configuration: 
	 CacheManager configuration: 
```
  This is as a result of your web server having less memory than you allocated in the cache configuration file. In this case edit the following file         **dslapi/dsl_service_parent_project/dsl_api_implementation/src/main/resources/ehcache.xml** to configure memory space in line with your webserver allocated memory  space by the JDK. eg.

```
  <?xml version="1.0" encoding="UTF-8"?>

  <ehcache
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:noNamespaceSchemaLocation="ehcache.xsd" updateCheck="true"
      monitoring="autodetect" dynamicConfig="true">

      <cache name="dslCache"
             maxEntriesLocalHeap="2"
             maxEntriesLocalDisk="1"
             eternal="true"
             diskSpoolBufferSizeMB="6"
             timeToIdleSeconds="0" timeToLiveSeconds="0"
             memoryStoreEvictionPolicy="LFU"
             transactionalMode="off">
          <persistence strategy="localTempSwap" />
      </cache>

  </ehcache>
```

### Installation

1. Clone the repo
   ```sh
   git clone https://github.com/uonafya/dslapi.git
   ```
2. Set up database connetion to the MoH DAP Warehouse by editing the following file in the project:
   ```sh
    dslapi/dsl_service_parent_project/dsl_api_implementation/src/main/resources/database.properties 
   ```
4. Change directory.
   ```sh
   cd dslapi
   ```
3. Compile the package
   ```sh
   mvn package
   ```
Compiled war file is placed in the target directory of the dslapi module which can be deployed to your webserver, currently tested on Apache Tomcat.   

### Test unit & API documentation

The project uses [Spring rest docs](https://spring.io/projects/spring-restdocs) to auto generate the documentation of the APIs. They leverage on unit tests to test the APIs and document both request and responses, see here unit tests in: **dslapi/dsl_api/src/test/java/com/healthit/dslservice/docs/**. The Documentation uses [asciidoc](https://asciidoctor.org/) markup format as a template for the request and response test done in the unit tests. Access template format **dslapi/dsl_api/src/docs/api-guide.adoc**

#### Documentation
http://dsl.health.go.ke/dsl/api/

<!-- LICENSE -->
## License

Distributed under the GPL-3.0 License. See `LICENSE` for more information.
