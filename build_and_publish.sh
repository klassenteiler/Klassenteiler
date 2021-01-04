# enter the tag as parameter like this 'bash build_and_publish 1.2'
sudo docker login --username=klassenteilen
docker build -t klassenteilen/klassenteiler-backend:$1 ./Backend/scala-backend/
docker build -t klassenteilen/klassenteiler-frontend:$1 ./klassentrenner-ui
sudo docker push klassenteilen/klassenteiler-backend:$1
sudo docker push klassenteilen/klassenteiler-frontend:$1