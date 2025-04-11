FROM node:18-alpine

WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .

ENV NODE_ENV=production
ENV PORT=8084
ENV MONGO_URI=mongodb://mongodb:27017/tracking_db
ENV REDIS_URL=redis://redis:6379

EXPOSE 8084

CMD ["node", "src/server.js"]