This application provides a simple example of a Spring Java Web Application,
with an AngularJS front end, secured via Azure Active Directory.

To implement Single Sign on with Azure Active Directory, you will need to
register your application in the Azure portal, and copy the client ID and
client secret into the applications.properties file.

1) Sign into your Azure account
    a) Go to the portal at portal.azure.com
2) Create a new App Registration
    a) Select "Azure Active Directory" -> "App registrations" -> "+ New application registration"
    b) Supply a name for your application (Eg. "oauth-client-example")
    c) Supply the sign-on URL of "http://localhost:8080/login/azure" and click "Create"
3) Configure the Application's Permissions
    a) Go to the Settings page, and click "Required permissions" -> "Windows Azure Active Directory"
    b) Make sure "Sign in and read user profile" is checked
    c) If not, click "Save" and then "Grant Permissions"
4) Set the Application's Reply URL
    a) Go to the Settins page, and click "Reply URLs"
    b) Edit the existing reply URL to read "http://localhost/login/azure"
5) Create a client secret
    a) Go to the Settings page, and click "Keys"
    b) Give the key a name (Eg. "oauth2-key") and select a duration. Leave the value cell blank.
    c) Click "Save" and copy the generated value to the ui-service application.properties file's client.secret property.
6) Copy the client ID to the Properies file
    a) Go to the Settings page, and find the Application ID
    b) Copy the Application ID value to the ui-service application.properties file's client ID property.


