Canvas-based home automation written in Java inspired by PLC programming. Connect and control Modbus, FRITZ!Box, and Shelly devices (under development) with a Web GUI
You can checkout as Eclipse Project.
Update Maven
Run: mdn.jh.automation.Main
Find hopefully http://127.0.0.1:8080/home-automation
By default the webserver is running on 0.0.0.0:8080


Paramter:

Parameter:
-c TIME  cycle time (updates) in seconds - default 2s
-h print help message
-v Verbose mode
-l LOGLEVEL (0=All (like -v), 10=Serious, 11=Off; Default=8 (Info))
--server-address ADDRESS  Tomcat bind address (default: 0.0.0.0)
--server-port PORT        Tomcat port (default: 8080)
-with-users               Enable web users and access control (default: disabled)

Web access control
------------------

Start the application with `-with-users` to enable authentication. On the first
browser connection, the web interface asks you to create the initial
administrator. Administrators can then create view-only users, users with read
and write access, and additional administrators. They can also allow the live
view to be opened without login; changing components or devices still requires
write access.

Users are stored locally in `web-users.properties`. Passwords are never stored
in plain text: each password has its own random salt and is hashed with
PBKDF2-HMAC-SHA256. The file is ignored by Git and is restricted to the process
owner on file systems that support POSIX permissions. Back it up separately if
required. Use HTTPS or a trusted private network because login credentials must
otherwise travel over an unencrypted HTTP/WebSocket connection.

Output API
----------

Read a data-source or logic output by component ID as JSON:

`GET /api/output?id=123`

Request XML with `?format=xml` or the `Accept: application/xml` header. The
response contains `id`, `datatype`, `value`, and `name`. When web users are
enabled and anonymous viewing is disabled, use HTTP Basic authentication with
an account that has read access, for example:

`curl -u username:password 'http://localhost:8080/api/output?id=123'`

Dashboard
---------

Open `/dashboard.html` to display configurable dashboard pages. Users with
write access can add and name tabs, freely position text labels and output
fields, and configure a scale factor plus an optional prefix or suffix unit.
Compact pushbutton and switch controls can operate matching Web UI Boolean
data sources and show their current value. Pushbuttons can have an optional
display name which defaults to the selected component name. Width and height can be configured
for every dashboard element. On pushbutton and switch controls, the edit and
delete icons must be held for at least one second before their action is enabled.
Double-clicking a pushbutton or switch never opens its editor.
Read-only and anonymous users can view the live values but cannot change the
layout. The shared layout is stored in `dashboard-layout.json`; this runtime
configuration file is ignored by Git.
