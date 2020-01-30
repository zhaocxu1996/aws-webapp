# webapp

## pre-requisites

Programming language: java

Frameworks: spring boot, hibernate

Other tools: postman, mysql, mysql workbench

## instructions

First clone the repo to the local and use IntelliJ IDEA to open it.

Then open the setting to config maven, version control and download lombok plugin.

Open the project structure to set java version and check if there are sth wrong with frameworks.

Run MySQL and MySQL workbench, create a new schema called v1 and config the database information in the application.yml.

Run the main method in WebappApplication.java and use Postman to test the APIs.

There are only 1 API do not need authentication, others must provide valid token using basic auth.