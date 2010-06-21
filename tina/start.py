#!/usr/bin/python
import os
import sys
import subprocess
from threading import Thread, Event
import platform

def kill (pid):
    if platform.system() == 'Linux':
        os.popen('kill -9 '+str(pid))
    elif platform.system() == 'Windows': 
        os.popen('TASKKILL /PID '+str(pid)+' /F')
    else:
        os.popen('kill -9 '+str(pid))

class Processus (Thread):
    def __init__(self):
        Thread.__init__(self)
        self.proc = None
        self.pid = 0
        
    def spawn(self, name, cmd, *args, **kwargs):
    
        try:
            pfile = open("."+name,"r")
            pid = int(pfile.read())
            print "killing %d"%pid
            kill(pid)
        except Exception, e:
            print e
            pass
            
        self.proc = subprocess.Popen(cmd, *args,**kwargs)
        self.pid = self.proc.pid
        
        pfile = open("."+name,"w")
        pfile.write("%d"%self.pid)
        pfile.close()

        self.proc.wait()

    def stop(self):
        if not self.proc:
            print "cannot stop process, because it is already stopped"
            return
        if not self.proc.poll():
            try:
                self.proc.terminate()
            except:
                print "couldn't terminate process %d, assuming it worked.."%self.proc.pid
    
class Server (Processus):
    def __init__(self, client=None):
        Processus.__init__(self)
        self.client = client
       
    def run(self):
        #if platform.system() == 'Linux':
        #    cmd = 'httpserver.py'
        #elif platform.system() == 'Windows': 
        #    cmd = 'httpserver.py'
        #else:
        #    cmd = 'httpserver.py'
        cmd = ['python', 'httpserver.py']
        self.spawn("server", cmd, bufsize=0, executable=None, stdin=None, stdout=None, stderr=None, preexec_fn=None, close_fds=False, shell=False, cwd=None, env=None, universal_newlines=False, startupinfo=None, creationflags=0)
        
        self.client.stop()

class Client (Processus):
    def __init__(self, server=None):
        Processus.__init__(self)
        self.server = server
        
    def run(self):
        #if platform.system() == 'Linux':
        #    cmd = ['xulrunner', 'application.ini']
        #elif platform.system() == 'Windows': 
        #    cmd = ['xulrunner', 'application.ini']
        #else:
        #    cmd = ['xulrunner', 'application.ini']
        cmd = ['xulrunner', 'application.ini']  
        self.spawn("client", cmd, bufsize=0, executable=None, stdin=None, stdout=None, stderr=None, preexec_fn=None, close_fds=False, shell=True, cwd=None, env=None, universal_newlines=False, startupinfo=None, creationflags=0)
        
        self.server.stop()


# create the client and the server
server = Server()
client = Client(server)
server.client = server

# TODO
# check if server is started


print "\nstarting tinasoft, please wait..\n-----------------------------------------------------------------\n"
server.start()
client.start()





