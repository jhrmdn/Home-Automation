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

Enable **Read-only web API → Enable read-only web API access** in any component's
**Details**, then save. Sources, logic components, and sinks all support this.
The JSON URL and **Download component JSON** link appear in Details:

`GET /api/component?id=123`

Add `&download=1` to download `component-123.json`. This endpoint always returns
JSON. It includes `id`, `name`, `kind`, `componentType`, `description`, `status`,
`valid`, and arrays of `inputs` and `outputs`. Each port has `index`, `name`,
`datatype`, a typed JSON `value`, and `valid`. Inputs also report `connected`;
ports report `overridden` and `overridePersistent` when applicable. Effective override values are included.
`configuration` contains component-specific settings (such as converter mappings,
thresholds, PID tuning, generator ranges, MQTT topics, Modbus registers, and
selected FRITZ!Box elements). Device login credentials are not included.
`connections` lists source/target component IDs and port indices without exporting
other components’ values. `automationPageId`, `bounds`, `colors`, and `api` describe
the component placement, display settings, and read-only endpoint.
Missing values and non-finite numbers are represented by `null` with `valid: false`.

Timer components also include `timer`: delay, pulse, and random timers expose
`settings` with durations in milliseconds, `elapsedMillis`, `remainingMillis`,
`elapsedTime`, and `remainingTime`. Random timers include `enabled`; weekly timers
include `enabled` and their `schedule` (day, time, and on/off state).

Access is disabled per component by default, including older configurations.
The setting persists across restarts. The fixed **Internal Webserver** must also
have its web API enabled. Disabled component exports return HTTP 404; a globally
disabled API returns HTTP 503. GET and HEAD are read-only; POST/PUT cannot change
values through this endpoint. This export setting is separate from the dedicated
Internal Webserver webhook and sink endpoints.

Existing web-user read permissions apply. When login is required, use HTTP Basic
authentication, for example:

```bash
curl --user api-user 'http://localhost:8080/api/component?id=123&download=1' -o component.json
```

FRITZ!Box passwords and other device credentials are not included. Sources and
sinks configured with **Login required** also require login for their JSON export.

The legacy `GET /api/output?id=123` uses the same per-component access setting and
returns the expanded JSON, retaining its top-level `id`, `name`, `datatype`, and
`value` fields. `?format=xml` or `Accept: application/xml` still returns the legacy
XML output view on that route.

Device settings and Binary converter
------------------------------------

In **Manage Devices**, FRITZ!Box devices support editing the host, username,
password, TLS validation mode, certificate, and update frequency. Leave the new
password and certificate empty to retain the stored values; use the explicit
clear controls to remove them. Modbus devices support editing the host, port,
device ID, and update frequency. Editing a device preserves its existing sources,
sinks, and connections.

FRITZ!Box source and sink details show the selected element or attribute alongside
the device name, product name, and identifier. Sources also show their XPath.

The **Binary** converter has one integer input and displays binary digits in rows
of exactly eight bits, highest byte first, with leading zeros as needed. For
example, `256` displays as `00000001` followed by `00000000` on the next row.
Negative numbers use 64-bit two's complement; fractional inputs are invalid.

Random number generator
-----------------------

Choose **Generators → Random number**, then open **Details** to set **Minimum**,
**Maximum**, and **Output type** (integer or decimal). The default is an integer
from 0 to 100. A new value is generated at the configured interval (one second by default). Integer mode includes both
limits; equal limits produce a constant value. Decimal mode supports fractional
limits. Both limits must be finite and minimum must not exceed maximum.

The component switch enables or disables generation. Connecting the Boolean
**Enable** input gives that input control instead. When disabled, the generator
holds its last value. Changing the range generates a value within the new range.
The range, output mode, and manual enable setting persist across restarts. The
read-only component JSON export also includes the generator settings.

The random number generator’s **Update interval** in Details accepts milliseconds,
seconds, or minutes (minimum 1 ms). Changes restart the interval immediately,
settings survive restarts, and JSON exports include `intervalMillis`. Existing
configurations default to one second.

Internal Webserver
------------------

**Internal Webserver** is a fixed, non-deletable device available automatically.
Add sources or sinks to it, then use **Manage Devices** to enable or disable the
web API and choose **Enable web API automatically at startup**. A new installation
starts with the API disabled. Existing Internal Webserver configurations retain
automatic startup when upgraded; duplicate devices are consolidated without losing
endpoints. The startup setting is saved separately from the current enable state.
Disabling the API returns HTTP 503 from `/api`, `/api/component`, and `/api/output`; the editor and
dashboard remain available. It uses the application's existing HTTP/HTTPS address
and port. Component Details shows
the generated API URL and lets you edit endpoint settings.

- **DataSource:** select `int` (signed 32-bit integer), `string`, or `number`
  (finite decimal number). Send `id` and `value` using GET query parameters or
  a POST with an `application/x-www-form-urlencoded` body. A successful update
  returns JSON containing `id` and `updated: true`. Invalid values return HTTP
  400 and preserve the previous value. Sources wait for a new webhook after
  application startup; incoming values are not saved to configuration.
- **DataSink:** select the input datatype and a response format: plain text,
  JSON, or XML. Connect the matching automation output, then GET the sink's URL.
  Plain text contains only the value; JSON is `{"id":123,"value":42.5}`; XML is
  `<output><id>123</id><value>42.5</value></output>`. A missing or invalid input
  returns HTTP 503. Disconnect components before changing their datatypes.
- **Login required:** enable separately on each endpoint. Requests then need
  HTTP Basic authentication with an existing web user: write access for sources,
  read access for sinks. Manage accounts in **Web users** with `-with-users`.
  Protected endpoints continue to require credentials even when anonymous viewing
  is enabled or the application runs without `-with-users`. With no configured
  account, a protected endpoint stays inaccessible.

Examples (replace IDs with those shown in Details):

```bash
# Update an integer/number source; --data-urlencode also handles string values.
curl --data-urlencode 'id=123' --data-urlencode 'value=42' http://localhost:8080/api

# Read a sink that requires login; curl prompts for the password.
curl --user api-user 'http://localhost:8080/api?id=456'
```

GET updates use `/api?id=123&value=42`. The path form
`/api/id=123?value=42` is also accepted. The existing `/api/output?id=...` API
remains available for reading component outputs.

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

### Dashboard numeric meters

In dashboard edit mode, **+ Balkenanzeige** adds a horizontal bar and
**+ Tachometer** adds a semicircular gauge. Both select a numeric component and
support minimum, maximum, scaling, units, size, position, and value color ranges.
The gauge ring shows fixed colored segments across the configured scale; only
the needle moves with the reading. Uncovered portions use the default color.
Maximum must exceed minimum. Colors use the scaled value; the first matching
inclusive range wins, otherwise the default color applies. Values outside the
scale remain visible as numbers while the bar or needle stops at the endpoint.
Missing or invalid readings show “Nicht verfügbar”. Settings persist in the
shared dashboard layout.

Dashboard zoom controls offer 10–200% magnification and **Alles anzeigen** to fit
the current page. Zoom is remembered locally in the browser, is available to
read-only viewers, and does not change saved component sizes or positions.

Bars default to horizontal and can be switched to vertical (filling upwards).
Gauges offer optional scale ticks, enabled by default with 10 divisions (11 ticks
including both endpoints). The division count is configurable from 1 to 100.

Deleting an Automation tab requires typing exactly **YES** and also deletes all
components on that tab, including their connections to other tabs. Components
on other tabs remain. The last Automation tab cannot be deleted.

### Reusable custom components

Open **Eigene Komponenten → Aus vorhandenen Komponenten erstellen** in Automation.
Select sources, logic, converters, timers or generators from the current tab. Their settings
and internal connections become a reusable template; the original components stay
in place. Select the exposed inputs and outputs and give each a unique name. Each
port retains the internal port's Number, Boolean or String datatype, and different
ports may use different datatypes. Inputs already connected inside the selection
cannot also be exposed. Sources are copied into each instance and remain connected to their existing device.
Datasinks cannot be included; connect them to the exposed outputs. Embedded source
settings are accessible through the source buttons on the component card. Webhook
sources receive their own IDs and API URLs per instance. Referenced devices must
exist when inserting a template.

Save the template, then choose **Einfügen** in the library as often as needed.
Each instance has independent internal components, timers and state. Existing
custom components can also be included in another template. Click a named output
on its card (or choose **Start connection** and the desired output), then select
the destination input. The read-only component API exports every named output.
The existing single-value dashboard and legacy output view show the first output.

Templates persist in `custom-components.json`. Instances embed their definitions
in the active configuration, so deleting the original construction or a missing
library file does not break already inserted components. Deleting an instance
stops its internal timers and removes its external connections. Templates support
1–200 internal components, up to 32 inputs and 1–32 outputs (up to 12 nesting levels).

### String Selector

In Automation, add **String Selector** and open its **Details** to enter text labels
and assigned 32-bit integer values. Select one label on the component card; its
numeric output immediately becomes the assigned value. **Standard (0)** clears the
selection. The source always starts at 0 after restart. Changing the option list
also resets the selection; editing other settings retains it.

In Dashboard edit mode, choose **+ String Selector**, select the source, and choose
**Combobox** or **Radiobutton-Liste**. Both views operate the same single selection,
including when several widgets reference it. Operating a selector uses the same
action permissions as dashboard switches. The read-only component API includes
its option list, selected index and numeric output.

### Fixed value input on the dashboard

Add **+ Fixed-Value-Eingabe** in dashboard edit mode and select an Integer or Float
fixed value source. Click its value to open the numeric keypad. It offers digits,
sign, backspace (⌫), clear (C), and the locale's decimal separator for Float values
(comma in German). Physical keyboard input also works. Only **Übernahme** sends and
saves the value; **Abbrechen** or Escape discards the draft. Live dashboard updates
do not replace a draft. The existing action permissions apply, and this control
cannot change the source's datatype.

New dashboard value fields, bars and gauges copy the selected Automation component’s
color ranges as editable defaults (Boolean value fields also copy True/False colors).
Selecting another component during creation refreshes those defaults. Saved dashboard
colors remain independent of later changes to the Automation component.

In dashboard edit mode, drag any edge or corner with the left mouse button to resize
a widget. The opposite edge stays fixed, zoom is respected, and releasing saves
the size and position. Escape cancels the drag. Each widget retains its minimum
size; normal view and read-only users cannot resize widgets.

### Dashboard images, frames and group movement

In edit mode, **+ Bild** uploads PNG/JPEG images (up to 4 MiB and 16 megapixels).
Images persist in `dashboard-images/`; back up this directory with `dashboard-layout.json`.
Image settings offer a description, fit/crop mode and geometry. Images can be resized
like other widgets. **+ Rahmen zeichnen** lets you drag a rectangle on the canvas;
its settings offer line color, width (1–30 px), and solid, dashed, dotted or double
lines. Frames remain behind dashboard controls.

With **Mehrfachauswahl**, select the elements and drag the move handle of any selected
item to move the whole group. Arrow keys move the selection by 1 px; Shift+arrow by
10 px. Relative spacing is retained, including at page boundaries. Arrow keys in
input fields or dialogs retain their normal function. Escape cancels mouse movement
or frame drawing. Movements are saved on mouse release or arrow-key release.
