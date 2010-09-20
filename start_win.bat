set NLTK_DATA=shared\nltk_data
PATH C:\Python26;%PATH%
python TinasoftPytextminer\httpserver.py TinasoftPytextminer/config_win.yaml
@ping 127.0.0.1 -n 5 -w 1000 > nul
start http://localhost:8888
