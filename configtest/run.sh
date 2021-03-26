docker stop HT_WAF
docker rm HT_WAF
docker build -t waf_hack .    
docker run -v "/${PWD}/modsechelp:/etc/custom" -p 8080:80 --name HT_WAF  waf_hack