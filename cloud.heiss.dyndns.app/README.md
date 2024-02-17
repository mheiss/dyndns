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
* **recordNames** - The name of the A records to update. Can be a comma separated list like: @, subdomain1, subdomain2  
* **ipServiceUrl** - The URL to call in order to get the IP address.

An optional configuration parameter is available to schedule the update on a regular base. If the parameter is configured then the application keeps running and triggers an update in the configured interval. 
Without the parameter the application terminates after a single update attempt.

* **updateIntervalInMinutes** - Schedules the update to run every xx minutes.

An empty configuration file is available here: /config/config.json.sample

## Usage ##
java -jar dyndns-app-x.x.x-jar-with-dependencies.jar config.json

## Run as service ##
A sample configuration file for a systemd.service is available here: /config/dyndns.service
Just copy the file to the /usr/lib/systemd/system directory so that the application is launched after startup. 
This requires that the 'updateIntervalInMinutes' configuration parameter is set.

## Build ##
.\gradle build
