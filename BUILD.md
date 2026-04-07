run this in mpj not in v2

Compile command:

javac -d v2/build v2/server/_.java v2/controller/_.java v2/service/_.java v2/repository/_.java v2/model/_.java v2/engine/_.java v2/util/\*.java

Run command:
java -cp v2/build server.MainServer



Available endpoints:
  POST /hospitals
  GET /hospitals
  PUT /hospitals/{id}/demand
  POST /vendors
  GET /vendors
  PUT /vendors/{id}/inventory
  POST /allocate
  GET /allocations
  GET /nearest-hospitals?lat=&lon=
  GET /resource-availability?lat=&lon=&type=


  javac -d . v2/server/*.java v2/controller/*.java v2/service/*.java v2/repository/*.java v2/model/*.java v2/engine/*.java v2/util/*.java