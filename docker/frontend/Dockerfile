#FROM node:16 as build-stage
FROM node:20 as build-stage  # LTS and fully ARM compatible
WORKDIR /app

COPY package*.json ./

RUN npm install

COPY . .

RUN npm run build --prod

FROM nginx:alpine

COPY nginx.conf /etc/nginx/nginx.conf

COPY --from=build-stage /app/dist/readcircle-ui /usr/share/nginx/html

EXPOSE 80