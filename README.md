# modsec_help
Try to build a "learning mode" for modsecurity WAF 

**Why**
I allways hear "f5 is the better WAF because you have a learning mode".... well there ist a learningmode inside f5 but no one realy knows what this thing is doeing and why.
But it is less effort to yust activate learning mode in QA and use the rules on prod.

I realy like WAF and modsec but the devs have to create the locationmatches(rules) manually and this is allways a problem between Sec and Dev.
Often when we look inside  a WAF config inside a projects after some times there is a lot of wildecards or the whole thing is not active anymore because of " some problems on prod"

And thats the reason why I write this little tool. On QA you set modsec to detection only mode and after your tests you take your logfile(modseclog) and generate the locationMatchtes.

Maybe at some point of time this will be highly automated server running on QS, fetch all modsec log files from your development enviroment, compare new and old rules inside git and when 
a change was made it creates a PR with the niew rule set.

But first the basics

**featues**
- only apche support atm
- "read" modsec log file
- generate whitelist locationMatches and deny everything else
- recognize IDs inside urls and add regex for it
- Whitelist HTTP Types
- Whitelist ParamNames
- simple regex for every param

**Open features**
- ADD resource wildcards (and as an option default on)
- read and print with input output files from cmd
- Add propper logging
- write propper getting started with modsec learning mode guide
- add automate testing with request to the ne locationmatches with docker
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

** Configtest **

Test the generate locationMatches:
1. add your loationsmatches to "configtest/modsechelp/modsec_9999.conf" 
2. "bash run.sh"
3. docker handelsthe rest and start apache with modsec and your rules, syntax errors should be checked on startup

to test the matches you should set SecRuleEngine From DetectionOnly to On 

you could use "header_00_config_template.conf" as a template for modsec configuration tp generate your logfiles 
