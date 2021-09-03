# modsec_help
Tool to generate modsecurity WAF Rules for apache from modsecurity log files

##Why

I want to have the possibility to create modsecurity WAF rules from the log files modsecurity is creating in "detectiononly" mode. I realy like modesecurity rules, the rules are easy to read if you take care, they are extremly powerfull and modsecurity is open source. But if you don't know what the application behind the firewall is doing it is pretty hard to write the rules your self or if you are to lazy write rules. Normally you would take an enterprise solution like f5 here, but I experienced that no one realy know at the end why the firewall is doing what. You use the learning mode in QA, test again with blocking mode, deploy to production and hope. If something get blocked that shouldn't be blocked you start again. It is realy hard to understand why a special request was blocked. Of course this is a Ui topic but there are more good reasons to use modsecurity rules.

1. Operations teams normally don't no know the application on API level but take care of the WAF. DEVS know the application best but don't have access to the WAF backend -> Move WAF topics to the dev teams as "infrastructure as code" 
2. If you need enterprise WAF in production and qa you have extreme higher costs -> modsecurity is open source and therefore cheap 

The important point is that the log files are containing every possible request. If you have a good testcoverage you can use the logs from qa. If not take the logs from production. Every time check if anything gets blocked before going live.


##features

- "read" modsec log files and create rules
- generate whitelist locationMatches and deny every unkown url
- recognize IDs inside urls and add regex for it
- Whitelist HTTP Types
- Whitelist parameter names
- Create simple regex for every param
- only create SecRules for responsetypes != 4XX
- update older modsecurity files with new locationmatches from new log input. It is recommended only parse locationmatches that where generated from this tool


##Open features

- add real xml and json support
	- atm only the single characters are added
- Add real logging
- add possibility to set params as sensitive inside the logs
 

##run the fat jar

 At the moment only relative paths from the execution directory are possible
Parameter:
- logfile -> logfile that should be parsed
- outputfilename (optional) -> name of the outputfile 
- existingmodsecurityfile (optional)  -> existing modsecrule file where the rules should be updated with new content from the log file
- It is possbile to overwrite all properties from comandline. Just add "propertyname value" add the end of the command, details see configuration. If you set a list you have to remove whitespaces between the elements e.g. one,two,three and not one, two, three


##configuration

-   **startRuleId**: Set the start for created rule Ids (Default: 666666)
- **forbidUnknownBodyParams**: Set to false if you want to allow unknown http bodies for some reasons
- **resourceUrlPlaceHolder**: Inside folders with static content there shouldn't be a rule for every single path. Therefore it is possible to add generic placeholders like: <LocationMatch "^/js/.+\.js$"> every file with the js ending is possible to access but only with GET. The folders can be configured as comma separated list. Default contains: js, css, resources, images, fonts, styles, img
- **denyAccessToUnknownUrl**: If set to true at the end will be added a rule that forbids access to the urls that weren't permitted by the rules before. Default ist true
- **allowed400HttpStatusCodes**: We only want rules for valid requests inside the training data. If the response of the application is 404 Not found we don't want to create a rule for this. But for 403 access denied you maybe want because the url is valid and only the user isn't permitted. Therefor you can add a regex for 4xx response codes eg. 403|412 .Default: 403 
- **regexPlaceholderForUUID**: We don't want a single locationmatch for every possible UUID inside an url to identify e.g. a user. Therefore a regex will be added to the locationmatch url string. default is "[a-fA-F0-9\-]+" but maybe you  want to use something more specific e.g."^[0-9a-fA-F]{8}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{12}$" for some reasons
- **regexPlaceholderForHashes**: We don't want a single locationmatch for every possible hash (and to regernate the rules for every update). Therefore a regex is added to the locationmatch url string. default is  "[a-fA-F0-9]+" maybe you want to change this
- **parameterWhiteList**: Inside fields like names you will never get any possible character in your testing scenario. Therefore it is possible to whitelist parameter names where the regex from parameterWhiteListRegex config is added
- **parameterWhiteListRegex**: Regex for parameterWhitelist entries Default is: . (all printable characters)


