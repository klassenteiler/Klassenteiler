# https://dzone.com/articles/how-to-dockerize-angular-app

# Stage 1
FROM node:14.15.1-alpine as build-step
RUN mkdir -p /app
WORKDIR /app
COPY package.json /app
RUN npm install
COPY . /app
RUN npm run build --prod
 
# Stage 2
FROM nginx:1.19.5-alpine
COPY --from=build-step /app/dist/klassentrener-ui /usr/share/nginx/html