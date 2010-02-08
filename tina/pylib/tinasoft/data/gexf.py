# -*- coding: utf-8 -*-
from tinasoft.data import Handler
import datetime

import tenjin
from tenjin.helpers import *   # or escape, to_str

engine = tenjin.Engine()

def render(**model):
    return engine.render('gexf.template', model)

# generic GEXF handler
class GEXFHandler (Handler):

    options = {
        'locale'     : 'en_US.UTF-8',
        'dieOnError' : False,
        'debug'      : False,
        'compression': None
    }
    
    def __init__(self, path, **opts):
        self.path = path
        self.loadOptions(opts)
        self.lang,self.encoding = self.locale.split('.')

# specific GEXF handler
class Engine (GEXFHandler):
    """
    Gexf Engine
    """
    def save(self, path):
        render(
           'date': datetime.datetime.now().strftime("%Y-%m-%d"), 
           'description' : "A test GEXF",
           'creator': ['Julian bilcke', 'Elias Showk'] 
        )

