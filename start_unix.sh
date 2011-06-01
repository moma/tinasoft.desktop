#!/bin/bash
export NLTK_DATA=shared/nltk_data
# was previously only "python", but it is preferrable to specify the version system
python2.6 TinasoftPytextminer/httpserver.py TinasoftPytextminer/config_unix.yaml &
