# -*- coding: utf-8 -*-
from tinasoft.data.Relational import *
from xpcom import components, verbose, COMException, ServerException, nsError

class TinasoftDataRelational(Api):
    _com_interfaces_ = components.interfaces.nsITinasoftDataRelational
    _reg_clsid_ = "{4ff50853-96cb-4eca-b633-43be1833ae90}"
    _reg_contractid_ = "Python.TinasoftDataRelational"

    def __init__(self):
        Api.__init__(self)
        self._dbFileName = None
        self._dbFile = None # holds the file name
        self._dbConn = None
        self._initialized = False
        self._storageService = components.classes["@mozilla.org/storage/service;1"].\
            getService(components.interfaces.mozIStorageService)

    def connect(self, filename):
        try:
            self._dbFilename = filename
            dirService = components.classes["@mozilla.org/file/directory_service;1"]\
                .getService( components.interfaces.nsIProperties )
            self._dbFile = dirService.get( "ProfD", components.interfaces.nsIFile )
            self._dbFile.append( self._dbFilename )
            self._dbConn = self._storageService.openDatabase( self._dbFile )
            #print("createDB Error exception ", self._dbConn.lastErrorString )
            if self._dbConn.connectionReady:
                sql = self.createTables()
                # Setup commonly used SQL statements
                self._setupSQLStatements()
                # TODO: Increment the version number on DB schema change
                # this._dbConn.executeSimpleSQL("REPLACE INTO prefs VALUES ('DB_SCHEMA_VERSION', '0.0.1')");
                self._initialized = True
                print( sql )
        except ServerException, e:
            print("catched : ", e)

    def _setupSQLStatements(self):
        # Select ALL tables
		sqlSelectAllTables = "SELECT name FROM sqlite_master WHERE type='table'";
		self.stmSelectAllTables = self._dbConn.createStatement(sqlSelectAllTables);


    def __del__(self):
        print "TinasoftDataRelational: destructing"

    def testXPCOM(self):
        cls = components.classes["Python.TestComponent"]
        ob = cls.createInstance(components.interfaces.nsIPythonTestInterfaceDOMStrings)
        print ob.GetStrings()
	
    # Drops and create brand new tables
	def resetDatabase(self):
        try:
			drop = "DROP TABLE IF EXISTS "
            tab = []
            while self.stmSelectAllTables.executeStep():
                tab.append( self.stmSelectAllTables.getString(0) )
            self.stmSelectAllTables.reset()
            for i in (length(tab)-1):
                self._dbConn.executeSimpleSQL( drop + tab[i] )
            self.createTables()
            return True
        except Exception, e:
			print("resetDatabase Exception ", e)
            print( self._dbConn.lastErrorString )
			return False

