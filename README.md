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
- "read" modsec log file
- generate whitelist locationMatches and deny everything else
- recognize IDs inside urls and add regex for it
- Whitelist HTTP Types
	- TODO: add http type enum
- Whitelist ParamNames
- simple regex for every param

**Open features**
- read and print with input output files from cmd
- write propper getting started with modsec learning mode guide
- add automate testing with request to the ne locationmatches with docker
- and stuff....
	
	




#configtest
simple test if everything went well.

add your loationsmatches to "modsec_9999.conf" 

and run bash run.sh

everything will be copied to docker an started.
Apache will load the configuration add check it on start up

you could use "header_00_config_template.conf" as a template for modsec configuration tp generate your logfiles 
