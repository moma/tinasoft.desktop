#!/bin/bash
export NLTK_DATA=TinasoftPytextminer/shared/nltk_data
TinasoftPytextminer/httpserver desktop_config_unix.yaml &
firefox http://localhost:8888
