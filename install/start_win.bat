set NLTK_DATA=TinasoftPytextminer\shared\nltk_data
start /B TinasoftPytextminer\httpserver.exe desktop_config_win.yaml
@ping 127.0.0.1 -n 5 -w 1000 > nul
start http://localhost:8888
