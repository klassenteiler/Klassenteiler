# https://dzone.com/articles/how-to-dockerize-angular-app

# Stage 1
FROM node:14.15.1-alpine as build-step
RUN mkdir -p /app
WORKDIR /app
COPY package.json package-lock.json /app/
RUN npm install
COPY . /app
# RUN npm run build
# RUN ng build --configuration=production
RUN npm run ng build -- --configuration=production
# maybe ng build --prod
 
# Stage 2
FROM nginx:1.19.5-alpine
COPY default.conf /etc/nginx/conf.d/default.conf
COPY --from=build-step /app/dist/klassentrener-ui /usr/share/nginx/html


# TO use it:
# docker build -t rauwuckl/test .
# docker run -d -it -p 80:80/tcp --name test rauwuckl/test