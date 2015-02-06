

MxDrawer {

    classvar >registery, <>registrationFilePaths;
    var <>onSelect;

    *new { arg onSelect;
        ^super.newCopyArgs(onSelect)
    }

    *add { arg title,  buildItemFunc;
        this.registery[title] = MxDrawerItem(title, buildItemFunc)
    }
    *addGroup { arg title, itemsFunc, buildItemFunc;
        this.registery[title] = MxDrawerItemGroup(title, itemsFunc, buildItemFunc)
    }

    *registery {
        ^registery ?? {
            registery = Dictionary.new;
            registrationFilePaths.do { arg p;
                p.loadPaths
            };
            registery
        }
    }
    *addRegistrationFile { arg path;
        // a file that will be loaded that will contain calls to .add / .addGroup
        registrationFilePaths = registrationFilePaths.add(path)
    }
    *initClass {
        var path;
        path = PathName(MxDrawer.class.filenameSymbol.asString).parentPath +/+ "drivers" +/+ "registerDrawerItems.scd";
        registrationFilePaths = [path];
    }

    guiClass { ^MxDrawerGui }
}


MxDrawerItem {

    var <>title, <>buildItemFunc;

    *new { arg title, buildItemFunc;
        ^super.newCopyArgs(title, buildItemFunc)
    }
    make { arg i, onMake;
        {
             onMake.value(buildItemFunc.value)
        }.fork(AppClock)
    }
}


MxDrawerItemGroup {

    var <>title, <>itemsFunc, <>buildItemFunc;

    *new { arg title, itemsFunc, buildItemFunc;
        ^super.newCopyArgs(title, itemsFunc, buildItemFunc)
    }
    drill {
        ^itemsFunc.value.collect { arg it, i;
            MxDrawerSubItem(it[0], this, it[1])
        }
    }
}


MxDrawerSubItem {

    var <>title, <>drawerItem, <>data;

    *new { arg title, drawerItem, data;
        ^super.newCopyArgs(title, drawerItem, data)
    }
    make { arg i, onMake;
        {
             onMake.value(drawerItem.buildItemFunc.value(data, title))
        }.fork(AppClock)
    }
}


MxDrawerGui : ObjectGui {

    var lv, keys, items, currentItemGroup, searchBox, bg, fg;

    writeName {}

    guiBody { arg layout, bounds;
        var width, action;
        bg = Color(0.21652929382936, 0.23886961779588, 0.26865671641791);
        fg = Color(0.94029850746269, 0.96588486140725, 1.0);
        width = min(layout.bounds.width, 200);
        // view = userView ?? { UserView(layout, bounds ?? { Rect(0, 0, 100, 800) }) };
        ActionButton(layout, "..", {
            this.drillUp
        });
        searchBox = TextField(layout, (width-28)@17);
        searchBox.string = "";
        searchBox.action = {
            this.search(searchBox.value)
        };
        searchBox.focusColor = Color.blue;

        // using ListView, though it cannot drag directly into a unit yet
        lv = ListView(layout, width@(layout.bounds.height-17-4));
        lv.background = bg;
        // all top level items, nothing unfolded
        this.drillUp;

        action = {
            var item;
                item = items[lv.value];
                if(item.isKindOf(MxDrawerItemGroup).not, {
                    item.title.inform("Loading");
                    item.make(lv.value, model.onSelect)
                }, {
                    this.drillDown(item);
                })
        };
        lv.mouseDownAction = { arg view, x, y, modifiers, buttonNumber, clickCount;
            // double click on a top level single item or unfolded sub-item => select
            if(clickCount == 2, action)
        };

        lv.stringColor = fg;
        lv.focusColor = Color.clear;
        lv.font = GUI.font.new(GUI.skin.fontSpecs[0], 10);
        lv.beginDragAction = {
            var key, item;
            item = items[lv.value];
            if(item.isKindOf(MxDrawerItemGroup).not, {
                item = item.make(lv.value);
            }, {
                item = nil
            });
            // should use a memento so that it can load asynch but you can already start dragging
            item
        };
        lv.enterKeyAction = {
            action.value;
            this.update;
        };
        lv.keyDownAction = this.keyDownResponder;
    }
    drillDown { arg itemGroup;
        var item;
        itemGroup = currentItemGroup = itemGroup ?? {
                item = items[lv.value];
                 if(item.isKindOf(MxDrawerItemGroup), {
                  item;
                 }, {
                     ^nil
                 })
        };
        items = itemGroup.drill; // title, data
        keys = items.collect(_.title);
        lv.items = keys;
        this.prSetColors;
        lv.refresh
    }
    drillUp {
        var labels;
        keys = MxDrawer.registery.keys.as(Array).sort;
        items = keys.collect { arg k;
            var it;
            it = MxDrawer.registery[k];
            if(it.isKindOf(MxDrawerItemGroup), {
                labels = labels.add( "*" ++ k ++ "*" )
            }, {
                labels = labels.add( k.asString )
            });
            it
        };
        currentItemGroup = nil;
        lv.items = labels;
        this.prSetColors;
    }
    prSetColors {
        if(GUI.scheme.id == 'qt', {
            lv.colors = bg ! items.size;
        }, {
            lv.background = bg;
        });
    }
    nextItem {
        lv.value = min(lv.value + 1, lv.items.size-1)
    }
    prevItem {
        lv.value = max(lv.value - 1, 0)
    }
    focusSearch {
        searchBox.focus
    }
    search { arg q;
        if(currentItemGroup.isNil, {
            this.drillUp
        }, {
            this.drillDown(currentItemGroup)
        });
        items = items.select { arg item; item.title.containsi(q) };
        keys = items.collect(_.title);
        lv.items = keys;
        lv.refresh;
    }
    keyDownResponder {
        var k;
        k = KeyResponder.new;
        //  option
        k.register(   \left  ,   false, false, true, false, {
            this.prevItem

        });
        //  option
        k.register(   \right  ,   false, false, true, false, {
            this.nextItem

        });
        //  option
        k.register(   \up  ,   false, false, true, false, {
            this.drillUp

        });
        //  option
        k.register(   \down  ,   false, false, true, false, {
            this.drillDown

        });
        //  option enter
        k.register(   3  ,   false, false, true, false, {
            lv.enterKeyAction.value

        });
        //  option 223
        k.register(   223  ,   false, false, true, false, {
            searchBox.focus
        });


//        //  shift-control arrow up
//        k.register(   \up  ,   false, false, false, false, {
//            this.drillUp
//        });
//        //  shift-control arrow down
//        k.register(   63233  ,   true, false, false, true, {
//            this.drillDown
//        });
//        //  control s-earch
//        k.register(   19  ,   false, false, false, true, {
//            searchBox.focus
//        });
//        //  shift arrow down  nav up
//        k.register(   \up  ,   true, false, false, false, {
//            this.prevItem
//        });
//        //  shift arrow down nav down
//        k.register(   63233  ,   true, false, false, false, {
//            this.nextItem
//        });
//        //  3 enter
//        k.register(   3  ,   false, false, false, false, {
//            lv.enterKeyAction.value
//        });

        ^k /*++ { arg view, char, modifier, unicode, keycode;
                lv.defaultKeyDownAction(char, modifier, unicode)
            };*/
    }
}
