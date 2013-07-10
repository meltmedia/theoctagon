#
# Add numerous users to the system
#
for i in {1..200}
do
  email="user$i@meltdev.com"
  echo creating user with email $email and password 'vespa'
  curl -X POST -H 'Content-Type: application/json' -d '{"email":"'$email'","password":"vespa"}' http://localhost:8080/api/user
  echo \n
done