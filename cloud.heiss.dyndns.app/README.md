## DynDns App ##
A Java application that connects to AZURE in order to update the IPv4 address of a DNS zone.

## Configuration ##
The application expects a JSON configuration file as input. The file contains the 
details how to connect to AZURE as well as how to get the IP and which DNS zone to update.

The configuration file must contain the following information:

* **tenantId** - The ID of the Azure Active Directory.
* **clientId** - The ID of the application that is allowed to connect and update the DNS
* **key** - The KEY that has been used to create the application.
* **resourceGroup** - The name of the resource group containing the DNS Zone.
* **zoneName** - The name of the DNS zone to update.
* **ipServiceUrl** - The URL to call in order to get the IP address.
* **updateIntervalInMinutes** - Schedules the update to run every xx minutes (optional)

## Usage ##
java -jar dyndns-app-x.x.x-jar-with-dependencies.jar config.json

## Build ##
mvn assembly:assembly -DdescriptorId=jar-with-dependencies
