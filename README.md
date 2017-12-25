# quarks

A Quark is a package of [SuperCollider](https://github.com/supercollider/supercollider) code containing classes, extension methods, documentation and server UGen plugins.

SuperCollider comes with a utility called Quarks that lets you browse the index of community contributed packages and install them. This repository contains that index.

Note: In older SuperCollider versions (3.6), Quarks still uses SVN (subversion). See the Quarks help file in your version of SuperCollider.

## Installing

If you just want to use or install Quarks, then you can do so from inside SuperCollider.
You do not need to download or clone anything from here. See the SuperCollider help file `Using Quarks` for a full tutorial.

### git

You should install git if you do not already have it:

http://git-scm.com/

If you cannot or do not want to install git then you can still download things you find on the webs and then install them manually:

```supercollider
Quarks.install("~/Downloads/some-thing-i-found-on-the-internet");
```

And you can also download all of the community contributed Quarks in a single download:

https://github.com/supercollider-quarks/downloaded-quarks

The Quarks interface will let you browse those and install them, just as normal.


## Installing a Quark

### using the gui:

```supercollider
Quarks.gui
```

This offers many community contributed packages most of which are hosted on github.

### by name

You can (un)install a Quark by name:

```supercollider
Quarks.install("UnitTesting");
Quarks.uninstall("UnitTesting");
```

### by git URL

```supercollider
Quarks.install("https://github.com/supercollider-quarks/UnitTesting.git");
Quarks.uninstall("https://github.com/supercollider-quarks/UnitTesting.git");
```

### from a local folder

```supercollider
// add your own classes
Quarks.install("~/supercollider/quarks/my-thing");
// install something that you downloaded and want to try out
Quarks.install("~/Downloads/something-i-found");
```

## Adding additional folders of Quarks

You can also add additional folders where you keep your personally developed quarks or those you have forked or manually downloaded.

These are folders that contain many quarks. Those will then be shown on the GUI as options for installing.

```supercollider
// put this in your startup.scd
// so that this folder is always available
// when you use supercollider
Quarks.addFolder("~/supercollider/quarks");
```


---

## Adding a Quark to this Directory

Publish your package to github under your own account.

Go here, click edit and then submit a pull request:

https://github.com/supercollider-quarks/quarks/blob/master/directory.txt

The format is:

    quarkname=https://github.com/you/quarkname

Ideally you should tag your releases with the version number and then specify it in the directory.txt:

    quarkname=https://github.com/you/quarkname@tags/0.1.0

The version is specified as a git refspec. `tags/x.y.z` is the preferred form.

## Tag your releases

Tagged version numbers will allow people to download a .tgz archive of the release and will enable people to switch between different versions.

Users can see and switch between versions in the interface, but only if they are tagged.

It will also enable people to specify an exact version of your quark when working on their own projects.

Note that a refspec is either tags/{tagname} or {commit-hash} but not a branch name. A branch is a continually changing, and packages releases need to be pinned. **Don't use branch names for tag names**, because this will cause obscure errors later.

## Claiming one of the migrated Quarks from SVN

The supercollider-quarks organization has lots of repositories that were migrated from the old SVN repository. Commits, dates and authorship were preserved.

You will find your old work here:

https://github.com/supercollider-quarks

The easiest thing to do is to just fork your Quark from here into your own account. It will appear as "forked from ..." on your github.

Another option is to git clone your quark from here and then create a fresh repository in your own github/bitbucket account and push it to there. Then it will not say "forked from..." Remember to set your working copy's upstream origin to your own github version.

It is also possible to transfer ownership, but for that you need to have admin access to both the supercollider-quarks group and the account you are transferring it to. Contact us and we can add you as an admin to supercollider-quarks.

If you've already moved your code to github or some other git host and want to use that version then just edit directory.txt to point to your preferred newer version.

After you've moved we can delete the quark from here to reduce any confusion.

### Backward incompatibilities

Quarks with spaces in the name had to have those spaces removed ("SenseWorld MiniBee" -> SWMiniBee).

The 'name' field is no longer required in the quark file, but its nice to have. The official name of the Quark is now the same name as the folder and the name of the git repository.

Quarks nested inside other quarks (eg. dewdrop_lib) are now un-nested.

The 'path' field is now obsolete and ignored.

---

[![Gitter](https://badges.gitter.im/Join Chat.svg)](https://gitter.im/supercollider-quarks/quarks?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
