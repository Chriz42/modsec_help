# modsec_help
Try to build a "learning mode" for modsecurity WAF 

**Why**
I allways hear "f5 is the better WAF because you have a learning mode".... well there ist a learningmode inside f5 but no one realy knows what this thing is doeing and why.
But it is less effort to yust activate learning mode in QA and use the rules on prod.

I realy like WAF and modsec but the devs have to create the locationmatches(rules) manually and this is allways a problem between Sec and Dev.
Often when we look inside  a WAF config inside a projects after some times there is a lot of wildecards or the whole thing is not active anymore because of " some problems on prod"

And thats the reason why I write this little tool. On QA you set SecRuleEngine to DetectionOnly and SecAuditEngine to On  mode and after your tests you take your logfile(modseclog) and generate the locationMatchtes.

Maybe at some point of time this will be highly automated server running on QS, fetch all modsec log files from your development enviroment, compare new and old rules inside git and when 
a change was made it creates a PR with the niew rule set.

But first the basics

**run as jar**

only relativ paths from the execution directory are possible at the moment
- logfile -> logfile that should be parsed
- outputfilename (optional) -> name of the outputfile 
- existingmodsecurityfile (optional)  -> existing modsecrule file where the rules should be updated with ne content from the log file

**configuration**
- startRuleId -> set the start for created rule Ids (Default: 666666)
- forbidUnknownBodyParams -> set to false if you want to allow unknown http bodies for some reasons
- resourceUrlPlaceHolder -> Inside folders with static content there shouldn't be a rule for every single path. Therefore it is possible to add generic placeholders like: <LocationMatch "^/js/.+\.js$"> every file with the js ending is possible to access but only with GET. The folders can be configured as comma separated list. Default contains: js, css, resources, images, fonts, styles, img
- denyAccessToUnknownUrl -> If set to true at the end will be added a rule that forbids access to the urls that weren't permitted by the rules before. Default ist true
- allowed400HttpStatusCodes -> We only want rules for valid requests inside the training data. If the response of the application is 404 Not found we don't want to create a rule for this. But for 403 access denied you maybe want because the url is valid and only the user isn't permitted. Therefor you can add a regex for 4xx response codes eg. 403|412 .Default: 403 
- regexPlaceholderForUUID -> We don't want a single locationmatch for every possible UUID inside an url to identify e.g. a user. Therefore a regex will be added to the locationmatch url string. default is "[a-fA-F0-9\-]+" but maybe you  want to use something more specific e.g."^[0-9a-fA-F]{8}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{12}$" for some reasons
- regexPlaceholderForHashes ->  We don't want a single locationmatch for every possible hash (and to regernate the rules for every update). Therefore a regex is added to the locationmatch url string. default is  "[a-fA-F0-9]+" maybe you want to change this
- parameterWhiteList -> inside fields like names you will never get any possible character in your testing scenario. Therefore it is possible to whitelist parameter names where the regex from parameterWhiteListRegex config is added
- parameterWhiteListRegex -> Regex for parameterWhitelist entries Default is: . (all printable characters)

**featues**
- only apache support atm
- "read" modsec log file
- generate whitelist locationMatches and deny everything else
- recognize IDs inside urls and add regex for it
- Whitelist HTTP Types
- Whitelist ParamNames
- simple regex for every param
- only create SecRules for responsetypes != 4XX
- update older modsecurity files with new locationmatches from new log inpu. It is recommanded only parse locationmatches that where generated from this tool


**Open features**
- add real xml and json support
	- atm only the single characters are added
- Add logging
- write  getting started with modsec learning mode guide
- add automate testing with request to the  locationmatches with docker 
- Add nginx support:
Problem LocationMatch is an apache directive.
But Modsec can use something like this:
 	SecRule REQUEST_FILENAME "^/admin$" "allow,id:111111111,chain"
            SecRule REQUEST_METHOD (GET) "chain"
            SecRule ARGS_GET_NAMES ^(firstname|lastname)$ "chain"
            SecRule ARGS_GET:firstname ^[a-zA-Z]+$ "chain"
            SecRule ARGS_GET:lastname ^[a-zA-Z]+$

	SecRule Request_URI "^/.*$" "deny,status:403,id:1111111111110"

	REQUEST_FILENAME is the url and all SecRules are chained and allow if everything match the whitelist secrules.
	SecRule 1111111111110 blocks everything else. 
	Con: you don't have a message inside the auditlog what went wrong...



you could use "header_00_config_template.conf" as a template for modsec configuration tp generate your logfiles 
