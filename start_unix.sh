#!/bin/bash
export NLTK_DATA=shared/nltk_data
python TinasoftPytextminer/httpserver.py TinasoftPytextminer/config_unix.yaml &
sleep 5
firefox http://localhost:8888 &
