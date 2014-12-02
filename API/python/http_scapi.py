"""
    Unfinished python implementation

    This calls the api but does not return results.
    I was going to block the flask thread until the OSC response had come in,
    and then return the result to the $.ajax request
    but this is quite messy.

    could use eventlets, pywebsocket and something like tornado

    but the node.js implementation in supercollider.js is much simpler and faster


"""
from flask import Flask, request, render_template, abort
import simplejson
import time

# requires pyOSC
from OSC import OSCClient,OSCMessage, OSCServer
import atexit

from optparse import OptionParser
parser = OptionParser()
parser.add_option("--host",dest="host",help="HTTP host",type=str,default="127.0.0.1")
parser.add_option("--port",dest="port",help="HTTP port",type=int,default=5000)
parser.add_option("--sc_host",dest="sc_host",help="SC OSC host",type=str,default="127.0.0.1")
parser.add_option("--sc_port",dest="sc_port",help="SC OSC port",type=int,default=57120)

(options, args) = parser.parse_args()


app = Flask(__name__)
app.threaded = True
app.debug = False




@app.route('/')
def index():
    return 'SuperCollider http interface'

@app.route('/fiddle')
def fiddle():
    return render_template("fiddle.html")

@app.route('/call/<path:path>',methods=["POST"])
def call(path):

    data = request.form['data']
    token = time.time().hex()
    args = [str(token), "/%s" % path]

    if data:
        try:
            data = simplejson.loads(data)
        except Exception,e:
            return ("Bad request %s %s" % (e,data), 400, [])
        else:
            for a in data.get('msg',[]):
                args.append(a)

    msg = OSCMessage("/API/call")
    for a in args:
        msg.append(a)

    try:
        sc.send(msg)
    except Exception,e:
        print "send error",msg,e
        return ("%s : %s" % (msg,e), 500, [] )

    # just saying I sent it

    # hmm.. this is not multithreaded
    # print "sent, sleeping"
    # time.sleep(10)
    return "sent: %s %s" % (path,args)
    # needs to block this thread till the return message comes back

    # store the token for later responses
    # or put them in redis
    # and when responses come then message back to the client
    # (using what ?)

    # or just block here for now
    # the token is largest part of message
    # better to move this stuff to js ?



if __name__ == "__main__":

    # return_port
    sc = OSCClient()
    sc.connect( (options.sc_host, options.sc_port) )

    # atexit runs whenever python shuts down
    # so it always disconnects no matter what happens
    def off():
        print "\nclosing OSC connection"
        sc.close()
    atexit.register(off)

    print 'SuperCollider HTTP API'
    app.config['SERVER_NAME'] = "%s:%s" % (options.host,options.port)
    app.run()


