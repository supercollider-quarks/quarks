

MxKrJackGui : ObjectGui {

    var ne;

    writeName {}
    guiBody { arg layout;
        ne = NumberEditor(model.value, model.spec);
        ne.gui(layout);
        ne.action = {
            // as long as you didn't get jacked into from something else
            // then you may move the fader
            if(model.isConnected.not, {
                model.value = ne.value
            })
        }
    }
    update {
        if(ne.value != model.value, {
            ne.value = model.value
        })
    }
}


MxArJackGui : ObjectGui {

}


MxStreamJackGui : MxKrJackGui {

/*
    var lastVal;
    writeName {}
    guiBody { arg layout;
        lastVal = CXLabel(layout, model.lastVal.asString);
        if(model.source.isKindOf(NumberEditor), { // default control
            model.source.gui(layout);
        })
    }
    update {
        {
            lastVal.label = model.lastVal.asString;
        }.defer
    }
*/

}
