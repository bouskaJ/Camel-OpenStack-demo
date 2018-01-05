# Demo project for Camel OpenStack component 

We will create new OpenStack slave and perform some command using camel-ssh component in this demo.


## Before you start
1) You will need running OpenStack instance
2) You have to substitute OpenStack credentials in MyRouteBuilder class
3) you have to substitute OpenStack node instance configuration stored in _src/data/node1.xml_ and  _src/data/node2.xml_ 

Troubleshooting:

* camel-ssh can't connect - make sure that you have enabled ssh connection in suitable security group