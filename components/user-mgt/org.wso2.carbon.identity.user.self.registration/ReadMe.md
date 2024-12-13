### APIs

#### Deployment.toml config

```toml
[[resource.access_control]]
context="(.*)/reg-orchestration(.*)"
secure=false
http_method="POST"
```

#### Config API
https://localhost:9443/reg-orchestration/config

#### Portal API
https://localhost:9443/reg-orchestration/portal

#### Testing the config through /registration/initiate api

use the following curl command to test the config. New flow is engaged with the applicationId set to "newflow"

```cURL
curl --location 'https://localhost:9443/api/users/v2/registration/initiate' \
--header 'Content-Type: application/json' \
--data '{
  "applicationId": "newflow"
}'
```



