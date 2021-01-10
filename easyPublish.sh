# enter the tag as parameter like this 'bash build_and_publish 1.2'
docker build -t klassenteilen/klassenteiler-backend:$1 ./Backend/scala-backend/
docker build -t klassenteilen/klassenteiler-frontend:$1 ./klassentrenner-ui
docker push klassenteilen/klassenteiler-backend:$1
docker push klassenteilen/klassenteiler-frontend:$1