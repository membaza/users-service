# users-service
A small microservice for managing user registrations, password changes and issue access tokens. Built using Spring Boot and Spring Security. User information is stored in a MongoDB database. Authentication information is passed using JWT tokens in every request, eliminating the need for a distributed token store.

## Features
* Register Users
* Require Email Confirmation
* Request Login / Refresh Token
* Manage User Roles using REST
* Update Credentials
* Delete User
* Expired Keys Are Automatically Purged
* Unconfirmed Changes Are Automatically Removed

## Configuration
To change the default configuration, create a file called `application.properties` in the same folder as the `.jar`-file and set any of the following options:
```properties
# Server Settings
server.port = 9465

# Application Settings
service.jwt.secret     = private_key.der
service.jwt.public     = public_key.der
service.jwt.issuer     = http://membaza.com
service.jwt.expiration = 60000

service.roles.default      = ROLE_USER
service.privileges.default =

service.purge.enabled = true

service.email.sender   = noreply@membaza.com
service.email.sitename = Membaza
service.email.siteurl  = http://membaza.com

# MongoDB Settings
spring.data.mongodb.host = localhost
spring.data.mongodb.port = 27017

# MailChimp Settings
mailgun.apiKey =
mailgun.domain = 
```

## API Commands
The following commands are implemented by the service.

### Create a new User
If an authentication token is specified and it has the `CREATE_USER` right, then the user is created immedietly. Otherwise, a confirmation email is sent to the specified email address with a confirmation code.

```
POST /users
{
    "email" : "...",
    "firstname" : "...",
    "lastname" : "...",
    "password" : "..."
}
```

### Verify Registration
Verify the registration of an user by specifying the code sent by email.

```
POST /users/{userId}/verify
{
    "code" : "..."
}
```

### Cancel Registration
The registration confirmation email was sent out by mistake. Cancel registration.

```
POST /users/{userId}/cancel
{
    "code" : "..."
}
```

### Login (Request a Token)
Request a JWT authentication token.

```
POST /users/login 
{
    "email" : "...",
    "password" : "..."
}
```

### Refresh Login Token
Request a new updated JWT authentication token by handing in the old (but still valid) one.

```
POST /users/refresh
{
    "token" : "..."
}
```

### Logut
Tell the server to clear any cache involving the authenticated user. (This is optional).

```
POST /users/logout
{
    "token" : "..."
}
```

### Assign Role
Requires the `ASSIGN_ROLE_*` privilege, where `*` is the role in question. 

```
POST /users/{userId}/roles/{role}
```

### Revoke Role
Requires the `REVOKE_ROLE_*` privilege, where `*` is the role in question. 

```
DELETE /users/{userId}/roles/{role}
```

### Delete User
Initiate deletion of user account. If an authentication token is specified and it has the `DELETE_USER` right, then the user is modified immedietly. Otherwise, a confirmation email is sent to the specified email address with a confirmation code.

```
DELETE /users/{userId}
```

Confirm deletion by specifying the code sent out by email:
```
DELETE /users/{userId}/verify
{
    "code" : "..."
}
```

Cancel deletion by specifying the code sent out by email:
```
DELETE /users/{userId}/cancel
{
    "code" : "..."
}
```

### Change Email
Initiate email change. If an authentication token is specified and it has the `MODIFY_USER` right, then the user is modified immedietly. Otherwise, a confirmation email is sent to the old email address with a confirmation code.

```
PUT /users/{userId}/email
{
    "email" : "..."
}
```

Confirm email change by specifying the code sent out by email:
```
PUT /users/{userId}/email/verify
{
    "code" : "..."
}
```

Cancel email change by specifying the code sent out by email:
```
PUT /users/{userId}/email/cancel
{
    "code" : "..."
}
```

### Change Password
Initiate password change. If an authentication token is specified and it has the `MODIFY_USER` right, then the user is modified immedietly. Otherwise, a confirmation email is sent to the email address of the user with a confirmation code.

```
PUT /users/{userId}/password
{
    "password" : "..."
}
```

Confirm password change by specifying the code sent out by email:
```
PUT /users/{userId}/password/verify
{
    "code" : "..."
}
```

Cancel password change by specifying the code sent out by email:
```
PUT /users/{userId}/password/cancel
{
    "code" : "..."
}
```

## License
Copyright 2017 Emil Forslund

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
