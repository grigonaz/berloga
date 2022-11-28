cd backend
call mvn clean package
cd target/angular-api-client
call npm i
call npm run build
cd ../../../frontend/src
rmdir api /s /q
mkdir api
xcopy ..\..\backend\target\angular-api-client\dist\ api\ /s /e /v
pause