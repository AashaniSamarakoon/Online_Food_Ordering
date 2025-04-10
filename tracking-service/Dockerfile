FROM node:18-alpine

WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .

# Install MongoDB tools (optional)
RUN apk add --no-cache mongodb-tools

EXPOSE 8084
CMD ["npm", "start"]