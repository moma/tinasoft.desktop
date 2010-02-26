XPCOM components go into this directory (Python, JavaScript and/or binary).

compile idl with :

$ tina/xulrunner/xpidl -m typelib -w -v -I /usr/share/idl/xulrunner-1.9.2.2pre/ tina/components/ITinasoft.idl
$ mv ITinasoft.xpt tina/components
