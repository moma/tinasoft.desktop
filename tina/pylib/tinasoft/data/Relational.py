import tinasoft.data

# SQL BACKEND LAYER
class Api():

    def __init__(self):
        self.tables = ['Corpora', 'Corpus', 'Document', 'NGram', 'AssocCorpus', 'AssocDocument', 'AssocNGramDocument', 'AssocNGramCorpus']
 
    # ALWAYS USE getTable to get table names in SQL
    def createTables(self):
        tables = []
        tables.append("create table Corpora (id VARCHAR PRIMARY KEY);")
        tables.append("create table Corpus (id VARCHAR PRIMARY KEY, period_start VARCHAR, period_end VARCHAR);")
        tables.append("create table Document (id VARCHAR PRIMARY KEY, date VARCHAR, blob BLOB);")
        tables.append("create table NGram (id VARCHAR PRIMARY KEY, str VARCHAR, blob BLOB);")
        tables.append("create table AssocNGramDocument (id1 VARCHAR, id2 VARCHAR, occs INTEGER, PRIMARY KEY (id1, id2));")
        tables.append("create table AssocNGramCorpus (id1 VARCHAR, id2 VARCHAR, occs INTEGER, PRIMARY KEY (id1, id2));")
        tables.append("create table AssocCorpus (id1 VARCHAR, id2 VARCHAR, PRIMARY KEY (id1, id2));")
        tables.append("create table AssocDocument (id1 VARCHAR, id2 VARCHAR, PRIMARY KEY (id1, id2));")
        return tables

    
    def insertCorpora(self):
        req = 'insert into Corpora values (?1)'
        return req

    def insertAssoc(self, myclassname):
        if myclassname.startswith('AssocNGram'):
            # ngid1, ngid2, occurences
            req = 'insert into ' + myclassname + ' values (?1, ?2, ?3)'
        else:
            req = 'insert into ' + myclassname + ' values (?1, ?2)'
        return req

    def insertCorpus(self):
        # id, period_start, period_end, blob
        req = 'insert into Corpus values (?1, ?2, ?3)'
        return req
    
    def insertDocument(self):
        req = 'insert into Document values (?1, ?2, ?3)'
        return req
    
    def insertNGram(self):
        req = 'insert into NGram values (?1, ?2, 3?)'
        return req
    
    def deleteAssoc( self, myclassname ): 
        if myclassname.startswith('AssocNGram'):
            req = 'delete from ' + myclassname + ' where id1 = ?1'
        else:
            req = 'delete from ' + myclassname + ' where id1 = ?1'
        return req

    def loadCorpora(self):
        req = 'SELECT id FROM Corpora WHERE id = ?1'
        return req
        
    def loadCorpus(self):
        req = 'SELECT id, period_start, period_end FROM Corpus' \
                +' WHERE id = ?1'
        return req
        
    def loadDocument(self):
        req = 'SELECT id, date, blob FROM Document WHERE id = ?1' 
        return req
        
    def loadNGram(self):
        req = 'SELECT id, blob FROM NGram WHERE id = ?1'
        return req

    def cleanAssocNGramDocument(self):
        req = 'delete from AssocNGramDocument' \
                +' where id1 not in (select id1 from AssocNGramCorpus where id2 = ?1)'
        return req

    def fetchCorpusNGram(self):
        req = ('select id, str, blob from NGram ' \
                +' as ng JOIN AssocNgramCorpus' \
                +' as assoc ON assoc.id1=ng.id AND assoc.id2 = ?1')
        return req
 
    def fetchCorpusNGramID(self):
        req = ('select id1 from AssocNGramCorpus' \
                +' where id2 = ?1')
        return req

    def fetchDocumentNGram(self):
        req = ('select ng.id, ng.str, ng.blob from NGram' \
                +' as ng JOIN AssocNGramDocument' \
                +' as assoc ON assoc.id1=ng.id AND assoc.id2 = ?1')
        return req

    def fetchDocumentNGramID(self):
        req = ('select id1 from AssocNGramDocument' \
                +' where id2 = ?1')
        return req

    def fetchCorpusDocumentID(self): 
        req = ('select id1 from AssocDocument' \
                +' where id2 = ?1')
        return req
