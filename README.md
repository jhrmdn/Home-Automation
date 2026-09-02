# Home Automation

Home Automation is a Java-based, PLC-inspired automation system with a visual
web interface. It supports logic components and integrations for Modbus,
FRITZ!Box, Shelly, MQTT, XML, and databases.

## Requirements and build

- JDK 26
- Maven 3.9 or newer, or an IDE with Maven support

Build and test the self-contained application JAR:

```bash
mvn clean package
```

The resulting file is `target/home-automation-1.0-SNAPSHOT.jar`. It includes
the embedded Tomcat server, the complete web application, and the WebSocket
endpoint; external `tomcat` and `webapp-classes` directories are not required.

## Starting the application

Copy the JAR into the directory where its persistent configuration should be
stored, then run:

```bash
java -jar home-automation-1.0-SNAPSHOT.jar
```

The server listens on all interfaces on port 8080 by default. Open:

- Automation editor: `http://127.0.0.1:8080/home-automation/`
- Dashboard: `http://127.0.0.1:8080/home-automation/dashboard.html`

Tomcat extracts packaged web resources into a temporary directory and removes
them during normal shutdown. Persistent files such as `settings.xml`,
`dashboard-layout.json`, `counter-values.properties`, `web-users.properties`,
certificates, and logs remain in the application's working directory.

## Command-line parameters

```text
java -jar home-automation-1.0-SNAPSHOT.jar [options]
```

| Option | Description |
| --- | --- |
| `-h` | Print the command-line help and exit. |
| `-c SECONDS` | Set the automation update-cycle interval in seconds. The default is `2`; the maximum accepted value is `30`. |
| `-v` | Enable the most verbose logging (`ALL`). This is equivalent to `-l 0`. |
| `-l LEVEL` | Set the Java logging level using the numeric mapping below. The default is `8` (`INFO`). |
| `-test` | Use the built-in sample FRITZ!Box device document instead of requesting live FRITZ!Box data. Intended for development and testing. |
| `-with-users` | Enable web authentication and access control. On first use, the browser prompts for creation of an administrator account. |
| `--server-address ADDRESS` | Set the Tomcat bind address or host name. The default is `0.0.0.0`; use `127.0.0.1` to allow local connections only. |
| `--server-port PORT` | Set the HTTP server port. Valid values are `1`–`65535`; the default is `8080`. |
| `--https` | Enable HTTPS. If `default.pem` does not exist in the working directory, a self-signed localhost certificate and private key are generated there automatically. |
| `--https-certificate FILE` | Enable HTTPS with the specified PEM file. The file must contain both the certificate chain and its unencrypted private key. |

Logging levels for `-l`:

| Value | Java logging level |
| --- | --- |
| `0`–`2` | `ALL` |
| `3` | `FINEST` |
| `4` | `FINER` |
| `5` | `FINE` |
| `6`–`7` | `CONFIG` |
| `8` | `INFO` |
| `9` | `WARNING` |
| `10` | `SEVERE` |
| `11` | `OFF` |

Example: listen only on the local machine, use port 8090, enable users, and
set warning-level logging:

```bash
java -jar home-automation-1.0-SNAPSHOT.jar \
  --server-address 127.0.0.1 \
  --server-port 8090 \
  -with-users \
  -l 9
```

Example: start HTTPS on port 8443 with the automatically managed local
`default.pem`:

```bash
java -jar home-automation-1.0-SNAPSHOT.jar --https --server-port 8443
```

The generated certificate is self-signed, so browsers display a warning until
it is trusted locally. For a trusted certificate, combine the certificate chain
and unencrypted private key in one PEM file and pass it with
`--https-certificate`. The option enables HTTPS by itself; `--https` is not
additionally required.

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
for every dashboard element. Editing, moving, and deleting components is only
available after explicitly enabling dashboard edit mode. Pushbutton and switch
edit and delete actions then respond immediately.
Read-only and anonymous users can view the live values but cannot change the
layout. The shared layout is stored in `dashboard-layout.json`; this runtime
configuration file is ignored by Git.
