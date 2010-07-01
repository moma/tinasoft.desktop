#!/usr/env/python
# -*- coding: utf-8 -*-
#  Copyright (C) 2009-2011 CREA Lab, CNRS/Ecole Polytechnique UMR 7656 (Fr)
#
#  This program is free software: you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation, either version 3 of the License, or
#  (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with this program.  If not, see <http://www.gnu.org/licenses/>.

__version__= "1.0"
__url__= "http://tinasoft.eu"
__longdescr__="Tinasoft Desktop for bottom-up thematic field recontruction and graph visualization"
__license__="GNU General Public License"
__author__="Moma group, CREA Lab, CNRS/Ecole Polytechnique UMR 7656 (Fr)"
__author_email__="github at sciencemapping dot com"

from distutils.core import setup
import py2exe
import os
from glob import glob

data_files = [("Microsoft.VC90.CRT", glob(r'e:\Microsoft.VC90.CRT\*.*'))]

setup (
    name = 'TinasoftDesktop',
    packages = ['start_win.py'],
    data_files = data_files,
    # py2exe special args
    console = ['start_win.py'],
    options = {"py2exe":
        {
        "bundle_files": 1,
        }
    },
    version = __version__,
    url = __url__,
    long_description = __longdescr__,
    license = __license__,
    author = __author__,
    author_email = __author_email__,
)
