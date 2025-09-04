

# Frontend build stage
FROM node:20 as build-stage
WORKDIR /app

# Copy package.json and install dependencies
COPY package*.json ./
RUN npm install

# Copy all source files and build the project
COPY . .
RUN npm run build --prod

# Nginx runtime stage
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# Copy custom Nginx config
COPY nginx.conf /etc/nginx/nginx.conf

# Copy built files from build-stage
COPY --from=build-stage /app/dist/readcircle-ui /usr/share/nginx/html

# Expose port 80
EXPOSE 80

# Default command to start Nginx
CMD ["nginx", "-g", "daemon off;"]
