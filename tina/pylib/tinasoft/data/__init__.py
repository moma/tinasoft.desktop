# -*- coding: utf-8 -*-
# APPLICATION LAYER
class Corpora:
    pass
class Corpus:
    pass
class Document:
    pass
class NGram:
    pass
class Assoc (tuple):
    pass
class AssocCorpus (Assoc):
    pass
class AssocDocument (Assoc):
    pass
class AssocNGramDocument (Assoc):
    pass
class AssocNGramCorpus (Assoc):
    pass

# SQL BACKEND LAYER
class Relational():

    def __init__():
        self.tables = []
 
    # ALWAYS USE getTable to get table names in SQL
    def getTable(self, clss):
        cName = clss.__name__
        # if already exist, return
        if cName in self.tables:
            return cName
        # else create it
        self.tables.append(cName)
        try:
            if cName == 'Corpora':
                self.execute('''create table '''+cName+''' (id VARCHAR PRIMARY KEY)''')
            if cName == 'Corpus':
                self.execute('''create table '''+cName+''' (id VARCHAR PRIMARY KEY, period_start VARCHAR, period_end VARCHAR)''')
            if cName == 'Document':
                self.execute('''create table '''+cName+''' (id VARCHAR PRIMARY KEY, date VARCHAR, blob BLOB)''')
            if cName == 'NGram':
                self.execute('''create table '''+cName+''' (id VARCHAR PRIMARY KEY, str VARCHAR, blob BLOB)''')
            if cName.startswith('AssocNGram') :
                self.execute('''create table '''+cName+''' (id1 VARCHAR, id2 VARCHAR, occs INTEGER, PRIMARY KEY (id1, id2))''')
            elif cName.startswith('Assoc'):
                self.execute('''create table '''+cName+''' (id1 VARCHAR, id2 VARCHAR, PRIMARY KEY (id1, id2))''')
            self.commit()
        except Exception, exc:
            # table already exists
            pass
        return cName

    
    def insertCorpora(self):
        req = 'insert into ' + self.getTable( Corpora ) + ' values (?)'
        return req

    def insertAssoc(self, assoc):
        myclassname = self.getTable(assoc)
        if myclassname.startswith('AssocNGram'):
            # ngid1, ngid2, occurences
            req = 'insert into ' + myclassname + ' values (?, ?, ?)'
        else:
            req = 'insert into ' + myclassname + ' values (?, ?)'
        return req

    def insertCorpus(self, id, period_start, period_end):
        # id, period_start, period_end, blob
        req = 'insert into '+ self.getTable( Corpus ) +' values (?, ?, ?)'
        tuple = ( id, period_start, period_end )
        return ( req, tuple )
    
    def insertDocument(self, id, date, obj):
        req = 'insert into '+ self.getTable(obj.__class__) +' values (?, ?, ?)'
        tuple = ( id, date, obj )
        return ( req, tuple )
    
    def insertNGram(self, id, str, obj):
        req = 'insert into '+ self.getTable(obj.__class__) +' values (?, ?, ?)'
        tuple = ( id, str, obj )
        return ( req, tuple )
    
    def deleteAssoc( self, assoc ): 
        myclassname = self.getTable(assoc.__class__)
        if myclassname.startswith('AssocNGram'):
            req = 'delete from ' + myclassname + ' where id1 = ?'
        else:
            req = 'delete from ' + myclassname + ' where id1 = ?'
        return req

    def loadCorpora(self, id ):
        req = 'SELECT id FROM '+ self.getTable( Corpora ) 
                +' WHERE id = ?'
        return ( req, [id] )
        
    def loadCorpus(self, id ):
        req = 'SELECT id, period_start, period_end FROM '+ self.getTable( Corpus )
                +' WHERE id = ?'
        return ( req, [id] )
        
    def loadDocument(self, id ):
        req = 'SELECT id, date, blob FROM '+ self.getTable( Document ) 
                +' WHERE id = ?'
        return ( req, [id] )
        
    def loadNGram(self, id ):
        req = 'SELECT id, blob FROM  '+ self.getTable( NGram ) 
                +' WHERE id = ?'
        return ( req, [id] )

    def cleanAssocNGramDocument( self, corpusNum ):
        req = 'delete from '+ self.getTable( AssocNGramDocument )
                +' where id1 not in (select id1 from AssocNGramCorpus where id2 = ?)'
        return ( req, [corpusNum] )

    def fetchCorpusNGram( self, corpusid ):
        req = ('select id, str, blob from  '+ self.getTable( NGram )
                +' as ng JOIN '+ self.getTable( AssocNGramCorpus ) 
                +' as assoc ON assoc.id1=ng.id AND assoc.id2 = ?')
        return (req, [corpusid])
 
    def fetchCorpusNGramID( self, corpusid ):
        req = ('select id1 from '+ self.getTable( AssocNGramCorpus )
                +' where id2 = ?')
        return (req, [corpusid])

    def fetchDocumentNGram( self, documentid ):
        req = ('select ng.id, ng.str, ng.blob from '+ self.getTable( NGram )
                +' as ng JOIN '+ self.getTable( AssocNGramDocument ) 
                +' as assoc ON assoc.id1=ng.id AND assoc.id2 = ?')
        return (req, [documentid])

    def fetchDocumentNGramID( self, documentid ):
        req = ('select id1 from '+ self.getTable( AssocNGramDocument )
                +' where id2 = ?')
        return (req, [documentid])

    def fetchCorpusDocumentID( self, corpusid ): 
        req = ('select id1 from '+ self.getTable( AssocDocument ) 
                +' where id2 = ?')
        return (req, [corpusid])
