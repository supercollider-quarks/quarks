

EagerBundle : MixedBundle {

    *ifNil { arg bundle;
        ^bundle ?? {this.new}
    }
    onSend { arg func;
        func.value
    }
    addFunction { arg func;
        func.value
    }
    sched { arg time=0.0, func;
        func.value
    }

    addMessage { arg receiver, selector, args;
        Message(receiver, selector, args).value
    }
    addOnSendMessage { arg receiver, selector, args;
        Message(receiver, selector, args).value
    }
}
