#!/usr/bin/python
import os
from os.path import join
import sys
import subprocess
from threading import Thread, Event
import platform
import time

TINASOFT_DIR="tina"
PYTEXTMINER_DIR="TinasoftPytextminer"

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
            #print "killing %d"%pid
            kill(pid)
        except Exception, e:
            #print e
            pass

        self.proc = subprocess.Popen(cmd, *args,**kwargs)
        self.pid = self.proc.pid

        pfile = open("."+name,"w")
        pfile.write("%d"%self.pid)
        pfile.close()

        self.proc.wait()

    def stop(self):
        if not self.proc:
            print "cannot stop process %d, already stopped"%self.pid
            return
        if not self.proc.poll():
            try:
                self.proc.terminate()
            except:
                print "couldn't terminate process %d, assuming it worked.."%self.proc.pid



class Server (Processus):

    def run(self):
        cmd = ['python', 'httpserver.py']
        env = {
          'NLTK_DATA' : os.path.abspath(join(PYTEXTMINER_DIR,"shared","nltk_data"))
        }
        self.spawn("server", cmd, cwd=PYTEXTMINER_DIR, bufsize=0, executable=None, stdin=None, stdout=None, stderr=None, preexec_fn=None, close_fds=False, shell=False, env=env, universal_newlines=False, startupinfo=None, creationflags=0)

        self.client.stop()



class Client (Processus):

    def run(self):
        """run xulrunner on our application"""

        if platform.system() == 'Linux':
            import commands
            commands.getstatusoutput('xulrunner --app '+ join(TINASOFT_DIR, 'application.ini'))
        elif platform.system() == 'Windows':
            import commands
            commands.getstatusoutput('xulrunner.exe --app '+ join(TINASOFT_DIR, 'application.ini'))
        else:
            import commands
            commands.getstatusoutput('/Library/Frameworks/XUL.framework/xulrunner-bin --app '+ join(TINASOFT_DIR, 'application.ini'))

        self.server.stop()




###########################################

server = Server()
client = Client()

# attach the two objects
server.client = client
client.server = server

try:
    print "\nstarting tinasoft server, please wait..\n-----------------------------------------------------------------\n"
    server.start()
    time.sleep(3)
    print "\nstarting tinasoft desktop, please wait..\n-----------------------------------------------------------------\n"
    client.start()
except KeyboardInterrupt:
    print "stopping"
    server.stop()
    client.stop()





