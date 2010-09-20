#!/bin/bash
export NLTK_DATA=shared/nltk_data
TinasoftPytextminer/httpserver config_unix.yaml &
sleep 5
firefox http://localhost:8888
