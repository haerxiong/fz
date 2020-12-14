jps|grep fz.jar|awk {'print $1'}|xargs -I {} kill {}
sudo nohup java -jar /var/lib/jenkins/workspace/fz/target/fz.jar --server.port=8092 &