/*
    FP Quark
    Copyright 2012 - 2013 Miguel Negrão.

    FP Quark: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FP Quark is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FP Quark.  If not, see <http://www.gnu.org/licenses/>.

    It is possible to add more type instances by adding the functions
    directly to the dict from the initClass function of the class that
    one wants to make an instance of some type class.
*/

Promise {
    var callbacks;
    var <isCompleted = false;
    var value;

    *new { |onComplete|
        ^super.newCopyArgs(onComplete).init
    }

    init {
        callbacks = [];
    }

    complete { |result|
        if( this.tryComplete(result) ) {
            ^this
        } {
            Error("Promise already completed").throw
        }
    }

    tryComplete { |result|

        ^if(isCompleted.not) {
            value = result;
            callbacks.copy.do{ |f|
                f.(result)
            };
            isCompleted = true;
            true
        } {
            false
        }
    }

    value {
        ^if( isCompleted ) {
            Some(value)
        } {
            None()
        }
    }

    success { |v| this.complete( v.success ) }
    trySuccess { |v| this.tryComplete( v.success ) }

    failure { |v| this.complete( v.fail ) }
    tryFailure { |v| this.tryComplete( v.fail ) }

    onComplete { |callback|
        if( isCompleted ) {
            callback.(value)
        } {
            callbacks = callbacks.add(callback)
        }
    }

    onSuccess { |callback|

        this.onComplete{ |result|
            result.match(callback,{})
        }
    }

    onFailure { |callback|

        this.onComplete{ |result|
            result.match({}, callback)
        }

    }

//Functor
    collect { |f|

        var p = Promise();

        this.onComplete{ |result|
            result.match({ |v|
                try {
                    p.success( f.(v) )
                } { |e|
                    p.failure(e)
                }

                },{ |e|
                    p.failure(e)
            })
        };
        ^p

    }

//Monad
    and { |b|
        ^this >>= { b }
    }

    >>= { |f|
        var p = Promise();

        this.onComplete{ |result|
            result.match({ |v|
                try{
                    f.(v).onComplete{ |result2|
                        p.complete(result2)
                    }

                } { |e|
                    p.failure(e)
                }
                },{ |e|
                    p.failure(e)
            })
        };
        ^p

    }

//Applicative
    <*> { |fa|
        var p = Promise();
        var thisResult = None();
        var thatResult = None();
        this.onComplete{ |v|
            v.match({ |x|
                if(thatResult.isDefined) {
                    p.success( x.(thatResult.get) )
                } {
                    thisResult = Some(x);
                } }, {
                    p.tryComplete(v)
            });
        };
        fa.onComplete{ |v|
            v.match({ |x|
                if(thisResult.isDefined) {
                    p.success( thisResult.get.(x) )
                } {
                    thatResult = Some(v);
                } }, {
                    p.tryComplete(v)
            })
        };
        ^p
    }

}

+ SynthDef {

    sendP { arg server, completionMsg;
        var servers = (server ?? { Server.allRunningServers }).asArray;
        var promises = servers.collect{ Promise() };
        var p = promises.reduce{ |a,b| a >>= { b } };
        [servers,promises].flopWith{ |each, p|
            var id = UniqueID.next;
            try{
                if(each.serverRunning.not) {
                    Error("Server % not running, could not send SynthDef.".format(server.name)).throw
                };
                if(metadata.trueAt(\shouldNotSend)) {
                    this.loadReconstructed(each, completionMsg);
                } {
                    this.doSend(each, completionMsg);
                };
                each.addr.sendBundle(0.1, ["/sync", id]);
                OSCFunc({ arg msg, time;
                    if( (msg[1]) == id ) {
                        p.success( this );
                    }
                }, '/synced', each.addr).oneShot;
            } { |e|
                p.failure(e)
            }
        };
        ^p
    }
}

+ Buffer {

    // read whole file into memory for PlayBuf etc.
    // adds a query as a completion message
    *readP { arg server,path,startFrame = 0,numFrames = -1, bufnum;
        var b, p = Promise();
        try{
            server = server ? Server.default;
            bufnum ?? { bufnum = server.bufferAllocator.alloc(1) };
            if(bufnum.isNil) {
                Error("No more buffer numbers -- free some buffers before allocating more.").throw
            };
            b = super.newCopyArgs(server, bufnum)
            .doOnInfo_({ p.success(b) }).cache
            .allocRead(path,startFrame,numFrames,{|buf|["/b_query",buf.bufnum]});
        } { |e|
            p.failure(e)
        };
        ^p
    }

}