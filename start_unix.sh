#!/bin/bash
export NLTK_DATA=shared/nltk_data
python TinasoftPytextminer/httpserver.py TinasoftPytextminer/config_unix.yaml &
