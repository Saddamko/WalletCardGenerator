# WalletCardGenerator
Apple Wallet Card Generator application for Tomcat server
The files that make up a pass are arranged in a package (also referred to as a bundle) called the pass package. At the center of the pass is a JSON file named pass.json, which defines the pass. The JSON file contains information that identifies the pass, text that appears on the pass, and other information about the pass. 
This application designed as Tomcat Web Servlet, and deployed as WAR file to Tomcat Catalina.
After deploying, this app is listening requests and doing some actions:
1. If URL consists "generate" command, wallet file ".pkpass" is created
2. If request is "GetWallet" - url for downloading pkpass is generated

