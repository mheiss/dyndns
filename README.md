## DynDns ##
This repository contains two small projects to build a custom DynDns solution for a DNS zone hosted in Azure.
It is a cheap solution for using a custom domain (myDomain.tld) in combination with a dynamic IP assigned by your ISP.

* DynDns App - Updates the IPv4 record of an Azure DNS zone 
* DynDns Servlet - Returns the current external IP of the caller

The Servlet is typically hosted in Azure as an App Service using the Free / Small pricing tear. 
The application is typically running on a local device thats always online (Raspberry Pi) to update the external IP on a regular base.

## Requirements ##
You need to own a domain name and you need to delegate the zone to Azure DNS by altering the nameservers to Microsoft. 